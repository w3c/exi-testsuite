// $Id: XasSerialization.java,v 1.1 2010/02/23 20:31:11 tkamiya Exp $
//
package fc.fp.util.xmlr;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Iterator;

import org.xmlpull.v1.XmlSerializer;
import fc.fp.syxaw.util.Log;
import fc.fp.util.xas.DataEventSequence;
import fc.fp.util.xas.Event;
import fc.fp.util.xas.EventList;
import fc.fp.util.xas.EventSequence;
import fc.fp.util.xas.TypedXmlSerializer;
import fc.fp.util.xas.XasUtil;
import fc.fp.util.xas.XmlReader;
import fc.fp.util.xas.XmlWriter;
import fc.fp.util.xmlr.tdm.Diff;
import fc.fp.util.xas.TypedXmlParser;
import fc.fp.util.xas.EventStream;

/** Methods and interfaces for reftree XAS serialization and deserialization.
 * This class, in combination with {@link ReferenceEvent} and
 * {@link ReferenceEventSequence}, provides functionality for reading reftrees
 * from XAS event sequences, as well as serializing reftrees as event sequences.
 */

public class XasSerialization {

  /** Default preamble for serialized trees. Currently set to the events
   * <code>{Event.createNamespacePrefix(ReferenceEvent.REF_NS, "ref"),
     (Event.createNamespacePrefix(Diff.DIFF_NS, "diff")}</code>
   */

  public static final EventSequence DEFAULT_PREAMBLE;

  static {
    EventList es = new EventList();
    es.add(Event.createNamespacePrefix(ReferenceEvent.REF_NS, "ref"));
    es.add(Event.createNamespacePrefix(Diff.DIFF_NS, "diff"));
    DEFAULT_PREAMBLE = es;
  }

  /** Obtain an instance of a generic codec.
   *
   * @see XasSerialization.GenericContentCodec
   * @return Generic codec instance
   */
  public static ContentCodec getGenericCodec() {
    return GENERIC_CODEC;
  }

  /** Read a reftree. The input event sequence is assumed to have the
   * structure documented in {@link XasSerialization.ContentReader}. This
   * method consumes all symbols except <code>content_start</code>
   * and <code>content_end</code>, which must be consumed by the
   * supplied content reader.
   * <p>XMLR references need to be encoded as {@link ReferenceEvent}s in the
   * event stream of the given reader; the method <i>will not</i> recognize
   * XML markup encoding references (this allows for custom
   * encoding of <code>ReferenceEvents</code>).
   *
   * @param rd events to deserialize as a reftree. The current position must be
   * at the root element <code>START_ELEMENT</code> event.
   * @param cf content reader for node content
   * @throws IOException if tree deserialization fails
   * @return the deserialized reftree
   */

  // FIXME-I : consider using RefEvSeq, not Xmlreader in interface
  public static RefTree readTree(XmlReader rd, ContentReader cf) throws
      IOException {
    boolean wholeDocument=true;
    Event e = rd.advance();
    if(e !=null && e.getType()!=Event.START_DOCUMENT ) {
      wholeDocument = false;
      rd.backup();
    }
    RefTreeNode root = readTree(rd, cf, null );
    if( wholeDocument ) {
      e = rd.advance();
      if( e == null || e.getType() != Event.END_DOCUMENT )
        throw new IOException("Expected to find end-of-document");
    }
    return new RefTreeImpl(root);
  }

  /** Get reader suited for data-oriented reftrees. The returned reader
   * is just an encapsulation of the sequence returned by
   * {@link #getDocumentSequence getDocumentSequence}.
   *
   * @param pr Xml parser
   * @return an Xml reader connected to the parser
   * @throws IOException if an I/O error occurs
   */
  public static XmlReader getTreeReader(TypedXmlParser pr) throws IOException {
    return new XmlReader(getDocumentSequence(new EventStream(pr)));
  }

  /** Get writer suited for data-oriented reftrees. The returned reader
   * is just an encapsulation of the serializer returned by
   * {@link #getDocumentSerializer getDocumentSerializer}.
   *
   * @param sz input serializer
   * @return xml writer connected to the serializer
   * @throws IOException if an I/O error occurs
   */
  public static XmlWriter getTreeWriter(TypedXmlSerializer sz) throws IOException {
    return new XmlWriter(getDocumentSerializer(sz));
  }

