// $Id: RefTrees.java,v 1.1 2010/02/23 20:31:10 tkamiya Exp $
package fc.fp.util.xmlr;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import fc.fp.syxaw.util.Log;

/** Algorithms and utilities for reftrees.
 */

public class RefTrees {

  private RefTrees() {} // Not instantiable


  /** Expand references in a reftree. The method replaces the tree and
   * node references in the given reftree with nodes from the
   * backing tree. Tree references that are in <code>allowedRefs</code>
   * and node references that are in
   * <code>allowedContent</code> are not replaced. Note that the substituted
   * tree references may contain node references, as long as they are in
   * <code>allowedContent</code>.
   *
   * <p>For instance, consider the backing tree B
   <pre>
   &lt;directory id="1" name="syxaw.hiit.fi"&gt;
     &lt;file name="kernel.bin" version="314" object="0agh5678zxlkj6h7" id="2"  /&gt;
     &lt;directory  name="subdir"  id="3"&gt;
       &lt;file name="file.txt" version="1" object="0agh5678zxwwj6h8" id="4" /&gt;
     &lt;/directory&gt;
   &lt;/directory&gt;
   </pre>
   * and the reftree R
   <pre>
   &lt;ref:tree id="1" /&gt;
   </pre>
   * The result of <code>expandRefs(R,{2,4},{1},B)</code> is
   <pre>
   &lt;ref:node id="1"&gt;
    &lt;ref:tree id="2" /&gt;
    &lt;directory name="subdir" id="3"&gt;
      &lt;ref:tree  id="4"  /&gt;
    &lt;/directory&gt;
   &lt;/ref:node&gt;
   </pre>
   *
   * <p>The sets passed as arguments to the method are guaranteed not to be
   * enumerated at any point. Thus, to quickly allow any content reference, it
   * is suffcient to supply a set whose {@link java.util.Set#contains contains}
   * method always returns true.
   *
   * @param tree tree to expand
   * @param allowedRefs set of {@link java.lang.String Strings}
   *   of allowed tree references. The strings are allowed target ids
   * @param allowedContent set of {@link java.lang.String Strings}
   *   of allowed node references. The strings are allowed target ids
   * @param backingTree tree containing the referenced nodes
   * @return expanded reftree
   */
  // Will share content with the tree passed
  public static RefTree expandRefs( RefTree tree, Set allowedRefs,
                                    Set allowedContent,
                                IdAddressableRefTree backingTree)
   throws NodeNotFoundException {
   final RefTreeNode root = expandRefs(tree.getRoot(),allowedRefs,allowedContent,
                                       backingTree,null,false);
   return new RefTreeImpl(root);
 }


  protected static RefTreeNodeImpl expandRefs( RefTreeNode root, Set allowedRefs,
                                           Set allowedContent,
                                IdAddressableRefTree backingTree,
                                RefTreeNode parent, boolean inBacking )
      throws NodeNotFoundException {
    RefTreeNodeImpl newRoot = null;
    if ( allowedRefs.contains( root.getId())) {
      // Put a tree ref
      newRoot = new RefTreeNodeImpl(parent,root.getId(),true,null);
    } else if ( !(!root.isReference() && !inBacking) &&
                allowedContent.contains(root.getId()) ) {
      // Put a node ref if the node is allowed and not an expanded node in srctree
      newRoot = new RefTreeNodeImpl(parent,root.getId(),false,null);
    } else { // Not reference, or not an allowed reference
      Object content = null;
      if( root.isReference() ) {
        RefTreeNode backingNode = backingTree.getNode(root.getId());
        if( backingNode == null )
          throw new NodeNotFoundException("Can't expand reference to",
                                          root.getId());
        else
          content = backingNode.getContent();
      } else
        content = root.getContent();
      newRoot = new RefTreeNodeImpl(parent, root.getId(), false,
                                    content);
    }
    if( !newRoot.isTreeRef() ) {
      // Expand children for everything but treerefs
      for (Iterator i =
           root.isTreeRef() ? backingTree.getNode(root.getId()).getChildIterator() :
           root.getChildIterator(); i.hasNext(); ) {
        RefTreeNodeImpl child = expandRefs( (RefTreeNode) i.next(), allowedRefs,
                                           allowedContent,
                                           backingTree, newRoot,
                                           root.isTreeRef());
        newRoot.addChild(child);
      }
    }
    return newRoot;
  }


