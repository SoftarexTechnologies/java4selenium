package run;

import common.enums.Browsers;
import common.enums.markers.SeleniumTest;
import common.enums.markers.SeleniumTestGroup;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tests.base.AbstractTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static run.RunConstants.*;
/**
 * Created by Ugene Reshetnyak on 12.11.2015.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String TEST_PACKAGE = "tests";

    private static CommandLine cmd;

    public static void main(String[] args) {

        System.out.println(Arrays.asList(args));

        initOptions(args);
        if (cmd == null) {
            System.out.println("Incorrect arguments!!!");
            System.out.println();
            printHelp();
            return;
        }

        if (cmd.hasOption(HELP)) {
            printHelp();
            return;
        } else if(cmd.hasOption(HELP_GRADLE)){
            printGradleHelp();
            return;
        }else if (cmd.hasOption(TEST_LIST)) {
            printTestList();
            return;
        }

        WebDriver driver = null;
        //Check browser as first argument
        if (cmd.hasOption(BROWSER)) {
            Browsers browser = Arrays.asList(Browsers.values()).stream().filter(item -> item.name().equals(cmd.getOptionValue(BROWSER))).findFirst().orElse(null);
            if (browser != null) {
                switch (browser) {
                    case CHROME:
                        driver = new ChromeDriver();
                        break;
                    case OPERA:
                        driver = new OperaDriver();
                        break;
                    case SAFARI:
                        driver = new SafariDriver();
                        break;
                    case IE:
                        driver = new InternetExplorerDriver();
                        break;
                    case FIREFOX:
                    default:
                        driver = new FirefoxDriver();
                        break;
                }
            }
        }

        List<String> selectedTestGroups = new ArrayList<>();
        List<String> selectedTests = new ArrayList<>();
        //Check tests as second argument
        if (!cmd.hasOption(ALL)) {
            //if tests set as groups
            if (cmd.hasOption(GROUPS)) {
                if (0 == cmd.getOptionValues(GROUPS).length) {
                    System.out.println("At least one test group expected!!!");
                    return;
                }
                selectedTestGroups = Arrays.asList(cmd.getOptionValues(GROUPS));
            }
            //if tests set as testName
            if (cmd.hasOption(TESTS)) {
                //if tests are not specified
                if (0 == cmd.getOptionValues(TESTS).length) {
                    System.out.println("At least one test expected!!!");
                    return;
                }
                selectedTests = Arrays.asList(cmd.getOptionValues(TESTS));
            }
        }

        //stop on error exist
        boolean stopOnError = cmd.hasOption(STOP_ON_ERROR);
        final List<Class<? extends AbstractTest>> tests = getAllTests();
        int failedTestCount = 0;
        int runTestCount = 0;
        try {
            for (Class<? extends AbstractTest> test : tests) {
                boolean testAllowed = true;
                if (!selectedTestGroups.isEmpty()) {
                    testAllowed = selectedTestGroups.stream().filter(group ->
                                    group.equals(test.getAnnotation(SeleniumTestGroup.class).name())
                    ).findFirst().isPresent();
                }
                if (!selectedTests.isEmpty()) {
                    boolean testAllowedByName = selectedTests.stream().filter(testItem ->
                                    testItem.equals(test.getAnnotation(SeleniumTest.class).name())
                    ).findFirst().isPresent();

                    testAllowed = selectedTestGroups.isEmpty() ? testAllowedByName : testAllowed || testAllowedByName;
                }
                if (testAllowed) {
                    runTestCount++;
                    if (test.newInstance().run(driver)) {
                        failedTestCount++;
                        if (stopOnError) {
                            System.out.println();
                            System.out.println("Failed on test " + test.getSimpleName());
                            break;
                        }
                    }
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } finally {
            System.out.println();
            System.out.println(" ====== ====== ====== Result ====== ====== ======");
            System.out.println("Executed " + runTestCount + " test(s)");
            System.out.println("Successfully: " + (runTestCount - failedTestCount));
            System.out.println("Failed: " + failedTestCount);
            System.out.println(" ====== ====== ======  End   ====== ====== ======");
        }
    }

    private static void initOptions(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder(HELP).longOpt(HELP_LONG).hasArg(false).build());
        options.addOption(Option.builder(HELP_GRADLE).longOpt(HELP_GRADLE_LONG).hasArg(false).build());
        options.addOption(Option.builder(TEST_LIST).longOpt(TEST_LIST_LONG).hasArg(false).build());
        options.addOption(Option.builder(BROWSER).longOpt(BROWSER_LONG).hasArg(true).build());
        options.addOption(Option.builder(ALL).hasArg(false).build());
        options.addOption(Option.builder(GROUPS).longOpt(GROUPS_LONG).hasArgs().build());
        options.addOption(Option.builder(TESTS).longOpt(TESTS_LONG).hasArgs().build());
        options.addOption(Option.builder(STOP_ON_ERROR).longOpt(STOP_ON_ERROR_LONG).hasArg(false).build());
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
    }

    private static List<Class<? extends AbstractTest>> getAllTests() {
        Reflections reflections = new Reflections(TEST_PACKAGE);
        return reflections.getSubTypesOf(AbstractTest.class)
                .stream().filter(item -> item.isAnnotationPresent(SeleniumTestGroup.class) && item.isAnnotationPresent(SeleniumTest.class))
                .collect(Collectors.toList());
    }

    private static void printTestList() {
        final List<Class<? extends AbstractTest>> tests = getAllTests();
        Map<String, List<String>> testsByGroups = new HashMap<>();
        tests.forEach(item -> {
            if (!testsByGroups.containsKey(item.getAnnotation(SeleniumTestGroup.class).name())) {
                testsByGroups.put(item.getAnnotation(SeleniumTestGroup.class).name(), new ArrayList<>());
            }
            testsByGroups.get(item.getAnnotation(SeleniumTestGroup.class).name()).add(item.getAnnotation(SeleniumTest.class).name());
        });
        System.out.println();
        System.out.println("====== ====== ====== Tests ====== ====== ======");
        System.out.println("Total : " + tests.size());
        System.out.println();
        testsByGroups.forEach((key, value) -> {
            System.out.println(key + " (" + value.size() + "):");
            value.forEach(item ->
                            System.out.println("\t" + item)
            );
        });
        System.out.println();
    }

    private static void printGradleHelp() {
        System.out.println();
        System.out.println("====== ====== ====== Help tutorial ====== ===== ======");
        System.out.println();
        System.out.println("Usage: gradle runTests [-options]");
        System.out.println();
        System.out.println("where options include:");
        System.out.println("    -P_browser, -P_b =<value>\tset browser to test - 'FIREFOX' is default. ('FIREFOX','CHROME','OPERA','SAFARI','IE')");
        System.out.println("    -P_all\t\t\tselect all existing tests to execute.");
        System.out.println("    -P_groups, -P_g=<\"[args...]\">\tspecifies list of groups of tests to execute.");
        System.out.println("    -P_tests, -P_t=<\"[args...]\">\tspecifies list of tests to execute");
        System.out.println("    -P_test_list, -P_tl\t\t\tprint list of existing tests");
        System.out.println("    -P_stop_on_error, -P_s\t\tif specified, tests will be stopper after first error");
        System.out.println("    -P_help_g, -P_hg\t\t\tprint this help message");
        System.out.println("    -P_help, -P_h\t\t\tprint help message for jar");
        System.out.println();
        System.out.println("====== ====== ====== ====== ====== ====== ===== ======");
        System.out.println();
    }

    private static void printHelp() {
        System.out.println();
        System.out.println("====== ====== ====== Help tutorial ====== ===== ======");
        System.out.println();
        System.out.println("Usage: *.jar [-options]");
        System.out.println();
        System.out.println("where options include:");
        System.out.println("    -browser, -b <value>\tset browser to test - 'FIREFOX' is default. ('FIREFOX','CHROME','OPERA','SAFARI','IE')");
        System.out.println("    -all\t\t\tselect all existing tests to execute.");
        System.out.println("    -groups, -g <args...>\tspecifies list of groups of tests to execute.");
        System.out.println("    -tests, -t <args...>\tspecifies list of tests to execute");
        System.out.println("    -test_list, -tl\t\tprint list of existing tests");
        System.out.println("    -stop_on_error, -s\t\tif specified, tests will be stopper after first error");
        System.out.println("    -help_g, -hg\t\tprint help message for gradle");
        System.out.println("    -help, -h\t\t\tprint this help message");
        System.out.println();
        System.out.println("====== ====== ====== ====== ====== ====== ===== ======");
        System.out.println();
    }
}
