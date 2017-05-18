// $Id: Util.java,v 1.1 2010/02/23 20:31:07 tkamiya Exp $
package fc.fp.syxaw.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import fc.fp.util.xmlr.RefTree;
import fc.fp.util.xmlr.RefTreeNode;

/** Various utlity methods. This class contains utlity methods for
 * <ul>
 * <li>copying and SHA-1 digesting streams</li>
 * <li>{@link #EMPTY_ARRAY} constant</li>
 * <li>field-by-field assignment between Java beans</li>
 * <li>lookup of values within an interval from an array</li>
 * <li>a data sink</li>
 * <li>boxing and printing objects and arrays</li>
 * <li><code>null</code>-safe equals and empty String tests</li>
 * <li>quick and simple XML serialization and deserialization</li>
 * <li>GUID<->filename conversion</li>
 * <li>easy parsing of an XML input stream into a SAX2 content handler</li>
 * <li>loading a files listing name=value pairs into the system properties</li>
 * </ul>
 *  */
public class Util {

  /** Size of byte buffer used when copying streams. Currently set to {@value} */
  public static final int COPY_BUF_SIZE=4096;

  protected static LinkedList buffers = new LinkedList();

  /** Copy an input stream to an output stream.
   * @param src Source stream
   * @param dest Destination stream
   * @throws IOException if an error occurs while copying
   */
  public static int copyStream( java.io.InputStream src,
                                java.io.OutputStream dest ) throws IOException {
    return copyStream(src,dest,Long.MAX_VALUE);
  }

  /** Copy an input stream to an output stream with maximum length
  * @param src Source stream
  * @param dest Destination stream
  * @param maxLeft maximum number of bytes to copy
  * @throws IOException if an error occurs while copying
*/
  public static int copyStream( java.io.InputStream src,
                                java.io.OutputStream dest, long maxLeft )
    throws IOException {
    byte[] buffer = getBuf();
    int total = 0, count = 0;
    do {
      int maxchunk = (int) (maxLeft > buffer.length ? buffer.length : maxLeft);
      count = src.read(buffer, 0, maxchunk);
      if (count > 0) {
        dest.write(buffer, 0, count);
        total += count;
        maxLeft -= count;
      }
    }
    while (count > -1 && maxLeft > 0);
    freeBuf(buffer); // Will be gc'd if not returned, so we needn't protect by try/catch
    return total;
  }

  /** Copy an input stream to an output stream and caculate SHA-1 hash.
   * @param src Source stream
   * @param dest Destination stream
   * @return SHA-1 digest of the source stream
   * @throws IOException if an error occurs while copying
   */

  public static byte[] copyAndDigestStream(java.io.InputStream src,
                                         java.io.OutputStream dest) throws
    IOException {
    byte[] buffer = getBuf();
    int total = 0, count = 0;
    java.security.MessageDigest sha = null;
    try {
      sha = java.security.MessageDigest.getInstance("SHA");
    } catch (java.security.NoSuchAlgorithmException x) {
      Log.log("Message digest algorithm not found", Log.FATALERROR);
    }
    do {
      count = src.read(buffer);
      if (count > 0) {
        sha.update(buffer, 0, count);
        dest.write(buffer, 0, count);
        total += count;
      }
    }
    while (count > -1 /*== buffer.length*/);
    freeBuf(buffer); // Will be gc'd if not returned, so we needn't protect by try/catch
    return sha.digest();
  }

   private static synchronized byte[] getBuf() {
     if( buffers.size() > 0)
       return (byte[]) buffers.removeFirst();
     else
       return new byte[COPY_BUF_SIZE];
   }

 private static synchronized void freeBuf(byte[] buf) {
   buffers.addLast(buf);
 }

 /** Empty array constant. */
 public final static Object[] EMPTY_ARRAY = new Object[0];


