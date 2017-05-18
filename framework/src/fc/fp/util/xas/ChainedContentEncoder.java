package fc.fp.util.xas;

import java.io.IOException;

import fc.fp.util.xas.ContentEncoder;
import fc.fp.util.xas.TypedXmlSerializer;
import fc.fp.util.xas.XasUtil;

/**
 * A content encoder implementing chaining of encoders.  The typical
 * usage pattern for {@link ContentEncoder} implementations is to
 * chain them, i.e. an encoder is given a pre-existing encoder, which
 * it delegates to if it does not recognize the type to encode.  This
 * class contains a field for this chained encoder, as well as a
 * method to insert the XML Schema type attribute.
 */
public abstract class ChainedContentEncoder implements ContentEncoder {

    protected ContentEncoder chain;

    /**
     * Insert an appropriate XML Schema type attribute.  It is the
     * responsibility of the {@link ContentEncoder#encode} method to
     * insert the <code>type</code> attribute for the object to
     * encode.  Since this code is practically always the same, it is
     * included as a common part of this class.  This method invokes
     * the {@link TypedXmlSerializer#attribute} method with the
     * supplied type name and other arguments.
     *
     * @param namespace the namespace URI of the type
     * @param name the local name of the type
     * @param ser the serializer to use for outputting the type
     * attribute
     */
    protected void putTypeAttribute (String namespace, String name,
				     TypedXmlSerializer ser)
	throws IOException {
	String prefix = ser.getPrefix(namespace, false);
	if (prefix != null) {
	    ser.attribute(XasUtil.XSI_NAMESPACE, "type", prefix + ":" + name);
	} else {
	    throw new IOException("Type namespace " + namespace
				  + " not recognized");
	}
    }

}
// arch-tag: 230a0da30bb3115ae30f7a775ac05009 *-