  protected static RefTreeNodeImpl readTree(XmlReader rd, ContentReader cf,
                                            RefTreeNode parent)
                                            throws IOException {
    Event e = rd.advance();
    // FIXME-W:  null ptr here means tag opened and stream ended
    RefTreeNodeImpl root = null;
    int ccount = 0;
    for(;e.getType() == Event.CONTENT;e=rd.advance())
      ccount++;
    if (e.getType() == Event.START_ELEMENT ||
                  e.getType() == ReferenceEvent.START_REF_NODE ) {
      boolean isNodeRef = e.getType() == ReferenceEvent.START_REF_NODE;
      RefTrees.IdentifiableContent content = null;
      String id = null;
      if( isNodeRef ) {
        if( ccount > 0 )
          // Text before a nf is not allowed as txt<ref:node id=x> associates
          // txt with id x -> id x not equal to base -> contradicts ref:node
          // semantics
          throw new IOException("Text content before node reference "+
                  ((ReferenceEvent) e).getTarget());
        ReferenceEvent re = (ReferenceEvent) e;
        if (re.getTargetType() != ReferenceEvent.TT_ID)
          Log.log("Target type != id not implemented", Log.ASSERTFAILED);
        id = re.getTarget();
      } else {
        rd.backup(ccount+1);
        content = cf.startContent(rd);
        id = content.getId();
      }
      root = new RefTreeNodeImpl(parent, id,false,
                                 isNodeRef ? null : content);

      for(RefTreeNodeImpl child = null; (child=readTree(rd,cf,root)) != null;)
        root.addChild(child);
      if( isNodeRef ) {
        e = rd.advance();
        if( e.getType() != ReferenceEvent.END_REF_NODE )
          throw new IOException("Expected </ref:node>");
      } else
        cf.finishContent(content, rd);
    } else if (e.getType() == ReferenceEvent.REF_TREE ) {
      ReferenceEvent re = (ReferenceEvent) e;
      if (re.getTargetType() != ReferenceEvent.TT_ID)
        Log.log("Target type != id not implemented", Log.ASSERTFAILED);
      return new RefTreeNodeImpl(parent,re.getTarget(),true,null);
    } else
      // END TAG CASE
      rd.backup(ccount+1); // No node start to be found here, EOL and backup

    return root;
  }

  /** Write a reftree. The output event sequence will have the
   * structure documented in {@link XasSerialization.ContentWriter}. This
   * method produces all symbols except <code>content_start</code>
   * and <code>content_end</code>, which must be produced by the
   * supplied content writer.
   *
   * <p>Reference nodes are written as {@link ReferenceEvent}s. Due to the
   * way XAS typed serialization works, these will automatically be
   * serialized properly to XML by {@link fc.fp.util.xas.TypedXmlSerializer}
   * and derivatives.
   *
   * @param tree tree to serialize
   * @param wr output writer
   * @param cw content writer for node contents
   * @throws IOException if serialization fails
   */
  public static void writeTree(RefTree tree, XmlWriter wr, ContentWriter cw)
      throws IOException {
//DBG  debugwriteTree(tree.getRoot(),cw,wr);
    writeTree(tree.getRoot(),cw,wr);
  }

/*DBG

  static int __depth=0;

  protected static void debugwriteTree(RefTreeNode n,
      ContentWriter sz, XmlWriter es ) throws IOException {
    if(__depth>40) {
      return;
    }
    __depth++;
    StringBuffer sb = new StringBuffer();
    for( int i=0;i<__depth;i++)
      sb.append(' ');
    if( !n.isReference() )
      sb.append(n.getId());
    else if( n.isNodeRef() )
      sb.append("N<"+n.getId()+">");
    else {
      sb.append("REF<"+n.getId()+">");
    }
    for( Iterator i = n.getChildIterator();i.hasNext();) {
      debugwriteTree((RefTreeNode) i.next(),sz,es);
    }
    System.err.println(sb.toString());
    __depth--;
  }
*/

