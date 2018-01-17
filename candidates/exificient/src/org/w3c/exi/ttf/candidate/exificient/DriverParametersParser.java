/*
 * EXI Testing Task Force Measurement Suite: http://www.w3.org/XML/EXI/
 *
 * Copyright © [2006] World Wide Web Consortium, (Massachusetts Institute of
 * Technology, European Research Consortium for Informatics and Mathematics,
 * Keio University). All Rights Reserved. This work is distributed under the
 * W3C® Software License [1] in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */

package org.w3c.exi.ttf.candidate.exificient;

import org.w3c.exi.ttf.parameters.DriverParameters;
import org.w3c.exi.ttf.parameters.PreserveParam;
import org.w3c.exi.ttf.parameters.TestCaseParameters;

import com.siemens.ct.exi.core.CodingMode;
import com.siemens.ct.exi.core.Constants;
import com.siemens.ct.exi.core.EXIFactory;
import com.siemens.ct.exi.core.EncodingOptions;
import com.siemens.ct.exi.core.FidelityOptions;
import com.siemens.ct.exi.grammars.GrammarFactory;
import com.siemens.ct.exi.core.grammars.Grammars;
import com.siemens.ct.exi.core.helpers.DefaultEXIFactory;

/**
 * @author Daniel.Peintner.EXT@siemens.com
 */

