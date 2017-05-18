package fc.fp.util.xas.codec;

import fc.fp.util.xas.ContentCodecFactory;
import fc.fp.util.xas.ContentEncoder;
import fc.fp.util.xas.ContentDecoder;
import fc.fp.util.xas.Qname;
import fc.fp.util.xas.XasUtil;

/**
 * A typed content codec factory for the {@link
 * fc.fp.util.xas.EventSequence} type.  This {@link
 * ContentCodecFactory} builds encoders and decoders recognizing the
 * {@link fc.fp.util.xas.EventSequence} Java type.  The XML name
 * given for this type has namespace {@link XasUtil#XAS_NAMESPACE} and
 * local name <code>"XmlEventSequence"</code>.  This type mapping is
 * registered during initialization of the class.
 */
public class EventSequenceCodecFactory extends ContentCodecFactory {

    private ContentCodecFactory factory;

    static {
	try {
	    ContentCodecFactory.addTypeMapping
		(Class.forName("fc.fp.util.xas.EventSequence"),
		 new Qname(XasUtil.XAS_NAMESPACE, "XmlEventSequence"));
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public EventSequenceCodecFactory () {
	this(null);
    }

    public EventSequenceCodecFactory (ContentCodecFactory factory) {
	this.factory = factory;
    }

    public ContentEncoder getChainedEncoder (ContentEncoder chain) {
	if (factory != null) {
	    chain = factory.getChainedEncoder(chain);
	}
	return new EventSequenceEncoder(chain);
    }

    public ContentDecoder getChainedDecoder (ContentDecoder chain) {
	if (factory != null) {
	    chain = factory.getChainedDecoder(chain);
	}
	return new EventSequenceDecoder(chain);
    }

}
// arch-tag: 56d399e7a2e4b2bea5df91d2fe51b6cc *-