  // ------------------- normalize algorithm --------------------------

  /** Normalize reftree references. A set of reftrees, which all reference
   * a common backing tree (ond only that tree), is <i>normalized</i>, iff
   * there are no tree references for which a descendant of the refrence target
   * is present in the trees. The condition must hold for any tree reference
   * in any input
   * tree, and a descendant may not occur in any tree.
   * <p>The practical usefulness of having a set of reftrees normalized is that
   * any tree reference may then be viewed as a handle for an
   * opaque subtree of the backing tree, as all other trees are guaranteed not
   * to include any nodes from that subtree (lest an identical tree reference).
   *
   * <p>One may, for instance, perform a three-way merge of a set of normalized
   * reftrees, as one would merge any ordinary XML file. The state of presence
   * for tree reference in the merged tree will then simply indicate the
   * presense or absence of an entire subtree .
   *
   * <p>This method performs normalization of the input trees and return the
   * result as a set of permitted tree reference targets for each of the trees.
   * The absence of a target from answer set <i>i</i> compared to the union of
   * all sets indicates that the target is not referenced in input
   * tree <i>i</i>.
   *
   * <p>As an example, consider the trees
<table><tr><th>backingTree</th><th>trees[0]</th><th>trees[1]</th></tr>
<tr valign="top"><td><pre>
&nbsp; r
&nbsp;/ \
a   c
|   |
b   d
&nbsp;   |
&nbsp;   e
</pre></td><td><pre>
&nbsp; r
&nbsp;/ \
i   c*
|
a*
</pre></td><td><pre>
&nbsp; r
&nbsp;/ \
a*  d'
</pre></td><tr></table>
   * where tree references are marked with a * (a* is a tree reference to a) and
   * node references are marked with a ' (the prime sign)
   * <p>In this case normalization yields <code>{{a,e}{a}}</code>. We may not
   * reference c* in <code>trees[0]</code>, since d, which is a descendant of
   * c, is present in <code>trees[1]</code>.
   * Also, we find that
   * the reference d* is not allowed in <code>trees[0]</code>, as the child list
   * of that node has changed in <code>trees[1]</code>.
   *
   * <p>Note that the output sets of this method are directly suited for use
   * with {@link #expandRefs expandRefs}.
   *
   * @param backingTree tree contatining all reference targets
   * @param trees trees to normalize
   * @param expandedContents set that is filled with the id of all
   *  non-reference nodes. Useful when determining what content to expand.
   *  Set to <code>null</code> if no such set is desired.
   * @return An array of sets, where each set contains the allowed reference
   * targets (id {@link java.lang.String Strings}) for the corresponding input
   * tree.
   * @throws NodeNotFoundException if a reference not in the backing tree
   * is found in one of the input trees.
   */
  public static Set[] normalize( IdAddressableRefTree backingTree,
                                 RefTree[] trees, Set expandedContents)
      throws NodeNotFoundException {
    // Build set of normalized refs
    Map commonRefs = new HashMap();
    Set innerNodes = new HashSet(); // inner nodes from reftree
    for (int iTree = 0; iTree < trees.length; iTree++) {
      Log.log("Adding tree "+trees[iTree],Log.INFO);
      addTreeRefs(backingTree, trees[iTree].getRoot(),
                  commonRefs, innerNodes, trees[iTree], expandedContents);
    }
    //return commonRefs;
    Set[] refset = new HashSet[trees.length];
    for (int iTree = 0; iTree < trees.length; iTree++) {
      refset[iTree] = new HashSet();
    }
    for( Iterator i = commonRefs.entrySet().iterator();i.hasNext(); ) {
      Map.Entry e = (Map.Entry) i.next();
      String id = (String) e.getKey();
      Set usedIn = (Set) e.getValue();
      for (int iTree = 0; iTree < trees.length; iTree++) {
        if( usedIn.contains(trees[iTree]) )
        refset[iTree].add(id);
      }
    }
    return refset;
  }

  /** Normalize reftree references. Equivalent to
   * <code>normalize(backingTree,trees,null)</code>.
   *
   * @see #normalize(IdAddressableRefTree,RefTree[],Set)
   * @param backingTree tree contatining all reference targets
   * @param trees trees to normalize
   * @return An array of sets, where each set contains the allowed reference
   * targets (id {@link java.lang.String Strings}) for the corresponding input
   * tree.
   * @throws NodeNotFoundException if a reference not in the backing tree
   * is found in one of the input trees.
   */

