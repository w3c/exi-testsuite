// $Id: Diff.java,v 1.1 2010/02/23 20:31:11 tkamiya Exp $

package fc.fp.util.xmlr.tdm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import fc.fp.syxaw.util.Log;
import fc.fp.util.xas.Event;
import fc.fp.util.xas.EventSequence;
import fc.fp.util.xas.XmlReader;
import fc.fp.util.xas.XmlWriter;
import fc.fp.util.xmlr.IdAddressableRefTree;
import fc.fp.util.xmlr.NodeNotFoundException;
import fc.fp.util.xmlr.RefTree;
import fc.fp.util.xmlr.RefTreeImpl;
import fc.fp.util.xmlr.RefTreeNode;
import fc.fp.util.xmlr.RefTreeNodeImpl;
import fc.fp.util.xmlr.RefTrees;
import fc.fp.util.xmlr.RefTrees.IdentifiableContent;
import fc.fp.util.xmlr.XasSerialization;
import tdm.lib.DiffAlgorithm;

/** Bridge between the reftrees and the <code>3dm</code> diff format.
 * The <code>3dm</code> diff format and reftrees have in common the encoding
 * of subtrees by using references to other trees.
 * This class provides functionality for quickly converting between these
 * formats without expensive subtree expansion or node matching. There are,
 * however, some limitations, stemming from the differences in the formats.
 * These limitations can always be bypassed by executing the standard
 * <code>3dm</code> diff algorithm on expanded or (better) normalized
 * input reftrees.
 * <p><b>Note:</b> The implementation is somewhat hampered by the lack
 * (as of 2004/11/23) of a fixed <code>3dm</code> diff format, in the sense
 * that the
 * tags and namespaces outputted may not be compatible with future versions of
 * <code>3dm</code>.
 */

public class Diff implements RefTree {

  /** Namespace for diff tags. */
  public static final String DIFF_NS ="http://www.hiit.fi/fc/xml/tdm/diff";
  /** Name of "copy tree" tag. */
  public static final String DIFF_COPY_TAG = "copy";
  /** Name of "insert tree" tag. */
  public static final String DIFF_INS_TAG = "insert";
  /** Name of the diff root tag. */
  public static final String DIFF_ROOT_TAG = "diff";

  /** Name of the source address attribute. */
  public static final String DIFF_CPYSRC_ATTR = "src";
  /** Name of the destination address attribute. */
  public static final String DIFF_CPYDST_ATTR = "dst";
  /** Name of the copy run length. */
  public static final String DIFF_CPYRUN_ATTR = "run";
  /** Name of the root operation attribute. */
  public static final String DIFF_ROOTOP_ATTR = "op";
  /** Name of the default root operation. */
  public static final String DIFF_ROOTOP_INS = "insert";

  protected RefTreeNode root = null;

  // hidden so far...
  protected Diff() {
  }

  /** Encode a reftree as a diff. In essence, the algorithm replaces
   * tree references with corresponding diff copy tags. Child lists of copy tags
   * that refer to consequtive nodes in a child list in the base tree are
   * run-length encoded using the <code>run</code> attribute.
   * <p>Note that node references and expanded nodes will be copied verbatim to
   * the output, i.e. the size of the diff is highly dependent on the efficient
   * use of tree references in the input tree.
   * <p>Note 2: all refterences in the refTree are assumed to be valid</p>
   * @param refTree reftree to encode
   * @param base tree containing nodes referenced in <code>refTree</code>
   * @throws IOException if encoding fails
   * @return the encoded diff
   */
  public static Diff encode(IdAddressableRefTree base, RefTree refTree) throws
      IOException {
    Diff d = new Diff();
    (d.new RefTreeDiffer(base,null)).runDiff(refTree.getRoot());
    return d;
  }

