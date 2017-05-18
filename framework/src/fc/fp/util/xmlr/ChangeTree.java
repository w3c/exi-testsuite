// $Id: ChangeTree.java,v 1.1 2010/02/23 20:31:10 tkamiya Exp $

package fc.fp.util.xmlr;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import fc.fp.syxaw.util.Log;
import fc.fp.syxaw.util.Util;


/** A mutable reftree that buffers changes as a reftree. The class implements
 * a mutable reftree that upon construction is identical to some underlying
 * id-addressable reftree. The tree may then be modified using the
 * {@link MutableRefTree} interface.
 * <p>A reftree expressing the tree using references to the underlying tree
 * is available troug the {@link #getChangeTree getChangeTree} method at
 * each point during the change cycle.
 * <p>For instance, consider a change tree with the underlying tree
 * <pre>
 * &lt;directory id="1" name="syxaw.hiit.fi"&gt;
 * &nbsp; &lt;file name="kernel.bin" version="314" object="0agh5678zxlkj6h7" id="2"  /&gt;
 * &nbsp; &lt;directory  name="subdir"  id="3"&gt;
 * &nbsp;   &lt;file name="file.txt" version="1" object="0agh5678zxwwj6h8" id="4" /&gt;
 * &nbsp; &lt;/directory&gt;
 * &lt;/directory&gt;
 * </pre>
 * We then insert a new node into the changetree:
 * <code>insert("1","5","&lt;file name='newfile' &gt;")</code>. Calling
 * <code>getChangeTree</code> after the insert would yield the reftree
 * <pre>
 * &lt;ref:node id="1"&gt;
 * &nbsp; &lt;ref:tree id="2"  /&gt;
 * &nbsp; &lt;ref:tree id="3" /&gt;
 * &nbsp; &lt;file name="newfile" id="5" /&gt;
 * &lt;/ref:node&gt;
 * </pre>
 * <p>This class is in some senses the dual of {@link RefTrees#apply}: the
 * former applies a reftree as a series of operations on a mutable reftree, the
 * latter expresses the result of a series of operations on a mutable reftree
 * as a reftree.
 * <p>Note that the implementation does not guarantee that a minimal
 * (in terms of nodes) reftree is returned by <code>getChangeTree</code>;
 * changes that cancel out may yield a reftree with more than a single
 * tree reference to the root of the underlying tree.
 * <p><b>Note:</b> Be careful to always update node content via the
 * {@link #update update} method. Accessing the <code>RefTreeNode</code>s
 *  directly may not be intercepted by the class, and may change the underlying
 * tree.<p>
 * <b>Note, BUG:</b>getNode() may return nodes from inside a deleted tree.
 */

// NOTE: deletion is a bit tricky; we don't want to traverse the target tree
// unnecessarilty = mark only roots as deleted
// BUT: this my cause nconsistencies in getNode(), unless we check all
// nodes on the path to the root... but that's a performance penalty
// Disable recorsive check in isDeleted() to gain more SPEED
//
public class ChangeTree extends MutableRefTreeImpl {

  private Map nodeById = new HashMap();
  private Set delRoots = new HashSet();
  private Node refRoot = null;
  private IdAddressableRefTree refTree = null;

  private long changeCount = 0;

  private IdAddressableRefTree backingTree;
  private MutableRefTree changeTarget;

  /** Create a new change tree. The tree is initially identical to the
   * underlying tree.
   * @param backingTree underlying tree
   */
  public ChangeTree(IdAddressableRefTree backingTree ) {
    this(backingTree,null);
  }

  /** Create a new change tree. The tree is initially identical to the
   * underlying tree. The changes to this tree are mirrored to
   * <code>changeTarget</code>.
   *
   * @param backingTree underlying tree
   * @param changeTarget tree to which any changes are mirrored. May be
   * same as <code>backingTree</code>.
   */
  public ChangeTree(IdAddressableRefTree backingTree, MutableRefTree changeTarget ) {
    this.backingTree=backingTree;
    this.changeTarget=changeTarget;
    reset();
  }

  /** Get the tree as a reftree. The reftree references nodes in the
   * underlying tree.
   *
   * @return the change tree as a reftree
   */
  public IdAddressableRefTree getChangeTree() {
    return refTree;
  }