  public static Set[] normalize( IdAddressableRefTree backingTree,
                                 RefTree[] trees )
      throws NodeNotFoundException {
    return normalize(backingTree,trees,null);
  }


  protected static void addTreeRefs(IdAddressableRefTree backingTree,
                                    RefTreeNode root,
                                    Map commonRefs, Set innerNodes,
                                    RefTree treeTag, Set expandedContents) throws
      NodeNotFoundException {
    if (root.isReference() || backingTree.contains(root.getId()))
      // NOTE: The latter condition ensures updated content triggers proper
      // expansion. If there are only inserts and no updates it can be skipped
      addTreeRef(backingTree, root, commonRefs, innerNodes, treeTag);
    if( expandedContents != null && !root.isReference() )
      expandedContents.add(root.getId());
    for ( Iterator i = root.getChildIterator(); i.hasNext();)
      addTreeRefs(backingTree, (RefTreeNode) i.next(), commonRefs, innerNodes,
                  treeTag, expandedContents);
  }

  protected static void downscan(RefTreeNode node,
                            Map refLeaves, Set refInnerNodes, Set tagSet ) {
    String id = node.getId();
    if( refInnerNodes.contains(id) ) {
      for( Iterator i = node.getChildIterator();i.hasNext();) {
        RefTreeNode child = (RefTreeNode) i.next();
        if( !refInnerNodes.contains(child.getId()) &&
            !refLeaves.containsKey(child.getId()) ) {
          refLeaves.put(child.getId(),tagSet);
        } else
          downscan( child, refLeaves, refInnerNodes, tagSet);
      }
    } else if( refLeaves.containsKey(id) ) {
      //Log.log("Downscan added "+id,Log.INFO);
      ((Set) refLeaves.get(id)).addAll(tagSet); // ASFAIK, |tagSet|=1 here
    } // Nowhere to be found. Note that it's impossible that the node is
      // inserted (as we are traversing backingTree) -> we should just stop
  }

  protected static void prohibit(String id,Map treeRefs,Set inner,IdAddressableRefTree bt)
      throws NodeNotFoundException {
    if (id == null || inner.contains(id) || bt.getNode(id) == null)
      return; // Already prohibited or Processing an inserted node
    prohibit(bt.getParent(id), treeRefs, inner, bt);
    Set tags = (Set) treeRefs.remove(id);
    if (tags == null) {
      tags = new HashSet();
    }
    for (Iterator i = bt.childIterator(id); i.hasNext(); ) {
      String cid = (String) i.next();
      if (treeRefs.containsKey(cid)) {
        ( (Set) treeRefs.get(cid)).addAll(tags);
      } else {
        Set tags2 = new HashSet(); // Beware of sharing copies of the sets!
        tags2.addAll(tags);
        treeRefs.put(cid, tags2);
      }
    }
    inner.add(id);
  }

  protected static void addRefToRefset(String id,RefTree treeTag,Map treeRefs,
                                       Set inner,IdAddressableRefTree bt) throws
      NodeNotFoundException{
    prohibit(bt.getParent(id), treeRefs, inner, bt);
    if (treeRefs.containsKey(id))
      ( (Set) treeRefs.get(id)).add(treeTag);
    else {
      Set tags = new HashSet();
      tags.add(treeTag);
      treeRefs.put(id, tags);
    }
  }

  // The core normalization algorithm:
  //
  // The general idea is to have two sets, refLeaves and refInnerNodes
  // - The base case is to add any treeref in any tree to refLeaves
  // - prohibit(id) prohibits the use of a certian ref; all nodes on the path
  // from the treeref to the root (in *both* the backing and current tree)
  // needs to be prohibited. prohibit() changes refLeaves so that any prohibited
  // refnodes move to innerNodes, and the allowed descendants of a ref replace the
  // ref in refLeaves
  // - if we find ourself adding a ref that is in innernodes, we tag all
  // descendants (in backingtree) of that innernode as used in refLeaves =
  // downscan()
  //
  // NOTE: Since refLeaves and innerNodes always deals with subtrees of backingTree,
  // we get away with using sets instead of trees, although the input trees
  // have differing structure.
  //

