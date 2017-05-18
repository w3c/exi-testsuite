// $Id: DataEventSequence.java,v 1.1 2010/02/23 20:31:08 tkamiya Exp $
// Moved here on 2004-10-19, before that:
// Id: fuegocore/syxaw/util/EssentialEvents.java,v 1.2 2004/08/18 14:44:43 ctl Exp
package fc.fp.util.xas;

import fc.fp.syxaw.util.Log;
import java.util.Enumeration;

/** Event sequence suitable for reading data-oriented XML. The class strips
 * out events that in frequently are of no significance to data-oriented XML.
 * <p>
 * These stripped events are {@link Event#COMMENT},
 * {@link Event#PROCESSING_INSTRUCTION}, and {@link Event#NAMESPACE_PREFIX}.
 * {@link Event#CONTENT} consisting of only whitespace are also stripped
 * by default. The {@link Event#START_DOCUMENT} and {@link Event#END_DOCUMENT}
 * events may optionally be stripped.
 */

public class DataEventSequence extends TransformedEventStream {

  protected boolean normalizeWhitespace = true;
  protected boolean stripDocumentStartEnd = false;

  protected EventList preamble=null;

  public DataEventSequence(EventSequence in) {
    super(in);
    // Preamble = all stuff except SD until first SE
    preamble = new EventList();
    for (Enumeration en = in.events();en.hasMoreElements();) {
      Event e = (Event) en.nextElement();
      if( e.getType()==Event.START_ELEMENT )
        break;
      if( e.getType() == Event.START_DOCUMENT )
        continue;
      preamble.add(e);
    }
  }

  public DataEventSequence(EventSequence in, boolean normalizeWhitespace,
                           boolean stripDocumentStartEnd) {
    this(in);
    this.normalizeWhitespace = normalizeWhitespace;
    this.stripDocumentStartEnd = stripDocumentStartEnd;
  }

  public EventSequence getPreamble() {
    return preamble;
  }

  protected void transform(Event ev, EventList el, XmlReader xr) {
    // Ignore comments
    if (ev.getType() == Event.COMMENT)
      return;
    // Ignore whitespace
    else if (ev.getType() == Event.CONTENT && normalizeWhitespace &&
        isWhiteSpace((String) ev.getValue()))
      return;
    // Ignore PIs
    else if (ev.getType() == Event.PROCESSING_INSTRUCTION) {
      return;
    }
    // Ignore namespace mapping events
    else if (ev.getType() == Event.NAMESPACE_PREFIX)
      return;
    else if( (ev.getType() == Event.START_DOCUMENT ||
        ev.getType() == Event.END_DOCUMENT) && stripDocumentStartEnd )
     return;
    // Passed filter
    el.add(ev);
  }

  protected final boolean isWhiteSpace(String s) {
    int pos=0;
    int len = s.length();
    if( len == 0 )
      return true;
    for( char ch=s.charAt(pos);pos<len;pos++)
      if( s.charAt(pos) > ' ' && !Character.isWhitespace(s.charAt(pos)) )
        return false;
    return true;
  }
}
// arch-tag: d38232e8b87b86f4217eb11f85cc2ab0 *-
