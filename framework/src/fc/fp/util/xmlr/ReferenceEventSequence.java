// $Id: ReferenceEventSequence.java,v 1.1 2010/02/23 20:31:10 tkamiya Exp $

package fc.fp.util.xmlr;

import fc.fp.util.xas.Event;
import fc.fp.util.xas.EventList;
import fc.fp.util.xas.EventSequence;
import fc.fp.util.xas.TransformedEventStream;
import fc.fp.util.xas.XmlReader;

/** Event sequence that decodes reference events. */

public class ReferenceEventSequence extends TransformedEventStream {

  /** Create a new sequence.
   *
   * @param in underlying event sequence, from which reference events
   * are decoded.
   */
  public ReferenceEventSequence(EventSequence in) {
    super(in);
  }

  protected void transform(Event ev, EventList el, XmlReader xr) {
    Event e = ReferenceEvent.decode(ev,xr);
    if( e!= null )
      el.add(e);
  }
}
// arch-tag: 00a6793acc7c08d6514d0c0b9a0e68d9 *-