  protected static void addTreeRef(IdAddressableRefTree backingTree,
                                   RefTreeNode node, Map refLeaves,
                                   Set refInnerNodes, RefTree treeTag) throws
      NodeNotFoundException {
    // Pre- and postcondition: subtrees in refLeaves are disjunct
    // -> node can only be in one of the subtrees in refLeaves
    String id = node.getId();
//    Log.log("Enter addRef, id="+id+" leaves="+refLeaves+", inner="+refInnerNodes,Log.INFO);
    if (node.isTreeRef() && refInnerNodes.contains(id)) {
      // This ref has been replaced by subrefs, run a downscan to
      // tag the subrefs with this tree
      // Note: backingTree.getNode(id) != null since it's a working ref (in innernodes!)
      Set tagSet = new HashSet();
      tagSet.add(treeTag);
      downscan(backingTree.getNode(id),refLeaves,refInnerNodes,tagSet);
      return;
    }
    if( node.isTreeRef() )
      addRefToRefset(id,treeTag,refLeaves,refInnerNodes,backingTree);
    else
      prohibit(id,refLeaves,refInnerNodes,backingTree);
    for (RefTreeNode scan = node.getParent(); scan != null;
         scan = scan.getParent()) {
      prohibit(scan.getId(),refLeaves,refInnerNodes,backingTree);
    }
  }


  //-------------------- Apply algorithm -------------------------------

  /** Apply a reftree to another reftree. Assume node ids are identical
   * in both trees, and automatically delete subtrees in target. Equivalent
   * to <code>apply(source, target, delRoots,
   * {@link #getIdentityIdMap() getIdentityIdMap()})</code> and deletion
   * of all subtrees in target, whose roots are in <code>delRoots</code>.
   * @see #apply(RefTree,MutableRefTree,Set,RefTrees.IdMap)
   * @param source source tree
   * @param target target tree to restructure
   * @throws NodeNotFoundException if the source tree contains an invalid
   * reference.
  */
  public static void apply(RefTree source, MutableRefTree target) throws
      NodeNotFoundException {
    apply(source, target, null, getIdentityIdMap());
  }

  /** Apply a reftree to another reftree. Assume node ids are identical
   * in both trees. Equivalent to
   * <code>apply(source, target, delRoots,
   * {@link #getIdentityIdMap() getIdentityIdMap()})</code>
   * @see #apply(RefTree,MutableRefTree,Set,RefTrees.IdMap)
   * @param source source tree
   * @param target target tree to restructure
   * @param delRoots roots of subtrees to delete from target in order to
   * complete the operation (by default, deletes are not automatically run on
   * the target tree)
   * @throws NodeNotFoundException if the source tree contains an invalid
   * reference.
   */

  public static void apply(RefTree source, MutableRefTree target,
                           Set delRoots) throws
      NodeNotFoundException {
    apply(source, target, delRoots, getIdentityIdMap());
  }

  // Algorithm that transforms target into source by executing move, ins del etc
  // on target

  /** Apply a reftree to another reftree. The process of applying a source
   * tree to a target tree means that the target tree is reshaped to be
   * isomorphic to the source tree. The reshaping takes place through the
   * operations of the {@link MutableRefTree} interface.
   * The apply algorithm recognizes
   * {@link RefTrees.IdenticalChildListIterator} child lists, and will never
   * traverse these.
   *
   * <p>In other words, the apply algorithm deduces a set of edit operations
   * to perform on the target tree in order to make it identical (except
   * for reference expansions) to the source tree.
   *
   * <p>For instance, consider the target and source trees
   <table><tr><th>Target</th><th>Source</th></tr>
   <tr valign="top"><td><pre>
   &nbsp; r
   &nbsp;/ \
   a   c
   |   |
   b   d
   &nbsp;   |
   &nbsp;   e
   </pre></td><td><pre>
   &nbsp; r
   &nbsp;/ \
   a'  c'
   |
   d*
   </pre></td><tr></table>
   * where tree references are marked with a * (d* is a tree reference to d and
   * node references are marked with a ' (the prime sign)
   * <p>Applying <code>source</code> to <code>target</code> would in this
   * case yield e.g. the following operations <code>move(d,a), delete(b)</code>
   * to be performed on <code>target</code>.
   *
   * <p>Note that the edit script may
   * be very short and fast to execute, although the target tree may contain
   * a vast amount of nodes. Also note that parts of the targer tree that are
   * refrenced by tree references from the source tree need not be traversed.
   *
   * @param source source tree
   * @param target target tree to restructure
   * @param delRoots roots of subtrees to delete from target in order to
   * complete the operation (by default, deletes are not automatically run on
   * the target tree)
   * @param idMap map of node ids between source and target
   * @throws NodeNotFoundException if the source tree contains an invalid
   * reference.
   */
  public static void apply(RefTree source, MutableRefTree target, Set delRoots,
                           IdMap idMap) throws
      NodeNotFoundException {
    Set deletia = delRoots == null ? new HashSet() : delRoots;
    apply(source, target, source.getRoot(), deletia, idMap);
    if( delRoots == null ) {
      // Autoclean
      for (Iterator i = deletia.iterator(); i.hasNext(); )
        target.delete( (String) i.next());
    }
  }

