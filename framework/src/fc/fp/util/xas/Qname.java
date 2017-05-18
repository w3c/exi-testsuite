package fc.fp.util.xas;

/**
 * A class for representing XML names. A name in an XML document has two parts:
 * a namespace URI and a local name. This class is used to hold such a pair as a
 * single object. It differs from other such classes in that it does not contain
 * the namespace prefix. Objects of this class are immutable.
 */
public class Qname {

    private String namespace;
    private String name;

    public Qname (String namespace, String name) {
	this.namespace = namespace;
	this.name = name;
    }

    public String getNamespace () {
	return namespace;
    }

    public String getName () {
	return name;
    }

    public int hashCode () {
	return namespace.hashCode() ^ name.hashCode();
    }

    public boolean equals (Object o) {
	if (this == o) {
	    return true;
	} else if (!(o instanceof Qname)) {
	    return false;
	} else {
	    Qname q = (Qname) o;
	    return name.equals(q.name) && namespace.equals(q.namespace);
	}
    }

    public String toString () {
	return "{" + namespace + "}" + name;
    }

}
// arch-tag: 0c25951ae7394de939ea2bb29c28b0bc *-