  protected static void writeTree(RefTreeNode n,
      ContentWriter sz, XmlWriter es ) throws IOException {
//    final Object __DEBUG__NULLCNT =
//        new fc.fp.syxaw.tests.TestVersionedDirTree.DirNodeContent(n.getId(),"!!!ERROR:null obj!!!","");
    if( !n.isReference() )
      sz.startObject(/*n.getContent()==null ? __DEBUG__NULLCNT :*/ n.getContent(),es);
    else if( n.isNodeRef() )
      es.addEvent(ReferenceEvent.createNodeReference(ReferenceEvent.TT_ID,n.getId()));
    else {
      // Treeref
      es.addEvent(ReferenceEvent.createTreeReference(ReferenceEvent.TT_ID, n.getId()));
      return;
    }
    for( Iterator i = n.getChildIterator();i.hasNext();) {
      writeTree((RefTreeNode) i.next(),sz,es);
    }
    if( !n.isReference() )
      sz.finishObject(n.getContent(),es);
    else
      es.addEvent(ReferenceEvent.createEndNodeReference());
  }

  /** Get document serializer instance. The instance uses the default
   * preamble, {@link #DEFAULT_PREAMBLE DEFAULT_PREAMBLE}. If the argument is
   * already a {@link XasSerialization.DocumentSerializer} no wrapping is performed.
   *
   * @param ser underlying serializer
   * @return serializer instance
   */

  public static DocumentSerializer getDocumentSerializer(TypedXmlSerializer ser) {
    return ser instanceof  DocumentSerializer ? (DocumentSerializer) ser :
        new DocumentSerializer(ser,DEFAULT_PREAMBLE);
  }

  /** Get event sequence for data-oriented XMLR documents. Equivalent to
   * <code>getDocumentSequence(source,true)</code>
   *
   * @param source sequence to filter
   * @return ReferenceEventSequence for the source sequence
   */


  public static ReferenceEventSequence getDocumentSequence( EventSequence source) {
    return getDocumentSequence(source,true);
  }

  /** Get event sequence for data-oriented XMLR documents. The returned event
   * sequence filters the underlying event sequence <code>source</code>
   *  in the following ways
   * <ul>
   *  <li><code>source</code> is filtered trough a
   *   {@link fc.fp.util.xas.DataEventSequence}</li>
   *  <li>XMLR references are decoded from <code>source</code> using
   *   {@link ReferenceEventSequence}
   * </ul>
   * @param source sequence to filter
   * @param trimWhitespace set to <code>true</code> is whitespace should be
   * trimmed from text content.
   * @return ReferenceEventSequence for the source sequence
   */
  public static ReferenceEventSequence getDocumentSequence( EventSequence source,
      boolean trimWhitespace) {
    return new ReferenceEventSequence(
        new DataEventSequence(source,trimWhitespace,true));
  }

  /** Convenience Xml serializer for full documents. This serializer
   * automatically outputs a <code>START_DOCUMENT</code> event and the
   * preamble passed to the constructor to the underlying serializer when
   * the first {@link #startTag startTag} call is made (i.e. the root tag is
   * opened).
   * <p>When an {@link #endTag endTag} call matching the initial
   * <code>startTag</code> is made, the serializer automatically outputs
   * an <code>END_DOCUMENT</code> event, i.e. when the root element is
   * closed, the document is automatically ended.
   */

  public static class DocumentSerializer implements TypedXmlSerializer {

    private EventSequence preamble=null;
    private TypedXmlSerializer s=null;
    private boolean documentEnded = false;
    private static final String DOCUMENT_HAS_ENDED
            = "The root tag has been closed";

    // preamble = events to put after startdoc and before root elem
    /** Create a new serializer.
     *
     * @param ser underlying serializer
     * @param preamble preamble to insert before the initial
     * <code>START_ELEMENT</code>
     */
    public DocumentSerializer(TypedXmlSerializer ser, EventSequence preamble) {
      this.s = ser;
      this.preamble = preamble;
    }


    public void setFeature(String string, boolean boolean1) {
      s.setFeature(string,boolean1);
    }

    public boolean getFeature(String string) {
      return s.getFeature(string);
    }

    public void setProperty(String string, Object object) {
      s.setProperty(string,object);
    }

    public Object getProperty(String string) {
      return s.getProperty(string);
    }

