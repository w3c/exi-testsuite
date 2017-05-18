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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Preserve enumeration.
 *
 * @author AgileDelta
 * @author Sun
 * @author Fujitsu
 * 
 */
public enum PreserveParam {
    /**
     * Preserve Comments.
     */
    comments,
    /** 
     * Preserve DTD/DOCTYPE Infoset information.
     */
    dtds,
    /**
     * Preserve entity references.
     */
    entityreferences,
    /**
     * Preserve original lexical value representation.
     */
    lexicalvalues,
    /**
     * Preserve notations.
     */
    notations,
    /**
     * Preserve Processing Instructions.
     */
    pis,
    /**
     * Preserve original namespace prefixes.
     */
    prefixes,
    /**
     * Preserve all whitespace.
     */
    whitespace;
    
    /**
     * Create a set of preserve options from a white space deliminated words.
     * <p>
     * Any unrecognized preserve options are ignored and not are not included
     * in the set.
     *
     * @param preserveList the white space deliminated list.
     */
    public static Set<PreserveParam> createPreserveSet(String preserveList, MeasureParam measure) {
        if (preserveList == null) preserveList = "";
        
        Set<PreserveParam> preserveSet = new HashSet<PreserveParam>();

        String[] preserves = preserveList.split("[\\s]+");
        for (String preserve : preserves) {
             try {
                 PreserveParam val = valueOf(preserve.toLowerCase());
                 switch (measure) {
                     case iot_decode:
                     case iot_encode:
                         // notations and whitespace are not used by iot.
                         if (val == entityreferences) // entityrefs implies dtds
                             preserveSet.add(dtds);
                         else if (val == dtds) // dtds implies entityrefs
                             preserveSet.add(entityreferences);
                         else if (val == notations)
                             break;
                     default:
                         preserveSet.add(val);
                         break;
                 }
             } catch (IllegalArgumentException e) {
                 // Ignore unrecognized value
             }
        }

        // In EXI, entityrefs implies dtds
        if (preserveSet.contains(entityreferences)) preserveSet.add(dtds);
        
        return Collections.unmodifiableSet(preserveSet);
    }     
}