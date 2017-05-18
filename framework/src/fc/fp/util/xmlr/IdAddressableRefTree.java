// $Id: IdAddressableRefTree.java,v 1.1 2010/02/23 20:31:10 tkamiya Exp $
package fc.fp.util.xmlr;

import java.util.Iterator;

/** A reftree that can be accessed by node ids in a random-access fashion. The
 * identifiers of the nodes in this class of trees are required to be unique.
 * An IdAddressableRefTree is typically used to hold the common base tree to
 * which the references in a set of reftrees point.
 */

public interface IdAddressableRefTree extends RefTree {

  /** Get node by id.
   * @param id id of node to access
   * @return node in the tree, or <code>null</code> if the tree has no such node
   */

  public RefTreeNode getNode(String id);

  /** Returns <code>true</code> if this tree contains the given node.
   * @param id id of node to search for
   * @return boolean <code>true</code> if the node exists in the tree
   */
  // FIXME-W consider deprecating
  public boolean contains(String id);

  /** Get parent id of node id.
   * @param nid id of node, whose parent id is desired
   * @return id of parent of <code>nid</code>, or <code>null</code>
   * if <code>nid</code> is root
   * @throws NodeNotFoundException if <code>nid</code> is not in the tree.
   */
  public String getParent(String nid) throws NodeNotFoundException;

  /** Get child ids of node id.
   * @param id id of node, whose child ids are desired
   * @return Iterator over the {@link java.lang.String} child ids
   * @throws NodeNotFoundException if <code>id</code> is not in the tree.
   */
  // Note: iterator is over child ids, not nodes!
  public Iterator childIterator(String id) throws NodeNotFoundException;
}
// arch-tag: 9c7eadd5d076df7b4e272354cf9f013e *-
