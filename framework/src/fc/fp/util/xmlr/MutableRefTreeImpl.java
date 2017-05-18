// $Id: MutableRefTreeImpl.java,v 1.1 2010/02/23 20:31:10 tkamiya Exp $
package fc.fp.util.xmlr;

/** Convenience base class for mutable reftree implementations. Provides
 * implemementations of <code>move</code> and <code>insert</code> without
 * position arguments in terms of those with postions, in order to protect
 * against inconsistencies among these.
 */

public abstract class MutableRefTreeImpl extends IdAddressableRefTreeImpl
    implements MutableRefTree {

  public abstract void delete(String id) throws NodeNotFoundException;


  /** Insert a new non-reference node. Equivalent to
   * <code>insert(parentId,{@link #DEFAULT_POSITION},newId,content)</code>
   *
   * @param parentId id of parent to the new node
   * @param newId id of the new node, must not already exist in the tree
   * @param content content object of new node
   * @throws NodeNotFoundException if the <code>parentId</code> node is
   * not in the tree.
   */
  // Final to 1) allow inling 2) discourage foul-ups in derived classes
  public final void insert(String parentId, String newId, Object content)
      throws NodeNotFoundException{
    insert(parentId, -1L, newId, content);
  }

  public abstract void insert(String parentId, long pos, String newId,
                              Object content) throws NodeNotFoundException;


  // Final to 1) allow inling 2) discourage foul-ups in derived classes
  /** Move a node in the tree.  Equivalent to
   * <code>move(nodeId,parentId,{@link #DEFAULT_POSITION})</code>
   *
   * @param nodeId node to move
   * @param parentId new parent of the node
   * @throws NodeNotFoundException if <code>nodeId</code> or
   * <code>parentId</code> is missing from the tree
   */

  public final void move(String nodeId, String parentId) throws
      NodeNotFoundException {
    move(nodeId, parentId, -1L);
  }

  public abstract void move(String nodeId, String parentId, long pos) throws
      NodeNotFoundException;

  public abstract boolean update(String nodeId, Object content) throws
      NodeNotFoundException;

}
// arch-tag: 25b6d034ef650715fb8ca0a0d6c45047 *-