    public void setOutput(OutputStream outputStream, String string) throws
        IOException {
      s.setOutput(outputStream,string);
    }

    public void setOutput(Writer writer) throws IOException {
      s.setOutput(writer);
    }

    /** Start document. The call is ignored in this serializer.
     */
    public void startDocument(String string, Boolean boolean1) {
      //throw new UnsupportedOperationException("Automatically generated by serializer");
    }

    /** End document. The call is ignored in this serializer.
     */
    public void endDocument() {
      //throw new UnsupportedOperationException("Automatically generated by serializer");
    }

    public void setPrefix(String string, String string1) throws IOException {
      s.setPrefix(string,string1);
    }

    public String getPrefix(String string, boolean boolean1) {
      return s.getPrefix(string,boolean1);
    }

    public int getDepth() {
      return s.getDepth();
    }

    public String getNamespace() {
      return s.getNamespace();
    }

    public String getName() {
      return s.getName();
    }

    public XmlSerializer startTag(String string, String string1) throws IOException {
      if( documentEnded )
        throw new IllegalStateException(DOCUMENT_HAS_ENDED);
      if( s.getDepth() <= 0) {
        XasUtil.outputEvent(Event.createStartDocument(),s);
        if( preamble != null )
          XasUtil.outputSequence(preamble,s);
        preamble = null; // Not needed anymore
      }
      s.startTag(string,string1);
      return this;
    }

    public XmlSerializer attribute(String string, String string1,
                                   String string2) throws IOException {
      if( documentEnded )
        throw new IllegalStateException(DOCUMENT_HAS_ENDED);
      s.attribute(string,string1,string2);
      return this;
    }

    public XmlSerializer endTag(String string, String string1) throws IOException {
      if( documentEnded )
        throw new IllegalStateException(DOCUMENT_HAS_ENDED);
      s.endTag(string,string1);
      if( s.getDepth() == 0) {
        XasUtil.outputEvent(Event.createEndDocument(),s);
        documentEnded = true;
        //s= null; // Use exceptions instead of: (line below)
        // Force nullptrex if somebody tries to add any events after this
      }
      return this;
    }

    public XmlSerializer text(String string) throws IOException {
      if( documentEnded )
        throw new IllegalStateException(DOCUMENT_HAS_ENDED);
      s.text(string);
      return this;
    }

    public XmlSerializer text(char[] charArray, int int1, int int2) throws IOException {
      if( documentEnded )
        throw new IllegalStateException(DOCUMENT_HAS_ENDED);
      s.text(charArray,int1,int2);
      return this;
    }

    public void cdsect(String string) throws IOException {
      if( documentEnded )
        throw new IllegalStateException(DOCUMENT_HAS_ENDED);
      s.cdsect(string);
    }

    public void entityRef(String string) throws IOException {
      if( documentEnded )
        throw new IllegalStateException(DOCUMENT_HAS_ENDED);
      s.entityRef(string);
    }

    public void processingInstruction(String string) throws IOException {
      if( documentEnded )
        throw new IllegalStateException(DOCUMENT_HAS_ENDED);
      s.processingInstruction(string);
    }

    public void comment(String string) throws IOException {
      if( documentEnded )
        throw new IllegalStateException(DOCUMENT_HAS_ENDED);
      s.comment(string);
    }

    public void docdecl(String string) throws IOException {
      if( documentEnded )
        throw new IllegalStateException(DOCUMENT_HAS_ENDED);
      s.docdecl(string);
    }

    public void ignorableWhitespace(String string) throws IOException {
      if( documentEnded )
        throw new IllegalStateException(DOCUMENT_HAS_ENDED);
      s.ignorableWhitespace(string);
    }

    public void flush() throws IOException {
      s.flush();
    }

    public TypedXmlSerializer typedContent(Object content, String namespace,
                                           String name) throws IOException  {
      if( documentEnded )
        throw new IllegalStateException(DOCUMENT_HAS_ENDED);
      s.typedContent(content,namespace,name);
      return this;
    }


    public TypedXmlSerializer typedAttribute (String namespace, String name, Object value) throws IOException {
	if( documentEnded )
	    throw new IllegalStateException(DOCUMENT_HAS_ENDED);
	s.typedAttribute(namespace, name, value);
	return this;
    }

  }

