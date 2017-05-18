package fc.fp.util.xas;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import fc.fp.util.Util;

/**
 * A representation of an event in an XML sequence. An XML document can be
 * viewed as a sequence of small events (e.g. element start, text content)
 * containing a string or two. This class represents one of these events.
 * 
 * <p>
 * The representation is chosen to be based on discriminators instead of
 * interfaces, since this eliminates the need of type casting in client code. A
 * discriminator-based approach also simplifies implementation of event
 * processors, since there is no need for the Visitor pattern and no need to
 * explicitly handle each uninteresting event.
 * 
 * <p>
 * The chosen representation requires access to the components of an event.
 * Since each event consists of at most three strings, this is simple to do.
 * Generically-named methods will return each of these components in a manner
 * specified by the description of each individual event type.
 * 
 * <p>
 * The method of construction also stems from the chosen representation. Instead
 * of constructors, the class has static factory methods for each of the
 * supported event types. This eliminates the need to check for incorrect data
 * supplied at construction stage.
 * 
 * <p>
 * The class supports subclass extensions that define new event types. This is
 * accomplished by using {@link #TYPE_EXTENSION_FLAG} as described in additional
 * documentation. The fields of an event are intended to be immutable, so they
 * are kept <code>private</code>. Also, accessor methods for these fields are
 * <code>final</code> to permit inlining them. Neither of these restrictions
 * should provide a hardship for type extensions.
 */
public class Event {

    int hashCode = 0;
    private int type;
    private String namespace;
    private String name;
    private Object value;
    private String qname;

    /**
         * The type of a document start event. This event has no components and
         * all component accessor methods return <code>null</code>.
         */
    public static final int START_DOCUMENT = 0;

    /**
         * The type of a document end event. This event has no components and
         * all component accessor methods return <code>null</code>.
         */
    public static final int END_DOCUMENT = 1;

    /**
         * The type of an element start event. This event has the namespace and
         * name components forming the element's qualified name and the
         * <code>getValue</code> method returns <code>null</code>.
         */
    public static final int START_ELEMENT = 2;

    /**
         * The type of an attribute event. This event has the namespace and name
         * components forming the attribute's qualified name and the value
         * component consisting of the attribute's value as a {@link String}.
         */
    public static final int ATTRIBUTE = 3;

    /**
         * The type of an element end event. This event has the namespace and
         * name components forming the element's qualified name and the
         * <code>getValue</code> method returns <code>null</code>.
         */
    public static final int END_ELEMENT = 4;

    /**
         * The type of a text content event. This event has the value component
         * a {@link String} forming the text content and the
         * <code>getNamespace</code> and <code>getName</code> methods return
         * <code>null</code>.
         */
    public static final int CONTENT = 5;

    /**
         * The type of a typed content event. This event is not a part of normal
         * XML usage. It represents content that has a type according to XML
         * Schema and can possibly be serialized into a non-text format. The
         * namespace and name components form the type's qualified name and the
         * value component is the content represented as an {@link Object}.
         */
    public static final int TYPED_CONTENT = 6;

    /**
         * The type of a namespace prefix event. This event has the namespace
         * component the namespace URI to map and the value component the
         * {@link String} prefix to map it to. The <code>getName</code> method
         * returns <code>null</code>.
         */
    public static final int NAMESPACE_PREFIX = 7;

    /**
         * The type of a comment event. This event has the value component a
         * {@link String} forming the text of the comment and the
         * <code>getNamespace</code> and <code>getName</code> methods return
         * <code>null</code>.
         */
    public static final int COMMENT = 8;

    /**
         * The type of a processing instruction event. This event has the value
         * component a {@link String} forming the text of the processing
         * instruction and the <code>getNamespace</code> and
         * <code>getName</code> methods return <code>null</code>.
         */
    public static final int PROCESSING_INSTRUCTION = 9;

    /**
         * The type of an entity reference event. This event has the name
         * component the name of the entity and the <code>getNamespace</code>
         * and <code>getValue</code> methods return <code>null</code>.
         */
    public static final int ENTITY_REFERENCE = 10;

    public static final int DOCTYPE_DECL = 11;

    public static final int TYPED_ATTRIBUTE = 12;

    /**
         * The bitmask to use for extensions. The event's type (the return value
         * of {@link #getType} anded with this value gives a flag value that can
         * be compared to see whether this event is of some extended type. The
         * value <code>0</code> for this flag indicates no extensions.
         */
    public static final int FLAG_BITMASK = 0xFF000000;