  /** Reset tree. Resets all changes made to the tree, causing the
   * tree to be identical to its underlying tree.
   */

  public void reset() {
    String rootId = backingTree.getRoot().getId();
    delRoots.clear();
    refRoot = new Node(rootId,null,null,true,false);
    refTree = new IdAddressableRefTreeImpl() {
      public RefTreeNode getRoot() {
        return new ProxyNode( refRoot, false );
      }

      public RefTreeNode getNode(String id) {
        return ChangeTree.this.getNode(id,false);
      }
    };

    nodeById.clear();
    nodeById.put(rootId,refRoot);
    changeCount = 0;
  }

  public RefTreeNode getRoot() {
    return new ProxyNode(refRoot,true); //  target.getRoot();
  }

  // Id navigation

  // FIXME,BUGREPORT: May return a node from inside a deleted subtree
  // Assume basetree = r
  //                  a b
  //                     c
  // Now, move c as child of a and delete a . The bug is that
  // findNode(c)!=null.
  // Conjecture: This happens because isDeleted recurses up to
  // the root using the *oringinal* location, not the deleted location.
  // Testcase: faxma.test.SyntheticDirTree rev 1.3 27.2.2006
  // On fix: remove bug descr from class javadoc!

  public RefTreeNode getNode(String id) {
    return getNode(id,true);
  }

  protected RefTreeNode getNode(String id, boolean transparent ) {
    RefTreeNode n = findNode(id);
    if( n == null ) n= backingTree.getNode(id);
    if( n!=null )
      n = new ProxyNode(n,transparent);
    return n != null && !isDeleted(n) ? n : null;
  }

//  int __deletdepth = 0;
  protected boolean isDeleted( RefTreeNode n ) {
    if( n == null )
      return false;
    if( delRoots.contains(n.getId()) )
     return true;
//    __deletdepth++;
//    if( __deletdepth > 100 )
//      Log.log("Infinite delete recursion :(",Log.ASSERTFAILED);
    if( isDeleted(n.getParent()) ) {
      //delRoots.add(n.getId()); // Cache whole path to node as deleted -> faster lookup
      // Actually, pretty useless: most nodes are not deleted -> must scan to
      // root :( anyway
//      __deletdepth--;
      return true;
    }
//    __deletdepth--;
    return false;
  }

  // Mutability

  public void delete(String id) throws NodeNotFoundException {
    RefTreeNode m = getNode(id);
    if( m == null ) // Already deleted
      throw new NodeNotFoundException(id);
    RefTreeNode p = m.getParent();
    if( p==null )
      throw new IllegalArgumentException("Cannot delete root");
    taint(p.getId(),null,true);
    Node n = findNode(id);
    // Remove from reftree
    if( ((Node) n.getParent()).children.remove(id) == null )
      Log.log("Parent/child inconsistency",Log.ASSERTFAILED);
    nodeById.remove(id); // WARNING: Assumes long-lived ids; recursive clean
                           // would be better
    delRoots.add(id);
    changeCount ++;
    // Propagate change
    if( changeTarget != null )
      changeTarget.delete(id);
  }

  public void insert(String parentId, long pos, String newId, Object content) throws
      NodeNotFoundException{
    taint(parentId,null,true);
    Node n = findNode(parentId);
    Node newNode = new Node(newId,n,content,false,false);
    newNode.children = childListFactory();
    n.children.put(newId,newNode);
    nodeById.put(newId,newNode);
    changeCount ++;
    // Propagate change
    if( changeTarget != null )
      changeTarget.insert(parentId,pos,newId,content);
  }

  public void move(String nodeId, String parentId, long pos)
      throws NodeNotFoundException {
    verifyMove(nodeId,parentId);
    taint(parentId,null,true);
    Node n = findNode(nodeId);
    Node p = findNode(parentId);
    // Move from source code...
    if( n== null ) {
      n = new Node(nodeId,p,null,true,false);
      String origParentId = backingTree.getNode(nodeId).getParent().getId();
      taint(origParentId,null,true);
      Node origParent = findNode(origParentId);
      origParent.children.remove(nodeId);
    } else {
      ( (Node) n.getParent()).children.remove(nodeId);
      ((Node) n).parent = p;
    }
    // ...to dest code
    p.children.put(nodeId,n);
    nodeById.put(nodeId,n);
    changeCount ++;
    // Propagate change
    if( changeTarget != null )
      changeTarget.move(nodeId,parentId,pos);
  }

