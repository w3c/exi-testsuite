// $Id: RefTree.java,v 1.1 2010/02/23 20:31:10 tkamiya Exp $
package fc.fp.util.xmlr;

/** Base interface for a reftree, i.e. an XMLR document parse tree. RefTrees
 * consist of nodes of class {@link RefTreeNode RefTreeNode}. */

public interface RefTree {

  /** Get root node of the reftree.
   *
   * @return RefTreeNode
   */
  public RefTreeNode getRoot();

}
// arch-tag: 67f683b77de6e2955e095c62f767d462 *-
