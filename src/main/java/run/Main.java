package run;

import common.enums.Browsers;
import common.enums.markers.SeleniumTest;
import common.enums.markers.SeleniumTestGroup;
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

/**
 * Created by Ugene Reshetnyak on 12.11.2015.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String TEST_PACKAGE = "tests";

    private static final String HELP = "HELP_ARG";

    private static final String DEFAULT_BROWSER = "DEFAULT_BROWSER_ARG";
    private static final String TEST_LIST_ARG = "TEST_LIST_ARG";
    private static final String STOP_ON_ERROR_ARG = "STOP_ON_ERROR_ARG";
    private static final String ALL_TESTS = "ALL_ARG";
    private static final String CUSTOM_TESTS = "CUSTOM_ARG";
    private static final String START_GROUPS = "START_GROUPS_ARG";
    private static final String START_TESTS = "START_TESTS_ARG";

    public static void main(String[] args) {

        if (args.length < 2) {
            if (args[0].trim().equals(HELP)) {
                printHelp();
            } else if (args[0].trim().equals(TEST_LIST_ARG)) {
                printTestList();
            } else {
                System.out.println("At least one argument expected!!!");
            }
            return;
        }

        WebDriver driver = null;
        //Check browser as first argument
        if (args[0].trim().equals(DEFAULT_BROWSER)) {
            Browsers browser = Arrays.asList(Browsers.values()).stream().filter(item -> item.name().equals(args[0].trim())).findFirst().orElse(null);
            if (browser != null) {
                switch (browser) {
                    case FIREFOX:
                        driver = new FirefoxDriver();
                        break;
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
                    default:
                        driver = null;
                        break;
                }
            }
        }

        final List<String> selectedTestGroups = new ArrayList<>();
        final List<String> selectedTests = new ArrayList<>();
        int argumentIndex = 2;
        //Check tests as second argument
        if (args[1].trim().equals(CUSTOM_TESTS)) {
            //if custom tests selected, check what arguments exist
            if (args.length < 3) {
                System.out.println("At least one test expected!!!");
                return;
            }
            //if tests set as groups

            if (args[argumentIndex].trim().equals(START_GROUPS)) {

                if (args.length < 4) {
                    System.out.println("At least one test group expected!!!");
                    return;
                }
                argumentIndex = 3;
                //fill list of test groups
                while (args.length > argumentIndex && !args[argumentIndex].trim().equals(START_TESTS)) {
                    selectedTestGroups.add(args[argumentIndex]);
                    argumentIndex++;
                }
            }
            //if tests set as testName
            if (args.length > argumentIndex && args[argumentIndex].trim().equals(START_TESTS)) {
                //if tests are not specified
                boolean testsExistInArgs = args.length < argumentIndex + 2;
                if (testsExistInArgs && selectedTestGroups.isEmpty()) {
                    System.out.println("At least one test group expected!!!");
                    return;
                }
                if (!testsExistInArgs) {
                    argumentIndex++;
                    //fill list of custom tests
                    while (args.length > argumentIndex && !args[argumentIndex].trim().equals(STOP_ON_ERROR_ARG)) {
                        selectedTests.add(args[argumentIndex]);
                        argumentIndex++;
                    }
                }
            }
        } else if (!args[1].trim().equals(ALL_TESTS)) {
            System.out.println("List of parameters is incorrect!!!");
            return;
        }
        //stop on error exist
        System.out.println();
        System.out.println(args[argumentIndex].trim());
        boolean stopOnError = args.length > argumentIndex && args[argumentIndex].trim().equals(STOP_ON_ERROR_ARG);

        final List<Class<? extends AbstractTest>> foundedTests = getAllTests();
        int failedTestCount = 0;
        int runTestCount = 0;
        try {
            for (Class<? extends AbstractTest> test : foundedTests) {
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
            System.out.println("Was executed " + runTestCount + " tests");
            System.out.println("Successfully: " + (runTestCount - failedTestCount));
            System.out.println("Failed: " + failedTestCount);
            System.out.println(" ====== ====== ======  End   ====== ====== ======");
        }


    }

    private static List<Class<? extends AbstractTest>> getAllTests() {
        Reflections reflections = new Reflections(TEST_PACKAGE);
        return reflections.getSubTypesOf(AbstractTest.class)
                .stream().filter(item -> item.isAnnotationPresent(SeleniumTestGroup.class) && item.isAnnotationPresent(SeleniumTest.class))
                .collect(Collectors.toList());
    }

    public static void printTestList() {
        final List<Class<? extends AbstractTest>> foundedTests = getAllTests();
        Map<String, List<String>> testsByGroups = new HashMap<>();
        foundedTests.forEach(item -> {
            if (!testsByGroups.containsKey(item.getAnnotation(SeleniumTestGroup.class).name())) {
                testsByGroups.put(item.getAnnotation(SeleniumTestGroup.class).name(), new ArrayList<>());
            }
            testsByGroups.get(item.getAnnotation(SeleniumTestGroup.class).name()).add(item.getAnnotation(SeleniumTest.class).name());
        });
        System.out.println("====== ====== ====== Tests ====== ====== ======");
        System.out.println("Total : " + foundedTests.size());
        System.out.println();
        testsByGroups.forEach((key, value) -> {
            System.out.println(key + " (" + value.size() + "):");
            value.forEach(item ->
                            System.out.println("\t" + item)
            );
        });
    }

    public static void printHelp() {
        System.out.println("====== ====== ====== Help tutorial ====== ===== ======");
        System.out.println();
        System.out.println("Usage: gradle runTests [-P_browser] -P_all");
        System.out.println("\t\t<to run all tests>");
        System.out.println("\tor  gradle runTests [-P_browser] [-options]");
        System.out.println("\t\t<to run custom selected tests>");
        System.out.println();
        System.out.println("where options include:");
        System.out.println("    -P_browser=<value>\t\tset browser to test - 'FIREFOX' is default. ('FIREFOX','CHROME','OPERA','SAFARI','IE')");
        System.out.println("    -P_all\t\t\tselect all existing tests to execute.");
        System.out.println("    -P_groups=<\"[args...]\">\tspecifies list of groups of tests to execute.");
        System.out.println("    -P_tests=<\"[args...]\">\tspecifies list of tests to execute");
        System.out.println("    -P_tests\t\t\tprint list of existing tests");
        System.out.println("    -P_stop_on_error\t\tif specified, tests will be stopper after first error");
        System.out.println("    -P_help\t\t\tprint this help message");
        System.out.println();
        System.out.println("====== ====== ====== ====== ====== ====== ===== ======");
    }
}
