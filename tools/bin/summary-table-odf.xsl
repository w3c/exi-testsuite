<?xml version="1.0" encoding="utf-8"?>
<!--
summary-table-odf: http://www.w3.org/XML/Group/EXI/TTFMS/tools/bin/summary-table-odf.xsl

Copyright ?2006 World Wide Web Consortium, (Massachusetts Institute of 
Technology, European Research Consortium for Informatics and Mathematics, 
Keio University). All Rights Reserved. This work is distributed under the W3C?
Software License [1] in the hope that it will be useful, but WITHOUT ANY 
WARRANTY; without even the implied warranty of MERCHANTABILITY or 
FITNESS FOR A PARTICULAR PURPOSE. 

[1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
                xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
                xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
                version="1.0">

  <xsl:output method="xml" indent="yes"/>

  <!-- generate document skeleton with table; then generate headers + cell data -->
  <xsl:template match="/">
    <office:document-content office:version="1.0">
      <office:body>
        <office:spreadsheet>
           <table:table>
              <xsl:apply-templates select="/results-table/result[1]" mode="header"/>
              <xsl:apply-templates select="/results-table/result"/>
           </table:table>
        </office:spreadsheet>
      </office:body>
    </office:document-content>
  </xsl:template>

  <!-- generate data row -->
  <xsl:template match="result">
    <table:table-row>
      <xsl:apply-templates select="*"/>
    </table:table-row>
  </xsl:template>

  <!-- generate value cell -->
  <xsl:template match="result/*">
    <table:table-cell>
      <xsl:choose>
        <xsl:when test="number(.)">
          <xsl:attribute name="office:value-type">float</xsl:attribute>
          <xsl:attribute name="office:value"><xsl:value-of select="number(.)"/></xsl:attribute>
        </xsl:when>
        <xsl:when test="normalize-space(.) = 'true' or normalize-space(.) = 'false'">
          <xsl:attribute name="office:value-type">boolean</xsl:attribute>
          <xsl:attribute name="office:boolean-value"><xsl:value-of select="normalize-space(.)"/></xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="office:value-type">string</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <text:p><xsl:value-of select="."/></text:p>
    </table:table-cell>
  </xsl:template>
  
  <!-- generate header row -->
  <xsl:template match="result" mode="header">
    <table:table-row>
      <xsl:apply-templates select="*" mode="header"/>
    </table:table-row>
  </xsl:template>

  <!-- generate cell in header row -->
  <xsl:template match="result/*" mode="header">
    <table:table-cell>
      <text:p><xsl:value-of select="local-name()"/></text:p>
    </table:table-cell>
  </xsl:template>
  
</xsl:stylesheet>
