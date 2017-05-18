// $Id: ReferenceEvent.java,v 1.1 2010/02/23 20:31:10 tkamiya Exp $

package fc.fp.util.xmlr;

import fc.fp.util.xas.Event;
import fc.fp.util.xas.EventSequence;
import fc.fp.util.xas.XmlReader;

import java.util.Enumeration;
import fc.fp.util.xas.EventList;
import fc.fp.syxaw.util.Log;

 /* A design decision was to keep different start and end events for
 * node refs. This is motivated by sticking to the event stream paradigm at
 * this point. Another possibility would be to contain the event seq.
 * between START_REF_NODE and END_REF_NODE inside the event, but that
 * would add tree structure to the sequence, which we really don't
 * want to do in this class.
 *
 * Another issue that is somewhat weakly addressed here is that of extensibility:
 * This ReferenceEvent class is rather strongly bound to a particular
 * XML format. interface + implementation would probablybe nicer
 */

/** Event class that recognizes XMLR markup. This class is responsible
* for the mapping between the XML representation of references and
* the concept of a reference.
* <p>The class maps the following events to XML (as XAS event sequences):
* <table>
* <tr><th>Event type</th><th>XML</th></tr>
* <tr><td>{@link #REF_TREE}</td><td><code>&lt;ref:tree&gt</code></td></tr>
* <tr><td>{@link #START_REF_NODE}</td><td><code>&lt;ref:node&gt</code></td></tr>
* <tr><td>{@link #END_REF_NODE}</td><td><code>&lt;/ref:node&gt</code></td></tr>
* </table>
* where <code>ref</code> is the namespace
* <code>http://www.hiit.fi/fc/xml/ref</code>
*/
public class ReferenceEvent extends Event {


    /** Constant for element id target references. */
    public static final int TT_ID = 0;  // Note! Implementation relies on these
    /** Constant for XPath target references. */
    public static final int TT_PATH = 1; // Being ordered by priority!
    /** Constant for generic target references. */
    public static final int TT_TARGET = 2;

    private String target=null;
    private int targetType=-1;

    // Event default XML mapping

    /** Namespace for XMLR tags. */
    public static final String REF_NS = "http://www.hiit.fi/fc/xml/ref";
    /** Namespace for XMLR attributes. */
    public static final String REF_ATT_NS = "";
    /** Tag name for tree reference. */
    public static final String REF_TAG_TREE = "tree";
    /** Tag name for node reference. */
    public static final String REF_TAG_NODE = "node";
    /** Attribute name for id target. */
    public static final String REF_ATT_ID = "id";
    /** Attribute name for XPath target. */
    public static final String REF_ATT_PATH = "path";
    /** Attribute name for generic target. */
    public static final String REF_ATT_TARGET = "target";

    private static final String[] ATTNAME_NY_METHOD={REF_ATT_ID,REF_ATT_PATH,REF_ATT_TARGET};

    // Types. Using string hashes might be a stupid idea (javac can make them work
    // in case statements, slower init), but in principle it
    // gives a global namespace for extensions

    /** Tree reference event type. */
    public static final int REF_TREE = Event.TYPE_EXTENSION_FLAG |
	((REF_NS+"/REF_TREE").hashCode()&~Event.FLAG_BITMASK);
    /** Start node reference event type. */
    public static final int START_REF_NODE = Event.TYPE_EXTENSION_FLAG |
	((REF_NS+"/START_REF_NODE").hashCode()&~Event.FLAG_BITMASK);
    /** End node reference event type. */
    public static final int END_REF_NODE = Event.TYPE_EXTENSION_FLAG |
	((REF_NS+"/END_REF_NODE").hashCode()&~Event.FLAG_BITMASK);


    protected ReferenceEvent (int type, String namespace, String name, Object value) {
	super(type,namespace,name,value);
    }

    private static ReferenceEvent createReference(int type, RefTarget t, EventSequence s) {
	ReferenceEvent e = new ReferenceEvent(type,null,null,s);
        if( t != null ) {
          e.target = t.target;
          e.targetType = t.targetType;
        }
	return e;
    }

    /** Encode event as event sequence.
     * @return XAS events encoding the XML representation of this  event
     */

    public EventSequence encode() {
	return (EventSequence) getValue();
    }

    /** Create a tree reference event.
     *
     * @param target id target of the reference
     * @return ReferenceEvent the created event
     */
    public static ReferenceEvent createTreeReference(String target) {
      return createTreeReference(TT_ID,target);
    }

    /** Create a tree reference event.
     *
     * @param method target reference method; one of {@link #TT_ID},
     * {@link #TT_PATH} or {@link #TT_TARGET}.
     * @param target target string
     * @return ReferenceEvent the created event
     */
    public static ReferenceEvent createTreeReference(int method, String target) {
      EventList es = new EventList();
      es.add(Event.createStartElement(REF_NS,REF_TAG_TREE));
      es.add(Event.createAttribute(REF_ATT_NS,ATTNAME_NY_METHOD[method],target));
      es.add(Event.createEndElement(REF_NS,REF_TAG_TREE));
      return createReference(REF_TREE,new RefTarget(method,target),es);
    }