  public boolean update(String nodeId, Object content) throws NodeNotFoundException{
    Object oldContent = getNode(nodeId).getContent();
    if( content.equals(oldContent) ) {
      //taint(nodeId,null,true); // This is actually somewhat questionable!!!!
                               // It means that if you touch a node with upd, you expand it
                               // OTOH, this makes RefTrees.apply() work due to its access pattern
      return false;
    }
    //Log.log("---------Old content is ",Log.INFO, fc.syxaw.proto.Util.toString( oldContent ));
    //Log.log("---------New content is ",Log.INFO,  fc.syxaw.proto.Util.toString( content ));
    taint(nodeId,content,true);
    changeCount ++;
    // Propagate change
    if( changeTarget != null )
      changeTarget.update(nodeId,content);
    return true;
  }

  // Check for the case that id is an ancestor of newParent
  protected void verifyMove( String id, String newParent ) {
    if( Util.equals(id,newParent) ) {
      //Log.log("Moving node to be child of its own subtree", Log.ASSERTFAILED);
      throw new IllegalArgumentException
          ("Moving node to be child of its own subtree");
    }
    if( newParent != null ) {
      try {
        verifyMove(id, getParent(newParent));
      } catch (NodeNotFoundException ex) {
        Log.log("Broken tree",Log.ASSERTFAILED);
      }
    }
  }

  protected Node findNode(String id) {
    return (Node) nodeById.get(id);
  }

  protected RefTreeNode taint(String id, Object content, boolean expandChildren)
      throws NodeNotFoundException {
    Node n = findNode(id);
    if( n== null ) {
      // No such node, taint parent
      taint(backingTree.getParent(id),null,true);
      n = findNode(id);
      if( n == null )
        Log.log("The node "+id+" should exist now.",Log.ASSERTFAILED);
    }
    if( expandChildren && n.isTreeRef ) {
      n.expandChildren(backingTree,id,nodeById);
    }
    if( content != null ) {
      n.expandContent(content);
    }
    /*java.io.PrintWriter pw = new java.io.PrintWriter(System.out);
    dump(pw,refRoot);
    pw.flush();*/
    return n;
  }


  private /*static*/ class Node implements RefTreeNode {

    private String id;
    private RefTreeNode parent;
    private Object content;
    private Map children= null; //null means same chlist as in target
    private boolean isTreeRef;
    private boolean isNodeRef;

    public Node(String id, RefTreeNode parent, Object content,
                boolean isTreeRef, boolean isNodeRef ) {
      if( content == null && !(isTreeRef || isNodeRef))
        Log.log("null content when not ref not supported",Log.ASSERTFAILED);
      this.id = id;
      this.parent = parent;
      this.content = content;
      this.isTreeRef = isTreeRef;
      this.isNodeRef = isNodeRef;
    }

    public String getId() {
      return id;
    }

    public RefTreeNode getParent() {
      return parent;
    }

    public Object getContent() {
      return content;
    }

    public int getChildCount() {
      if( children == null && isNodeRef )
        Log.log("A nodref should always have an expanded chlist",Log.ASSERTFAILED);
      return children == null ? 0 : children.size();
    }

    public boolean isReference() {
      return isTreeRef || isNodeRef;
    }

    public boolean isTreeRef() {
      return isTreeRef;
    }

    public boolean isNodeRef() {
      return isNodeRef;
    }

    public Iterator getChildIterator() {
      if( children == null && isNodeRef )
        Log.log("A nodref should always have an expanded chlist",Log.ASSERTFAILED);
      return children != null ?
          children.values().iterator() : Collections.EMPTY_LIST.iterator();
    }


    boolean contentExpanded() {
      return /*(expandflags&CONTENT_EXPANDED)!=0 &&*/
          content != null;
    }

    boolean childrenExpanded() {
      return /*(expandflags&CHILDREN_EXPANDED)!=0 &&*/ children != null;
    }

    private void expandContent( Object content ) {
      this.content = content;
      this.isNodeRef = false;
    }

    private void expandChildren( IdAddressableRefTree target, String id, Map nodeById)
        throws NodeNotFoundException {

      children = childListFactory();

      for( Iterator i=target.childIterator(id);i.hasNext(); ) {
        String cid = (String) i.next();
        RefTreeNode child  = new Node(cid, this, null, true, false );
        children.put(cid,child);
        nodeById.put(cid,child);
      }
      isNodeRef = isTreeRef;
      isTreeRef = false;
    }
  }

