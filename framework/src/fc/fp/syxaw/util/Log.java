// $Id: Log.java,v 1.1 2010/02/23 20:31:07 tkamiya Exp $
package fc.fp.syxaw.util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.ListIterator;

/** Logger used by Syxaw. Should really be integrated with a properly
 * set up Java logger.
 * <p>You can configure the log messages with the system property
 * <code>syxaw.logfilter</code>. The syntax is {+|-}regexp[,{+|-}regexp[,...]].
 * where regexp is a regexp matching class names, and + means show, while -
 * means don't show. The last matching rule applies. The default string is
 * <code>+.*,-fc.fp.syxaw.jnfsd.nfs_.*</code>.
 * <p>The system property <code>syxaw.loglevel</code> controls the detail
 * of the log messages.
 * <table><tr><th>Level</th><th>Messages</th><tr>
 * <tr><td>0</td><td>Show ASSERTFAILED</td></tr>
 * <tr><td>1</td><td>Show FATALERROR and level 0</td></tr>
 * <tr><td>2</td><td>Show ERROR and level 1</td></tr>
 * <tr><td>3</td><td>Show WARNING and level 2</td></tr>
 * <tr><td>4</td><td>Show INFO and level 3</td></tr>
 * <tr><td>5</td><td>Show DEBUG and level 4</td></tr>
 * <tr><td>1000</td><td>Show ALL</td></tr>
 * </table>
 */


public class Log {

  public static final int ASSERTFAILED = 0;
  public static final int FP_MISSING = 1;
  public static final int FATALERROR = 2;
  public static final int ERROR = 3;
  public static final int WARNING = 4;
  public static final int INFO = 5;
  public static final int DEBUG = 6;
  public static final int TIME = 7;

  private static long firstTs = -1;

  private static Map statLogs = java.util.Collections.synchronizedMap( new HashMap() );

  private static Map debugObjs = java.util.Collections.synchronizedMap( new HashMap() );

  private static Stack contexts = new Stack();
  private static long contextId = 0l;
  private static PrintStream lout = System.out;
  private static MessageSink mout = null;

  public static final int LEVEL =
      Integer.parseInt(System.getProperty("syxaw.loglevel","1000"));

  public static final boolean QUICKSTACK =
      Boolean.valueOf(System.getProperty("syxaw.quicklog",
                                         "false")).booleanValue();

  public static final int SPLIT_LINES =
          Integer.parseInt(System.getProperty("syxaw.log.maxline",
                                         "2000000000"));

  static {
    if( System.getProperty("syxaw.log.file",null)!= null ) {
      File f = new File(System.getProperty("syxaw.log.file",null));
      try {
        lout = new PrintStream(new FileOutputStream(f));
        System.setErr(lout); // Also, grab standard streams...
        System.setOut(lout);
      }
      catch (FileNotFoundException ex) {
        System.err.println("Log init: WARNING: Cannot open "+f);
      }
    }
    if( System.getProperty("syxaw.logfilter",null) != null )
      System.err.println("Log init: WARNING: FP edition has no filters");
  }

  private static Map resmap = new HashMap(); // Map

  private synchronized static String newContext( String id ) {
    return ( contexts.isEmpty() ? "" :
        ((String) contexts.peek())+"/" )+
        id+"."+contextId++;
  }

  public static void setMessageSink(MessageSink s) {
    mout = s;
  }

  public static void putDebugObj(Object scope, String id, long value ) {
    putDebugObj(scope,id,new Long(value));
  }

  public static void putDebugObj(Object scope, String id, Object value ) {
    String key = id+"\u0000"+scope.hashCode();
    debugObjs.put(key,value);
  }

  public static long getDebugLong(Object scope, String id,boolean delete ) {
   return ((Long) getDebugObj(scope,id,delete )).longValue();
  }

  public static Object getDebugObj(Object scope, String id,boolean delete ) {
    String key = id+"\u0000"+scope.hashCode();
    if( delete )
      return debugObjs.remove(key);
    else
      return debugObjs.get(key);
  }


  public synchronized static void beginContext(String id) {
    contexts.push(newContext(id));
  }

  public synchronized static void endContext() {
    contexts.pop();
  }

  public static void stat( String log, String key1, Object val1 ) {
    stat(log,new String[] {key1},new Object[] {val1});
  }

  public static void stat( String log, String key1, Object val1,
                    String key2, Object val2 ) {
    stat(log,new String[] {key1,key2},new Object[] {val1,val2});
  }


  public static void stat( String log, String key1, Object val1,
                    String key2, Object val2,
                    String key3, Object val3 ) {
    stat(log,new String[] {key1,key2,key3},new Object[] {val1,val2,val3});
  }