  /** Interface for reading node contents. The input XAS event sequence is
    * assumed to conform to the following grammar (EBNF)
   <pre>
   root := node
   node_list := node*
   node := refence_start node_list reference_end |
           content_start node_list content_end
   </pre>
    * where <code>root</code> is the start symbol.
    * The purposes of the methods in this interface is to consume the
    * <code>content_start</code> and <code>content_end</code> symbols and,
    * at the same time, generate the content of the node.
    *
    * <p>A particluarly natural mapping to XML is to use an opening tag as the
    * <code>content_start</code> symbol, and the corresponding closing tag as the
    * <code>content_end</code> symbol. Using such a mapping, the parse structure of
    * the XML document will match the structure of the reftree.
    * References are deserialized like this by default.
    * @see fc.fp.util.xmlr.XasSerialization#readTree
    */

  // content start/end block
  // FIXME-W startContent vs startObject in Writer; consider uniform names
  public interface ContentReader {

    /** Read initial part of node content. In other words, consume the
     * <code>content_start</code> symbol and return a new object that will
     * contain the deserialized form of the <code>content_start</code> symbol.
     *
     * @param r reader to read content from, positioned at
     * <code>content_start</code>
     * @return IdentifiableContent node content (may be partially initialized)
     * @throws IOException if content reading fails
     */
    public RefTrees.IdentifiableContent startContent(XmlReader r) throws IOException;

    /** Finish reading node content.
     *
     * @param c object returned by the corresponding
                     {@link #startContent startContent}
     * @param r reader to read content from, positioned at <code>content_end</code>
     * @throws IOException if content reading fails
     */
    public void finishContent(Object c, XmlReader r) throws IOException;
  }

  /** Interface for writing node content. The serialialized format conforms
   * to the EBNF grammar defined in {@link XasSerialization.ContentReader}. The
   * purpose of the methods in this class is to produce the
   * <code>content_start</code> and <code>content_end</code> symbols.
   */

  public interface ContentWriter {

    /** Write start of content. The method is responsible for writing the
     * <code>content_start</code> symbol to the output writer.
     *
     * @param o node content to write
     * @param writer writer to write to
     * @throws IOException if the content write fails.
     */
    public void startObject(Object o, XmlWriter writer) throws IOException;

    /**  Write end of content. The method is responsible for writing the
     * <code>content_end</code> symbol to the output writer.
     *
     * @param o node content to write
     * @param writer writer to write to
     * @throws IOException if the content write fails.
     */
    public void finishObject(Object o, XmlWriter writer) throws IOException;
  }

  /** Combined content reader and writer.
   */

  public interface ContentCodec extends ContentReader, ContentWriter {}

  private static final ContentCodec GENERIC_CODEC = new GenericContentCodec();

  /** Generic XAS node content. The class stores node content as XAS
   * event sequences. There are three sequences available: the head sequence,
   * the tail sequence, and the full sequence. The full sequence is the
   * concatenation of the head and tail sequences.
   * <p>The typical use of this class with {@link XasSerialization.ContentReader
   * ContentReader} and  {@link XasSerialization.ContentWriter ContentWriter}
   * is to store the <code>content_start</code>
   * symbol in the head sequence and the <code>content_end</code> symbol
   * in the tail sequence. In practice, this would mean that the
   * <code>START_ELEMENT</code> along with attribute events would be stored in
   * the head sequence, and corersponding <code>END_ELEMENT</code> event in
   * the tail sequence.
   */

  public static class GenericContent implements RefTrees.IdentifiableContent {

    EventSequence head;
    EventSequence tail = null;
    String id;
    private static long idGen = 0l;

