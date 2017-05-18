package fc.fp.util.xmlr;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import fc.fp.util.Util;

/** Default implementation of {@link RefTreeNode}. The class provides a
 * space-efficient default implementation for reftree nodes. The implementation
 * does not allow non-reference nodes to have a <code>null</code>
 * content.
 */

public class RefTreeNodeImpl implements RefTreeNode {

  private Object content = null;
  private String id;
  private RefTreeNode parent;
  private boolean isTreeRef = false; // content==null -> ref
                                     // type of ref by this flag

  LinkedList children = null; //new LinkedList();

  /** Create a new node from template.
   *
   * @param parent parent reftree node, or <code>null</code> if none
   * @param id unique id of the node
   * @param values template reftreenode to copy (shallow)
   */

  public RefTreeNodeImpl(RefTreeNode parent, String id, RefTreeNode values) {
    this(parent,id,values.isTreeRef(),values.getContent());
  }

  /** Create a new ordinary (non-reference) node.
   * @param parent parent reftree node, or <code>null</code> if none
   * @param id unique id of the node
   * @param content content object for the node, must not be <code>null</code>
   */

  public RefTreeNodeImpl(RefTreeNode parent, String id, Object content) {
    this(parent,id,false,content);
    if( content == null )
      throw new IllegalArgumentException();
  }

  /** Create a new node. A <code>null</code> content implies that the node is
   * a reference, in which case the <code>isTreeRef</code> flag determines
   * if the reference is a node or tree reference.
   *
   * @param parent parent reftree node, or <code>null</code> if none
   * @param id unique id of the node
   * @param isTreeRef <code>true</code> if the node is a tree reference
   * @param content content of node, or <code>null</code> if the node is a
   * reference node.
   */
  public RefTreeNodeImpl(RefTreeNode parent, String id,
                         boolean isTreeRef, Object content ) {
    this.id = id;
    this.parent = parent;
    this.isTreeRef = isTreeRef;
    this.content = content;
  }

  // RefTreeNode

  public Iterator getChildIterator() {
    return children == null ?
        Collections.EMPTY_LIST.iterator() : children.iterator();
  }

  public Object getContent() {
    return content;
  }

  public String getId() {
    return id;
  }

  public RefTreeNode getParent() {
    return parent;
  }

  public boolean isNodeRef() {
    return content == null && !isTreeRef;
  }

  public boolean isReference() {
    return content == null;
  }

  public boolean isTreeRef() {
    return content == null && isTreeRef;
  }


  // Extra stuff

  /** Set content of node.
   * @param content content of node, or <code>null</code> to make the node
   * a reference node, in which case the <code>isTreeRef</code> flag determines
   * if the reference is a node or tree reference.
   */

  public void setContent(Object content) {
    this.content = content;
  }

  /** Set to <code>true</code> to make the node a tree reference.
   * @param treeRef boolean
   */

  public void setTreeRef(boolean treeRef) {
    this.isTreeRef = treeRef;
  }

  /** Set node parent.
   * @param parent new parent of the node, or <code>null</code> if none.
   */
  public void setParent(RefTreeNode parent) {
    this.parent = parent;
  }

  /** Append node to the child list. As a side effect, the parent of the
   * child is set to this node.
   *
   * @param n child node to append
   */
  public void addChild( RefTreeNodeImpl n) {
    ensureChildList();
    children.add(n);
    n.setParent(this);
  }

  /** Remove node from child list. As a side effect, the parent of the node
   * is set to <code>null</code>.
   * @param n node to remove
   * @return <code>true</code> if the node was found and removed from the child list
   */
  public boolean removeChild( RefTreeNodeImpl n) {
    ensureChildList();
    if( children.remove(n) ) {
      n.setParent(null);
      return true;
    }
    return false;
  }

  /** Get first child in child list.
   * @return first child in child list, or <code>null</code> if the node has
   *   no children.
   */

  public RefTreeNodeImpl firstChild() {
    if( children == null || children.size() == 0 )
      return null;
    return (RefTreeNodeImpl) children.getFirst();
  }

  /** Get last child in child list.
   * @return last child in child list, or <code>null</code> if the node has
   *   no children.
   */

  public RefTreeNodeImpl lastChild() {
    if( children == null || children.size() == 0 )
      return null;
    return (RefTreeNodeImpl) children.getLast();
  }

  /** Compare nodes for equality. Requires the content object to implement
   * {@link java.lang.Object#equals} properly.
   */

  public boolean equals(Object obj) {
    if( !(obj instanceof RefTreeNode ))
      return false;
    RefTreeNode n = (RefTreeNode) obj;
    return Util.equals(n.getId(),id) && n.isNodeRef() ==
        isNodeRef() && n.isTreeRef() == isTreeRef() &&
        Util.equals(n.getContent(),getContent());
  }

  protected final void ensureChildList() {
    if (children == null)
      children = new LinkedList();
  }

}
// arch-tag: 63bbd5d1c5ea7345aa8a040d30d38304 *-