  public static void stat( String log, String key1, Object val1,
                    String key2, Object val2,
                    String key3, Object val3,
                    String key4, Object val4 ) {
    stat(log,new String[] {key1,key2,key3,key4},new Object[] {val1,val2,val3,val4});
  }


  public static void stat( String log, String key1, Object val1,
                    String key2, Object val2,
                    String key3, Object val3,
                    String key4, Object val4,
                    String key5, Object val5) {
    stat(log,new String[] {key1,key2,key3,key4,key5},new Object[] {val1,val2,val3,val4,val5});
  }

  public static Map getStat(String log ) {
    return getStat(log,0);
  }

  public static Map getStat(String log, int age ) {
    Vector v = (Vector) statLogs.get(log);
    if( v!= null )
      return (Map) v.elementAt(v.size()-(age+1));
    else
      return null;
  }

  public static int getStatSize(String log ) {
    Vector v = (Vector) statLogs.get(log);
    if( v!= null )
      return v.size();
    else return 0;
  }

  public static Object getStat(String log, String key ) {
    Map m= getStat(log);
    return m!=null ? m.get(key) : null;
  }

  public static Object getStat(String log, String key, int age ) {
    Map m= getStat(log,age);
    return m!=null ? m.get(key) : null;
  }

  protected synchronized static void stat(String log,String[] keys,Object[] vals) {
      //NOFP assert( keys.length == vals.length );
    Vector stats = (Vector) statLogs.get(log);
    if( stats==null) {
      stats = new Vector();
      statLogs.put(log,stats);
    }
    Map valmap = new HashMap();
    valmap.put("_ts",new Long(System.currentTimeMillis()));
    if( !contexts.isEmpty() )
      valmap.put("_cx",contexts.peek());
    for( int i=0;i<keys.length;i++)
      valmap.put(keys[i],vals[i]);
    stats.add(valmap);
    log("Stat to "+log+": "+valmap,Log.INFO);
    //log("Log is now "+stats,Log.INFO);
  }

  public static void allocres( Object key, String text ) {
    try {
      java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
      java.io.PrintWriter pw = new java.io.PrintWriter(os);
      pw.println("MSG: " + text);
      (new Exception()).printStackTrace(pw);
      pw.flush();
      os.close();
      resmap.put(key,new String(os.toByteArray()));
    } catch (java.io.IOException x ) {
      Log.log("",Log.FATALERROR,x);
    }
  }

  public static void freeres( Object key ) {
    resmap.remove(key);
  }

  public static void dumpres() {
    System.err.println("-------ALLOC RES DUMP -------");
    for( java.util.Iterator i= resmap.entrySet().iterator();i.hasNext();) {
      Map.Entry e = (Map.Entry) i.next();
      System.err.println(e.getValue());
    }
  }

  protected Log() {
  }

  private final static String levels[] =
      {"ASSERT FAIL","FP:MISSING",
      "FATAL ERROR","E","W","I","D","T"};

  public static void log( String msg, int level) {
    log(msg,level,null,2);
  }

  public static void log( String msg, int level, Object o ) {
    log(msg,level,o,2);

  }

  public static void time( String msg ) {
    log(""+System.currentTimeMillis()+": "+msg,Log.TIME,null,2);
  }

  public static void log( String msg, int level, Object o, int stpos ) {
    if( level > LEVEL ) return;
    if( firstTs == -1 ) {
      firstTs = System.currentTimeMillis();
      log("Time zero ",Log.INFO, new Date(firstTs) );
    }
    String srcClass = "[not shown]";
    String srcMethod = "";
    String location = srcClass + "." + srcMethod;
    if( !QUICKSTACK || level <= WARNING ) {
      srcClass = "<unknown class>";
      srcMethod = "<unknown method>";
      location = srcClass + "." + srcMethod;
    }
    long ts = (System.currentTimeMillis() - firstTs)/10;
    String cmsg = Util.format(String.valueOf(ts/100),-4)+"."+
                  Util.format(String.valueOf(ts%100),2,'0') + " "+
                  levels[level] + " " +
                       /*srcClass + "." +
                       srcMethod + ": " +*/
                       msg + (o == null ? "" :
                              " " + o.toString());
    if( mout != null ) {
      lout.println(cmsg);
      mout.message(cmsg);
    } else {
      while (cmsg.length() > SPLIT_LINES) {
        int chomppos = cmsg.indexOf('\n') + 1;
        if (chomppos > SPLIT_LINES || chomppos < 1)
          chomppos = SPLIT_LINES;
        lout.println(cmsg.substring(0, chomppos));
        cmsg = cmsg.substring(chomppos);
      }
      lout.println(cmsg);
      lout.flush();
    }
    if (level <= FATALERROR) {
      (new Throwable()).printStackTrace(System.err);
      dumpres();
      System.gc();
      //try { Thread.sleep(4000); } catch (InterruptedException x ) {}
      System.exit( -1);
    }
  }

