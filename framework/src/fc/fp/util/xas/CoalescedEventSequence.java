package fc.fp.util.xas;

/**
 * An event sequence that coalesces all content events.  This {@link
 * EventSequence} implementation takes an underlying {@link
 * EventSequence} and provides a view of that where there are no
 * consecutive {@link Event#CONTENT} events.  This is useful when
 * e.g. using the XML serialization format and there are many escaped
 * characters that would each be returned as separate {@link
 * Event#CONTENT} events.
 */
public class CoalescedEventSequence extends TransformedEventStream {

    private StringBuffer content = new StringBuffer();

    protected void transform (Event ev, EventList el, XmlReader xr) {
	if (ev != null) {
	    if (ev.getType() == Event.CONTENT) {
		Event e = xr.getCurrentEvent();
		boolean looped = false;
		while (e != null && e.getType() == Event.CONTENT) {
		    if (!looped) {
			content.append((String) ev.getValue());
		    }
		    looped = true;
		    content.append((String) e.getValue());
		    xr.advance();
		    e = xr.getCurrentEvent();
		}
		if (looped) {
		    ev = Event.createContent(content.toString());
		    content.setLength(0);
		}
	    }
	    el.add(ev);
	}
    }

    /**
     * Standard constructor.
     *
     * @param es the {@link EventSequence} to wrap and provide content
     * coalescing for
     */
    public CoalescedEventSequence (EventSequence es) {
	super(es);
    }

}
// arch-tag: cf06315d3ad60872710970f5b38dce7c *-
