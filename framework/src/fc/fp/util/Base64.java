/*
 * EXI Testing Task Force Measurement Suite: http://www.w3.org/XML/EXI/
 *
 * Copyright ? [2006] World Wide Web Consortium, (Massachusetts Institute of
 * Technology, European Research Consortium for Informatics and Mathematics,
 * Keio University). All Rights Reserved. This work is distributed under the
 * W3C? Software License [1] in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */
package fc.fp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;



/**
 * @author Fujitsu
 */
public class Base64 {
  
  private static final String BASE64_ASCIIS =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
  private static final byte[] BASE64CHARS = new byte[8192];
  static {
    // Base64 asciis
    for (int i = BASE64_ASCIIS.length(); --i >= 0;) {
      char c = BASE64_ASCIIS.charAt(i);
      BASE64CHARS[c / 8] |= 1 << (7 - c % 8);
    }
  }

  private static boolean isBase64Char(char c) {
    return (BASE64CHARS[c / 8] & (1 << (7 - c % 8))) != 0;
  }

  static final private byte[] m_octets; // Base64 ASCII -> byte (6 bits)

  static {
    m_octets = new byte[256];

    for (int i = 'Z'; i >= 'A'; i--)
      m_octets[i] = (byte)(i - 'A');
    for (int i = 'z'; i >= 'a'; i--)
      m_octets[i] = (byte)(i - 'a' + 26);
    for (int i = '9'; i >= '0'; i--)
      m_octets[i] = (byte)(i - '0' + 52);

    m_octets['+']  = 62;
    m_octets['/']  = 63;
  }

  /**
   * Binary sequence
   */
  private byte[] byteSeq;
  /**
   * Base64 representation of the binary
   */
  private String charSeq;
  /**
   * Calculated hash code
   */
  private int hashCode;

  /**
   * Constructor
   */
  private Base64() {
    charSeq = null;
    hashCode = 0;
  }

  /**
   * Decoding constructor
   * @param s Base64 string
   */
  public Base64(String s) {
    this();
    byteSeq = decode(s);
  }

  /**
   * Encoding constructor
   * @param b binary sequence
   */
  public Base64(byte[] b) {
    this();
    if (b == null) {
      byteSeq = new byte[0];
    } else {
	  int size = b.length;
	  byteSeq = new byte[size];
	  System.arraycopy(b, 0, byteSeq, 0, size);
    }
  }

  /**
   * Get Base64 string representation
   */
  @Override
  public String toString() {
    if (charSeq == null) {
	String s;
	if (byteSeq.length == 0) {
	  s = "";
	} else {
	  StringBuffer sb = new StringBuffer(byteSeq.length * 2);
	  encode(byteSeq, sb);
	  s = sb.toString();
	}
	charSeq = s;
    }
    return charSeq;
  }

  /**
   * Comparator
   */
  @Override
  public boolean equals(Object o) {
	if (o == this) {
	  return true;
    }
    if (o instanceof Base64) {
	  Base64 b = (Base64)o;
	  if (b.byteSeq.length == byteSeq.length) {
	    for (int i = byteSeq.length; --i >= 0;) {
	      if (b.byteSeq[i] != byteSeq[i]) {
	       return false;
	      }
	    }
	    return true;
	  }
    }
    return false;
  }

  /**
   * Get hash code
   * @return hash code
   */
  @Override
  public int hashCode() {
    int h = hashCode;
    if (h == 0) {
	int i = byteSeq.length;
	for (h = i; --i >= 0; h += byteSeq[i]) {
	  h *= 31;
	}
	if (h == 0) {
	  h = 1;
	}
	hashCode = h;
    }
    return h;
  }


  /**
   * NOTE: This method has to be in sync with the other decode method.
   */
  public static byte[] decode(String norm) throws Base64Exception {
    byte[] octets = null;
    if (norm != null) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try {
        int len;
        if ((len = norm.length()) > 0) {
          final char[] enc = new char[4];
          int pos;
          for (pos = 0; pos < len;) {
            int nc;
            for (nc = 0; nc < 4 && pos < len; pos++) {
              final char c = norm.charAt(pos);
              if (isBase64Char(c))
                enc[nc++] = c;
              else if (!Character.isWhitespace(c)) {
                throw new Base64Exception(norm);
              }
            }
            if (nc == 4) {
              if (enc[0] == '=' || enc[1] == '=') { // invalid
                throw new Base64Exception(norm);
              }
              final byte b0, b1, b2, b3;
              b0 = m_octets[enc[0]];
              b1 = m_octets[enc[1]];
              baos.write(b0 << 2 | b1 >> 4);
              if (enc[2] == '=') { // it is the end
                if (enc[3] != '=') {
                  throw new Base64Exception(norm);
                }
                break;
              }
              b2 = m_octets[enc[2]];
              baos.write((byte)(((b1 & 0x0F) << 4) | ((b2 >> 2) & 0x0F)));
              if (enc[3] == '=') // it is the end
                break;
              b3 = m_octets[enc[3]];
              baos.write((byte)(b2 << 6 | b3));
            }
            else if (nc > 0) { // not multiple of four
              throw new Base64Exception(norm);
            }
          }
          for (; pos < len; pos++) { // Check if there are any extra chars
            if (!Character.isWhitespace(norm.charAt(pos))) {
              throw new Base64Exception(norm);
            }
          }
        }
      }
      finally {
        try {
          baos.close();
          octets = baos.toByteArray();
        }
        catch (IOException ioe) {
          throw new Base64Exception(ioe);
        }
      }
    }
    return octets;
  }

  public static void encode(byte[] octets, StringBuffer encodingResult) {
    if (octets != null && encodingResult != null) {
      final int len = octets.length;
      if (len > 0) {
        int pos, mod;
        for (pos = 0, mod = 1; pos < len; mod++) {
          int n, st;
          for (n = 0, st = pos; n < 3 && pos < len; pos++, n++);
          assert n == 1 || n == 2 || n == 3;
          byte b0, b1;
          byte b2 = 64, b3 = 64;
          if ( (b0 = (byte) (octets[st] >> 2)) < 0)
            b0 = (byte) (b0 ^ 0xC0);
          if (n > 1) {
            if ( (b1 = (byte) (octets[st + 1] >> 4)) < 0)
              b1 = (byte) (b1 ^ 0xF0);
            b1 = (byte) ( (octets[st] & 0x03) << 4 | b1);
            if (n > 2) { // n == 3
              if ( (b2 = (byte) (octets[st + 2] >> 6)) < 0)
                b2 = (byte) (b2 ^ 0xFC);
              b2 = (byte) ( (octets[st + 1] & 0x0F) << 2 | b2);
              b3 = (byte) (octets[st + 2] & 0x3F);
            }
            else { // n == 2
              b2 = (byte) ( (octets[st + 1] & 0x0F) << 2);
            }
          }
          else { // n == 1
            b1 = (byte) ( (octets[st] & 0x03) << 4);
          }
          encodingResult.append(BASE64_ASCIIS.charAt(b0));
          encodingResult.append(BASE64_ASCIIS.charAt(b1));
          encodingResult.append(BASE64_ASCIIS.charAt(b2));
          encodingResult.append(BASE64_ASCIIS.charAt(b3));
          if (mod == 18) {
            encodingResult.append('\n');
            mod = 0;
          }
        }
      }
    }
  }
  
  public static class Base64Exception extends RuntimeException {
    private static final long serialVersionUID = -2889929293953776202L;
    private Base64Exception(String s) {
      super(s);
    }
    private Base64Exception(IOException e) {
      super("unexpected internal error", e);
    }
  }

}