    /** Create a start node reference event.
     *
     * @param target id target of the reference
     * @return ReferenceEvent the created event
     */
    public static ReferenceEvent createNodeReference(String target) {
      return createNodeReference(TT_ID,target);
    }

    /** Create a start node reference event.
     *
     * @param method target reference method; one of {@link #TT_ID},
     * {@link #TT_PATH} or {@link #TT_TARGET}.
     * @param target target string
     * @return ReferenceEvent the created event
     */
    public static ReferenceEvent createNodeReference(int method, String target) {
      EventList es = new EventList();
      es.add(Event.createStartElement(REF_NS,REF_TAG_NODE));
      es.add(Event.createAttribute(REF_ATT_NS,ATTNAME_NY_METHOD[method],target));
      return createReference(START_REF_NODE,new RefTarget(method,target),es);
    }

    /** Create a end node reference event.
     * @return ReferenceEvent the created event
     */

    public static ReferenceEvent createEndNodeReference() {
      EventList es = new EventList();
      es.add(Event.createEndElement(REF_NS,REF_TAG_NODE));
      return createReference(END_REF_NODE,null,es);
    }

    // Decode item from an ES to RefEvents

    /** Decode reference event XML. Decodes one (or more) events
     * from the given reader
     * as a reference event. If no reference event is recognized at
     * the current position, the next event from the reader is returned.
     * @param events reader to decode events from
     * @return decoded event
     */

    public static Event decode(XmlReader events) {
      Event e = events.advance();
      return decode(e,events);
    }

    // Suitable for TransformedES implementation

    /** Decode reference event XML. Decodes one (or more) events
     * from the given reader
     * as a reference event. If no reference event is recognized at
     * the current position, the next event from the reader is returned.
     * Suitable for use with
     * {@link fc.fp.util.xas.TransformedEventStream}.
     * @param e the next event from the reader
     * @param events the reader from which <code>e</code> came; the
     * cursor is immediately following the position of <code>e</code>
     * @return decoded event
     */

    public static Event decode(Event e, XmlReader events) {
	if( e!= null && REF_NS.equals(e.getNamespace())) {
	    if( e.getType() == START_ELEMENT && REF_TAG_TREE.equals( e.getName() ) ) {
              events.backup();
              EventSequence tagEvents = events.currentDelimiter();
              RefTarget t = decodeTarget(tagEvents);
              return createReference(REF_TREE, t, tagEvents);
	    } else if( e.getType() == START_ELEMENT && REF_TAG_NODE.equals( e.getName() ) ) {
              events.backup();
              EventSequence tagEvents = events.currentDelimiter();
              RefTarget t = decodeTarget(tagEvents);
              return createReference(START_REF_NODE, t, tagEvents);
	    } else if( e.getType() == END_ELEMENT && REF_TAG_NODE.equals( e.getName() ) ) {
              events.backup();
              EventSequence tagEvents = events.currentDelimiter();
              return createReference(END_REF_NODE, null, tagEvents);
	    } else if( e.getType() == END_ELEMENT && REF_TAG_TREE.equals( e.getName() ) ) {
              return decode(events); // SKip tree endtag
            } else {
              Log.log("Unknown tag in XMLR namespace", Log.ERROR);
              //throw new java.io.IOException();
              return null;
            }
	} else
	    return e;
    }

    /** Get the value of target.
     * @return value of target.
     */
    public String getTarget() {
	return target;
    }

    /** Get the value of target type. Valid target types are {@link #TT_ID},
     * {@link #TT_PATH} and {@link #TT_TARGET}.
     * @return value of target.
     */
    public int getTargetType() {
	return targetType;
    }


    public String toString () {
	if(getType()==REF_TREE)
	    return "EX({REF_TREE}"+target+")";
	else if(getType()==START_REF_NODE)
	    return "EX({START_REF_NODE}"+target+")";
	else if(getType()==END_REF_NODE)
	    return "EX({END_REF_NODE})";
	else
	    return super.toString();
    }

    private static RefTarget decodeTarget( EventSequence s ) {
	RefTarget t = new RefTarget();
	for(Enumeration en=s.events();en.hasMoreElements();) {
	    Event e = (Event) en.nextElement();
	    if( e.getType()==Event.ATTRIBUTE ) {
		t.target = (String) e.getValue();
		if( REF_ATT_ID.equals(e.getName()) && t.targetType < TT_ID ) {
		    t.targetType = TT_ID;
		} else if( REF_ATT_PATH.equals(e.getName()) && t.targetType < TT_PATH ) {
		    t.targetType = TT_PATH;
		}  else if( REF_ATT_TARGET.equals(e.getName()) && t.targetType < TT_TARGET ) {
		    t.targetType = TT_TARGET;
		    //being cautious: break; // Highest priority target found, break loop (optimization)
		}
	    }
	}
	return t;
    }

    private static class RefTarget {

	String target;
	int targetType;

	public RefTarget() {
	    target = null;
	    targetType = -1;
	}

        public RefTarget(int type, String tgt) {
          target = tgt;
          targetType = type;
        }


    }
}
// arch-tag: af784241bf5cc50371b5bdd3cc0cdd98 *-
