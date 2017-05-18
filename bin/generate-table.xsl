<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:japex="http://www.sun.com/japex/testSuiteReport" 
   exclude-result-prefixes="xs japex" version="2.0">
    
    <xsl:output method="xml"
        cdata-section-elements="script style"
        indent="yes"
        encoding="utf-8"
	doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" 
	doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" />
    
    <xsl:template match="/">
        <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
            <head>
                <title><xsl:value-of select="japex:testSuiteReport/@name"/></title>
                
                <style type="text/css">
                    body {color: black; background: white;}
                    .pass { background-color: #00aa00; }
                    .fail { background-color: #aa0000; }
                    th { background-color: grey; }
                </style>
            </head>
            <body>
                <div class="testreport">
                    <h1>Interoperability Test Report</h1>
                    <p><xsl:value-of select="japex:testSuiteReport/japex:configFile"/></p>
                    <p><xsl:value-of select="japex:testSuiteReport/japex:dateTime"/></p>
                    <table>
                        <tbody>
                            <xsl:for-each select="japex:testSuiteReport/japex:driver">
                               <tr>
				<th>Test case</th>
				<th><xsl:value-of select="@name" /></th>
				<th>Specification reference</th>
				<th>Input file</th>
				<th>Testcase description</th></tr>
                                
                                <xsl:apply-templates select="japex:testCase" />
                                
                            </xsl:for-each>
                        </tbody>    
                    </table>
                </div>	
            </body>
        </html>
    </xsl:template>
    
    <xsl:variable name="testdata-dir" select="'http://www.w3.org/XML/Group/EXI/TTFMS'"/>
    
    <xsl:template match="japex:testCase">
        <tr xmlns="http://www.w3.org/1999/xhtml" class="testcase">
          <td><xsl:value-of select="groupId"/> | <xsl:value-of select="@name"/></td>
            <xsl:choose>
                <xsl:when test="japex:resultValue = 'NaN'">
                    <td class="fail">FAILED</td>
                   
                </xsl:when>
                <xsl:otherwise>
                    <td class="pass">PASSED</td>
                </xsl:otherwise>
            </xsl:choose>
            <td><a href="{reference}">specification</a></td>  
            <td><a href="{$testdata-dir}/{japex:inputFile}">file</a></td>  
            <td>
            	<xsl:choose>
                	<xsl:when test="description != 'n/a'">
                    		<xsl:value-of select="description"/>
                	</xsl:when>
                	<xsl:otherwise>
                	    	<xsl:value-of select="testDescription"/>
                	</xsl:otherwise>
            	</xsl:choose>
	    </td>
            
        </tr>    
    </xsl:template>
   
</xsl:stylesheet>
