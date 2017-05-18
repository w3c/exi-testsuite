package fc.fp.util.xas;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

/**
 * An extended interface to permit the output of typed XML content. Normally a
 * legacy application would output both typed and untyped content encoded as a
 * text {@link String}, making it impossible for an alternative encoder to
 * determine whether the content should be encoded specially or it already is.
 * This interface is intended for use when the content has already been encoded
 * and must not be encoded further. The normal <code>text()</code> methods are
 * to be used when default (or no) encoding is appropriate.
 */
public interface TypedXmlSerializer extends XmlSerializer {

    /**
         * Output the given content as the specified type. This method will
         * output an {@link Object} having the given XML Schema datatype. If the
         * datatype has no special encoding known to this serializer, it will
         * fall back on the <code>toString()</code> method of the supplied
         * object.
         * 
         * @param content the object to output
         * @param namespace the namespace URI of the XML Schema type
         * @param name the local name of the XML Schema type
         * @return this encoder for the purposes of chaining
         * 
         * @throws IllegalArgumentException if the given type name was not
         *         introduced by the preceding start element event
         */
    TypedXmlSerializer typedContent (Object content, String namespace,
	    String name) throws IOException;

    TypedXmlSerializer typedAttribute (String namespace, String name,
	    Object content) throws IOException;

}
// arch-tag: 366bef025b0e66e746f5819591fbe228 *-
