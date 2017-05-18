package fc.fp.util.xas.codec;

import java.util.Vector;

import fc.fp.util.xas.ContentDecoder;
import fc.fp.util.xas.ChainedContentDecoder;
import fc.fp.util.xas.XmlReader;
import fc.fp.util.xas.EventList;
import fc.fp.util.xas.XasUtil;
import fc.fp.util.Util;

/**
 * A typed content decoder for the {@link Vector} type.  This class
 * extends the {@link ContentDecoder} abstraction for decoding {@link
 * Vector} objects from XML.
 */
public class VectorDecoder extends ChainedContentDecoder {

    public VectorDecoder (ContentDecoder chain) {
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
	    && Util.equals(name, "vector")) {
	    Vector v = new Vector();
	    Object item;
	    while ((item = expect(XasUtil.XAS_NAMESPACE, "item", reader))
		   != null) {
		v.addElement(item);
	    }
	    result = v;
	} else if (chain != null) {
	    result = chain.decode(namespace, name, reader, attributes);
	}
	return result;
    }

}
// arch-tag: 28205714e7d7ae0ff541281528661cc1 *-