  /** Encode a reftree as a diff. In essence, the algorithm replaces
   * tree references with corresponding diff copy tags. Child lists of copy tags
   * that refer to consequtive nodes in a child list in the base tree are
   * run-length encoded using the <code>run</code> attribute. This variant
   * checks for consequtiveness using a {@link SequenceTester} rather than
   * by using the actual base tree. Thus, this encoder does not need the base
   * tree at all, as long as the consequtiveness of ids may be determined by some
   * other means.
   * <p>Note that node references and expanded nodes will be copied verbatim to
   * the output, i.e. the size of the diff is highly dependent on the efficient
   * use of tree references in the input tree.
   * <p>Note 2: all refterences in the refTree are assumed to be valid</p>
   *
   * @param refTree reftree to encode
   * @param st algorithm for testing if two nodes from <code>refTree</code>
   * are in sequence in the base tree.
   * @return the encoded diff
   * @throws IOException if encoding fails
   */
  public static Diff encode( RefTree refTree, SequenceTester st) throws
      IOException {
    Diff d = new Diff();
    (d.new RefTreeDiffer(null,st)).runDiff(refTree.getRoot());
    return d;
  }

  /** Decode diff. In essence, the algorithm replaces the diff <code>copy</code>
   * tags in the input with tree refernces to the base tree. This particular
   * implementations constructs a reftree which is lazily constructed, e.g.
   * <code>&lt;copy src="xxx" run="nnn" /&gt;</code> wont be expanded to a
   * run of tree references until the corresponding nodes are visited.
   * <p>Note that the algorithm cannot decode generic <code>3dm</code> diffs;
   * <b>only those diffs which copy full rather than truncated subtrees may be
   * decoded</b>. In other words, the <code>copy</code> tags in the diff may
   * not have any children. Note that {@link #encode encode} never produces
   * such undecodable diffs.
   *
   * @param base base tree to resolve refernces against
   * @return RefTree decoded reftree
   * @throws IOException if decoding fails
   */

  public RefTree decode( IdAddressableRefTree base ) throws IOException {
    Object c = getRoot().getContent();
    RefTreeNode rtroot=null;
    if( c instanceof DiffOperation &&
        ((DiffOperation) c).getOperation() == DiffOperation.ROOT_INSERT ) {
      Iterator insTree = getRoot().getChildIterator();
      if( !insTree.hasNext() )
        throw new DiffFormatException("Diff encodes empty XML document");
      RefTreeNode diffRoot = (RefTreeNode) insTree.next();
      if( diffRoot.getContent() instanceof DiffOperation ) {
        // This takes care of the case <diff rootop=ins><copy id=nn run=1></diff>
        DiffOperation ro = (DiffOperation) diffRoot.getContent();
        if( ro.getOperation() != DiffOperation.COPY ||
              ro.getRun() != 1 )                    
          // I guess we could allow diff:insert, but that is really redundant
          throw new DiffFormatException("Invalid diff operation at XPath /0/0: ");
        diffRoot= new RefTreeNodeImpl(null,ro.getSource(),true,null); 
      }
      rtroot = new DelayedRefTreeNode(diffRoot, base);
    } else if( c instanceof DiffOperation &&
        ((DiffOperation) c).getOperation() == DiffOperation.ROOT_COPY ) {
        //      Iterator tree = new CopySequenceIterator((DiffOperation) c,null,base);
        rtroot = new RefTreeNodeImpl(null, ( (DiffOperation) c).getSource(),
                                     true, null);
    } else
      throw new DiffFormatException("Invalid diff root tag");
    return new RefTreeImpl(rtroot);
  }

  /** Write diff as XML. The method differs from
   * {@link XasSerialization#writeTree writeTree} in the respect that it knows
   * how to serialize nodes that represent 3dm diff tags.
   *
   * @param wr writer to output the diff to
   * @param cw content writer for user node contents (need not handle diff tags)
   * @throws IOException if writing fails
   */

  public void writeDiff(XmlWriter wr, XasSerialization.ContentWriter cw) throws
      IOException {
    XasSerialization.writeTree(this, wr, new DiffContentWriter(cw));
  }

  /** Write diff as XML. Equivalent to <code>d.writeDiff(wr,cw)</code>.
   *
   * @see #writeDiff(XmlWriter,XasSerialization.ContentWriter)
   * @param d diff to write
   * @param wr writer to output the diff to
   * @param cw content writer for user node contents (need not handle diff tags)
   * @throws IOException if writing fails
   */
  public static void writeDiff(Diff d, XmlWriter wr, XasSerialization.ContentWriter cw) throws
      IOException {
    d.writeDiff(wr, new DiffContentWriter(cw));
  }

