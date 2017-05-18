// $Id: MagicInputStream.java,v 1.1 2010/02/23 20:31:07 tkamiya Exp $
package fc.fp.util;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;

/** Input stream with a heuristically determined MIME type.  */

// NOTE: allows chaining; ie delegates to underlying MagicIs if possible&needed
public abstract class MagicInputStream extends FilterInputStream {

    private String mimeType = null;
    private InputStream origIn;

    public MagicInputStream(InputStream in) throws IOException {
        super(in);
        origIn = in;
        int magicSize = getMagicSize();
        if (!in.markSupported()) {
            this.in = new BufferedInputStream(in, magicSize+1);
        }
        identify();
    }

    /** Get MIME type of stream data.
     * @return MIME type of data, or <code>null</null> if the type could not
     * be determined
     */

    public String getMimeType() throws IOException {
        return mimeType == null ? (origIn instanceof MagicInputStream ?
           ( (MagicInputStream) origIn).getMimeType() : null) :
            mimeType;
    }

    /** Get maximum number of bytes needed for identification.
     * @return 4 (the default size of the magic signature)
     */

    protected int getMagicSize() {
        return 4;
    }

    protected void identify() throws IOException {
        int magicSize = getMagicSize();
        byte[] magic = new byte[magicSize];
        int mlen = 0;
        try {
            in.mark(magicSize);
            for (int i = 0; i < magic.length; i++) {
                if (in.read(magic, i, 1) < 1)
                    break;
                mlen++;
            }
        } catch (IOException x) {
            ; // Magic read fails silently
        } finally {
            in.reset();
        }
        mimeType = identify(magic, mlen);
    }

    /** This method identifies the stream MIME type.
     * @param len int Length of magic signature
     * @param magic byte[] magic signature, i.e. the first n bytes of the stream
     * @return String Identified MIME type, or <code>null</code> if unidentified
     */
    protected abstract String identify(byte[] magic, int len);
}
// arch-tag: 42a27023f3614d5cc0cdc618380670bb *-
