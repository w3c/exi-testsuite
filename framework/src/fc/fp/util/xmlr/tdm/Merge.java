// $Id: Merge.java,v 1.1 2010/02/23 20:31:11 tkamiya Exp $
package fc.fp.util.xmlr.tdm;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.xmlpull.v1.XmlSerializer;
import fc.fp.syxaw.util.Log;
import fc.fp.util.xas.EventSerializer;
import fc.fp.util.xas.SaxWriter;
import fc.fp.util.xas.TypedXmlSerializer;
import fc.fp.util.xas.XmlReader;
import fc.fp.util.xas.XmlWriter;
import fc.fp.util.xmlr.IdAddressableRefTree;
import fc.fp.util.xmlr.RefTree;
import fc.fp.util.xmlr.RefTreeNode;
import fc.fp.util.xmlr.RefTreeNodeImpl;
import fc.fp.util.xmlr.ReferenceEvent;
import fc.fp.util.xmlr.XasSerialization;
import tdm.lib.BaseNode;
import tdm.lib.BranchNode;
import tdm.lib.ConflictLog;
import tdm.lib.MatchArea;
import tdm.lib.MatchedNodes;
import tdm.lib.Node;
import tdm.lib.XMLNode;
import java.util.Stack;
import fc.fp.util.xmlr.RefTreeImpl;

/** Three-way merging of reftrees using the <code>3dm</code> algorithm.
 * The class implements three-way merging of reftrees. Merging is performed
 * by the <code>tdm.lib.Merge</code> algorithm.
 * The input trees normally need to be normalized in order to ensure that all
 * nodes that need to be visisted during the merge are expanded.
 */

public class Merge {

  /** Default merger for nodes which always fails. Recall that the node
   * merger only gets called when both branches are modified, so this merger
   * is in fact not as useless as it sounds.
   */
  public static final NodeMerger DEFAULT_NODE_MERGER =
      new NodeMerger() {
        public RefTreeNode merge(RefTreeNode base, RefTreeNode n1,
                             RefTreeNode n2) {
      return null; // No dice
    }
  };


  private static Random rnd = new Random();
  private static RuntimeException NO_SUCH_OP = new UnsupportedOperationException();

  protected Map baseIx = new HashMap();
  protected Map leftIx = new HashMap();
  protected Map rightIx = new HashMap();
  protected BaseNode t0r;
  protected BranchNode t1r;
  protected BranchNode t2r;
  protected NodeMerger nm;

  private Merge() {
  }

  /** Merge reftrees. Perform a 3dm three-way merge of the input reftrees.
   *
   * @param base base tree
   * @param t1 first changed tree
   * @param t2 second changed tree
   * @param log xml serializer that receives the <code>3dm</code> conflict log
   * @param nodeMerger node merger
   * @return RefTree merged tree
   * @throws IOException if an I/O error occurs during merge
   */
  public static RefTree merge( IdAddressableRefTree base, IdAddressableRefTree t1,
                            IdAddressableRefTree t2,
                            TypedXmlSerializer log,
                            NodeMerger nodeMerger) throws IOException {
    Merge m = new Merge(base, t1, t2, null, null, nodeMerger);
    RefTreeExternalizer ext = new RefTreeExternalizer();
    m.merge(log, ext);
    return ext.getTree();
  }

  /** Merge reftrees. Perform a 3dm three-way merge of the input reftrees. The
   * merged tree undergoes a XAS serialization and deserialization cycle. This
   * cycle allows e.g. populating the merged tree with suitable content objects
   * (via the given {@link fc.fp.util.xmlr.XasSerialization.ContentReader
   *  ContentReader}).
   *
   * @param base base tree
   * @param t1 first changed tree
   * @param t2 second changed tree
   * @param log xml serializer that receives the <code>3dm</code> conflict log
   * @param cc content codec for conversion back and forth between XAS event
   * sequences.
   * @param nodeMerger node merger
   * @return RefTree merged tree
   * @throws IOException if an I/O error occurs during merge
   */

  public static RefTree merge( IdAddressableRefTree base, IdAddressableRefTree t1,
                            IdAddressableRefTree t2,
                            TypedXmlSerializer log,
                            XasSerialization.ContentCodec cc,
                            NodeMerger nodeMerger) throws IOException {
    EventSerializer tmes = new EventSerializer();
    merge(base,t1,t2,XasSerialization.getDocumentSerializer(tmes),log,cc,
          nodeMerger);
    return XasSerialization.readTree(new XmlReader(
        XasSerialization.getDocumentSequence(tmes.getCurrentSequence())),cc);
  }