  public interface MessageSink {
    public void message(String msg);
  }

  private static List monlist = new LinkedList();
  private static Map monxlist = new HashMap();
  private static long MW_INFO   = 0x0000000100000000l;
  private static long MW_DETACH = 0x0000000200000000l;

  public static final Object memwatch(Object o) {
    monlist.add(o);
    monxlist.put(new LongHolder(MW_INFO | monlist.size() - 1), getLoc(1,5));
    return o;
  }

  public static final Object memwatch(Object o, String msg, Object detacher) {
    monlist.add(o);
    long ix = monlist.size() - 1;
    monxlist.put(new LongHolder(MW_INFO | ix ),
                 msg == null ? getLoc(1,2) : msg);
    if( detacher != null )
    monxlist.put(new LongHolder(MW_DETACH | ix ),detacher);
    return o;
  }

  private static String[] leaks = new String[64];
  private static String[] msgs = new String[64];

  // Intermediate results on memory reduction. May fluctuate wildly
  private static int[] delta_ms = new int[msgs.length];

  public static final void memstat(PrintStream pw, String className,
                                   String keyword) {
    // Implementation note: care has been taken so as to not allocate
    // stuff in the freeing loop (we cant control _mw_detach though..:)
    // This explains the long list of locals + LongHolder and other silliness
    Class[] MW_DT_ARGS = new Class[] {Object.class};
    Object[] dargs = new Object[1];
    long nid=1000000l;
    ListIterator li = monlist.listIterator();
    String msg=null;
    Object detacher=null,obj=null;
    Class lastObj=null;
    LongHolder msgK = new LongHolder(-1l);
    LongHolder detachK = new LongHolder(-1l);
    int ix=-1,k;
    long topstart = -1;
    long end = -1l;
    long freecount=0;
    long leaked=0,prevleaked=0;
    long detaches = 0;
    boolean match = false;
    boolean fwd=true;
    if( "##MWRIP##".equals(className) ) {
      Log.log("Memwatcher resource RIP",Log.INFO);
      leaks = null;
      msgs = null;
      delta_ms = null;
      monlist.clear();
      li = monlist.listIterator();
      monxlist.clear();
    }
    try {
      long start = Long.MAX_VALUE; //
      for( k=0;k<10;k++) {
        start=Math.min(start,gcloop());
        Thread.sleep(1000);
      }
      topstart = start;
      end=start; // In case of no matches
      for(;;) {
        if( fwd ? !li.hasNext() : !li.hasPrevious() ) {
          if( leaked == prevleaked || monlist.size() == 0 )
            break; // Done
          // New lap due to leaks
          /*Log.log("leaks requires one more lap (for duplicate objs), leaksnow="+
                  leaked+", newfwd=" +
                  !fwd, Log.INFO);*/
          prevleaked = leaked;
          leaked=0;
          fwd = !fwd;
          continue;
        }
        ix = fwd ? li.nextIndex() : li.previousIndex();
        obj = fwd ? li.next() : li.previous();
        if( obj instanceof WeakReference )
          obj = ((WeakReference) obj).get();
        if( obj == null )
          continue;
        msgK.set(MW_INFO + ix);
        detachK.set(MW_DETACH + ix);
        msg = (String) monxlist.get(msgK);
        detacher = monxlist.get(detachK);
        match = (className == null || obj.getClass().getName().indexOf(className) != -1) &&
              (keyword == null || (msg != null && msg.indexOf(keyword) != -1));
        if (!match)
          continue;
        lastObj = obj.getClass();
        li.set(null);
        if (detacher != null) {
          dargs[0] = obj;
          detacher.getClass().getMethod("_mw_detach", MW_DT_ARGS).invoke(detacher,
                  dargs);
          dargs[0]=null;
          detaches++;
        }
        WeakReference or = new WeakReference(obj);
        obj = null;
        end = gcloop(); // kill any weakrefs
        //Log.log("OBJ: "+freecount,Log.INFO);
        if( or.get() == null ) {
          if( freecount < msgs.length ) {
            msgs[(int) freecount] = msg;
            delta_ms[(int) freecount] = (int) (start-end);
          }
          freecount++;
        } else {
          li.set(or); // Weakref to leaked -> any other ref may free it
          if( leaked < leaks.length )
          leaks[(int) leaked]=msg;
          leaked++;
        }
        or=null;
        start=end;
      }
      for( k=0;k<5;k++) {
        end=Math.min(start,gcloop());
        Thread.sleep(250);
      }
    }  catch( java.lang.Exception ex) {
      Log.log("Excepted (most likely on detach)",Log.INFO,ex);
      pw.println("Excepted (most likely on detach).");
    }
    if( msgs == null )
      pw.println("Memory Watcher has been GCd!");
    String line = "No objects freed. (freeMem="+(topstart-end)+", used="+end+")";
    if( lastObj != null ) {
      line = lastObj.getName()+", freeMem="+(topstart-end)+
             ", count="+freecount+", detached="+detaches+", leaked="+leaked+
             ", used="+end;
    }
    pw.println(line);
    for(int i=0;msgs!=null && i<freecount && i<msgs.length;i++) {
      if( msgs[i] != null )
        pw.println(Util.format("M"+i,-5,' ')+" "+
                   Util.format(""+delta_ms[i],-6,' ')+": "+
                   msgs[i]);
    }

    for(int i=0;leaks != null && i<leaked && i<leaks.length;i++) {
      if( leaks[i] != null )
        pw.println(Util.format("L"+i,-5,' ')+": "+leaks[i]);
    }

    /*
    while( monlist.size() > 0 ) {
        Object obj = monlist.remove(0);
        WeakReference or = new WeakReference(obj);
        long start = gcloop();
        obj = null;
        long end = gcloop();
        do {
            if( start != end || or.get()!= null )
                break;
            try { Thread.sleep(250); } catch ( InterruptedException ex){};
            end = gcloop();
        } while( true );
        String line =
              (id.hasNext() ? id.next().toString() : String.valueOf(nid++) ) +
              ": delta_m="+(start-end)+
                (or.get()==null ? "" : ", leaked.");
        pw.println(line);
    }*/

  }

