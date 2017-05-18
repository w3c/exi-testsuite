// $Id: RefTreeImpl.java,v 1.1 2010/02/23 20:31:10 tkamiya Exp $
package fc.fp.util.xmlr;

/** Default implementation of {@link RefTree}.
 */

public class RefTreeImpl implements RefTree {

  protected RefTreeNode root;

  /** Create a new reftree.
   * @param root root of the reftree
   */
  public RefTreeImpl(RefTreeNode root) {
    this.root = root;
  }

  public RefTreeNode getRoot() {
    return root;
  }
}
// arch-tag: db2b79aa0b4ca2a07e645fcaa608ba50 *-