    /** Create a new content object. If the supplied event sequence contains an
     * {@link fc.fp.util.xas.Event#ATTRIBUTE ATTRIBUTE} event, whose local name
     * is <code>id</code>, then the value of that event is used as the id for
     * this object. Otherwise, a global instance count of this type is used as
     * id.
     * @param es head sequence of the object.
     */
    public GenericContent(EventSequence es) {
      // Scan for id attribute
      String id = null;
      for( Enumeration en = es.events();en.hasMoreElements() && id==null;) {
        Event e = (Event) en.nextElement();
        if( e.getType() == Event.ATTRIBUTE && "id".equals(e.getName()) )
          id= e.getValue().toString();
      }
      /*DBG // Verify exactly 1 start element
      int ses=0;
      for( Enumeration en = es.events();en.hasMoreElements();) {
        Event e = (Event) en.nextElement();
        if( e.getType() == Event.START_ELEMENT )
          ses++;
      }
      if(ses!= 1)
        Log.log("Wrong no of startelems "+ses+"nid="+id+" seq="+es,Log.ASSERTFAILED);
      */
      this.head = es;
      this.id = id == null ? String.valueOf(idGen++) : id;
    }

    /** Crete a new content object.
     *
     * @param es head sequence of the object.
     * @param id id of the object
     */

    public GenericContent(EventSequence es, String id) {
      if (id != null)
        this.id = id;
      else
        this.id = String.valueOf(idGen++);
      this.head = es;
    }

    public String getId() {
      return id;
    }

    /** Get head event sequence.
     *
     * @return head sequence
     */
    public EventSequence getHead() {
      return head;
    }

    /** Set tail event sequence.
     *
     * @param tail tail sequence
     */

    public void setTail(EventSequence tail) {
      /*DBG // Verify exactly 1 endelement
      int ees=0;
      for( Enumeration en = tail.events();en.hasMoreElements();) {
        Event e = (Event) en.nextElement();
        if( e.getType() == Event.END_ELEMENT )
          ees++;
      }
      if(ees!= 1)
        Log.log("Wrong no of endelems "+ees,Log.ASSERTFAILED);
      */
      this.tail = tail;
    }

    /** Get tail event sequence.
     *
     * @return tail sequence
     */
    public EventSequence getTail() {
      return tail;
    }

    /** Get full event sequence.
     * @return full sequence
     */
    public EventSequence getSequence() {
      EventList es = new EventList();
      es.addAll(getHead());
      es.addAll(getTail());
      return es;
    }

    public boolean equals(Object o) {
      if( !(o instanceof  GenericContent))
        return false;
      GenericContent c=(GenericContent) o;
/*      Log.log("This: head="+head.toString()+",tail="+tail.toString(),Log.INFO);
      Log.log("Compare: head=" + c.head.toString() + ",tail=" + c.tail.toString(),
              Log.INFO);*/
      return head.equals(c.head)  && (tail==tail ||
                                      (tail!=null && tail.equals(c.tail)));
    }
  }

  /** Codec for generic XML content. The generic codec is suitable for
   * serialization and deserialization of generic XML documents, where the
   * XML parse tree structure corresponds to that of the reftree, and where
   * nodes are encoded as elements.
   *
   * <p>The <code>content_start</code> and
   * <code>content_end</code> symbols are parsed as follows:
   <pre>
   content_start := {@link fc.fp.util.xas.XmlReader#currentDelimiter reader.currentDelimiter()}
   content_end := {@link fc.fp.util.xas.XmlReader#currentDelimiter reader.currentDelimiter()}
   </pre>
   * where <code>content_start</code> is stored in a
   * {@link XasSerialization.GenericContent} head and
   * <code>content_end</code> in its tail.
   *<p>The serialization of the symbols is
   <pre>
   content_start := (GenericContent) o).getHead()
   content_end := (GenericContent) o).getTail()
   </pre>
   *
   */
  public static class GenericContentCodec implements ContentCodec {

    /** Read start of generic content.
     *
     * @param r input reader
     * @return {@link XasSerialization.GenericContent} with the
     * head sequence initialized
     */
    public RefTrees.IdentifiableContent startContent(XmlReader r) {
      EventSequence es = r.currentDelimiter();
      return new GenericContent(es);
    }

    /** Read end of generic content. Initializes the tail sequence
     * of the given object.
     *
     * @param c {@link XasSerialization.GenericContent} returned by the
     * corresponding <code>startContent</code> method call.
     * @param r input reader
     */
    public void finishContent(Object c, XmlReader r) {
      if( c instanceof GenericContent) {
        EventSequence es =r.currentDelimiter(); // Eat end tag
        ((GenericContent) c).setTail(es);
      } else {
        throw new IllegalArgumentException("Object is wrong class");
      }
    }