  private static class LongHolder {
    long l;

    public LongHolder( long l) {
      this.l = l;
    }

    public int hashCode() {
      return (int)(l ^ (l >>> 32));
    }

    public void set(long l) {
      this.l = l;
    }

    public boolean equals(Object o) {
      return ((o instanceof LongHolder) && ((LongHolder) o).l == l);
    }

  }

  private static final long gcloop() {
    System.runFinalization();
    Thread.yield();
    for (long usedmem = usedMemory(); ; ) {
      //System.runFinalization();
      //Thread.yield();
      System.gc();
      Thread.yield();
      long usedmem2 = usedMemory();
      if (usedmem2 == usedmem) // Also stop on heap increase
        return usedmem;
      usedmem = usedmem2;
    }
  }

  public static final long usedMemory(String loc) {
    long mem=Long.MAX_VALUE;
    try {
      for (int k = 0; k < 1; k++) {
        mem = Math.min(mem, gcloop());
        if( k > 0 )
          Thread.sleep(1000);
      }
    } catch ( InterruptedException ex) { /* Delib empty */ }
    log("Used memory: "+Util.format(""+mem,-9,' ')+" "+loc,ERROR);
    return mem;
  }

  private static long usedMemory() {
    // as suggested by jkangash
    return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
  }

  private static final StringWriter stackOut = new StringWriter(1024);
  private static final PrintWriter stackWr = new PrintWriter(stackOut);

  public static final String getLoc(int offset, int maxdepth) {
    offset+=0; // Skip this method
    maxdepth+=2;
    String lastStep="";
    String steps="";
    stackOut.getBuffer().setLength(0);
    (new Throwable()).printStackTrace(stackWr);
    stackWr.flush();
    int depth=-1;
    StringBuffer buf = stackOut.getBuffer();
    int wordStart = -1;
    for( int pos=0;pos < buf.length() && depth < maxdepth ;pos++) {
      char c = buf.charAt(pos);
      boolean atlb = c == '\n' | pos==buf.length()-1;
      if( atlb ) {
        depth++;
        //Log.log("atlb="+((int) c),Log.INFO);
        wordStart = -1;
      }
      if( Character.isWhitespace(c) ) {
        wordStart = -1;
        continue;
      }
      if( wordStart < 0 && !Character.isWhitespace(c) ) {
        wordStart = pos;
        continue;
      }
      if( c == '(' && (depth > offset) ) {
        String step = buf.substring(wordStart, pos);
        steps += (lastStep.length() > 0 ? "," : "")+rpath(lastStep,step);
        lastStep = step;
      }
    }
    return steps;
  }

  public static final String rpath(String p1, String p2) {
    int match=0;
    while (match < Math.min(p1.length(), p2.length()) &&
           p1.charAt(match) == p2.charAt(match) ) {
      match++;
    }
    String res =  match-1 > 0 ? "#"+p2.substring(match) : p2;
    return res;
  }

  public static final void setOut( PrintStream out ) {
    lout = out;
  }
  public static void main(String[] args) {
    System.out.println(getLoc(0,100));
    foo();
  }
  public static void foo() {
    System.out.println(getLoc(0,100));
    bar();
  }
  public static void bar() {
    System.out.println(getLoc(0,0));
  }


}
// arch-tag: f35689b1572ba5df1863c94d88994cfe *-
