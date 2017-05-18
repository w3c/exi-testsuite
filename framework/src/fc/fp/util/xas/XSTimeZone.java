package fc.fp.util.xas;

import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class XSTimeZone {

    private final int m_span;
    
    public XSTimeZone(int milliseconds) {
      m_span = milliseconds;
    }

    /**
     * Return the span in milliseconds.
     */
    public int getSpan() {
      return m_span;
    }
    
    public Object clone() {
      return new XSTimeZone(m_span);
    }
    
    public TimeZone toTimeZone() {
      return new SimpleTimeZone(m_span, "");
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof XSTimeZone && m_span == ((XSTimeZone)obj).m_span) {
        return true;
      }
      return false;
    }
    
}