  protected static void apply(RefTree newTree, MutableRefTree target,
                              RefTreeNode currentNewNode,
                              Set delRoots, IdMap newToThis) throws
      NodeNotFoundException {
    String currentLocationN = currentNewNode.getId();
    String currentLocationT = newToThis.getDestId(currentNewNode);

    if (currentNewNode.isTreeRef()) {
      //Log.log("Reftree @ "+currentLocation+", stopping recursion.",Log.INFO);
      return;
    }
    // Reconcile content. ///Q = old update code
    // NOTE: We treat the id and content of a node separately (structops
    // and contentops). This will lead to an anomaly when renaming and moving
    // a file in a name-based tree: two mv (renameTo) operations will be
    // executed, the first moving the file and the latter changing the
    // name. This is no problem; just an interesting observation.
    // NOTE2: This goes for INS nodes as well. First they are inserted, then updated.
    // We probably want to fix this!
    if (!currentNewNode.isReference())
      target.update(currentLocationT, currentNewNode.getContent());
      //Log.log("Reconciling "+getPath(currentLocation),Log.INFO);

      // Build useful map children of current node
    Map newNodeChildren = new HashMap();
    {
      Iterator i = currentNewNode.getChildIterator();
      if( i == IDENTICAL_CHILDLIST ) {
//        Log.log("Skipping identicla child list...",Log.INFO);
        return; // Child list matches target tree
      }
      for (;i.hasNext(); ) {
        RefTreeNode child = (RefTreeNode) i.next();
        newNodeChildren.put(child.getId(), child);
      }
    }
    // Check for deleteted, moved away and updated nodes, and add these to delRoots
    Set visitedChildren = new HashSet(); // Contains new/src ids
    // IMPORTANT since dest id's may be allocated on the fly
    RefTreeNode current = target.getNode(currentLocationT); //  Never null, since current loc always in both trees
    for (Iterator i = current.getChildIterator(); i.hasNext(); ) {
      RefTreeNode cchild = (RefTreeNode) i.next();
      String childSrcId = newToThis.getSrcId(cchild);
      if (!newNodeChildren.containsKey(childSrcId)) {
        delRoots.add(cchild.getId()); // Tentative delete. May also have been moved somewhere...
        ///tentativeDelete(cchild.getId());
      } else
        visitedChildren.add(childSrcId);
    }
    // Check for inserted and moved move here nodes
    for (Iterator i = newNodeChildren.values().iterator(); i.hasNext(); ) {
      RefTreeNode newNode = (RefTreeNode) i.next();
      String newIdT = newToThis.getDestId(newNode);
      if (visitedChildren.contains(newNode.getId()))
        continue; // Already processed
      String oldLocation = target.contains(newIdT) ? target.getParent(newIdT) : null;
      if (oldLocation == null) {
        if( newNode.isReference() )
          // Trying to insert a reference; not possible since a refernce MUST
          // exist in the target tree
          throw new NodeNotFoundException(newNode.getId());
        target.insert(currentLocationT, newIdT, newNode.getContent());
      } else if (!oldLocation.equals(currentLocationT)) {
        // HMM: Is it possible that we get to move here, and *after* that
        // add to delRoots?
        // That should not be possible, since we won't see the moved here
        // node when looping at the source -> it won't appear to be deleted
        if (delRoots.contains(newIdT)) {
//          Log.log("Found tentative delete: " + newIdT, Log.INFO);
          delRoots.remove(newIdT);
//        resurrect(newIdT);
        }
        target.move(newIdT, currentLocationT); // Move here case
      } /*else {
        // This is a case that makes me uncomfortable
        // What happens is that a destId is found at the current location, which
        // was not seen while looping at the dest list a second ago
        // It means a node we thought was no longer used in fact will be
        // re used. It seems that it is best to treat this as a resurrection
        // Happens when a same-named file is deleted and removed
        // A better way would probably be to control IdMap allocations
        // more precisely by some enterdir/exitdir calls
        delRoots.remove(newIdT);
///      resurrect(newIdT);
        Log.log("IdMap resurrected " + newIdT, Log.FATALERROR);
      }*/
        else
        Log.log("Should not get here (node should be in visitedChildren)"+
                "oldloc="+oldLocation+", newloc="+currentLocationT+
                ",newIdN="+newNode.getId()+", visitedNodes="+visitedChildren,
                Log.FATALERROR); // path matches exactly*/
    }

    // Recurse on newTree child list, as it should now be identical to the
    // child list of this tree (except for the nodes that are to be moved
    // away)

    for (Iterator i = newNodeChildren.values().iterator(); i.hasNext(); ) {
      apply(newTree, target, (RefTreeNode) i.next(), delRoots, newToThis);
    }
  }