  /** Deserialize diff from XML. Deserializes a diff generated by this class or
   * a <code>3dm</code> diff (with some restrictions, see
   * {@link #decode decode}) as a {@link Diff} object. The method differs from
   * {@link XasSerialization#readTree readTree} in the respect that it knows
   * how to deserialize nodes that represent diff tags.
   *
   * @param rd reader to read diff from
   * @param cf content reader for node contnet (need not handle diff tags)
   * @param baseRootId String
   * @throws IOException
   * @return Diff
   */

  public static Diff readDiff(XmlReader rd, XasSerialization.ContentReader cf,
                              String baseRootId) throws
      IOException {
    Diff d = new Diff();
    RefTree t = XasSerialization.readTree(rd, new DiffContentReader( cf, baseRootId ));
    d.root = t.getRoot();
    return d;
  }

  // Reftree iface ----

  public RefTreeNode getRoot() {
    return root;
  }

  // Get id's for RefTreeNodes and Diffops (by srcid)
  // no src -> null (should never occur in the simpler diff format we use here,
  // where <ins> tags are never used

  /** Differencing algorithm implementation */
  protected class RefTreeDiffer extends DiffAlgorithm {

    private RefTreeNodeImpl currentNode = null;
    private IdAddressableRefTree base;
    private SequenceTester st;
    private Map successors = new HashMap();

    public RefTreeDiffer(IdAddressableRefTree base, SequenceTester st) {
      if( base != null && st != null )
        Log.log("Either must be null",Log.ASSERTFAILED);
      this.base =base;
      this.st = st;
    }

    public void runDiff(RefTreeNode root) throws IOException {
      diff(root);
    }

    protected List getStopNodes(Object changeNode) {
      RefTreeNode n = ( (RefTreeNode) changeNode);
      List l = null;
      if (n.isTreeRef())
        l = Collections.EMPTY_LIST;
      else {
        l = new ArrayList(1);
        l.add(changeNode);
      }
      return l;
    }

    protected Object lookupBase(Object changeNode) {
      RefTreeNode n = ( (RefTreeNode) changeNode);
      String id = n.getId();
      /// Is it really OK to return a node in the same tree?
      // should be, if all <refs> exist in base, which the must do if
      // change refs base. But what if they don't?
      // Maybe a base.getNode() would prevent errors here
      // OTOH, that slows things down....
      return n.isTreeRef() ? changeNode : null;
    }

    protected void content(Object node, boolean start) {
      RefTreeNodeImpl n = null;
      if (node instanceof DiffAlgorithm.DiffOperation) {
        DiffAlgorithm.DiffOperation op = (DiffAlgorithm.DiffOperation) node;
        Diff.DiffOperation c =
            new Diff.DiffOperation(op.getOperation(), identify(op.getSource()),
                                   identify(op.getDestination()), op.getRun(),
                                   identify(op.getSource())); // id by src
        n = new RefTreeNodeImpl(currentNode, c.getId(), false, c);
      } else
        n = new RefTreeNodeImpl(currentNode, ( (RefTreeNode) node).getId(),
                                (RefTreeNode) node);
      if (start) {
        if (currentNode != null)
          currentNode.addChild(n);
        else
          root = n;
      }
      if (start)
        currentNode = n;
      else
        currentNode = (RefTreeNodeImpl) currentNode.getParent();
    }

    protected Iterator getChildIterator(Object changeNode) {
      return ( (RefTreeNode) changeNode).getChildIterator();
    }

    protected boolean appends(Object baseTailO, Object baseNextO) {
      RefTreeNode baseTail = (RefTreeNode) baseTailO;
      RefTreeNode baseNext = (RefTreeNode) baseNextO;
      if( st != null )
        return st.inSequence(baseTail,baseNext);
      String baseId = ((RefTreeNode) baseTail).getId();
      String succ = ensureSuccessor(baseId,successors,base);
      return succ != NO_SUCCESSOR && succ.equals(((RefTreeNode) baseNext).getId());
    }

    protected String identify(Object node) {
      if( node == DiffAlgorithm.DiffOperation.NO_VALUE )
        return null;
      if (! (node instanceof RefTreeNode)) {
        Log.log("Errorneous node class", Log.ASSERTFAILED);
        return "ERROR:" + node.toString();
      }
      return ( (RefTreeNode) node).getId();
    }

  }

  private static final String NO_SUCCESSOR = "NO_SUCC";

