package fc.fp.util.xas.codec;

import java.io.IOException;

import fc.fp.util.xas.ContentEncoder;
import fc.fp.util.xas.ChainedContentEncoder;
import fc.fp.util.xas.EventSequence;
import fc.fp.util.xas.TypedXmlSerializer;
import fc.fp.util.xas.XasUtil;
import fc.fp.util.Util;

/**
 * A typed content encoder for the {@link EventSequence} type.  This
 * class implements the {@link ContentEncoder} interface for encoding
 * {@link EventSequence} objects into XML.
 */
public class EventSequenceEncoder extends ChainedContentEncoder {

    public EventSequenceEncoder (ContentEncoder chain) {
	this.chain = chain;
    }

    public boolean encode (Object o, String namespace, String name,
			   TypedXmlSerializer ser)
	throws IOException {
	boolean result = false;
	if (Util.equals(namespace, XasUtil.XAS_NAMESPACE)
	    && Util.equals(name, "XmlEventSequence")) {
	    if (o instanceof EventSequence) {
		//System.out.println("Encoding event sequence " + o);
		putTypeAttribute(namespace, name, ser);
		EventSequence e = (EventSequence) o;
		XasUtil.outputSequence(e, ser);
		result = true;
	    }
	} else if (chain != null) {
	    result = chain.encode(o, namespace, name, ser);
	}
	return result;
    }

}
// arch-tag: c7ffe663d7a97277ce7bac7333acb9ee *-
