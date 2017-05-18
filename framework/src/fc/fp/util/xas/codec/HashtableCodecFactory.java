package fc.fp.util.xas.codec;

import fc.fp.util.xas.ContentCodecFactory;
import fc.fp.util.xas.ContentEncoder;
import fc.fp.util.xas.ContentDecoder;
import fc.fp.util.xas.Qname;
import fc.fp.util.xas.XasUtil;

/**
 * A typed content codec factory for the {@link java.util.Hashtable}
 * type.  This {@link ContentCodecFactory} builds encoders and
 * decoders recognizing the {@link java.util.Hashtable} Java type.
 * The XML name given for this type has namespace {@link
 * XasUtil#XAS_NAMESPACE} and local name <code>"hashtable"</code>.
 * This type mapping is registered during initialization of the class.
 */
public class HashtableCodecFactory extends ContentCodecFactory {

    private ContentCodecFactory factory;

    static {
	try {
	    ContentCodecFactory.addTypeMapping
		(Class.forName("java.util.Hashtable"),
		 new Qname(XasUtil.XAS_NAMESPACE, "hashtable"));
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public HashtableCodecFactory () {
	this(null);
    }

    public HashtableCodecFactory (ContentCodecFactory factory) {
	this.factory = factory;
    }

    public ContentEncoder getChainedEncoder (ContentEncoder chain) {
	if (factory != null) {
	    chain = factory.getChainedEncoder(chain);
	}
	return new HashtableEncoder(chain);
    }

    public ContentDecoder getChainedDecoder (ContentDecoder chain) {
	if (factory != null) {
	    chain = factory.getChainedDecoder(chain);
	}
	return new HashtableDecoder(chain);
    }

}
// arch-tag: 49f0e6a2b069248af28f216bf2d97839 *-