  protected static String ensureSuccessor(String baseId, Map succcessors,
                                     IdAddressableRefTree base) {
    final String NO_PREDECESSOR = "NO_PRED";
    String succ = (String) succcessors.get(baseId);
    try {
      if (succ == null) {
        String pid = base.getParent(baseId);
        String prev = NO_PREDECESSOR;
        for (Iterator i = base.childIterator(pid); i.hasNext(); ) {
          String nid = (String) i.next();
          if( prev != NO_PREDECESSOR )
            succcessors.put(prev,nid);
          prev=nid;
        }
        succcessors.put(prev,NO_SUCCESSOR);
        succ = (String) succcessors.get(baseId);
        if( succ == null )
          Log.log("Broken parent/child relationship",Log.ASSERTFAILED);
      }
    } catch (NodeNotFoundException x ) {
      Log.log("Broken reftree",Log.FATALERROR,x); // Not sure how to report this...
    }
    return succ;
  }


  /** Writer for diff nodes. */
  protected static class DiffContentWriter implements XasSerialization.ContentWriter {

    XasSerialization.ContentWriter wr;

    public DiffContentWriter(XasSerialization.ContentWriter writer) {
      this.wr = writer;
    }

    public void startObject(Object o, XmlWriter writer) throws IOException {
      if (! (o instanceof DiffOperation)) {
        wr.startObject(o, writer);
        return;
      }
      DiffOperation op = (DiffOperation) o;
      switch (op.getOperation()) {
        case DiffOperation.ROOT_INSERT:
        case DiffOperation.ROOT_COPY:
          writer.addEvent(Event.createNamespacePrefix(DIFF_NS, "diff"));
          writer.addEvent(Event.createStartElement(DIFF_NS, DIFF_ROOT_TAG));
          if (op.getOperation() == DiffOperation.ROOT_INSERT)
            writer.addEvent(Event.createAttribute("", DIFF_ROOTOP_ATTR,
                                                  DIFF_ROOTOP_INS));
          break;
        case DiffOperation.COPY:
          writer.addEvent(Event.createStartElement(DIFF_NS, DIFF_COPY_TAG));
          addAttributes(op, writer);
          break;
        case DiffOperation.INSERT:
          writer.addEvent(Event.createStartElement(DIFF_NS, DIFF_INS_TAG));
          addAttributes(op, writer);
          break;
        default:
          throw new UnsupportedOperationException("Unknown diffop: " +
                                                  op.getOperation());
      }

    }

    public void finishObject(Object o, XmlWriter writer) throws IOException {
      if (! (o instanceof DiffOperation)) {
        wr.finishObject(o, writer);
        return;
      }
      DiffOperation op = (DiffOperation) o;
      switch (op.getOperation()) {
        case DiffOperation.ROOT_INSERT:
        case DiffOperation.ROOT_COPY:
          writer.addEvent(Event.createEndElement(DIFF_NS, DIFF_ROOT_TAG));
          // end pfx mapping?
          break;
        case DiffOperation.COPY:
          writer.addEvent(Event.createEndElement(DIFF_NS, DIFF_COPY_TAG));
          break;
        case DiffOperation.INSERT:
          writer.addEvent(Event.createEndElement(DIFF_NS, DIFF_INS_TAG));
          break;
        default:
          throw new UnsupportedOperationException("Unknown diffop: " +
                                                  op.getOperation());
      }
    }

    protected void addAttributes(DiffOperation op, XmlWriter w) throws
        IOException {
      if (op.getDestination() != null)
        w.addEvent(Event.createAttribute("", DIFF_CPYDST_ATTR,
                                         op.getDestination()));
      if (op.getSource() != null)
        w.addEvent(Event.createAttribute("", DIFF_CPYSRC_ATTR,
                                         op.getSource()));
      if (op.getRun() != null)
        w.addEvent(Event.createAttribute("", DIFF_CPYRUN_ATTR,
                                         op.getRun().toString()));
    }

  }

  /** Content reader for diff tags. */
  protected static class DiffContentReader implements XasSerialization.ContentReader {

    private static String rootId;

    private XasSerialization.ContentReader next;
    private IdentifiableContent diffTag=null; // Kill me

    public  DiffContentReader( XasSerialization.ContentReader aNext, String rootId ) {
      next = aNext;
      this.rootId = rootId;
    }

