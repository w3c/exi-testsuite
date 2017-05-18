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

import java.net.URI;

/**
 * @author AgileDelta
 * @author Sun
 * 
 */
public enum DataSourceSinkParam {
    memory,
    network,
    file;
    
    public static DataSourceSinkParam createDataSourceSinkParam(
            URI dataSourceSink) throws IllegalArgumentException {        
        return valueOf(dataSourceSink.getScheme());
    }     
}
