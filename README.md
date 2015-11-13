# Java4selenium

License: [MIT](License.txt)

This is console applications.

## Usage

### Run with gradle

gradle runTests [-options] 

where options include:
*    -P_browser, -P_b =<value>     set browser to test - 'FIREFOX' is default. ('FIREFOX','CHROME','OPERA','SAFARI','IE')
*    -P_all                        select all existing tests to execute.
*    -P_groups, -P_g=<"[args...]"> specifies list of groups of tests to execute.
*    -P_tests, -P_t=<"[args...]">  specifies list of tests to execute
*    -P_test_list, -P_tl	   print list of existing tests
*    -P_stop_on_error, -P_s	   if specified, tests will be stopper after first error
*    -P_help_g, -P_hg		   print help message for gradle
*    -P_help, -P_h                 print help message for jar

### build fat jar

> gradle fatJar

### run with jar

*.jar [-options]

where oprions include:
*    -browser, -b <value>     	   set browser to test - 'FIREFOX' is default. ('FIREFOX','CHROME','OPERA','SAFARI','IE')
*    -all                          select all existing tests to execute.
*    -groups, -g <args...> 	   specifies list of groups of tests to execute.
*    -tests, -t <args...>  	   specifies list of tests to execute
*    -test_list, -tl	   	   print list of existing tests
*    -stop_on_error, -s	   	   if specified, tests will be stopper after first error
*    -help_g, -hg		   print help message for gradle
*    -help, -h                 	   print help message for jar

## Example

### call help
for gradle:
> gradle runTests -P_all

for jar:
> *.jar -help

### run all tests
for gradle:
> gradle runTests -P_all

for jar:
> *.jar -all

### run custom groups of tests on custom browser
for gradle:
> gradle runTests -P_browser="OPERA" -P_groups="['GoogleGroup','JiraGroup']"

for jar:
> *.jar -browser="OPERA" -groups GoogleGroup JiraGroup

### run custom group of tests and list of tests
for gradle:
> gradle runTests -P_groups="['GoogleGroup']" -P_tests="['JiraLoginTest','JiraLoguotTest2']"

for jar:
> *.jar -groups GoogleGroup -tests JiraLoginTest JiraLoguotTest2