    /**
         * The flag for events with additional types. If an event's type anded
         * with {@link #FLAG_BITMASK} gives this value, it means that the event
         * is of some application-specific extended type. Generic event
         * processors can get a {@link EventSequence} equivalent to this
         * extended event by calling {@link #getValue} (i.e. the {@link Object}
         * returned by that method is of type {@link EventSequence}).
         */
    public static final int TYPE_EXTENSION_FLAG = 0x43000000;

    protected Event (int type, String namespace, String name, Object value) {
	this.type = type;
	this.namespace = namespace;
	this.name = name;
	this.value = value;
	this.qname = getQName(namespace, name);
    }

    /**
         * Create a document start event.
         */
    public static Event createStartDocument () {
	return new Event(START_DOCUMENT, null, null, null);
    }

    /**
         * Create a document end event.
         */
    public static Event createEndDocument () {
	return new Event(END_DOCUMENT, null, null, null);
    }

    /**
         * Create an element start event.
         * 
         * @param namespace the namespace URI of the element
         * @param name the local name of the element
         */
    public static Event createStartElement (String namespace, String name) {
	return new Event(START_ELEMENT, namespace, name, null);
    }

    /**
         * Create an attribute event.
         * 
         * @param namespace the namespace URI of the attribute
         * @param name the local name of the attribute
         * @param value the value of the attribute
         */
    public static Event createAttribute (String namespace, String name,
	    String value) {
	return new Event(ATTRIBUTE, namespace, name, value);
    }

    public static Event createTypedAttribute (String namespace, String name,
	    Object value) {
	return new Event(TYPED_ATTRIBUTE, namespace, name, value);
    }

    /**
         * Create an element end event.
         * 
         * @param namespace the namespace URI of the element
         * @param name the local name of the event
         */
    public static Event createEndElement (String namespace, String name) {
	return new Event(END_ELEMENT, namespace, name, null);
    }

    /**
         * Create a text content event.
         * 
         * @param text the text of the event
         */
    public static Event createContent (String text) {
	return new Event(CONTENT, null, null, text);
    }

    /**
         * Create a typed content event.
         * 
         * @param namespace the namespace URI of the content type
         * @param name the local name of the content type
         * @param value the value of the content
         */
    public static Event createTypedContent (String namespace, String name,
	    Object value) {
	return new Event(TYPED_CONTENT, namespace, name, value);
    }

    /**
         * Create a namespace prefix event.
         * 
         * @param namespace the namespace URI to map
         * @param value the prefix to map it to
         */
    public static Event createNamespacePrefix (String namespace, String value) {
	if (value == null) {
	    value = "";
	}
	return new Event(NAMESPACE_PREFIX, namespace, null, value);
    }

    /**
         * Create a comment event.
         * 
         * @param text the text of the comment
         */
    public static Event createComment (String text) {
	return new Event(COMMENT, null, null, text);
    }

    /**
         * Create a processing instruction event.
         * 
         * @param text the text of the processing instruction
         */
    public static Event createProcessingInstruction (String text) {
	return new Event(PROCESSING_INSTRUCTION, null, null, text);
    }

    /**
         * Create an entity reference event.
         * 
         * @param name the name of the entity
         */
    public static Event createEntityReference (String name) {
	return new Event(ENTITY_REFERENCE, null, name, null);
    }

    public static Event createDoctypeDeclaration (String content) {
	return new Event(DOCTYPE_DECL, null, null, content);
    }

    /**
         * Return the type of this event.
         */
    public final int getType () {
	return type;
    }

    /**
         * Return the namespace URI of this event. The returned value will be
         * <code>null</code> if the event does not have a name.
         */
    public final String getNamespace () {
	return namespace;
    }

    /**
         * Return the local name of this event. The returned value will be
         * <code>null</code> if the event does not have a name.
         */
    public final String getName () {
	return name;
    }

    /**
         * Return the value of this event. The returned value will be
         * <code>null</code> if this event does not have a value.
         */
    public final Object getValue () {
	return value;
    }

    private boolean equalsValue (Object v1, Object v2) {
    	if (v1 == null) {
    		return v2 == null;
    	} else if (v1.equals(v2)) {
    		return true;
    	} else if (v1 instanceof byte[] && v2 instanceof byte[]) {
    		return Arrays.equals((byte[]) v1, (byte[]) v2);
    	} else if (v1 instanceof List<?> && v2 instanceof List<?>) {
    		List<?> l1 = (List<?>) v1;
    		List<?> l2 = (List<?>) v2;
    		int z = l1.size();
    		if (l2.size() != z) {
    			return false;
    		}
    		for (int i = z; --i >=0;) {
    			if (!equalsValue(l1.get(i), l2.get(i))) {
    				return false;
    			}
    		}
    		return true;
    	}
    	return false;
    }

