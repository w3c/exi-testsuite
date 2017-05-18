ROSETTANET SAMPLE FILES FOR EXI EVALUATION
==========================================
Greg White, gwhite at stanford.edu, 1-Apr-2006

Source for RosettaNet: 
Benson Cheng & Tony Curwen
IBM Business Transformation Outsourcing (BTO).

Notes 
----- 

* XSDs *are* used by RosettaNet implementations, but they're applied
  at the application level. While IBM has these XSDs in file form,
  they are confidential so were withheld from the EXI test suite.

* Files were run through a simple anonymizer by IBM, replacing TEXT
  element content with random char or digital data.

dtds refered to:
[palace:exi/other-test-data/RnFiles] greg% awk '/^\<\!DOCTYPE.+SYSTEM.+\.dtd/ {print $4}' *.xml 
"3A1_MS_V02_00_QuoteConfirmation.dtd">
"3A1_MS_V02_00_QuoteConfirmation.dtd">
"3A4PurchaseOrderRequestMessageGuideline_v1_4.dtd">
"Pip3A4PurchaseOrderAcceptanceGuideline.dtd">
"Pip3A4PurchaseOrderAcceptanceGuideline.dtd">
"3A7_MS_V02_00_PurchaseOrderUpdateNotification.dtd">
"3A7_MS_V02_00_PurchaseOrderUpdateNotification.dtd">
"5C2_MS_V01_00_DesignRegistrationRequest.dtd">
"7B5_MS_V01_00_NotifyOfManufacturingWorkOrder.dtd">
"ReceiptAcknowledgementMessageGuideline.dtd">
"PreamblePartMessageGuideline.dtd">
"ServiceHeaderPartMessageGuideline.dtd">
"DeliveryHeader_MS_V02_00.dtd">
"Preamble_MS_V02_00.dtd">
"ServiceHeader_MS_V02_00.dtd">