public class DriverParametersParser {
	public EXIFactory createFactory(DriverParameters driverParams,
			TestCaseParameters testCaseParams) throws Exception {

		// set-up default EXI factory
		EXIFactory f = DefaultEXIFactory.newInstance();
		
		// set alignment and compression mode
		if (testCaseParams.compression || driverParams.isDocumentAnalysing) {
			// use "compression"
			f.setCodingMode(CodingMode.COMPRESSION);
		} else if (testCaseParams.preCompression) {
			// use "preCompression"
			f.setCodingMode(CodingMode.PRE_COMPRESSION);
		} else if (testCaseParams.byteAlign) {
			// use "byteAlign"
			f.setCodingMode(CodingMode.BYTE_PACKED);
		} else {
			// default mode, bitAlign
			f.setCodingMode(CodingMode.BIT_PACKED);
		}

		// blockSize when compressing or pre-compressing
		if (testCaseParams.blockSize > 0
				&& testCaseParams.blockSize != Constants.DEFAULT_BLOCK_SIZE) {
			f.setBlockSize(testCaseParams.blockSize);
		}

		// optional string table settings: valueMaxLength,
		// valuePartitionCapacity
		if (testCaseParams.valueMaxLength >= 0) {
			f.setValueMaxLength(testCaseParams.valueMaxLength);
		}
		if (testCaseParams.valuePartitionCapacity >= 0) {
			f.setValuePartitionCapacity(testCaseParams.valuePartitionCapacity);
		}

		// code EXI fragment instead of an EXI document
		if (testCaseParams.fragments) {
			f.setFragment(true);
		}

		// use schema information (default == schema-less)
		if ((testCaseParams.useSchemas || driverParams.isSchemaOptimizing)) {
			// schema-informed
			GrammarFactory grammarFactory = GrammarFactory.newInstance();
			Grammars grammars;
			if (testCaseParams.schemaLocation == null
					|| testCaseParams.schemaLocation.length() == 0
					// || testCaseParams.schemaLocation.endsWith("autoschema.xsd")
					) {
				// XSD built-in types only
				grammars = grammarFactory.createXSDTypesOnlyGrammars();
			} else {
				// load XML schema file
				grammars = grammarFactory
						.createGrammars(testCaseParams.schemaLocation);
			}
			f.setGrammars(grammars);
		}

		// Strict interpretation of schemas
		FidelityOptions fo = testCaseParams.schemaDeviations ? FidelityOptions
				.createDefault() : FidelityOptions.createStrict();
		f.setFidelityOptions(fo);

		// specify whether comments, pis, etc. are preserved
		for (PreserveParam param : testCaseParams.preserves) {
			switch (param) {
			case comments:
				fo.setFidelity(FidelityOptions.FEATURE_COMMENT, true);
				break;
			case pis:
				fo.setFidelity(FidelityOptions.FEATURE_PI, true);
				break;
			case prefixes:
				fo.setFidelity(FidelityOptions.FEATURE_PREFIX, true);
				break;
			case dtds:
				fo.setFidelity(FidelityOptions.FEATURE_DTD, true);
				// encode any entity reference as ER event in EXI
				f.getEncodingOptions().setOption(
						EncodingOptions.RETAIN_ENTITY_REFERENCE);
				break;
			case lexicalvalues:
				fo.setFidelity(FidelityOptions.FEATURE_LEXICAL_VALUE, true);
				break;
			case entityreferences:
				fo.setFidelity(FidelityOptions.FEATURE_DTD, true);
				break;
			// case notations:
			default:
				throw new UnsupportedOperationException(
						"[EXIficient] PreserveParam '" + param.toString()
								+ "' is not supported");
			}
		}

		// selfContained elements
		if (testCaseParams.selfContainedQNames != null
				&& testCaseParams.selfContainedQNames.length > 0) {
			f.setSelfContainedElements(testCaseParams.selfContainedQNames);
			fo.setFidelity(FidelityOptions.FEATURE_SC, true);
		}

		// datatypeRepresentationMap
		if (testCaseParams.dtrMapTypes != null
				&& testCaseParams.dtrMapRepresentations != null
				&& testCaseParams.dtrMapTypes.length != 0
				&& testCaseParams.dtrMapTypes.length == testCaseParams.dtrMapRepresentations.length) {
			f.setDatatypeRepresentationMap(testCaseParams.dtrMapTypes,
					testCaseParams.dtrMapRepresentations);
		}
		
		// EXI profile options
		if(testCaseParams.useProfile) {
			// local value partitions
			f.setLocalValuePartitions(testCaseParams.localValuePartitions);
			//  max built-in grammars
			if(testCaseParams.maxBuiltinGr >= 0) {
				f.setMaximumNumberOfBuiltInElementGrammars(testCaseParams.maxBuiltinGr);	
			}
			// max built-in productions
			if(testCaseParams.maxBuiltinProd >= 0) {
				f.setMaximumNumberOfBuiltInProductions(testCaseParams.maxBuiltinProd);
			}
		}

		// test framework issues when insignificant xsi:nil and xsi:types are removed
		f.getEncodingOptions().setOption(EncodingOptions.INCLUDE_INSIGNIFICANT_XSI_NIL);
		// f.getEncodingOptions().setOption(EncodingOptions.INCLUDE_INSIGNIFICANT_XSI_TYPE);

		// "includeCookie"
		if (testCaseParams.includeCookie) {
			f.getEncodingOptions().setOption(EncodingOptions.INCLUDE_COOKIE);
		}
		// "includeOptions"
		if (testCaseParams.includeOptions) {
			f.getEncodingOptions().setOption(EncodingOptions.INCLUDE_OPTIONS);
		}
		// "includeSchemaId",
		if (testCaseParams.includeSchemaId) {
			f.getEncodingOptions().setOption(EncodingOptions.INCLUDE_SCHEMA_ID);
		}
		// include profile values
		if(testCaseParams.includeProfileValues) {
			f.getEncodingOptions().setOption(EncodingOptions.INCLUDE_PROFILE_VALUES);
		}
		
		// Canonical EXI
		if("iot_c14n_encode".equals(driverParams.measure.toString())) {
			f.getEncodingOptions().setOption(EncodingOptions.CANONICAL_EXI);
		}
		if(testCaseParams.utcTime) {
			f.getEncodingOptions().setOption(EncodingOptions.UTC_TIME);
		}
		if (!testCaseParams.includeOptions) { // "omitOptionsDocument"
			f.getEncodingOptions().unsetOption(EncodingOptions.INCLUDE_OPTIONS);
		}

		return f;
	}
}
