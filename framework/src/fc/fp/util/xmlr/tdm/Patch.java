// $Id: Patch.java,v 1.1 2010/02/23 20:31:11 tkamiya Exp $
// History:
// This file has contained F IXMESaxAdapter and F IXMEXasAdapter in r1.4
package fc.fp.util.xmlr.tdm;

import java.io.IOException;

import fc.fp.util.xas.TypedXmlSerializer;
import fc.fp.util.xas.XmlWriter;
import fc.fp.util.xmlr.IdAddressableRefTree;
import fc.fp.util.xmlr.MutableRefTree;
import fc.fp.util.xmlr.NodeNotFoundException;
import fc.fp.util.xmlr.RefTree;
import fc.fp.util.xmlr.RefTrees;
import fc.fp.util.xmlr.XasSerialization;


/** Patch a refree with a <code>3dm</code> diff. The implementation is
 * limited to diffs which only refers to full subtrees, such as those
 * produced by {@link Diff}, see {@link Diff#decode Diff.decode} for details.
 * <p>Note that patch is a trivial operation due to the great similarity
 * between <code>3dm</code> diffs and reftrees. In particular,
 * if no node expansion is desired, the process is essentially equivalent to
 * <code>RefTrees.apply(diff.decode(),base)</code>
 */

public class Patch {

  private static final XasSerialization.ContentCodec GENERIC_CODEC =
      XasSerialization.getGenericCodec();

  protected Patch() {
  }

  /** Patch a refree with a diff. The output is streamed to a
   * <code>TypedXmlSerializer</code>.
   *
   * @param base base tree to patch
   * @param diff diff to apply
   * @param expandDiffRefs set to <code>true</code> if tree references in the diff
   * shall be expanded to the corresponding nodes in <code>base</code>.
   * @param out serializer for the patched reftree
   * @param cw content writer for the node contents
   * @throws IOException if an i/O error occurs
   * @throws NodeNotFoundException if the diff contains an invalid reference
   */
  public static void patch(IdAddressableRefTree base, Diff diff,
                              boolean expandDiffRefs,
                              TypedXmlSerializer out,
                              XasSerialization.ContentWriter cw)
      throws IOException, NodeNotFoundException {
      RefTree patched = patch(base,diff,expandDiffRefs);
      XasSerialization.writeTree(patched,new XmlWriter(out),cw);
  }

  /** Patch a refree with a diff.
   *
   * @param base base tree to patch
   * @param diff diff to apply
   * @param expandDiffRefs set to <code>true</code> if tree references in the diff
   * shall be expanded to the corresponding nodes in <code>base</code>.
   * @return patched reftree (may share contents and structure with
   * <code>base</code>)
   * @throws IOException if an i/O error occurs
   * @throws NodeNotFoundException if the diff contains an invalid reference
   */
  public static RefTree patch(IdAddressableRefTree base, Diff diff,
                              boolean expandDiffRefs)
      throws IOException, NodeNotFoundException {
    RefTree patch=diff.decode(base);
    if( expandDiffRefs ) {
        MutableRefTree patched = RefTrees.getMutableTree(base);
        RefTrees.apply(patch,patched);
        patch=patched;
    }
    return patch;
  }
}
// arch-tag: 6f8962aa9227fe33d43d7bda4b1b42c5 *-
