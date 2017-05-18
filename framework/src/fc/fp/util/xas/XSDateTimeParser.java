package fc.fp.util.xas;

import java.math.BigDecimal;

public final class XSDateTimeParser {
  
  private int m_year, m_month, m_day, m_hour, m_min, m_tz;
  private BigDecimal m_sec;
  private boolean m_hasTimeZone;
  private static final int DIGITCHARBUF_SZ = 64;
  private final char[] m_digitchars;
  
  XSDateTimeParser() {
      m_digitchars = new char[DIGITCHARBUF_SZ];
  }
  
  public XSDateTime parseDateTime(String src)  {
      
      int pos = 0;
      
      final int len;
      if ((len = src.length()) == 0)
        return null;

      posLoop1:
      for (; pos < len; pos++) {
        switch (src.charAt(pos)) {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
          default:
            break posLoop1;
        }
      }

      int npos;
      if ((npos = getYear(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int year = m_year;
      
      if ((npos = getMonth(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int month = m_month;
      
      if ((npos = getDay(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int day = m_day;
      
      if ((npos = getHour(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int hour = m_hour;

      if ((npos = getMin(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int min = m_min;

      if ((npos = getSec(src, pos, len)) == pos)
        return null;
      pos = npos;
      final BigDecimal sec = m_sec;
      
      
      if (pos == len) {
        m_hasTimeZone = false;
      }
      else {
        final char c;
        
        if ((c = src.charAt(pos)) == 'Z') {
          if (len == ++pos) {
            m_tz = 0;
            m_hasTimeZone = true;
          }
          else
            return null;
        }
        else if (!Character.isWhitespace(c)) {
          
          int sign = 1;
          switch (src.charAt(pos)) {
            case '-':
              sign = -1;
            case '+':
              pos++; 
              break;
            default:
              return null;
          }
          if ((npos = getHour(src, pos, len)) == pos)
            return null;
          pos = npos;
          if ((npos = getMin(src, pos, len)) == pos)
            return null;
          pos = npos;
          m_tz = sign * (( m_hour * 60 * 60 * 1000) + ( m_min * 60 * 1000));
          m_hasTimeZone = true;
        }
      }

      for (; pos < len; pos++) {
        switch (src.charAt(pos)) {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
          default:
            return null;
        }
      }
      
      if (hour == 24 && (min != 0 || m_sec.compareTo(BigDecimal.ZERO) != 0)) {
          return null;
      }
      
      m_year = year;
      m_month = month;
      m_day = day;
      m_hour = hour;
      m_min = min;
      m_sec = sec;
      
      XSDateTime dateTime = new XSDateTime();
      dateTime.set(java.util.Calendar.YEAR, m_year);
      dateTime.set(java.util.Calendar.MONTH, m_month);
      dateTime.set(java.util.Calendar.DAY_OF_MONTH, m_day);
      dateTime.set(java.util.Calendar.HOUR_OF_DAY, m_hour);
      dateTime.set(java.util.Calendar.MINUTE, m_min);
      dateTime.setSec(m_sec);
      if (m_hasTimeZone) {
          dateTime.setTimeZone(new XSTimeZone(m_tz));
      }
      return dateTime;
  }
  
  public XSDateTime parseDate(String src) {
      
      int pos = 0;
      
      final int len;
      if ((len  = src.length()) == 0)
        return null;

      posLoop1:
      for (; pos < len; pos++) {
        switch (src.charAt(pos)) {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
          default:
            break posLoop1;
        }
      }

      int npos;
      if ((npos = getYear(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int year = m_year;
      
      if ((npos = getMonth(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int month = m_month;
      
      if ((npos = getDay(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int day = m_day;
      
      
      if (pos == len) {
        m_hasTimeZone = false;
      }
      else {
        final char c;
        
        if ((c = src.charAt(pos)) == 'Z') {
          if (len == ++pos) {
            m_tz = 0;
            m_hasTimeZone = true;
          }
          else
            return null;
        }
        else if (!Character.isWhitespace(c)) {
          
          int sign = 1;
          switch (src.charAt(pos)) {
            case '-':
              sign = -1;
            case '+':
              pos++; 
              break;
            default:
              return null;
          }
          if ((npos = getHour(src, pos, len)) == pos)
            return null;
          pos = npos;
          if ((npos = getMin(src, pos, len)) == pos)
            return null;
          pos = npos;
          m_tz = sign * (( m_hour * 60 * 60 * 1000) + ( m_min * 60 * 1000));
          m_hasTimeZone = true;
        }
      }

      for (; pos < len; pos++) {
        switch (src.charAt(pos)) {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
          default:
            return null;
        }
      }
      
      m_year = year;
      m_month = month;
      m_day = day;

      XSDateTime dateTime = new XSDateTime();
      dateTime.set(java.util.Calendar.YEAR, m_year);
      dateTime.set(java.util.Calendar.MONTH, m_month);
      dateTime.set(java.util.Calendar.DAY_OF_MONTH, m_day);
      if (m_hasTimeZone) {
        dateTime.setTimeZone(new XSTimeZone(m_tz));
      }
      return dateTime;
  }
  
  public XSDateTime parseTime(String src) {
      
      int pos = 0;
      
      final int len;
      if ((len  = src.length()) == 0)
        return null;

      posLoop1:
      for (; pos < len; pos++) {
        switch (src.charAt(pos)) {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
          default:
            break posLoop1;
        }
      }

      int npos;
      if ((npos = getHour(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int hour = m_hour;

      if ((npos = getMin(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int min = m_min;

      if ((npos = getSec(src, pos, len)) == pos)
        return null;
      pos = npos;
      final BigDecimal sec = m_sec;
      
      
      if (pos == len) {
        m_hasTimeZone = false;
      }
      else {
        final char c;
        
        if ((c = src.charAt(pos)) == 'Z') {
          if (len == ++pos) {
            m_tz = 0;
            m_hasTimeZone = true;
          }
          else
            return null;
        }
        else if (!Character.isWhitespace(c)) {
          
          int sign = 1;
          switch (src.charAt(pos)) {
            case '-':
              sign = -1;
            case '+':
              pos++; 
              break;
            default:
              return null;
          }
          if ((npos = getHour(src, pos, len)) == pos)
            return null;
          pos = npos;
          if ((npos = getMin(src, pos, len)) == pos)
            return null;
          pos = npos;
          m_tz = sign * (( m_hour * 60 * 60 * 1000) + ( m_min * 60 * 1000));
          m_hasTimeZone = true;
        }
      }

      for (; pos < len; pos++) {
        switch (src.charAt(pos)) {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
          default:
            return null;
        }
      }
      
      m_hour = hour;
      m_min = min;
      m_sec = sec;
   
      XSDateTime dateTime = new XSDateTime();
      dateTime.set(java.util.Calendar.HOUR_OF_DAY, m_hour);
      dateTime.set(java.util.Calendar.MINUTE, m_min);
      dateTime.setSec(m_sec);
      if (m_hasTimeZone) {
          dateTime.setTimeZone(new XSTimeZone(m_tz));
      }
      return dateTime;
  }
  
  public XSDateTime parseGYearMonth(String src) {
      
      int pos = 0;
      
      final int len;
      if ((len  = src.length()) == 0)
        return null;

      posLoop1:
      for (; pos < len; pos++) {
        switch (src.charAt(pos)) {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
          default:
            break posLoop1;
        }
      }

      int npos;
      if ((npos = getYear(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int year = m_year;
      
      if ((npos = getMonth(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int month = m_month;
      
      
      if (pos == len) {
        m_hasTimeZone = false;
      }
      else {
        final char c;
        
        if ((c = src.charAt(pos)) == 'Z') {
          if (len == ++pos) {
            m_tz = 0;
            m_hasTimeZone = true;
          }
          else
            return null;
        }
        else if (!Character.isWhitespace(c)) {
          
          int sign = 1;
          switch (src.charAt(pos)) {
            case '-':
              sign = -1;
            case '+':
              pos++; 
              break;
            default:
              return null;
          }
          if ((npos = getHour(src, pos, len)) == pos)
            return null;
          pos = npos;
          if ((npos = getMin(src, pos, len)) == pos)
            return null;
          pos = npos;
          m_tz = sign * (( m_hour * 60 * 60 * 1000) + ( m_min * 60 * 1000));
          m_hasTimeZone = true;
        }
      }

      for (; pos < len; pos++) {
        switch (src.charAt(pos)) {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
          default:
            return null;
        }
      }
      
      m_year = year;
      m_month = month;

      XSDateTime dateTime = new XSDateTime();
      dateTime.set(java.util.Calendar.YEAR, m_year);
      dateTime.set(java.util.Calendar.MONTH, m_month);
      if (m_hasTimeZone) {
        dateTime.setTimeZone(new XSTimeZone(m_tz));
      }
      return dateTime;
  }
  
  public XSDateTime parseGMonthDay(String src) {
      
      int pos = 0;
      
      final int len;
      if ((len  = src.length()) == 0)
        return null;

      posLoop1:
      for (; pos < len; pos++) {
        switch (src.charAt(pos)) {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
          default:
            break posLoop1;
        }
      }

      if (src.charAt(pos++) != '-')
          return null;
      else if (pos < len) {
          if (src.charAt(pos++) != '-') {
              return null;
          }
      }
      else
          return null;
      
      int npos;
      if ((npos = getMonth(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int month = m_month;
      
      if ((npos = getDay(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int day = m_day;
      
      
      if (pos == len) {
        m_hasTimeZone = false;
      }
      else {
        final char c;
        
        if ((c = src.charAt(pos)) == 'Z') {
          if (len == ++pos) {
            m_tz = 0;
            m_hasTimeZone = true;
          }
          else
            return null;
        }
        else if (!Character.isWhitespace(c)) {
          
          int sign = 1;
          switch (src.charAt(pos)) {
            case '-':
              sign = -1;
            case '+':
              pos++; 
              break;
            default:
              return null;
          }
          if ((npos = getHour(src, pos, len)) == pos)
            return null;
          pos = npos;
          if ((npos = getMin(src, pos, len)) == pos)
            return null;
          pos = npos;
          m_tz = sign * (( m_hour * 60 * 60 * 1000) + ( m_min * 60 * 1000));
          m_hasTimeZone = true;
        }
      }

      for (; pos < len; pos++) {
        switch (src.charAt(pos)) {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
          default:
            return null;
        }
      }
      
      m_month = month;
      m_day = day;

      XSDateTime dateTime = new XSDateTime();
      dateTime.set(java.util.Calendar.MONTH, m_month);
      dateTime.set(java.util.Calendar.DAY_OF_MONTH, m_day);
      if (m_hasTimeZone) {
        dateTime.setTimeZone(new XSTimeZone(m_tz));
      }
      return dateTime;
  }

  public XSDateTime parseGYear(String src) {
      
      int pos = 0;
      
      final int len;
      if ((len  = src.length()) == 0)
        return null;

      posLoop1:
      for (; pos < len; pos++) {
        switch (src.charAt(pos)) {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
          default:
            break posLoop1;
        }
      }

      int npos;
      if ((npos = getYear(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int year = m_year;
      
      
      if (pos == len) {
        m_hasTimeZone = false;
      }
      else {
        final char c;
        
        if ((c = src.charAt(pos)) == 'Z') {
          if (len == ++pos) {
            m_tz = 0;
            m_hasTimeZone = true;
          }
          else
            return null;
        }
        else if (!Character.isWhitespace(c)) {
          
          int sign = 1;
          switch (src.charAt(pos)) {
            case '-':
              sign = -1;
            case '+':
              pos++; 
              break;
            default:
              return null;
          }
          if ((npos = getHour(src, pos, len)) == pos)
            return null;
          pos = npos;
          if ((npos = getMin(src, pos, len)) == pos)
            return null;
          pos = npos;
          m_tz = sign * (( m_hour * 60 * 60 * 1000) + ( m_min * 60 * 1000));
          m_hasTimeZone = true;
        }
      }

      for (; pos < len; pos++) {
        switch (src.charAt(pos)) {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
          default:
            return null;
        }
      }
      
      m_year = year;

      XSDateTime dateTime = new XSDateTime();
      dateTime.set(java.util.Calendar.YEAR, m_year);
      if (m_hasTimeZone) {
        dateTime.setTimeZone(new XSTimeZone(m_tz));
      }
      return dateTime;
  }
  
  public XSDateTime parseGMonth(String src) {
      
      int pos = 0;
      
      final int len;
      if ((len  = src.length()) == 0)
        return null;

      posLoop1:
      for (; pos < len; pos++) {
        switch (src.charAt(pos)) {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
          default:
            break posLoop1;
        }
      }

      if (src.charAt(pos++) != '-')
          return null;
      else if (pos < len) {
          if (src.charAt(pos++) != '-') {
              return null;
          }
      }
      else
          return null;
      
      int npos;
      if ((npos = getMonth(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int month = m_month;
      
      
      if (pos == len) {
        m_hasTimeZone = false;
      }
      else {
        final char c;
        
        if ((c = src.charAt(pos)) == 'Z') {
          if (len == ++pos) {
            m_tz = 0;
            m_hasTimeZone = true;
          }
          else
            return null;
        }
        else if (!Character.isWhitespace(c)) {
          
          int sign = 1;
          switch (src.charAt(pos)) {
            case '-':
              sign = -1;
            case '+':
              pos++; 
              break;
            default:
              return null;
          }
          if ((npos = getHour(src, pos, len)) == pos)
            return null;
          pos = npos;
          if ((npos = getMin(src, pos, len)) == pos)
            return null;
          pos = npos;
          m_tz = sign * (( m_hour * 60 * 60 * 1000) + ( m_min * 60 * 1000));
          m_hasTimeZone = true;
        }
      }

      for (; pos < len; pos++) {
        switch (src.charAt(pos)) {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
          default:
            return null;
        }
      }
      
      m_month = month;

      XSDateTime dateTime = new XSDateTime();
      dateTime.set(java.util.Calendar.MONTH, m_month);
      if (m_hasTimeZone) {
        dateTime.setTimeZone(new XSTimeZone(m_tz));
      }
      return dateTime;
  }
  
  public XSDateTime parseGDay(String src) {
      
      int pos = 0;
      
      final int len;
      if ((len  = src.length()) == 0)
        return null;

      posLoop1:
      for (; pos < len; pos++) {
        switch (src.charAt(pos)) {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
          default:
            break posLoop1;
        }
      }

      if (src.charAt(pos++) != '-')
          return null;
      else if (pos < len) {
          if (src.charAt(pos++) != '-') {
              return null;
          }
          else if (pos < len) {
              if (src.charAt(pos++) != '-') {
                  return null;
              }
          }
          else
              return null;
      }
      else
          return null;
      
      int npos;
      if ((npos = getDay(src, pos, len)) == pos)
        return null;
      pos = npos;
      final int day = m_day;
      
      
      if (pos == len) {
        m_hasTimeZone = false;
      }
      else {
        final char c;
        
        if ((c = src.charAt(pos)) == 'Z') {
          if (len == ++pos) {
            m_tz = 0;
            m_hasTimeZone = true;
          }
          else
            return null;
        }
        else if (!Character.isWhitespace(c)) {
          
          int sign = 1;
          switch (src.charAt(pos)) {
            case '-':
              sign = -1;
            case '+':
              pos++; 
              break;
            default:
              return null;
          }
          if ((npos = getHour(src, pos, len)) == pos)
            return null;
          pos = npos;
          if ((npos = getMin(src, pos, len)) == pos)
            return null;
          pos = npos;
          m_tz = sign * (( m_hour * 60 * 60 * 1000) + ( m_min * 60 * 1000));
          m_hasTimeZone = true;
        }
      }

      for (; pos < len; pos++) {
        switch (src.charAt(pos)) {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
          default:
            return null;
        }
      }
      
      m_day = day;

      XSDateTime dateTime = new XSDateTime();
      dateTime.set(java.util.Calendar.DAY_OF_MONTH, m_day);
      if (m_hasTimeZone) {
        dateTime.setTimeZone(new XSTimeZone(m_tz));
      }
      return dateTime;
  }

  private int getYear(final String src, final int initialPos, final int len) {
      
      int pos = initialPos;

      
      if (len == pos)
        return initialPos;

      char ch;
      
      
      final boolean negative;
      if ((ch = src.charAt(pos)) == '-') {
        negative = true;
        ++pos;
      }
      else if (ch == '+') {
        return initialPos;
      }
      else
        negative = false;
      
      int year = 0;
      int startPos = pos;
          
      
      boolean zero = src.charAt(startPos) == '0';

      posLoop:
      for(; pos < len; pos++){
        final char c;
        switch (c = src.charAt(pos)){
          case '-':
          case '+':
          case 'Z':
            break posLoop;
          case '0' :
          case '1' :
          case '2' :
          case '3' :
          case '4' :
          case '5' :
          case '6' :
          case '7' :
          case '8' :
          case '9' :
            year = 10 * year + (c - '0');
            break;
          default  :
            return initialPos;
        }
      }
      
      int n_digits = pos - startPos;
      
      
      if (n_digits < 4 || n_digits > 4 && zero == true || year == 0) {
        return initialPos;
      }

      if (negative)
        year = 0 - year;
      
      m_year = year;
      return pos;
  }
      
  private int getMonth(final String src, final int initialPos, final int len) {
      
      int pos = initialPos;

      
      if (len == pos)
        return initialPos;

      
      if (src.charAt(pos) == '-') {
        ++pos;
      }
      
      
      int month = 0;
      final int startPos = pos;

      posLoop:
      for(; pos < len; pos++){
        final char c;
        switch (c = src.charAt(pos)) {
          case '-':       
          case '+':       
          case 'Z':       
            break posLoop;
          case '0' :
          case '1' :
          case '2' :
          case '3' :
          case '4' :
          case '5' :
          case '6' :
          case '7' :
          case '8' :
          case '9' :
            month = 10 * month + (c - '0');
            break;
          default  :
            return initialPos;
        }
      }

      
      if(pos - startPos != 2 || month < 1 || 12 < month) {
        return initialPos;
      }
      
      m_month = month;
      return pos;
  }
    
  private int getDay(final String src, final int initialPos, final int len) {

      int pos = initialPos;
      
      
      if (len == pos)
        return initialPos;

      
      if (src.charAt(pos) == '-') {
        ++pos;
      }
      
      
      int day = 0;
      final int startPos = pos;

      posLoop:
      for( ; pos < len; pos++) {
        final char c;
        switch (c = src.charAt(pos)) {
          case 'T':       
          case '-':       
          case '+':       
          case 'Z':       
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break posLoop;
          case '0' :
          case '1' :
          case '2' :
          case '3' :
          case '4' :
          case '5' :
          case '6' :
          case '7' :
          case '8' :
          case '9' :
            day = 10 * day + (c - '0');
            break;
          default  :
            return initialPos;
        }
      }

      
      if((pos - startPos) != 2 || day < 1 || 31 < day)
        return initialPos;
      
      m_day = day;
      return pos;
  }
    
  private int getMin(final String src, final int initialPos, final int len) {
      
      int pos = initialPos;
      
      
      if (len == pos)
        return initialPos;
      
      
      if (src.charAt(pos) == ':') {
        ++pos;
      }
      
      
      int min = 0;
      final int startPos = pos;

      posLoop:
      for( ; pos < len; pos++) {
        final char c;
        switch (c = src.charAt(pos)){
          case ':':       
            break posLoop;
          case '0' :
          case '1' :
          case '2' :
          case '3' :
          case '4' :
          case '5' :
          case '6' :
          case '7' :
          case '8' :
          case '9' :
            min = 10 * min + (c - '0');
            break;
          default  :
            return initialPos;
        }
      }

      
      if((pos - startPos) != 2 || min < 0 || 59 < min) {
        return initialPos;
      }

      m_min = min;
      return pos;
  }
    
  private int getHour(final String src, final int initialPos, final int len) {
      int pos = initialPos;
      
      
      if (len == pos)
        return initialPos;
      
      
      if (src.charAt(pos) == 'T') {
        ++pos;
      }
      
      
      int hour = 0;
      final int startPos = pos;

      posLoop:
      for( ; pos < len; pos++) {
        final char c;
        switch (c = src.charAt(pos)) {
          case ':':       
            break posLoop;
          case '0' :
          case '1' :
          case '2' :
          case '3' :
          case '4' :
          case '5' :
          case '6' :
          case '7' :
          case '8' :
          case '9' :
            hour = 10 * hour + (c - '0');
            break;
          default  :
            return initialPos;
        }
      }

      
      
      if((pos - startPos) != 2 || hour < 0 || 24 < hour) {
        return initialPos;
      }
      
      m_hour = hour;
      return pos;
  }
  
  private final int getSec(final String src, final int initialPos, final int len) {

      int pos = initialPos;
        
      
      if (len == pos)
        return initialPos;
        
      
      if (src.charAt(pos) == ':') {
        ++pos;
      }

      int n_digits;
      posLoop:
      for (n_digits = 0; pos < len; pos++){
        final char c;
        switch (c = src.charAt(pos)){
        case '-':
        case '+':
        case 'Z':
        case '\t':
        case '\n':
        case '\r':
        case ' ':
          break posLoop;
        case '0' :
        case '1' :
        case '2' :
        case '3' :
        case '4' :
        case '5' :
        case '6' :
        case '7' :
        case '8' :
        case '9' :
        case '.' :
          m_digitchars[n_digits++] = c;
          break;
        default  :
          return initialPos;
        }
      }
      m_sec = new BigDecimal(new String(m_digitchars, 0, n_digits));
      return pos;
  }

  
}