 /** Find first entry in integer array, whose value is in a given range.
  * @param ar Array to perform lookup in
  * @param keystart lower limit of range, range includes this value
  * @param keyend high limit of range, range excludes this value
  * @param forbidden a value that may not be looked up.
  * @return index of entry in array, -1 if no entry found
  */

 public static int arrayLookup( int[] ar, int keystart, int keyend,
                                int forbidden ) {
   for( int i=0;ar!= null && i<ar.length;i++)
     if( ar[i] >= keystart && ar[i] < keyend && ar[i]!= forbidden ) return i;
   return -1;
 }

 /** Find first entry in integer array, whose value is in a given range.
  * @param ar Array to perform lookup in
  * @param keystart lower limit of range, range includes this value
  * @param keyend high limit of range, range excludes this value
  * @return index of entry in array, -1 if no entry found
  */

 public static int arrayLookup( int[] ar, int keystart, int keyend ) {
   for( int i=0;ar!= null && i<ar.length;i++)
     if( ar[i] >= keystart && ar[i] < keyend ) return i;
   return -1;
 }

 /** OutputStream that discards all data. <code>/dev/null</code> in Java. */

 public static class Sink extends OutputStream {
   public void write(byte[] b) {
   }

   public void write(byte[] b, int off, int len) {
   }

   public void write(int b) {
   }
 }

 // Formatting thingies

 /** Format string to given width using spaces.
  * @param s string to format
  * @param width magnitude of width of output string,
  * if &lt; 0 <code>s</code> is adjusted right.
  */

 public static String format(String s, int width) {
   return format(s, width, ' ');
 }

 /** Format string to given width.
  * @param s string to format
  * @param width magnitude of width of output string,
  * if &lt; 0 <code>s</code> is adjusted right.
  * @param filler character to use as filler.
  */

 public static String format(String s, int width, char filler) {
   StringBuffer buf = new StringBuffer();
   if (s == null)
     s = "(null)";
   int strlen = s.length();
   int fill = Math.abs(width) - strlen;
   if (fill > 0 && width < 0)
     for (; fill-- > 0; buf.append(filler))
       ;
   buf.append(s);
   if (fill > 0 && width > 0)
     for (; fill-- > 0; buf.append(filler))
       ;
   return buf.toString();
 }

 /** Null-safe equals. <code>equals(null,null)</code> is <code>true</code>. */
 public static final boolean equals( Object a, Object b) {
   if( a== null )
     return b == null;
   return a.equals(b);
 }

 /** Null-safe string emptyness test.
  * @return <code>s== null || s.length() == 0</code> */
 public static final boolean isEmpty( String s) {
   return s== null || s.length() == 0;
 }

 public static final String nullToEmpty(String s) {
   return s != null ? s : "";
 }

 /** Load a Java properties file into the system properties.
   */

  public static void loadConfiguration(String file) throws IOException {
    Properties p = new Properties();
    FileInputStream in = new FileInputStream(file);
    p.load(in);
    in.close();
    for (Iterator i = p.keySet().iterator(); i.hasNext(); ) {
      String key = (String) i.next();
      System.setProperty(key, p.getProperty(key));
    }
  }

/*
  public static void print(RefTree t ) {
    print(t,System.out);
  }

  public static void print(RefTree t, OutputStream out ) {
    try {
      PrintWriter pw = new PrintWriter(out);
      TypedXmlSerializer ser = new DefaultXmlSerializer();
      ser.setOutput(pw);
      ser.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
      XmlWriter writer = new XmlWriter(ser);
      writer.addEvent(Event.createNamespacePrefix(ReferenceEvent.REF_NS, "ref"));
      XasSerialization.writeTree(t, writer, XmlUtil.simpleBeanWriter());
      ser.flush();
    } catch (Exception ex) {
      Log.log("Cant print tree",Log.ERROR,ex);
    }
  }*/