    /** Write start of object. Writes the head sequence of the given
     * object.
     *
     * @param o {@link XasSerialization.GenericContent} object to serialize
     * @param writer XmlWriter
     * @throws IOException if writing fails
     */

    public void startObject(Object o, XmlWriter writer) throws IOException {
      if( o instanceof GenericContent) {
        writer.addEvents(((GenericContent) o).getHead());
      } else
        throw new IOException("Codec requires GenericContent object");
    }

    /** Write end of object. Writes the tail sequence of the given
     * object.
     *
     * @param o {@link XasSerialization.GenericContent} object to serialize
     * @param writer XmlWriter
     * @throws IOException if writing fails
     */

    public void finishObject(Object o, XmlWriter writer) throws IOException {
      if( o instanceof GenericContent) {
        writer.addEvents(((GenericContent) o).getTail());
      } else
        throw new IOException("Codec requires GenericContent object");
    }
  }

  /** Codec for XML content with text nodes. The codec is suitable for
   * serialization and deserialization of generic XML documents that may
   * include text nodes. Works similar to
   * {@link XasSerialization.GenericContentCodec}, but text content is
   * attached to the tree nodes according to which element is "closest".
   * <ol>
   * <li>Text content before and after a start element is added to the
   * head sequence of the corresponidng node</li>
   * <li>Text before an end element is added to tail sequence of the
   * corresponidng node, unless it is already covered by the previous rule.
   * This is the case ...</close>txt</close2> -> txt is added to close2</li>
   * </ol>
   */
  public static class TextContentCodec implements ContentCodec {

    /** Read start of generic content.
     *
     * @param r input reader
     * @return {@link XasSerialization.GenericContent} with the
     * head sequence initialized
     */
    public RefTrees.IdentifiableContent startContent(XmlReader r) {
      int start = r.getCurrentPosition();
      while( r.getCurrentEvent().getType() == Event.CONTENT )
        r.advance();
      r.currentDelimiter();
      while( r.getCurrentEvent().getType() == Event.CONTENT )
        r.advance();
      return new GenericContent(r.getEventSequence().subSequence(start,
              r.getCurrentPosition()));
    }

    /** Read end of generic content. Initializes the tail sequence
     * of the given object.
     *
     * @param c {@link XasSerialization.GenericContent} returned by the
     * corresponding <code>startContent</code> method call.
     * @param r input reader
     */
    public void finishContent(Object c, XmlReader r) {
      int start = r.getCurrentPosition();
      while( r.getCurrentEvent().getType() == Event.CONTENT )
        r.advance();
      int startelem = r.getCurrentPosition();
      if( c instanceof GenericContent) {
        EventSequence es =r.currentDelimiter(); // Eat end tag
        if( startelem > start )
          es = r.getEventSequence().subSequence(start,r.getCurrentPosition());
        ((GenericContent) c).setTail(es);
      } else {
        throw new IllegalArgumentException("Object is wrong class");
      }
    }

    /** Write start of object. Writes the head sequence of the given
     * object.
     *
     * @param o {@link XasSerialization.GenericContent} object to serialize
     * @param writer XmlWriter
     * @throws IOException if writing fails
     */

    public void startObject(Object o, XmlWriter writer) throws IOException {
      //Log.log("HEAD: "+((GenericContent) o).getHead(),Log.DEBUG);
      if( o instanceof GenericContent) {
        writer.addEvents(((GenericContent) o).getHead());
      } else
        throw new IOException("Codec requires GenericContent object");
    }

    /** Write end of object. Writes the tail sequence of the given
     * object.
     *
     * @param o {@link XasSerialization.GenericContent} object to serialize
     * @param writer XmlWriter
     * @throws IOException if writing fails
     */

    public void finishObject(Object o, XmlWriter writer) throws IOException {
      //Log.log("TAIL: "+((GenericContent) o).getTail(),Log.DEBUG);
      if( o instanceof GenericContent) {
        writer.addEvents(((GenericContent) o).getTail());
      } else
        throw new IOException("Codec requires GenericContent object");
    }
  }

}
// arch-tag: 5f420e85765a922333e7a2c669450d09 *-