  /** Merge reftrees. Perform a 3dm three-way merge of the input reftrees.
   * The merged tree is streamed as XAS events as it is being constructed,
   * and is not instantiated in memory.
   *
   * @param base base tree
   * @param t1 first changed tree
   * @param t2 second changed tree
   * @param tm serializer that receives the merged tree
   * @param log xml serializer that receives the <code>3dm</code> conflict log
   * @param cw content codec for conversion back and forth between XAS event
   * sequences.
   * @param nodeMerger node merger
   * @throws IOException if an I/O error occurs during merge
   */
  public static void merge( IdAddressableRefTree base, IdAddressableRefTree t1,
                            IdAddressableRefTree t2, TypedXmlSerializer tm,
                            TypedXmlSerializer log,
                            XasSerialization.ContentCodec cw,
                            NodeMerger nodeMerger) throws IOException {
    Merge m = new Merge(base,t1,t2,null,null,nodeMerger);
    XmlWriter wr = new XmlWriter(tm);
    m.merge( log, new XasExternalizer(wr,cw));
  }

  protected Merge( IdAddressableRefTree base, IdAddressableRefTree t1,
                            IdAddressableRefTree t2, XmlSerializer tm,
                            XmlSerializer log,
                            NodeMerger nm) {
   // Get an unique fake root id. A little convoluted, but should work.
   String fakeRootId = "";
   for (; base.contains(fakeRootId) || t1.contains(fakeRootId) ||
        t2.contains(fakeRootId);
        fakeRootId += Integer.toHexString(rnd.nextInt()));
   RefTreeNode fakeRoot = new RefTreeNodeImpl(null, fakeRootId, false, null);
   t0r = new BaseNode(fakeRoot, base.getRoot(), baseIx);
   t1r = new BranchNode(fakeRoot, t1.getRoot(), baseIx, true);
   t1r = new BranchNode(fakeRoot, t2.getRoot(), baseIx, false);
   this.nm = nm;
  }

  protected void merge( TypedXmlSerializer log, tdm.lib.XMLNode.Externalizer ext )
      throws IOException {
    try {
      tdm.lib.Merge m = new tdm.lib.Merge(null);
      m.treeMerge(t1r, t2r, ext, new NodeMergerProxy(nm));
      ConflictLog cl = m.getConflictLog();
      if( cl.hasConflicts() ) {
        cl.writeConflicts( new SaxWriter( log  ,null) );
      }
    } catch( Exception x) {
      Log.log("Merge ex",Log.ERROR,x);
      if( x instanceof IOException )
        throw (IOException) x;
    }
  }


  static class XasExternalizer implements tdm.lib.XMLNode.Externalizer {

    XmlWriter wr;
    XasSerialization.ContentWriter cw;

    public XasExternalizer( XmlWriter wr, XasSerialization.ContentWriter cw ) {
      this.wr = wr;
      this.cw = cw;
    }

    public void startNode(tdm.lib.XMLNode xn) throws IOException {
      //Log.log("Open",Log.DEBUG,((XMLNode) n).getRefTreeNode());
      RefTreeNode n = ((XMLNode) xn).getRefTreeNode();
      if( n.isTreeRef() )
        wr.addEvent(ReferenceEvent.createTreeReference(n.getId()));
      else if( n.isNodeRef() )
        wr.addEvent(ReferenceEvent.createNodeReference(n.getId()) );
      else
        cw.startObject(n.getContent(),wr);
    }

    public void endNode(tdm.lib.XMLNode xn) throws IOException {
      //Log.log("CLose",Log.DEBUG,((XMLNode) n).getRefTreeNode());
      RefTreeNode n = ((XMLNode) xn).getRefTreeNode();
      if( n.isTreeRef() )
        ; // NOP as <ref:tree /> is only one event
      else if( n.isNodeRef() )
        wr.addEvent(ReferenceEvent.createEndNodeReference() );
      else
        cw.finishObject(n.getContent(),wr);
    }
  }

  static class RefTreeExternalizer implements tdm.lib.XMLNode.Externalizer {

    RefTreeNode root;
    Stack parents= new Stack();

    public RefTreeExternalizer() {
    }

    public void startNode(tdm.lib.XMLNode xn) throws IOException {
      RefTreeNodeImpl parent = parents.isEmpty() ? null :
                               (RefTreeNodeImpl) parents.peek();
      RefTreeNode n = ((XMLNode) xn).getRefTreeNode();
      RefTreeNodeImpl emitted =
          new RefTreeNodeImpl(null,n.getId(),n);
      if( parent == null )
        root = emitted;
      else
        parent.addChild(emitted);
      parents.push(emitted);
    }

