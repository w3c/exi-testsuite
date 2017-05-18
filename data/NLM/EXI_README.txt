EXI test cases of National Library of Medicine (NLM) XML formats.
=================================================================
EXI Contact: gwhite at stanford.edu, 1-Apr-2006

The NLM manages a number of standard XML vocabularies for medical 
data records and data exchange. Among these, MEDLINE/Pubmed.
 
MEDLINE is the primary component of PubMed (http://pubmed.gov); a link
to PubMed is found on the NLM home page at http://www.nlm.nih.gov.

From http://www.nlm.nih.gov

Relevance to XML: 
MEDLINE/Pubmed is the primary citation index of medical literature.

Provenance: 
-----------
Main contact for test case provenance :
NONE so far.

Source URI:
http://www.nlm.nih.gov/bsd/sample_records_avail.html

Data
----
MEDLINE
-------
From http://www.nlm.nih.gov/bsd/sample_records_avail.html

Data file(s):
     medsamp2006.xml - This is a small sample file of 87
representative records covering each of the five status categories of
records distributed to MEDLINE/PubMed licensees (i.e., MEDLINE,
In-Data-Review, In-process, PubMed-not-MEDLINE, and OLDMEDLINE) is
available.

MEDLINE DTDs:
nlmmedline_060101.dtd. This DTD
references the NLMMedlineCitation DTD at
nlmmedlinecitation_060101.dtd
that in turn references the new NLMSharedCatCit DTD at
nlmsharedcatcit_060101.dtd that
in turn references the NLMCommon DTD at
dtd/nlmcommon_060101.dtd.

CatfilePlus in XML
-------------------
From http://www.nlm.nih.gov/bsd/sample_records_avail.html
Data files: 
     catplussamp2006.xml

CatfilePlus in XML is defined by three NLM DTDs:
The 2006 NLMCatalogRecord DTD is available at
nlmcatalogrecord_060101.dtd. This
DTD references the NLMSharedCatCit DTD at
nlmsharedcatcit_060101.dtd (as MEDLINE above) that
in turn references the NLMCommon DTD
nlmcommon_060101.dtd (as MEDLINE above).

Serfile
-------
From http://www.nlm.nih.gov/bsd/sample_records_avail.html
Data files: 
     sersamp2006.xml A 60 record sample serfile.

DTDs
The req DTDs are as MEDLINE above.
