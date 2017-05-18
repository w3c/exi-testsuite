package fc.fp.util.xas.codec;

import fc.fp.util.xas.ContentDecoder;
import fc.fp.util.xas.ChainedContentDecoder;
import fc.fp.util.xas.XmlReader;
import fc.fp.util.xas.Event;
import fc.fp.util.xas.EventList;
import fc.fp.util.xas.XasUtil;
import fc.fp.util.Util;

/**
 * A typed content decoder for the {@link
 * fc.fp.util.xas.EventSequence} type.  This class extends the
 * {@link ContentDecoder} abstraction for decoding {@link
 * fc.fp.util.xas.EventSequence} objects from XML.
 */
public class EventSequenceDecoder extends ChainedContentDecoder {

    public EventSequenceDecoder (ContentDecoder chain) {
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
	    && Util.equals(name, "XmlEventSequence")) {
	    //System.out.println("Decoding event sequence");
	    int pos = reader.getCurrentPosition();
	    EventList e = new EventList();
	    int depth = 0;
	    Event ev = reader.advance();
	    while (ev != null && (depth > 0
				  || ev.getType() != Event.END_ELEMENT)) {
		//System.out.println("Adding event " + ev);
		e.add(ev);
		if (ev.getType() == Event.START_ELEMENT) {
		    depth += 1;
		} else if (ev.getType() == Event.END_ELEMENT) {
		    depth -= 1;
		}
		ev = reader.advance();
	    }
	    if (ev != null && depth == 0
		&& ev.getType() == Event.END_ELEMENT) {
		reader.backup();
		result = e;
	    } else {
		reader.setCurrentPosition(pos);
	    }
	} else if (chain != null) {
	    result = chain.decode(namespace, name, reader, attributes);
	}
	return result;
    }

}
// arch-tag: 00f0a7db203e11dcf5b0991d2b93c930 *-
