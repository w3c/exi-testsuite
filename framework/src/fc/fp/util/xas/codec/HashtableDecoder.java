package fc.fp.util.xas.codec;

import java.util.Hashtable;

import fc.fp.util.xas.ContentDecoder;
import fc.fp.util.xas.ChainedContentDecoder;
import fc.fp.util.xas.XmlReader;
import fc.fp.util.xas.EventList;
import fc.fp.util.xas.XasUtil;
import fc.fp.util.Util;

/**
 * A typed content decoder for the {@link Hashtable} type.  This class
 * extends the {@link ContentDecoder} abstraction for decoding {@link
 * Hashtable} objects from XML.
 */
public class HashtableDecoder extends ChainedContentDecoder {

    public HashtableDecoder (ContentDecoder chain) {
	super(null);
	if (chain == null) {
	    throw new IllegalArgumentException("Chained decoder must be"
					       + " non-null");
	}
	this.chain = chain;
    }

    public Object decode (String namespace, String name, XmlReader reader,
			  EventList attributes) {
	Object result = null;
	if (Util.equals(namespace, XasUtil.XAS_NAMESPACE)
	    && Util.equals(name, "hashtable")) {
	    Hashtable h = new Hashtable();
	    Object key;
	    while ((key = expect(XasUtil.XAS_NAMESPACE, "key", reader))
		   != null) {
		Object value = expect(XasUtil.XAS_NAMESPACE, "value", reader);
		if (value != null) {
		    h.put(key, value);
		}
	    }
	    result = h;
	} else if (chain != null) {
	    result = chain.decode(namespace, name, reader, attributes);
	}
	return result;
    }

}
// arch-tag: 4aac0a4f80ac54bce1f14b12d26df05c *-
