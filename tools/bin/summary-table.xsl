<?xml version="1.0" encoding="utf-8"?>
<!--
summary-table: http://www.w3.org/XML/Group/EXI/TTFMS/tools/bin/summary-table.xsl

Copyright (C) 2006 World Wide Web Consortium, (Massachusetts Institute of 
Technology, European Research Consortium for Informatics and Mathematics, 
Keio University). All Rights Reserved. This work is distributed under the W3
Software License [1] in the hope that it will be useful, but WITHOUT ANY 
WARRANTY; without even the implied warranty of MERCHANTABILITY or 
FITNESS FOR A PARTICULAR PURPOSE. 

[1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:r="http://www.sun.com/japex/testSuiteReport"
                version="1.0">

  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="/">
    <results-table>
      <xsl:apply-templates select=".//r:driver"/>
    </results-table>
  </xsl:template>
  <xsl:template match="r:driver">
    <xsl:apply-templates select="r:testCase"/>
  </xsl:template>
  
  <!-- suppress certain test cases -->
  <xsl:template match="r:testCase[ string(number(r:resultValue)) = 'NaN' ]"/>
  <xsl:template match="r:driver[@normal][pos != last()]"/>
  <xsl:template match="r:driver[@name = 'EsxmlBothSAX'][starts-with(../r:configFile,'compaction-schema.xml')]"/>
  
  <!-- output testcase -->
  <xsl:template match="r:testCase">
    <result>
      <config><xsl:value-of select="ancestor::r:testSuiteReport/r:configFile"/></config>
      <driver><xsl:value-of select="parent::r:driver/@name"/></driver>
      <property><xsl:value-of select="ancestor::r:testSuiteReport/org.w3c.exi.ttf.measurementProperty"/></property>
      <candidate><xsl:value-of select="parent::r:driver/org.w3c.exi.ttf.driver.candidateName"/></candidate>
      <application-class><xsl:value-of select="parent::r:driver/org.w3c.exi.ttf.applicationClass"/></application-class>
      <schema-analysis><xsl:value-of select="normalize-space(parent::r:driver/org.w3c.exi.ttf.driver.schemaOptimizing) = 'true'"/></schema-analysis>
      <document-analysis><xsl:value-of select="normalize-space(parent::r:driver/org.w3c.exi.ttf.driver.documentAnalysing) = 'true'"/></document-analysis>
      <testcase-group><xsl:value-of select="substring-before(@name,'/')"/></testcase-group>
      <testcase-directory><xsl:call-template name="extract-directory"><xsl:with-param name="value" select="@name"/></xsl:call-template></testcase-directory>
      <testcase><xsl:value-of select="@name"/></testcase>
      <value><xsl:value-of select="r:resultValue"/></value>
      <unit><xsl:value-of select="ancestor::r:testSuiteReport/r:resultUnit"/></unit>
      <normal><xsl:value-of select="boolean(@normal)"/></normal>
      <time><xsl:value-of select="ancestor::r:testSuiteReport/r:dateTime"/></time>
      <environment><xsl:value-of select="ancestor::r:testSuiteReport/r:osName"/></environment>
      <reference-value><xsl:value-of select="ancestor::r:testSuiteReport/r:driver[@normal][last()]/r:testCase[@name = current()/@name]/r:resultValue"/></reference-value>
      <relative-value><xsl:value-of select="r:resultValue div ancestor::r:testSuiteReport/r:driver[@normal][last()]/r:testCase[@name = current()/@name]/r:resultValue"/></relative-value>
      <suspicious><xsl:value-of select="r:resultValue div ancestor::r:testSuiteReport/r:driver[@normal][last()]/r:testCase[@name = current()/@name]/r:resultValue > 1500"/></suspicious>
      <testcase-clean><xsl:value-of select="count( ancestor::r:testSuiteReport/r:driver[ org.w3c.exi.ttf.driver.candidateName != 'Asn1Ber' ][ org.w3c.exi.ttf.driver.candidateName != 'Esxml' or ../org.w3c.exi.ttf.measurementProperty != 'encode' ] ) = count(ancestor::r:testSuiteReport/r:driver[ org.w3c.exi.ttf.driver.candidateName != 'Asn1Ber' ][ org.w3c.exi.ttf.driver.candidateName != 'Esxml' or ../org.w3c.exi.ttf.measurementProperty != 'encode' ]/r:testCase[ @name = current()/@name ][ string(number(r:resultValue)) != 'NaN' ] )"/></testcase-clean>
    </result>    
  </xsl:template>
  
  <!-- extract the directory name from a string -->
  <xsl:template name="extract-directory">
    <xsl:param name="value" select="."/>
    <xsl:if test="contains($value,'/')">
      <xsl:value-of select="substring-before($value,'/')"/>
      <xsl:if test="contains(substring-after($value,'/'),'/')">
        <xsl:text>/</xsl:text>
        <xsl:call-template name="extract-directory">
          <xsl:with-param name="value" select="substring-after($value,'/')"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
