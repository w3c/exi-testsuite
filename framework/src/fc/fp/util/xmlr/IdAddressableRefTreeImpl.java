// $Id: IdAddressableRefTreeImpl.java,v 1.1 2010/02/23 20:31:10 tkamiya Exp $

package fc.fp.util.xmlr;

import java.util.Iterator;

/** Abstract default implementation for {@link IdAddressableRefTree}. The
 * class provides default implementations for the {@link #contains contains},
 * {@link #getParent getParent} and {@link #childIterator childIterator}
 * methods using the {@link RefTreeNode} interface.
 * <p>The user needs to implement {@link #getNode getNode} in a subclass. The
 * other methods may be overriden if there is a way to quickly navigate the
 * tree ids without accessing the actual reftree nodes.
 */

public abstract class IdAddressableRefTreeImpl implements IdAddressableRefTree {

  public abstract RefTreeNode getNode(String id);

  public boolean contains(String id) {
    return getNode(id) != null;
  }

  /** Get parent id of node id. The parent id is in esssence  obtained by
   * <code>getNode(nid).getParent().getId()</code>.
   * @param nid id of node, whose parent id is desired
   * @return id of parent of <code>nid</code>, or <code>null</code>
   * if <code>nid</code> is root
   * @throws NodeNotFoundException if <code>nid</code> is not in the tree.
   */
  public String getParent(String nid) throws NodeNotFoundException {
    RefTreeNode n = getNode(nid);
    if (n == null)
      throw new NodeNotFoundException(nid);
    RefTreeNode parent = n.getParent();
    return parent == null ? null : parent.getId();
  }

  /** Get child ids of node id. The child ids are obtained by
   * accessing <code>getNode(id).childIterator()</code>.
   * @param id id of node, whose child ids are desired
   * @return Iterator over the {@link java.lang.String} child ids
   * @throws NodeNotFoundException if <code>id</code> is not in the tree.
   */

  public Iterator childIterator(String id) throws NodeNotFoundException {
    RefTreeNode n = getNode(id);
    if (n == null)
      throw new NodeNotFoundException(id);
    final Iterator niter = n.getChildIterator();
    return new Iterator() {
      public void remove() {
        niter.remove();
      }

      public boolean hasNext() {
        return niter.hasNext();
      }

      public Object next() {
        return ( (RefTreeNode) niter.next()).getId();
      }
    };
  }

}
// arch-tag: f7a7e92bff9602818a6c150b11565738 *-
