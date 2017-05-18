package fc.fp.util.xas.codec;

import fc.fp.util.xas.ContentCodecFactory;
import fc.fp.util.xas.ContentEncoder;
import fc.fp.util.xas.ContentDecoder;
import fc.fp.util.xas.Qname;
import fc.fp.util.xas.XasUtil;

/**
 * A typed content codec factory for the {@link java.util.Vector}
 * type.  This {@link ContentCodecFactory} builds encoders and
 * decoders recognizing the {@link java.util.Vector} Java type.  The
 * XML name given for this type has namespace {@link
 * XasUtil#XAS_NAMESPACE} and local name <code>"vector"</code>.  This
 * type mapping is registered during initialization of the class.
 */
public class VectorCodecFactory extends ContentCodecFactory {

    private ContentCodecFactory factory;

    static {
	try {
	    ContentCodecFactory.addTypeMapping
		(Class.forName("java.util.Vector"),
		 new Qname(XasUtil.XAS_NAMESPACE, "vector"));
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public VectorCodecFactory () {
	this(null);
    }

    public VectorCodecFactory (ContentCodecFactory factory) {
	this.factory = factory;
    }

    public ContentEncoder getChainedEncoder (ContentEncoder chain) {
	if (factory != null) {
	    chain = factory.getChainedEncoder(chain);
	}
	return new VectorEncoder(chain);
    }

    public ContentDecoder getChainedDecoder (ContentDecoder chain) {
	if (factory != null) {
	    chain = factory.getChainedDecoder(chain);
	}
	return new VectorDecoder(chain);
    }

}
// arch-tag: ec8b0385a9d00a44d0e36976fa1c9cf4 *-
