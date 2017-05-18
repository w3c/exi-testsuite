// $Id: MutableRefTree.java,v 1.1 2010/02/23 20:31:10 tkamiya Exp $
package fc.fp.util.xmlr;

/** Base interface for mutable reftrees. Mutable reftrees are reftrees whose
 * structure as well as node content may be modified. The interface makes use
 * of node ids rather than the {@link RefTreeNode} nodes themselves.
 * <p>The interface accomodates for ordered as well as
 * unordered/automatically ordered child lists by providing two variants of the
 * <code>insert</code> and <code>move</code> methods.
 */

public interface MutableRefTree extends IdAddressableRefTree {

  /** Constant indicating default position in child list. */
  public static final long DEFAULT_POSITION = -1L;

  /** Delete a subtree. Deletes the subtree rooted at <code>id</code> from
   * the tree.
   * @param id root of subtree to delete
   * @throws NodeNotFoundException if <code>id</code> is not in the tree
   */
  public void delete(String id) throws NodeNotFoundException;

  // Ins at natural position.
  /** Insert a new non-reference node. The insert happens at the default
   * position in the child list (as defined by the particular
   * <code>MutableRefTree</code> implementation).
   *
   * @param parentId id of parent to the new node
   * @param newId id of the new node, must not already exist in the tree
   * @param content content object of new node
   * @throws NodeNotFoundException if the <code>parentId</code> node is
   * not in the tree.
   */
  public void insert(String parentId, String newId,
                     Object content) throws NodeNotFoundException;

  // Ins at particular natural position. May not be supported

  /** Insert a new non-reference node. Some implementations may not support
   * insertion at other positions than
   * {@link #DEFAULT_POSITION DEFAULT_POSITION}.
   *
   * @param parentId id of parent to the new node
   * @param pos position in child list, before which the new node is inserted
   *  (i.e. 0 implies at the start of the list). {@link #DEFAULT_POSITION DEFAULT_POSITION}
   * inserts at the default position (as defined by the particular
   * <code>MutableRefTree</code> implementation)
   * @param newId id of the new node, must not already exist in the tree
   * @param content content object of new node
   * @throws NodeNotFoundException if the <code>parentId</code> node is
   * not in the tree.
   */

  public void insert(String parentId, long pos, String newId,
                     Object content) throws NodeNotFoundException;

  /** Move a node in the tree. The node target position among its siblings
   * is the default position in the child list (as defined by the particular
   * <code>MutableRefTree</code> implementation).
   * <p><b>Note:</b> The result of a move
   * that would make a node an ancestor of itself is undefined. Some
   * implementations may detect this condition and throw an
   * {@link java.lang.IllegalArgumentException}
   *
   * @param nodeId node to move
   * @param parentId new parent of the node
   * @throws NodeNotFoundException if <code>nodeId</code> or
   * <code>parentId</code> is missing from the tree
   */
  public void move(String nodeId, String parentId) throws NodeNotFoundException;


  /** Move a node in the tree. Some implementations may not support
   * moves to other positions than
   * {@link #DEFAULT_POSITION DEFAULT_POSITION}.
   * <p><b>Note:</b> The result of a move
   * that would make a node an ancestor of itself is undefined. Some
   * implementations may detect this condition and throw an
   * {@link java.lang.IllegalArgumentException}
   *
   * @param nodeId node to move
   * @param parentId new parent of the node
   * @param pos node target position among its siblings, with 0
   * signifying before the first child and
   * {@link #DEFAULT_POSITION DEFAULT_POSITION} at the default position.
   * @throws NodeNotFoundException if <code>nodeId</code> or
   * <code>parentId</code> is missing from the tree
   */

  public abstract void move(String nodeId, String parentId, long pos) throws
      NodeNotFoundException;

  /** Update content of a node.
   *
   * @param nodeId id of node whose cotent is updated
   * @param content new content of the node
   * @return boolean <code>true</code> if the content was updated
   *  (in the case that <code>!oldContent.equals(content)</code>)
   * @throws NodeNotFoundException if <code>nodeId</code> is not in the
   * tree
   */
  public abstract boolean update(String nodeId, Object content) throws
      NodeNotFoundException;

}
// arch-tag: e748e93a331f0e85d49c98e1545f0e49 *-
