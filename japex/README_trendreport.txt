Japex -- TrendReport


TrendReport generates Time-Series charts based on Japex reports. TrendReport version 0.1 was designed to 
use outside script to drive full trend report. The program itself can only create one chart at a time. 
TrendReport 0.2 update on top of the version 0.1 to support generating complete trend report. Both interfaces 
are supported with this version.

1. version 0.1
TrendReport title reportPath outputPath date offset [driver] [testcases] [-O/OVERWRITE]
    where:
    title -- name of the chart to be generated
    reportPath -- path where the TrendReport tool will search for any Japex reports
    outputPath -- path where charts and html index page will be saved.
    date -- a specific date in format "yyyy-MM-dd", including "Today", 
            from or to which a trend report shall be made
    offset -- days, weeks, months or years from/to the above date a trend report will be created. 
            Supports format: xD where x is a positive or negative integer, and D indicates Days
            Similarily, xW, xM and xY are also support where W=Week, M=Month, and Y=Year
    driver -- optional. Name of a driver for which trend report is to be generated. All drivers if not specified.
    testcase(s) -- specific test(s) for a driver for which a trend report will be created. Use keyword "all" 
                to display all testcases. If testcases are not specified, the tool will generate
                a means chart.
 
    options:
    -O or -Overwrite -- overwrite existing report under "outputPath"



2. version 0.2
   TrendReport title reportPath outputPath [date] [offset] [-d {driver}] [-m {means}] [-t {test}] [-H/HISTORY] [-O/OVERWRITE]
    where:
    title -- name of the chart to be generated
    reportPath -- path to where the report directory is
    outputPath -- path where the html report will be saved. 
    [date] -- a specific date in format "yyyy-MM-dd", including "Today", 
            from or to which a trend report shall be made. Default "Today"
    [offset] -- days, weeks, months or years from/to the above date a trend report will be created. 
            Supports format: xD where x is a positive or negative integer, and D indicates Days
            Similarily, xW, xM and xY are also support where W=Week, M=Month, and Y=Year. Default 1Y
    -d/driver driver1:driver2:... -- name of driver(s) for which a trend report is to be generated. All drivers if not specified.
    -m/means means1:means2:... -- one or more of the three means. Use keyword "all" to display all three means. All means specified will be placed on one chart.
    -t/testcases test1:test2:... -- specify test(s) for which a trend report will be created. Use keyword "all" 
                to display all testcases. 
    -gs/groupsize size -- when displaying all testcases, this parameter regulates the max number of testcases to be written on each chart. The default is 4.
 
    support version 0.1 interface:
    [driver] --
    options:
    -H or -History -- indicate that the trend report should be saved to a subdirectory in a timestamp format
    -O or -Overwrite -- overwrite existing report under "outputPath"

The default trend report contains three means charts and the same number of charts as testcases. 
To generate a default trend report, use bin/trendReport_default.sh or the following ant target:

    <!--generate trend report over one year period of time-->
    <target name="default">
        <java dir="." fork="true" classname="com.sun.japex.TrendReport">
            <!-- Japex.jar should be on the path -->
            <classpath refid="class.path"/>
            <arg value="JAXWS Performance Trend Report"/>
            <arg line="/projects/reports"/>  <!--reports directory-->
	    <arg line="/projects/trends"/>   <!--trend report output directory-->
            <arg line="-overwrite"/>
        </java>
    </target>

    <!--generate trend report over 6-month period of time-->
    <target name="default">
        <java dir="." fork="true" classname="com.sun.japex.TrendReport">
            <classpath refid="class.path"/>
            <arg value="JAXWS Performance Trend Report"/>
            <arg line="/projects/reports"/>
	    <arg line="/projects/trends"/>
            <arg line="today"/>
            <arg line="-6M"/> 
            <arg line="-overwrite"/>
        </java>
    </target>

For a single driver report, the three means are all displayed in one chart. Testcases are grouped
based upon group size with a default value of 4. Note that the grouping mechenism calculates group 
sizes for tests to avoid a very small final group. Therefore, the final group size may be smaller or
equal to the group size specified. For example, if the total number of testcases is 9 and group size 
equals 4 (by default), the program would generate 3 charts each containing 3 testcases.

To create a report for a single driver, use bin/trendReport_FI.sh as an example or 
the following ant target:
    <!--generate trend report over 6-month period of time-->
    <target name="trendreport-FIDriver">
        <java dir="." fork="true" classname="com.sun.japex.TrendReport">
            <classpath refid="class.path"/>
            <arg value="Trend Report for FI Driver"/>
            <arg line="/projects/reports"/>
	    <arg line="/projects/trends"/>
            <arg line="-d FIDriver"/>
            <arg line="-overwrite"/>
        </java>
    </target>