  /** Comparator that orders short strings before longer ones. Same-length
   * strings are alphabetically ordered. May be used to e.g. sort number
   * strings */
  public static final Comparator STRINGS_BY_LENGTH = new Comparator() {
    public final int compare(Object o1, Object o2) {
      if( o1 == null )
        return -1;
      int l1= o1.toString().length();
      int l2= o2 != null ? o2.toString().length() : l1;
      return l1 != l2 ? l1-l2 : o1.toString().compareTo(o2.toString());
    }
  };

  /** Delete directory tree. Deletes the
   *  directory tree rooted at <code>f</code> including <code>f</code>.
   *
   * @param f root of tree to delete
   * @throws IOException if an I/O error occurs
   */
  public static void delTree( File f) throws IOException {
    delTree(f,true,null);
  }

  /** Delete directory tree. Deletes the
   * directory tree rooted at <code>f</code>. The directory
   * <code>f</code> itself is deleted if <code>delRoot</code> is
   * <code>true</code>.
   *
   * @param f root of tree to delete
   * @param delRoot set to <code>true</code> to delete root.
   * @throws IOException if an I/O error occurs
   */
  public static void delTree( File f, boolean delRoot)
          throws IOException {
    delTree(f,delRoot,null);
  }

  /** Delete directory tree. Deletes the
   * directory tree rooted at <code>f</code>. The directory
   * <code>f</code> itself is deleted if <code>delRoot</code> is
   * <code>true</code>.
   *
   * @param f root of tree to delete
   * @param fi filter that an entry must pass in order to be deleted
   *           (<code>null</code> means all pass)
   * @param delRoot set to <code>true</code> to delete root.
   * @throws IOException if an I/O error occurs
   */

  public static void delTree( File f, boolean delRoot, FilenameFilter fi )
          throws IOException {
    // First, delete children
    if( f.isDirectory() ) {
      File[] entries = f.listFiles();
      for(int i=0;i<entries.length;i++)
        delTree(entries[i],true,fi);
    }
    // Then delete this node
    if( delRoot && (fi==null || fi.accept(f.getParentFile(),f.getName())) &&
                    !f.delete() )
      throw new IOException("Can't delete "+f);
  }