  /** Get identity idmap instance. Returns an idmap that maps node ids
   * onto themselves.
   *
   * @return identity id map
   */
  public static IdMap getIdentityIdMap() {
    return IdentityIdMap.getInstance();
  }


  /** Two-way map for node ids. This class provides a base for mapping ids
   * between two trees, namely the <code>source</code> and
   * <code>destination</code> trees.
   */

  public static abstract class IdMap {

    /** Get destination id.
     *
     * @param srcId id of node in the source tree
     * @param src source node. May be unavailable, in which case
     * this parameter should be set to <code>null</code>
     * @throws NodeNotFoundException if mapping fails
     * @return id in the destination tree.
     */
    public abstract String getDestId(String srcId, RefTreeNode src) throws
        NodeNotFoundException;

    /** Get source id.
     *
     * @param dstId id of node in the destination tree
     * @param dest destination node. May be unavailable, in which case
     * this parameter should be set to <code>null</code>
     * @throws NodeNotFoundException if mapping fails
     * @return id in the source tree.
     */

    public abstract String getSrcId(String dstId, RefTreeNode dest) throws
        NodeNotFoundException;

    /** Get destination id. Equivalent to
     * <code>getDestId(srcId,null)</code>.
     * @see #getDestId(String,RefTreeNode)
     *
     * @param srcId id of node in the source tree
     * @throws NodeNotFoundException if mapping fails
     * @return id in the destination tree.
     */

    public final String getDestId(String srcId) throws
        NodeNotFoundException {
      return getDestId(srcId,null);
    }

    /** Get source id. Equivalent to
     * <code>getSrcId(dstId,null)</code>.
     * @see #getSrcId(String,RefTreeNode)
     *
     * @param dstId id of node in the destination tree
     * @throws NodeNotFoundException if mapping fails
     * @return id in the source tree.
     */

    public final String getSrcId(String dstId) throws
        NodeNotFoundException {
      return getSrcId(dstId,null);
    }

    /** Get destination id. Equivalent to
     * <code>getDestId(src.getId(),src)</code>.
     * @see #getDestId(String,RefTreeNode)
     *
     * @param src source node
     * @throws NodeNotFoundException if mapping fails
     * @return id in the destination tree.
     */

    public final String getDestId(RefTreeNode src) throws
        NodeNotFoundException {
      return getDestId(src.getId(),src);
    }

    /** Get source id. Equivalent to
     * <code>getSrcId(dest.getId(),dest)</code>.
     * @see #getSrcId(String,RefTreeNode)
     *
     * @param dest destination node.
     * @throws NodeNotFoundException if mapping fails
     * @return id in the source tree.
     */
    public final String getSrcId(RefTreeNode dest) throws
        NodeNotFoundException {
      return getSrcId(dest.getId(),dest);
    }

  }

  private static final class IdentityIdMap extends IdMap {

    private static IdMap map = new IdentityIdMap();

    private IdentityIdMap() {
    }

    public static IdMap getInstance() {
      return map;
    }

    public final String getDestId(String srcId, RefTreeNode src) throws NodeNotFoundException {
      return srcId;
    }

