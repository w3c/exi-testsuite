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

package org.w3c.exi.ttf.parameters;

/**
 * Measure enumeration.
 * 
 * @author AgileDelta
 * @author Sun
 * @author Fujitsu
 * 
 */
public enum MeasureParam {
    /**
     * Measure compactness.
     */
    compactness,
    /**
     * Measure processing decoding.
     */
    decode,
    /**
     * Measure processing encoding.
     */
    encode,
    /**
     * Decode streams testing interoperability.
     */
    iot_decode,
    /**
     * Encode streams testing interoperability.
     */
    iot_encode,
    /**
     * Encode test for c14 interoperability.
     */
    iot_c14n_encode;
    
    /**
     * Create the measure from a string.
     * <p>
     *
     * @param measure the measure property as a string
     */
    public static MeasureParam createMeasure(String measure) {
        if (measure == null) measure = "";
        
        try {
            return valueOf(measure.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }     
}