  /** Convert byte array to hex string.
   *
   * @param v byte array to convert
   * @return hex string for the array
   */
  public static String getHexString(byte[] v) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < v.length; i++) {
      sb.append(hexDigits[ (v[i] >> 4) & 0xf]);
      sb.append(hexDigits[v[i] & 0xf]);
    }
    return sb.toString();
  }

  private static final char[] hexDigits = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8',
      '9', 'a', 'b', 'c', 'd', 'e', 'f'};

  /** Convert hex string to byte array.
   *
   * @param val hex string to convert
   * @return byte array
   */
  public static byte[] getBytesFromHexString(String val) {
    if (val.length() == 0)
      return new byte[0]; // Empty array
    if (val.length() % 2 != 0)
      throw new IllegalArgumentException("Invalid length");
    byte[] storage = new byte[val.length() / 2];
    for (int i = 0; i < val.length(); i += 2) {
      storage[i / 2] = (byte) Integer.parseInt(val.substring(i, i + 2), 16);
    }
    return storage;
  }

  // Filename, regexp and urlencoding safe names: '+'<->'-' and '/'<->'_'
  // + remove trailing = as an optimization

  /** Convert base64 encoded string to modified base64. Syxaw uses a slightly
   * modified base64 encoding for object UIDs:
   * <ul>
   * <li>The <code>+</code> sign is replaced with <code>-</code></li>
   * <li>The <code>/</code> sign is replaced with <code>_</code></li>
   * <li>Trailing <code>=</code> signs are stripped</li>
   * </ul>
   * <p>The author feels a little bit sorry for breaking the basic base64
   * encoding, but this one really fits much better in URLs and file names.
   * Sorry.
   * @param b64 base-64 encoded string
   * @return base64-modified encoded string
   */
  public static String base64toBase64Mod( String b64) {
    return b64.replace('+','-').replace('/','_').replace('=',' ').trim();
  }

  /** Convert modified base64 encoded string to  base64. See
   * {@link #base64toBase64Mod} for a description of the formats.
   * @param b64mod base64-modified encoded string
   * @return base64 encoded string
   */

  public static String baseMod64toBase64( String b64mod) {
    if (b64mod.length() % 3 == 1)
      b64mod = b64mod + "==";
    else if (b64mod.length() % 3 == 2)
      b64mod = b64mod + "=";
    return b64mod.replace('-','+').replace('_','/');
  }

  /** Print reftree statistics. Logs number of nodes, node refernces and
   tree references. */
  public static void statTree(RefTree t) {
    long counters[] = new long[3];
    statTree(t.getRoot(),counters);
    Log.log("Tree stats (total,nrefs,trefs)=",Log.INFO,Arrays.toString(counters));
  }

  protected static void statTree(RefTreeNode n, long[] counters) {
    counters[0]++;
    counters[1]+=n.isNodeRef() ? 1 : 0;
    counters[2]+=n.isTreeRef() ? 1 : 0;
    for(Iterator i = n.getChildIterator();i.hasNext();) {
      statTree((RefTreeNode) i.next(),counters);
    }
  }

  public static final String encodeStr(String s) {
    StringBuffer sb = null;
    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      if (ch < 33 || ch == '%') {
        if (sb == null) {
          sb = new StringBuffer(2*s.length());
          sb.append(s.substring(0, i));
        }
        sb.append("%" + getHexString(new byte[] {(byte) ch}));
      } else if (sb != null)
        sb.append(ch);
    }
    return sb != null ? sb.toString() : s;
  }

  public static final String decodeStr(String s) {
    StringBuffer sb = null;
    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      if (ch == '%') {
        if (sb == null) {
          sb = new StringBuffer(2*s.length());
          sb.append(s.substring(0, i));
        }
        sb.append(new String(getBytesFromHexString(s.substring(i+1,i+3))));
        i+=2;
      } else if (sb != null)
        sb.append(ch);
    }
    return sb != null ? sb.toString() : s;
  }

  /* decenc test

  public static void main(String[] args) {
    StringBuffer sb = new StringBuffer();
    for(int i=0;i<256;i++)
      sb.append((char) i);
    String s = sb.toString();
    System.out.println("Str="+s+"\nEnc="+encodeStr(s)+"\ndec(enc)="+
            decodeStr(encodeStr(s)));
    if( !s.equals(decodeStr(encodeStr(s))) )
      System.out.println("DEC/ENC FAILED!");
  }*/

  public static String[] split(String s, char ch) {
    if( s==null )
      return null;
    int count=1;
    for(int i=-1;(i=s.indexOf(ch,i+1))!=-1;)
      count++;
    String[] split= new String[count];
    int item=0;
    int start=0;
    int end=0;
    do {
      end=s.indexOf(ch,start);
      if( end == -1 ) {
        split[item]=s.substring(start);
      } else {
        split[item++]=s.substring(start,end);
        start=end+1;
      }
    } while( end != -1);
    return split;
  }

  // emulate by regexp [[:space:]]+
  public static final String[] splitWords(String s) {
    int cstart = -1, // -1 = scanning space, other = startpos of content
                 len = s.length();
    LinkedList l = new LinkedList();
    if( s == null )
      return null;
    for( int pos =0 ; pos < len; pos ++) {
      char ch = s.charAt(pos);
      if( cstart < 0 ) {
        if( ch==0x0a || Character.isSpaceChar(ch) )
          continue;
        cstart = pos;
      } else {
        if( ch!=0x0a && !Character.isSpaceChar(ch) )
          continue; // Not space, and more chars
        l.add(s.substring(cstart,pos));
        cstart=-1;
      }
    }
    if( cstart >= 0 )
      l.add(s.substring(cstart));  // Emit final token
    return (String[]) l.toArray(new String[l.size()]);
  }


  // Notifies via a call to needStream() when the underlying stream is needed

  /** An input stream that produces its data when needed. When the first read
   * of the stream is encountered, the stream starts a data producer thread
   * and calls {@link #stream stream()} from within this thread.
   * <p<Override the {@link #stream stream} method to implement your own
   * stream data producer.
   */

  public abstract static class DelayedInputStream extends InputStream {

    protected InputStream in = null;
    private boolean hasStream = false;
    private IOException delayedEx = null;

    protected boolean needStream(boolean needsData) throws IOException {
      final PipedOutputStream out = new PipedOutputStream();
      in = new PipedInputStream(out);
      (new Thread() {
        public void run() {
          try {
            stream(out);
          } catch (IOException x) {
            Log.log("IOExcept in delayed producer, bubbling up...", Log.ERROR,
                    x);
            DelayedInputStream.this.delayedEx = x;
          } finally {
            try {
              out.close();
            } catch (IOException x) {
              ;
              /*Intentional*/
            }
          }
        }
      }).start();
      return true;

    }

    /** Produce stream data. Called whn somebody attempts to read from this
     * stream. The method is called in a seprarate producer thread that is
     * connected to this stream via piped streams.
     *
     * @param out stream to write produced data to.
     * @throws IOException if an I/O error occurs
     */
    protected abstract void stream(OutputStream out) throws IOException;

    /** Called when the stream data is no logner needed. */
    protected void streamDone() {}

    public int available() throws IOException {
      checkException();
      if (!hasStream)
        hasStream = needStream(false);
      return in.available();
    }

    // This op won't trigger creation!
    public void close() throws IOException {
      checkException();
      streamDone();
      if (in != null)
        in.close();
    }

    public synchronized void mark(int readlimit) {
      try {
        if (!hasStream)
          hasStream = needStream(false);
      } catch (IOException x) {
        delayedEx = x;
        return;
      }
      in.mark(readlimit);
    }

    public boolean markSupported() {
      try {
        if (!hasStream)
          hasStream = needStream(false);
      } catch (IOException x) {
        delayedEx = x;
        return false;
      }
      return in.markSupported();
    }

    public int read() throws IOException {
      checkException();
      if (!hasStream)
        hasStream = needStream(true);
      return in.read();
    }

    public int read(byte[] b) throws IOException {
      checkException();
      if (!hasStream)
        hasStream = needStream(true);
      return in.read(b);
    }

    public int read(byte[] b, int off, int len) throws IOException {
      checkException();
      if (!hasStream)
        hasStream = needStream(true);
      return in.read(b, off, len);
    }

    public synchronized void reset() throws IOException {
      checkException();
      if (!hasStream)
        hasStream = needStream(false);
      in.reset();
    }

    public long skip(long n) throws IOException {
      checkException();
      if (!hasStream)
        hasStream = needStream(false);
      return in.skip(n);
    }

    final private void checkException() throws IOException {
      if (delayedEx != null) {
        IOException ex = delayedEx;
        delayedEx = null;
        throw ex;
      }
    }
  }

  /** Class for holding an object. Convenience methods for accessing the
   * expected type*/

  public static class ObjectHolder {
    private Object o;

    public ObjectHolder(Object o) {
      this.o = o;
    }

    public ObjectHolder(boolean b) {
      o = new Boolean(b);
    }

    public Object get() {
      return o;
    }
    public boolean booleanValue() {
      return ((Boolean) o).booleanValue();
    }

    public int intValue() {
      return ((Integer) o).intValue();
    }

    public String stringValue() {
      return (String) o;
    }

    public void set(Object o) {
      this.o = o;
    }
  }
}
// arch-tag: 5e23e83b11b1f121c2a136669b69a7e1 *-