    public final String getSrcId(String dstId,  RefTreeNode dest) throws NodeNotFoundException {
      return dstId;
    }
  }


  // Return single-node reftree with root = <ref:tree id="#id-of-argtree" />

  /** Get a single-node tree reference reftree. Return a reftree
   * consisting of a single tree reference to the root of the given tree.
   * @param t reftree, whose root should be referenced
   * @return a single node reference tree to the root of <code>t</code>
   */
  public static IdAddressableRefTree getRefTree( RefTree t ) {
    return getRefTree(t.getRoot().getId());
  }

  // Return single-node reftree with root = <ref:tree id="#rootId arg" />

  /** Get a single-node tree reference reftree. Return a reftree
   * consisting of a single tree reference to the target given by
   * <code>rootId</code>.
   *
   * @param rootId target id
   * @return a signle node reference tree
   */

  public static IdAddressableRefTree getRefTree( final String rootId ) {
    return getAddressableTree(new RefTreeImpl(
        new RefTreeNodeImpl(null,rootId,true,null)));
  }

  /** Make reftree mutable. Returns mutable variant of the given tree.
   * If the tree is already mutable, the tree itself is returned.
   * Otherwise it is suitably wrapped to provide mutability.
   *
   * @param t tree to make mutable
   * @return mutable variant of <code>t</code>
   */
  public static MutableRefTree getMutableTree( RefTree t) {
    return t instanceof MutableRefTree ? (MutableRefTree) t:
        new ChangeTree(getAddressableTree(t));
  }

  /** Make reftree id-adressable. Returns id-addressable variant of the
   * given tree. If the tree is already id-addressable, the tree itself is
   * returned; otherwise it is suitably wrapped to provide addressability.
   *
   * @param t tree to make addressable
   * @return addressable variant of <code>t</code>
   */
  public static IdAddressableRefTree getAddressableTree( RefTree t) {
    class AddressableTree extends IdAddressableRefTreeImpl {

      private Map index = new HashMap();
      private RefTree t;

      public AddressableTree(RefTree t) {
        this.t = t;
        init(t.getRoot());
      }

      public RefTreeNode getNode(String id) {
        return (RefTreeNode) index.get(id);
      }

      public RefTreeNode getRoot() {
        return t.getRoot();
      }

      private void init(RefTreeNode root) {
        if( index.put(root.getId(),root) != null )
          Log.log("Duplicate id "+root.getId(),Log.ASSERTFAILED);
        for(Iterator i = root.getChildIterator();i.hasNext();)
          init((RefTreeNode) i.next());
      }
    }
    return t instanceof IdAddressableRefTree ?  // Don't build unneccesary indexes
        (IdAddressableRefTree) t :  new AddressableTree(t);
  }

  /** Interface for node content that has a unique id. */
  public interface IdentifiableContent {
    /** Get id of content.
     *
     * @return id of content
     */
    public String getId();
  }

  /** An infinite set. Note that only the
   * <code>contains()</code> method in the  set works.
   */

  public static final Set INFINITE_SET = new InfiniteSet();

  /** Return the complementary set for a given set. Note that only the
   * <code>contains()</code> method in the returned set works.
   *
   * @param s set to complement
   * @return the complement of s
   */
  public static Set getComplement(Set s) {
    return new ComplementSet(s);
  }

  /** Combine two reftrees. Combines two reftrees <code>aRb</code> and
   * <code>bRc</code> into <code>aRc</code>, where
   * <code><i>x</i>R<i>y</i></code> means a tree <i>x</i> that references
   * another tree <i>y</i>.
   *
   * @param aRb RefTree with references to the tree <code>bRc</code>
   * @param bRc RefTree with references to the tree <code>c</code>
   * @param c The tree <code>c</code>
   * @throws NodeNotFoundException if an invalid reference is encountered
   * @return RefTree <code>aRc</code>, i.e. a tree equivalent to
   * <code>aRb</code>, but with references to the tree <code>c</code>.
   */
  public static RefTree combine(RefTree aRb, RefTree bRc,
                                final IdAddressableRefTree c) throws
      NodeNotFoundException {
    final IdAddressableRefTree bRcAddr = RefTrees.getAddressableTree(bRc);
    Set[] allowedTrees = RefTrees.normalize(c, new RefTree[] {aRb,
                                            bRcAddr});
    //Log.log("Allowed treerefs are "+allowedTrees[0],Log.INFO);
    return RefTrees.expandRefs(aRb, allowedTrees[0],
                               new BaseSet() {
      public boolean contains(Object o) {
        return RefTrees.isReferenced( (String) o, bRcAddr,c);
      }
    }, c);
  }