    public void endNode(tdm.lib.XMLNode xn) throws IOException {
      parents.pop();
    }

    public RefTree getTree() {
      return new RefTreeImpl(root);
    }
  }

  /** Interface for custom node three-way mergers. */
  public interface NodeMerger {
    /** Perform a three-way merge of reftree nodes. Only called if both
     * <code>n1</code> and <code>n2</code> differ from <code>base</code>.
     *
     * @param base node in the base tree
     * @param n1 node in the first variant tree (<code>t1</code>
     *  parameter of the various <code>merge</code> methods)
     * @param n2 node in the second variant tree (<code>t2</code>
     *  parameter of the various <code>merge</code> methods)
     * @return merged node, or <code>null</code> if the node cannot be merged
     */
    RefTreeNode merge(RefTreeNode base, RefTreeNode n1, RefTreeNode n2);
  }

  class NodeMergerProxy implements tdm.lib.XMLNode.Merger {

    private NodeMerger nm;

    public NodeMergerProxy(NodeMerger nm) {
      this.nm = nm;
    }

    public tdm.lib.XMLNode merge(tdm.lib.XMLNode baseNode,
                                 tdm.lib.XMLNode aNode,
                                 tdm.lib.XMLNode bNode) {
      RefTreeNode merged = nm.merge( ( (XMLNode) baseNode).getRefTreeNode(),
                                    ( (XMLNode) aNode).getRefTreeNode(),
                                    ( (XMLNode) bNode).getRefTreeNode());
      return merged == null ? null : new XMLNode(merged);
    }
  }

  class BranchNode extends tdm.lib.BranchNode {

    protected boolean isLeft;
    protected RefTreeNode n;

    // base ix is map of id->baseNode
    public BranchNode(RefTreeNode root, RefTreeNode onlyChild, Map baseIx,
                      boolean isLeft) {
      this.n = root;
      this.childPos =0;
      this.parent = null;
      this.content = new XMLNode(n);
      this.baseMatch = (BaseNode) baseIx.get(n.getId());
      this.isLeft = isLeft;
      children.add(new BranchNode( onlyChild , 0, this,baseIx, isLeft));
      (isLeft ? leftIx : rightIx).put(n.getId(),this);
    }

    public BranchNode(RefTreeNode n, int childPos, BranchNode parent,
                      Map baseIx, boolean isLeft ) {
      this.n = n;
      this.childPos = childPos;
      this.parent = parent;
      this.content = new XMLNode(n);
      this.baseMatch = (BaseNode) baseIx.get(n.getId());
      this.isLeft = isLeft;
      int pos=0;
      for (Iterator i = n.getChildIterator(); i.hasNext(); ) {
        children.add(new BranchNode( (RefTreeNode) i.next(), pos++, this,
                                    baseIx, isLeft));
      }
      (isLeft ? leftIx : rightIx).put(n.getId(),this);
    }

    public tdm.lib.BaseNode getBaseMatch() {
      return baseMatch;
    }

    public int getBaseMatchType() {
      return getBaseMatch() == null ? 0 : MATCH_FULL;
    }

    public tdm.lib.BranchNode getFirstPartner(int typeFlags) {
      return (BranchNode) (!isLeft ? leftIx : rightIx).get(n.getId());
    }

    public MatchedNodes getPartners() {
      tdm.lib.MatchedNodes partners =
          new tdm.lib.MatchedNodes(baseMatch);
      if( hasBaseMatch() )
        partners.addMatch(getFirstPartner(MATCH_FULL));
      return partners;
    }

    public boolean hasBaseMatch() {
      return getFirstPartner(MATCH_FULL) != null;
    }

    public boolean isLeftTree() {
      return isLeft;
    }

    /*Base impl is ok for these:
    public Node getChildAsNode(int ix)
    public int getChildCount()
    public int getChildPos()
    public tdm.lib.XMLNode getContent()
    public Node getLeftSibling()
    public Node getRightSibling()
    public Node getParentAsNode()
    public boolean hasLeftSibling()
    public boolean hasRightSibling()
    public tdm.lib.BranchNode getChild(int ix)
    public tdm.lib.BranchNode getParent()
    public boolean isMatch(int type) */

    // Not needed
    public void addChild(Node n) {
      throw NO_SUCH_OP;
    }

    public void addChild(int ix, Node n) {
      throw NO_SUCH_OP;
    }

    public void debug(PrintWriter pw, int indent) {
      throw NO_SUCH_OP;
    }

    public void debugTree(PrintWriter pw, int indent) {
      throw NO_SUCH_OP;
    }