    private boolean componentEquals (Event e) {
	return Util.equals(namespace, e.namespace) && Util.equals(name, e.name)
		&& equalsValue(value, e.value);
    }

    public boolean equals (Object o) {
	boolean equal = false;
	if (o instanceof Event) {
	    Event e = (Event) o;
	    if (type == e.type) {
		equal = componentEquals(e);
	    } else if ((type == Event.ATTRIBUTE && e.type == Event.TYPED_ATTRIBUTE)
		    || type == Event.TYPED_ATTRIBUTE
		    && e.type == Event.ATTRIBUTE) {
		equal = componentEquals(e);
	    }
	}
	return equal;
    }

    public final int hashCode () {
    	if (hashCode == 0) {
    		hashCode = 31
    			^ ((type == Event.TYPED_ATTRIBUTE) ? Event.ATTRIBUTE : type);
    	}
    	return hashCode;

	/*
         * return type ^ (namespace == null ? 0 : namespace.hashCode()) ^ (name ==
         * null ? 0 : name.hashCode()) ^ (value == null ? 0 : value.hashCode());
         */
	// Inline (due to efficiency needs) of:
	// return type ^ Util.hashCode(namespace) ^ Util.hashCode(name)
	// ^ Util.hashCode(value);
    }

    public String toString () {
	switch (type) {
	case START_DOCUMENT:
	    return "SD()";
	case END_DOCUMENT:
	    return "ED()";
	case START_ELEMENT:
	    return "SE(" + qname + ")";
	case ATTRIBUTE:
	    return "AT(" + qname + "=" + value + ")";
	case TYPED_ATTRIBUTE:
	    return "TA(" + qname + "=" + value + ")";
	case END_ELEMENT:
	    return "EE(" + qname + ")";
	case CONTENT:
	    return "CH(" + value + ")";
	case TYPED_CONTENT:
	    return "TC((" + qname + ")" + value + ")";
	case NAMESPACE_PREFIX:
	    return "NP(" + MapURI.getURI(namespace) + "=" + value + ")";
	case COMMENT:
	    return "CM(" + value + ")";
	case PROCESSING_INSTRUCTION:
	    return "PI(" + value + ")";
	case ENTITY_REFERENCE:
	    return "ER(" + name + ")";
	case DOCTYPE_DECL:
	    return "DT(" + value + ")";
	default:
	    return "UE()";
	}
    }

	public static String getQName(String uri, String ln) {
		if (uri != null) {
			StringBuilder sb = new StringBuilder(100);
			sb.append('{');
			sb.append(MapURI.getURI(uri));
			sb.append('}');
			sb.append((ln == null) ? "<null>" : ln);
			return sb.toString();
		}
		return ((ln == null) ? "" : ln);
	}

	public static class MapURI {
		private final String uri;
		private final String alt;

		private MapURI(String uri, String alt) {
			this.uri = uri;
			this.alt = alt;
		}
		
		private static final List<MapURI> mapURI = new LinkedList<MapURI>();
		private static boolean useMapping = false;
		
		public static void enableMapping() {
			useMapping = true;
		}
		
		public static void disableMapping() {
			useMapping = false;
		}
		
		public static boolean getMappingStatus() {
			return useMapping;
		}
		
		private static boolean initialized = false;
		
		public static void setDefaultMapping() {
			if (!initialized) {
				addURI("http://www.w3.org/2001/XMLSchema-instance", ".XSI.");
				addURI("http://www.w3.org/2001/XMLSchema", ".XSD.");
				addURI("http://www.w3.org/XML/1998/namespace", ".XML.");
				addURI("http://schemas.xmlsoap.org/wsdl/", ".WSDL.");
				initialized = true;
			}
		}
		
		public static void addURI(String uri, String alt) {
			if (uri != null && uri.length() != 0
					&& alt != null && alt.length() != 0) {
				mapURI.add(new MapURI(uri, alt));
			}
		}

		public static String getURI(String uri) {
			if (uri == null) {
				return "<null>";
			}
			if (useMapping) {
				ListIterator<MapURI> iter = mapURI.listIterator();
				while (iter.hasNext()) {
					MapURI map = iter.next();
					if (uri.equals(map.uri)) {
						return map.alt;
					}
				}
			}
			return uri;
		}
		
		public static List<MapURI> getCurrentMapping() {
			return mapURI;
		}
		
		public static void clearCurrentMapping() {
			mapURI.clear();
			initialized = false;
		}
	}
}
// arch-tag: 387a4be6e032ae54022cf33638d356d4 *-
