package fc.fp.util.xas;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

public class XSDateTime {

  private int m_year;
  private int m_month;
  private int m_day;
  private int m_hour;
  private int m_min;

  private BigDecimal m_sec;

  private XSTimeZone m_timeZone;

  private static final java.text.DecimalFormat m_yearFormat = new java.text.DecimalFormat( "0000");
  private static final java.text.DecimalFormat m_monthFormat = new java.text.DecimalFormat( "00");
  private static final java.text.DecimalFormat m_dayFormat = new java.text.DecimalFormat( "00");
  private static final java.text.DecimalFormat m_hourFormat = new java.text.DecimalFormat( "00");
  private static final java.text.DecimalFormat m_minFormat = new java.text.DecimalFormat( "00");
  private static final java.text.DecimalFormat m_secFormat = new java.text.DecimalFormat( "00");
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof XSDateTime) {
      return fieldWiseEqual((XSDateTime)obj);
    }
    return false;
  }

  private boolean fieldWiseEqual(XSDateTime d){
    if (m_year == d.m_year && m_month == d.m_month && m_day  == d.m_day &&
        m_hour  == d.m_hour && m_min == d.m_min) {
      if (m_sec != null && m_sec.compareTo(d.m_sec) == 0 || m_sec == null && d.m_sec == null) {
        if (m_timeZone != null && m_timeZone.equals(d.m_timeZone) ||
          m_timeZone == null && d.m_timeZone == null) {
          return true;
        }
      }
    }
//    System.out.println(this.toString());
//    System.out.println(d.toString());
    return false;
  }
  
  public static final XSTimeZone UTC = new XSTimeZone(0);
    static final java.util.TimeZone TZ_UTC = new java.util.SimpleTimeZone(0, "UTC");
    
  public static final int NOT_SPECIFIED = Integer.MIN_VALUE;

  public XSDateTime() {
    m_year     = NOT_SPECIFIED;
    m_month    = NOT_SPECIFIED;
    m_day      = NOT_SPECIFIED;
    m_hour     = NOT_SPECIFIED;
    m_min      = NOT_SPECIFIED;
    m_sec      = null;
    m_timeZone = null;
  }

  private XSDateTime(int Y, int M, int D, int hh, int mm, BigDecimal ss, XSTimeZone tz) {
    this();
    m_year = Y;
    m_month = M;
    m_day = D;
    m_hour = hh;
    m_min = mm;
    m_sec = ss;
    m_timeZone=tz;
  }

  public XSDateTime(XSDateTime dateTime) {
      this(dateTime.m_year, dateTime.m_month, dateTime.m_day,
           dateTime.m_hour, dateTime.m_min, dateTime.m_sec,
           dateTime.m_timeZone != null ? (XSTimeZone)dateTime.m_timeZone.clone() : null);
  }

  public XSDateTime(Calendar calendarInstance) {
      this();
      set(java.util.Calendar.YEAR, calendarInstance.get(Calendar.YEAR));
      set(java.util.Calendar.MONTH, calendarInstance.get(Calendar.MONTH) + 1);
      set(java.util.Calendar.DAY_OF_MONTH, calendarInstance.get(Calendar.DAY_OF_MONTH));
      set(java.util.Calendar.HOUR_OF_DAY, calendarInstance.get(Calendar.HOUR_OF_DAY));
      set(java.util.Calendar.MINUTE, calendarInstance.get(Calendar.MINUTE));
      final BigInteger milliSec = BigInteger.valueOf(
          calendarInstance.get(Calendar.SECOND) * 1000 +
          calendarInstance.get(Calendar.MILLISECOND));
      setSec(new BigDecimal(milliSec, 3));
      setTimeZone(new XSTimeZone(calendarInstance.getTimeZone().getRawOffset()));
  }

  /**
     * Returns year value.
     * @return year value if available, otherwise returns NOT_SPECIFIED.
     */
  public int getYear() {
      return m_year;
  }

  /**
     * Returns month value.
     * @return month value if available, otherwise returns NOT_SPECIFIED.
     */
  public int getMonth() {
      return m_month;
  }
  
  /**
     * Returns day value.
     * @return day value if available, otherwise returns NOT_SPECIFIED.
     */
  public int getDay() {
      return m_day;
  }

  /**
     * Returns hour value.
     * @return hour value if available, otherwise returns NOT_SPECIFIED.
     */
  public int getHour() {
      return m_hour;
  }

  /**
     * Returns minute value.
     * @return minute value if available, otherwise returns NOT_SPECIFIED.
     */
  public int getMinute() {
      return m_min;
  }

  /**
     * Returns second value.
     * @return second value if available, otherwise returns null.
     */
  public BigDecimal getSecond() {
      return m_sec;
  }

  public void set( int field, int value) {
    switch( field) {
    case java.util.Calendar.YEAR:
      m_year = value;
      return;
    case java.util.Calendar.MONTH:
      m_month = value;
      return;
    case java.util.Calendar.DAY_OF_MONTH:
      m_day = value;
      return;
    case java.util.Calendar.HOUR_OF_DAY:
      m_hour = value;
      return;
    case java.util.Calendar.MINUTE:
      m_min = value;
      return;
    }
    throw new java.lang.IllegalArgumentException();
  }

  public void setSec(BigDecimal value) {
      m_sec = value;
  }

  public boolean isSet(int field) {
    switch( field) {
    case java.util.Calendar.YEAR:
      return m_year != NOT_SPECIFIED;
    case java.util.Calendar.MONTH:
      return m_month != NOT_SPECIFIED;
    case java.util.Calendar.DAY_OF_MONTH:
      return m_day != NOT_SPECIFIED;
    case java.util.Calendar.HOUR_OF_DAY:
      return m_hour != NOT_SPECIFIED;
    case java.util.Calendar.MINUTE:
      return m_min != NOT_SPECIFIED;
    case java.util.Calendar.SECOND:
      return m_sec != null;
    }
    throw new java.lang.IllegalArgumentException();
  }

  public XSTimeZone getTimeZone() {
    return m_timeZone;
  }

  public void setTimeZone( XSTimeZone tz) {
    m_timeZone = tz;
  }

  public String toString() {
    /*
     *  type name    format          time zone  sign
     * -------------------------------------------------------------
     * dateTime   CCYY-MM-DDThh:mm:ss.sss    *      *
     * time      hh:mm:ss.sss         *      -
     * date      CCYY-MM-DD          *      *
     * gYearMonth  CCYY-MM            *      *
     * gYear    CCYY            *      *
     * gMonthDay  --MM-DD            *      -
     * gDay      ---DD            *      -
     * gMonth    --MM--            *      -
     *
    */
    StringBuffer buff = new StringBuffer();
    if ( m_year != NOT_SPECIFIED) {
            buff.append( m_yearFormat.format( m_year));
      if( m_month != NOT_SPECIFIED) {
                buff.append( '-');
        buff.append( m_monthFormat.format( m_month));
        if( m_day != NOT_SPECIFIED) {
                    buff.append( '-');
          buff.append( m_dayFormat.format( m_day));
          StringBuffer timeBuff = toTimeString();
          if( timeBuff.length() > 0) {
                        buff.append( 'T');
            buff.append( timeBuff.toString());
                        buff.append( toTimeZoneString().toString());
            return buff.toString();            }
                    buff.append( toTimeZoneString().toString());
          return buff.toString();          }
                buff.append( toTimeZoneString().toString());
        return buff.toString();        }
            buff.append( toTimeZoneString().toString());
      return buff.toString();      } else {
            buff.append( "-");
      if( m_month != NOT_SPECIFIED) {
                buff.append( '-');
        buff.append( m_monthFormat.format( m_month));
        if( m_day != NOT_SPECIFIED) {
                    buff.append( '-');
          buff.append( m_dayFormat.format( m_day));
                    buff.append( toTimeZoneString().toString());
          return buff.toString();          }
                buff.append( toTimeZoneString().toString());
        return buff.toString();        }
            buff.append( '-');
      if( m_day != NOT_SPECIFIED) {
                buff.append( '-');
        buff.append( m_dayFormat.format( m_day));
                buff.append( toTimeZoneString().toString());
        return buff.toString();        }
      buff.setLength( 0); 
      buff.append( toTimeString().toString());
            buff.append( toTimeZoneString().toString());
      return buff.toString();      }
  }
  
  private StringBuffer toTimeString() {
    StringBuffer buff = new StringBuffer();
    if( m_hour != NOT_SPECIFIED && m_min != NOT_SPECIFIED && m_sec != null) {
      buff.append( m_hourFormat.format( m_hour));
      buff.append( ':');
      buff.append( m_minFormat.format( m_min));
      buff.append( ':');
      m_secFormat.setMaximumFractionDigits( 2000);
      buff.append( m_secFormat.format( m_sec));
    }
    return buff;    }
  
  private StringBuffer toTimeZoneString() {
    StringBuffer buff = new StringBuffer();
    if( m_timeZone != null) {
      if( m_timeZone.equals( UTC)) {
                buff.append( 'Z');
      } else {
                long offset =  m_timeZone.getSpan();
        buff.append( ( offset >= 0)? '+': '-');
        offset = java.lang.Math.abs( offset);
        buff.append( m_hourFormat.format( offset / ( 1000 * 60 * 60)));
        buff.append( ':');
        buff.append( m_minFormat.format( (offset / ( 1000 * 60)) % 60));
      }
    }
    return buff;
  }
  
}