    public IdentifiableContent startContent(XmlReader r) throws IOException {
      //------------ Copy tag?
      EventSequence es = r.currentDelimiter(DIFF_NS,DIFF_COPY_TAG);
      if( es != null ) {
        String src=null,dst=null;
        Long run=null;
        for( Enumeration en=es.events();en.hasMoreElements();) {
          Event e = (Event) en.nextElement();
          if (Event.ATTRIBUTE != e.getType())
            continue;
          if (DIFF_CPYSRC_ATTR.equals(e.getName()))
            src = e.getValue().toString();
          else if (DIFF_CPYDST_ATTR.equals(e.getName()))
            dst = e.getValue().toString();
          else if (DIFF_CPYRUN_ATTR.equals(e.getName())) {
            try {
              run = new Long(e.getValue().toString());
            } catch (NumberFormatException x) {
              throw new DiffFormatException(
                  "Non-numeric run: " + e.getValue());
            }
          } else
            throw new DiffFormatException("Unknown attribute: " + e.getName());
        }
        diffTag = new DiffOperation(DiffOperation.COPY,src,dst,run,src);
        return diffTag;
      }

      //------------- Insert tag?
      es = r.currentElement(DIFF_NS,DIFF_INS_TAG);
      if( es != null ) { // FIXME-W actually, this code should not just be
        // for reftrees; the ex below should occur on decodeTorefTree
        // -> we should parse ins tags
        throw new DiffFormatException(
            "Diffs with inserts cannot be decoded to reftrees");
      }
      //------------- Root tag?
      es = r.currentDelimiter(DIFF_NS,DIFF_ROOT_TAG);
      if( es != null ) {
        boolean rootIsIns = false;
        for( Enumeration en=es.events();en.hasMoreElements();) {
          Event e = (Event) en.nextElement();
          if (Event.ATTRIBUTE != e.getType())
            continue;
          if (DIFF_ROOTOP_ATTR.equals(e.getName())) {
            if (!DIFF_ROOTOP_INS.equals(e.getValue()))
              throw new DiffFormatException("Unknown root operation " +
                                            e.getValue());
            rootIsIns = true;
          }
        }
        IdentifiableContent c = new DiffOperation(
            rootIsIns ? DiffOperation.ROOT_INSERT : DiffOperation.ROOT_COPY,
            rootId,null,null,rootId);
        if (!rootIsIns) {
          diffTag = c;
        } else
          diffTag = null;
        return c;
      }
      diffTag = null;
      return next.startContent(r);
    }

    public void finishContent(Object c, XmlReader r) throws IOException {
      if( c instanceof DiffOperation ) {
        String tag = null;
        switch( ( (DiffOperation) c).getOperation() ) {
          case DiffOperation.ROOT_COPY:
          case DiffOperation.ROOT_INSERT:
            tag = DIFF_ROOT_TAG;
            break;
          case DiffOperation.COPY:
            tag = DIFF_COPY_TAG;
            break;
          case DiffOperation.INSERT:
            tag = DIFF_INS_TAG;
            break;
          default:
            Log.log("Unknown diffop",Log.ASSERTFAILED);
        }
        EventSequence es = r.currentDelimiter(DIFF_NS, tag);
        Event e = es != null ? es.get(0) : null;
        if ( es == null || e.getType() != Event.END_ELEMENT )
          throw new DiffFormatException(
              "Expected closing tag </"+tag+">");
      } else
        next.finishContent(c,r);
    }
  }

  /** Exception singaling an invalid diff. */

  public static class DiffFormatException extends IOException {

    /** Create a new exception.
     *
     * @param msg message
     */
    public DiffFormatException(String msg) {
      super(msg);
    }
  }

  /** Interface for class implementing node a sequence test.
   */

  public interface SequenceTester {
    /** Tests if two reftree nodes follow each other. Should return
     * <code>true</code> if and only if <i>a</i> and <i>b</i> have a common
     * parent <i>p</i>,
     * and the child list iterator of <i>p</i> would return <i>b</i>
     * immediately after <i>b</i>;  i.e. <i>b</i> follows <i>a</i>
     * immediately in the child list of <i>p</i>.
     *
     * @param a First Node
     * @param b Second Node
     * @return <code>true</code> if b follows a
     */
    public boolean inSequence(RefTreeNode a, RefTreeNode b);
  }

