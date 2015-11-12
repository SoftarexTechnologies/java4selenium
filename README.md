# Java4selenium

License: [MIT](License.txt)

This is console applications.

## Usage

gradle runTests [-Pbrowser] -Pall
		<to run all tests>
	or  gradle runTests [-Pbrowser] [-options]
		<to run custom selected tests>

where options include:
*    -Pbrowser=<value>           set browser to test - 'FIREFOX' is default. ('FIREFOX','CHROME','OPERA','SAFARI','IE')
*    -Pall                       select all existing tests to execute.
*    -Pgroups=<"[args...]">      specifies list of groups of tests to execute.
*    -Ptests=<"[args...]">       specifies list of tests to execute
*    -P?help                     print this help message

## Example

Open console window at project root.

### call help

> gradle runTests -P_all

### run all tests

> gradle runTests -P_all

### run custom groups of tests on custom browser

> gradle runTests -P_browser="OPERA" -P_groups="['GoogleGroup','JiraGroup']"

### run custom group of tests and list of tests

> gradle runTests -P_groups="['GoogleGroup']" -P_tests="['JiraLoginTest','JiraLoguotTest2']"
