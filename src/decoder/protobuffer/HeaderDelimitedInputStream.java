package decoder.protobuffer;

import java.io.*;

public class HeaderDelimitedInputStream extends InputStream {

    private final InputStream baseStream;
    private long sectionBytesRemaining = 0;
    private boolean eof = false;
    private int sectionFourCC;
    private int fileFormatFourCC;
    private int fileVersion;
    private boolean parsedFiledHeader = false;

    public static final int FOURCC( char a, char b, char c, char d ) {
        return ((a&0xFF)<<24)|((b&0xFF)<<16)|((c&0xFF)<<8)|((d&0xFF));
    }

    private int readInt() throws IOException {
        int b0 = baseStream.read();
        int b1 = baseStream.read();
        int b2 = baseStream.read();
        int b3 = baseStream.read();
        if( b0==-1 || b1==-1 || b2==-1 || b3==-1 ) {
            eof = true;
            return 0;
        }
        return ((b0&0xFF)<<24)|((b1&0xFF)<<16)|((b2&0xFF)<<8)|((b3&0xFF));
    }

    public HeaderDelimitedInputStream(InputStream baseStream) {
        this.baseStream = baseStream;
    }

    public HeaderDelimitedInputStream(File file) throws FileNotFoundException {
        this.baseStream = new FileInputStream(file);
    }

    public boolean parseFileHeader() throws IOException {
        if( parsedFiledHeader )
            return true;
        fileFormatFourCC = readInt();
        fileVersion = readInt();
        parsedFiledHeader = !eof;
        return parsedFiledHeader;
    }

    @Override
    public int read() throws IOException {
        if( sectionBytesRemaining <1 )
            return -1;
        int result = baseStream.read();
        if( result >= 0 )
            sectionBytesRemaining--;
        return result;
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        if( sectionBytesRemaining <1 )
            return -1;
        if( sectionBytesRemaining < byteCount )
            byteCount = (int) sectionBytesRemaining;
        int result = baseStream.read(buffer, byteOffset, byteCount);
        if( result > 0 )
            sectionBytesRemaining -=result;
        return result;
    }

    @Override
    public long skip(long byteCount) throws IOException {
        if( sectionBytesRemaining <1 )
            return -1;
        if( sectionBytesRemaining < byteCount )
            byteCount = sectionBytesRemaining;
        long result = baseStream.skip(byteCount);
        if( result > 0 )
            sectionBytesRemaining -=result;
        return result;
    }

    public boolean nextSection() throws IOException {
        if(eof)
            return false;
        if( sectionBytesRemaining>0 )
            baseStream.skip(sectionBytesRemaining);
        sectionFourCC = readInt();
        sectionBytesRemaining = readInt();
        return !eof;
    }

    public long getSectionBytesRemaining() {
        return sectionBytesRemaining;
    }

    public int getSectionFourCC() {
        return sectionFourCC;
    }

    public int getFileFormatFourCC() {
        return fileFormatFourCC;
    }

    public int getFileVersion() {
        return fileVersion;
    }

    @Override
    public void close() throws IOException {
        baseStream.close();
    }
}