  /** Diff operation node content. Used in the Diff reftree nodesto encode
   *  diff operations. */
  protected static class DiffOperation implements IdentifiableContent  {

   public static final int ROOT_COPY = DiffAlgorithm.DiffOperation.ROOT_COPY;
   public static final int ROOT_INSERT = DiffAlgorithm.DiffOperation.ROOT_INSERT;
   public static final int COPY = DiffAlgorithm.DiffOperation.COPY;
   public static final int INSERT = DiffAlgorithm.DiffOperation.INSERT;

   private int operation;
   private String source;
   private String destination;
   private String id;
   private Long run;

   protected DiffOperation(int aOperation, String aSource, String aDestination,
                           Long aRun, String aid) {
     operation = aOperation;
     source = aSource;
     destination = aDestination;
     run = aRun;
     id = aid;
   }


    public int getOperation() {
      return operation;
    }

    public String getSource() {
      return source;
    }

    public String getDestination() {
      return destination;
    }

    public Long getRun() {
      return run;
    }

    public String getId() {
      return id;
    }
  }

  /** RefTreeNode that know how to expand a sequence of copy ops as
   * its childlist.
   */

  protected static class DelayedRefTreeNode implements RefTreeNode {

    final RefTreeNode node;
    final IdAddressableRefTree base;

    public DelayedRefTreeNode(RefTreeNode node, IdAddressableRefTree base) {
      this.node = node;
      this.base = base;
    }

    public Iterator getChildIterator() {
      final Iterator baseIterator = node.getChildIterator();
      return new Iterator() {
        Iterator copySequenceIterator=null; // != null when active
        public void remove() {
          throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
          return (copySequenceIterator != null
                 && copySequenceIterator.hasNext()) || baseIterator.hasNext();
        }

        public Object next() {
          if( copySequenceIterator != null ) {
            if( copySequenceIterator.hasNext() )
              return copySequenceIterator.next();
            else
              copySequenceIterator = null;
          }
          RefTreeNode next = (RefTreeNode) baseIterator.next();
          if( next.getContent() instanceof DiffOperation ) {
            if( next.getChildIterator().hasNext() )
              throw new IllegalStateException(
                  "diffops may not have children in" +
                  "this version of the algo");
            copySequenceIterator = new CopySequenceIterator( (DiffOperation)
                next.getContent(), DelayedRefTreeNode.this,base);
            return copySequenceIterator.next();
          }
          return new DelayedRefTreeNode( next, base );
        }
      };
    }

    public Object getContent() {
      return node.getContent();
    }

    public String getId() {
      return node.getId();
    }

    public RefTreeNode getParent() {
      RefTreeNode n = node.getParent();
      return n != null ? new DelayedRefTreeNode( n, base ) : null;
    }

    public boolean isNodeRef() {
      return node.isNodeRef();
    }

    public boolean isReference() {
      return node.isReference();
    }

    public boolean isTreeRef() {
      return node.isTreeRef();
    }

  }

  /** Iterator that expands diff copy tags to tree references. */
  protected static class CopySequenceIterator implements Iterator {

    protected Map successors = new HashMap(); // = new HashMap(); // Move out of iterator for more speed!!!

    String id;
    long left;
    DelayedRefTreeNode parent;

    public CopySequenceIterator(DiffOperation op, DelayedRefTreeNode parent,
                                IdAddressableRefTree baseTree) {
      if (op.getOperation() != DiffOperation.ROOT_COPY &&
          op.getOperation() != DiffOperation.COPY)
        throw new IllegalStateException("No inserts should be seen here");
      id = op.getSource();
      ensureSuccessor(id, successors, baseTree);
      left = op.getRun() != null ? op.getRun().longValue() : 1;
      if( left < 1 )
        Log.log("CS iter should never map to <1 elems!", Log.ASSERTFAILED);
      this.parent = parent;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
      return left > 0;
    }

    // NOTE: Always has a first element
    public Object next() {
      if (left <= 0 || id == NO_SUCCESSOR)
        throw new NoSuchElementException();
      String current = id;
      id = (String) successors.get(id);
      left--;
      return /*new DelayedRefTreeNode(*/
          new RefTreeNodeImpl(parent, current, true, null);/*)*/
    }
  }

}
// arch-tag: 87c0c79f182ec345fb03c5b197031c02 *-
