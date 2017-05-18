// $Id: NodeNotFoundException.java,v 1.1 2010/02/23 20:31:10 tkamiya Exp $
package fc.fp.util.xmlr;

/** Exception indicating that a node was not found. Only raised
 * by operations that requires the node in question to exist.
 */

public class NodeNotFoundException extends Exception {

  private String id;

  /** Create a new exception. */
  public NodeNotFoundException() { //FIXME-W: Consider deprecating
    super();
  }

  /** Create a new exception.
   * @param id id of missing node
   */
  public NodeNotFoundException(String id) {
    super("Node: " + id);
    this.id = id;
  }

  /** Create a new exception.
   * @param msg additional explanation
   * @param id id of missing node
   */
  public NodeNotFoundException(String msg,String id) {
    super(msg+" " + id);
    this.id = id;
  }

  /** Get id of missing node.
   * @return id of the missing node which caused the exception
   */

  public String getId() {
    return id;
  }
}
// arch-tag: b871dc488468810e3fbd101d6fee9376 *-