    public MatchArea getMatchArea() {
      throw NO_SUCH_OP;
    }

    public void removeChild(int ix) {
      throw NO_SUCH_OP;
    }

    public void removeChildren() {
      throw NO_SUCH_OP;
    }

    public void replaceChild(int ix, Node n) {
      throw NO_SUCH_OP;
    }

    public void setContent(tdm.lib.XMLNode aContent) {
      throw NO_SUCH_OP;
    }

    public void setMatchArea(MatchArea anArea) {
      throw NO_SUCH_OP;
    }

    public void delBaseMatch() {
      throw NO_SUCH_OP;
    }

    public void setBaseMatch(tdm.lib.BaseNode p, int amatchType) {
      throw NO_SUCH_OP;
    }

    public void setMatchType(int amatchType) {
      throw NO_SUCH_OP;
    }

    public void setPartners(MatchedNodes p) {
      throw NO_SUCH_OP;
    }

  }

  class BaseNode extends tdm.lib.BaseNode {

    RefTreeNode n;

    public BaseNode(RefTreeNode root, RefTreeNode onlyChild, Map baseIx) {
      this.n = root;
      this.childPos = 0;
      this.parent = null;
      this.content = new XMLNode(n);
      children.add(new BaseNode(onlyChild, 0, this, baseIx));
      baseIx.put(n.getId(),this);
    }

    // base ix is map of id->baseNode, filled in on create
    public BaseNode(RefTreeNode n, int childPos, BaseNode parent,
                      Map baseIx ) {
      this.n = n;
      this.childPos = childPos;
      this.parent = parent;
      this.content = new XMLNode(n);
      int pos=0;
      for (Iterator i = n.getChildIterator(); i.hasNext(); ) {
        children.add(new BaseNode( (RefTreeNode) i.next(), pos++, this,
                                    baseIx));
      }
      baseIx.put(n.getId(),this);
    }


    public MatchedNodes getLeft() {
      tdm.lib.MatchedNodes matches =
          new tdm.lib.MatchedNodes(this);
      if( leftIx.containsKey(n.getId()) )
        matches.addMatch((BranchNode) leftIx.get(n.getId()));
      return matches;
    }


    public MatchedNodes getRight() {
      tdm.lib.MatchedNodes matches =
          new tdm.lib.MatchedNodes(this);
      if( rightIx.containsKey(n.getId()) )
        matches.addMatch((BranchNode) rightIx.get(n.getId()));
      return matches;
    }

    /* Base impl is ok for these:
    public Node getChildAsNode(int ix)
    public int getChildCount()
    public int getChildPos()
    public tdm.lib.XMLNode getContent()
    public Node getLeftSibling()
    public Node getRightSibling()
    public Node getParentAsNode()
    public boolean hasLeftSibling()
    public boolean hasRightSibling()
    public tdm.lib.BaseNode getChild(int ix)
    public tdm.lib.BaseNode getParent()
    */

   // Not needed
    public void addChild(Node n) {
      throw NO_SUCH_OP;
    }

    public void addChild(int ix, Node n) {
      throw NO_SUCH_OP;
    }

    public void debug(PrintWriter pw, int indent) {
      throw NO_SUCH_OP;
    }

    public void debugTree(PrintWriter pw, int indent) {
      throw NO_SUCH_OP;
    }

    public MatchArea getMatchArea() {
      throw NO_SUCH_OP;
    }

    public void removeChild(int ix) {
      throw NO_SUCH_OP;
    }

    public void removeChildren() {
      throw NO_SUCH_OP;
    }

    public void replaceChild(int ix, Node n) {
      throw NO_SUCH_OP;
    }

    public void setContent(tdm.lib.XMLNode aContent) {
      throw NO_SUCH_OP;
    }

    public void setMatchArea(MatchArea anArea) {
      throw NO_SUCH_OP;
    }

    public void swapLeftRightMatchings() {
      throw NO_SUCH_OP;
    }
  }

  class XMLNode extends tdm.lib.XMLNode {

    RefTreeNode n;

    public XMLNode( RefTreeNode n) {
      this.n = n;
    }

    public boolean contentEquals(Object a) {
      return n.equals( ((XMLNode) a).n );
    }

    public int getContentHash() {
      // Not needed in 3dm merge phase
      throw new UnsupportedOperationException();
    }

    public int getInfoSize() {
      // Not needed in 3dm merge phase
      throw new UnsupportedOperationException();
    }

    public RefTreeNode getRefTreeNode() {
      return n;
    }
  }
}
// arch-tag: 0e4e430cd54276cf7046828184d8e8b8 *-
