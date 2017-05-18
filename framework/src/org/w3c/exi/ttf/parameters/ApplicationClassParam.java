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
 * Application class enumeration.
 *
 * @author AgileDelta
 * @author Sun
 * 
 */
public enum ApplicationClassParam {
    /**
     * Use neither Schema nor Compression.
     */
    neither,
    /**
     * Compression should be used, but Schema should not be used.
     */
    document,
    /**
     * Schema should be used.
     */
    schema,
    /**
     * Compression and Schema should be used.  
     */
    both;    
    
    /**
     * Create the application class from a string.
     * <p>
     * If the string is not a valid application class then the default
     * application class {@link #neither} is returned.
     *
     * @param applicationClass the application class as a string
     */
    public static ApplicationClassParam createApplicationClass(String applicationClass) {
        if (applicationClass == null) applicationClass = "";
        
        try {
            return valueOf(applicationClass.toLowerCase());
        } catch (IllegalArgumentException e) {
            return ApplicationClassParam.neither;
        }
    }     
}