  public boolean hasChanges() {
    return changeCount > 0;
  }

  private static Map childListFactory() {
    // FIXME-W Unordered inserts really need "ordering help" ->
    // MutableRefTrees should take a id comparator for construction!
    return new TreeMap(Util.STRINGS_BY_LENGTH);
  }


  // Node class for opaque traversal of this tree = get the reftree
  // It's a little kludgy that we have to encapsulate the Node objects
  // this way, but externally it's much nicer than having two
  // modes for the tree

  private class ProxyNode implements RefTreeNode {

    private RefTreeNode n;
    private boolean transparent;

    public ProxyNode(RefTreeNode n, boolean transparent) {
      if( n == null )
        Log.log("Can't proxy null",Log.ASSERTFAILED);
      this.n = n;
      this.transparent = transparent;
    }

    public String getId() {
      return n.getId();
    }

    public RefTreeNode getParent() {
      // First try changed tree, then backing tree; ensures that
      // we "jump back" into the change tree if going up from
      // a node in the backing tree
      RefTreeNode p = findNode(n.getId());
      if( p != null )
        p= p.getParent();
      else
        p = n.getParent();
      return p != null ?
          new ProxyNode( p, transparent ) : null;
    }

    public Object getContent() {
      Object content = n.getContent();
/*OLD      if( n instanceof Node && content == null && transparent )
        return backingTree.getNode(n.getId()).getContent();*/
      if( n instanceof Node && !((Node) n).contentExpanded()
          && transparent ) {
        RefTreeNode n2 = backingTree.getNode(n.getId());
        return n2!= null ? n2.getContent() : null;
      }
      return content;
    }


    public Iterator getChildIterator() {
      final Iterator niter =
          n instanceof Node && transparent && !((Node) n).childrenExpanded() ?
          backingTree.getNode(n.getId()).getChildIterator() : n.getChildIterator();
      return new Iterator() {
        public void remove() {
          niter.remove();
        }

        public boolean hasNext() {
          return niter.hasNext();
        }

        public Object next() {
          ProxyNode p = new ProxyNode( (RefTreeNode) niter.next(),transparent );
          //if( isDeleted(p) ) // This check is quite expensive
          //  Log.log("Returning deleted node",Log.ASSERTFAILED);
          return p;
        }
      };

    }

    public boolean isReference() {
      return isTreeRef() || isNodeRef();
    }

    public boolean isTreeRef() {
      if(/* n instanceof Node && !((Node) n).childrenExpanded() &&*/ transparent ) {
        RefTreeNode n2 = backingTree.getNode(n.getId());
        return n2 !=null ? n2.isTreeRef() : n.isTreeRef();
      }
      if( (!n.isTreeRef())^(((Node) n).childrenExpanded()) )
        Log.log("treeref/child discrepancy",Log.ASSERTFAILED);
      return n.isTreeRef();
    }

    public boolean isNodeRef() {
      if( /*n instanceof Node && !((Node) n).childrenExpanded() &&*/ transparent ) {
        RefTreeNode n2 = backingTree.getNode(n.getId());
        return n2 !=null ? n2.isNodeRef() : n.isNodeRef();
      }
      if( (n.isNodeRef())^(n.getContent()==null) && !n.isTreeRef() )
        Log.log("ref/content discrepancy; nr="+n.isNodeRef()+" cnt"
               ,Log.ASSERTFAILED, n.getContent());
      return n.isNodeRef();
    }
  }

}
// arch-tag: ed1e1f7cc8d521cfa3cf9411fdfa021c *-