  // Check if id is referenced from t, also considering nodes inside
  // treerefs to backingtree. NOTE: returns false if id is expanded in t!
  private static boolean isReferenced(String id, IdAddressableRefTree t,
                                IdAddressableRefTree backingTree) {
    // Try to se if parent of id reftree'd from t
    boolean checkRoot=true;
    for( RefTreeNode parent=backingTree.getNode(id);
         parent != null; parent=parent.getParent()) {
      RefTreeNode n= t.getNode(parent.getId());
      if(n!= null && (n.isTreeRef() || (checkRoot && n.isNodeRef())))
        return true;
      checkRoot=false;
    }
    return false;
  }

  /** Class for iterators returning identical child lists. The iterator
   * signifies that the child list of the node is identical to the
   * child list of the same node in the tree that is referenced by the
   * reftree of the node.<p>
   * It may be used when it
   * is unecessarily expensive to expand the child list of a node as a list of
   * reference nodes, just because the node itself has changed. In those cases,
   * this iterator may be used as the return value of the
   * {@link RefTreeNode#getChildIterator} function, the benefit being that
   * some (and eventually all...)
   * XMLR algorithms will recognize this iterator class as a token to
   * avoid visiting the child nodes. <p>
   * The class allows the use of an underlying iterator for the full child list,
   * in those cases when an XMLR algorithm does require the child list.
   */

  public static class IdenticalChildListIterator implements Iterator {
    private static final String MISSING_ITER="Full child list not available.";

    private Iterator fullListIterator;

    /** Create identical child list iterator.
     *
     * @param realListIterator Iterator for the full child list.
     * <code>null</code> may be used as a special token to signify that the
     * iterator may never be traversed (Attempts will cause
     * <code>UnsupportedOperationExceptions</code>).
     */
    public IdenticalChildListIterator(Iterator realListIterator) {
      fullListIterator = realListIterator;
    }

    public void remove() {
      if (fullListIterator == null)
        throw new UnsupportedOperationException(MISSING_ITER);
      fullListIterator.remove();
    }

    public boolean hasNext() {
      if (fullListIterator == null)
        throw new UnsupportedOperationException(MISSING_ITER);
      return fullListIterator.hasNext();
    }

    public Object next() {
      if (fullListIterator == null)
        throw new UnsupportedOperationException(MISSING_ITER);
      return fullListIterator.next();
    }
  };

  /** Constant for non-traversable unchanged child list. */
  public static final Iterator IDENTICAL_CHILDLIST =
      new IdenticalChildListIterator(null);


  private abstract static class BaseSet implements Set {

    public BaseSet() {
    }

    UnsupportedOperationException NO_OP =
        new UnsupportedOperationException("Infinite sets do not support this operation");

    public int size() {
      return Integer.MAX_VALUE;
    }

    public void clear() {
      throw NO_OP;
    }

    public boolean isEmpty() {
      return false;
    }

    public Object[] toArray() {
      throw NO_OP;
    }

    public boolean add(Object o) {
      throw NO_OP;
    }

    public abstract boolean contains(Object o);

    public boolean equals(Object o) {
      return false;
    }

    public boolean remove(Object o) {
      throw NO_OP;
    }

    public boolean addAll(Collection c) {
      throw NO_OP;
    }

    public boolean containsAll(Collection c) {
      throw NO_OP;
    }

    public boolean removeAll(Collection c) {
      throw NO_OP;
    }

    public boolean retainAll(Collection c) {
      throw NO_OP;
    }

    public Iterator iterator() {
      throw NO_OP;
    }

    public Object[] toArray(Object[] a) {
      throw NO_OP;
    }
  }

  private static class InfiniteSet extends BaseSet {
    public InfiniteSet() {
    }

    public boolean contains(Object o) {
      return true;
    }
  }

  private static class ComplementSet extends BaseSet {
    Set s;
    public ComplementSet(Set s) {
      this.s = s;
    }

    public boolean contains(Object o) {
      return !s.contains(o);
    }
  }

}
// arch-tag: df724a023466ebf71d88b1db8bd067f7 *-
