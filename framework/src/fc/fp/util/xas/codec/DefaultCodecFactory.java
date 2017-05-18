package fc.fp.util.xas.codec;

import fc.fp.util.xas.ContentCodecFactory;
import fc.fp.util.xas.ContentEncoder;
import fc.fp.util.xas.ContentDecoder;

/**
 * A default factory for typed content encoders and decoders.  An
 * object of this class is suitable to use as the default {@link
 * ContentCodecFactory} implementation.  It recognizes some standard
 * structured Java types.
 */
public class DefaultCodecFactory extends ContentCodecFactory {

    private static ContentCodecFactory factory =
	new HashtableCodecFactory
	(new VectorCodecFactory
	 (new EventSequenceCodecFactory()));

    public ContentEncoder getChainedEncoder (ContentEncoder chain) {
	return factory.getChainedEncoder(chain);
    }

    public ContentDecoder getChainedDecoder (ContentDecoder chain) {
	return factory.getChainedDecoder(chain);
    }

}
// arch-tag: 48e706c71fe386fed8daed45146b867b *-
