package okio;


import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import static okio.Util.checkOffsetAndCount;

/**
 * Created by pc on 2018/1/20.
 */
public class RealBufferedSource implements BufferedSource {

    private Source source;
    private Buffer buffer;
    boolean closed;

    public RealBufferedSource(Source source) {
        buffer = new Buffer();
        this.source = source;
    }

    public RealBufferedSource(InputStream inputStream) {
        buffer = new Buffer();
        source = new Source() {
            @Override
            public long read(Buffer data, long length) throws IOException {
                long size = length;
                if(inputStream.available() <= 0){
                    return -1;
                }
                if(length > inputStream.available()){
                    size = inputStream.available();
                }
                // TODO
                byte[] bytes = new byte[(int) size];
                inputStream.read(bytes);
                data.write(bytes);
                return buffer.size();
            }

            @Override
            public void close() throws IOException {
                inputStream.close();
            }

            @Override
            public Timeout timeout() {
                return Timeout.NONE;
            }
        };
    }


    @Override
    public long read(Buffer data, long length) throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + length);
        }
        long result = 0;
        try{
            result = buffer.write(source, length);
        }catch (EOFException e){

        }
        return buffer.read(data, length);
    }

    @Override
    public void close() throws IOException {
        source.close();
        closed = true;
    }

    @Override
    public Timeout timeout() {
        return Timeout.NONE;
    }

    @Override
    public String readUtf8() throws IOException {
        buffer.writeAll(source);
        return buffer.readUtf8();
    }

    @Override
    public String readUtf8(int length) throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + length);
        }
        long extra = length - buffer.size();
        if(extra > 0){
            buffer.write(source, extra);
        }
        return buffer.readUtf8(length);
    }

    @Override
    public byte readByte() throws IOException {
        require(1);
        return buffer.readByte();
    }

    @Override
    public boolean exhausted() {
        return buffer.size() == 0;
    }

    @Override
    public short readShort() throws IOException {
        require(2);
        return buffer.readShort();
    }

    @Override
    public short readShortLe() throws IOException {
        require(2);
        return buffer.readShortLe();
    }

    @Override
    public void skip(long count) throws IOException {
        require(count);
        long toSkip = Math.min(count, buffer.size());
        buffer.skip(toSkip);
    }

    @Override
    public int readInt() throws IOException {
        require(4);
        return buffer.readInt();
    }



    @Override
    public int readIntLe() throws IOException {
        require(4);
        return buffer.readIntLe();
    }

    @Override
    public long readLong() throws IOException {
        require(8);
        return buffer.readLong();
    }

    @Override
    public long readLongLe() throws IOException {
        require(8);
        return buffer.readLongLe();
    }

    @Override
    public Buffer buffer() {
        return buffer;
    }

    @Override
    public int readAll(Sink sink) throws IOException {
        loadAllSourceIntoBuffer();
        int result = buffer.size();
        if(buffer.size() > 0){
            sink.write(buffer, buffer.size());
        }
        return result;
    }

    private void loadAllSourceIntoBuffer() throws IOException {
        while (true) {
            if(source.read(buffer, Segment.SIZE) == -1){
                break;
            }
        }
    }

    @Override
    public void readFully(Buffer sink, long length) throws IOException {
        while (buffer.size() < length){
            if(source.read(buffer, Segment.SIZE) == -1){
                buffer.readFully(sink, length);
                throw new EOFException();
            }
        }
        buffer.readFully(sink, length);
    }

    @Override
    public void readFully(byte[] sink) throws IOException {
        loadAllSourceIntoBuffer();
        if(buffer.size() < sink.length){
            buffer.read(sink);
            throw new EOFException();
        }
        buffer.readFully(sink);
    }

    @Override
    public int read(byte[] sink) throws IOException {
        return read(sink, 0, sink.length);
    }

    @Override
    public byte[] readByteArray() throws IOException {
        loadAllSourceIntoBuffer();
        return buffer.readByteArray();
    }

    @Override
    public int read(byte[] sink, int offset, int byteCount) throws IOException {
        if (closed) {
            throw new IOException("closed");
        }
        checkOffsetAndCount(sink.length, offset, byteCount);

        if (buffer.size() == 0) {
            long count = source.read(buffer, Segment.SIZE);
            if (count == -1) {
                return -1;
            }
        }

        return buffer.read(sink, offset, byteCount);
    }

    @Override
    public byte[] readByteArray(int count) throws IOException {
        byte[] result = new byte[count];
        request(count);
        read(result);
        return result;
    }

    @Override
    public ByteString readByteString() throws IOException {
        return new ByteString(readByteArray());
    }

    @Override
    public ByteString readByteString(int count) throws IOException {
        return new ByteString(readByteArray(count));
    }

    @Override
    public String readString(int count, Charset charset) throws IOException {
        return new ByteString(readByteArray(count)).toString(charset);
    }

    @Override
    public String readString(Charset charset) throws IOException {
        return new ByteString(readByteArray()).toString(charset);
    }

    @Override
    public int indexOf(byte b) throws IOException {
        return indexOf(b, 0, Integer.MAX_VALUE);
    }

    @Override
    public int indexOf(byte b, int fromIndex) throws IOException {
        return indexOf(b, fromIndex, Integer.MAX_VALUE);
    }

    @Override
    public int indexOf(byte b, int fromIndex, int toIndex) throws IOException {
        if(fromIndex < 0){
            throw new IllegalArgumentException("fromIndex < 0");
        }
        if(fromIndex > toIndex){
            throw new IllegalArgumentException("Expected failure: fromIndex > toIndex");
        }
        if(fromIndex == toIndex){
            return -1;
        }
        require(fromIndex);
        final int initialBufferSize = buffer.size();
        for(int i = fromIndex; i < toIndex && i < initialBufferSize; i++){
            if(buffer.getByte(i) == b){
                return i;
            }
        }
        int searchIndex = initialBufferSize - 1;
        while(true){
            if(source.read(buffer, 1) == -1){
                return -1;
            }
            searchIndex++;
            if(buffer.getByte(buffer.size() - 1) == b){
                return searchIndex;
            }
        }
    }

    @Override
    public int indexOf(ByteString byteString) throws IOException {
        return indexOf(byteString, 0);
    }

    @Override
    public int indexOf(ByteString byteString, int fromIndex) throws IOException {
        if(byteString == null || byteString.getData().length == 0){
            throw new IllegalArgumentException("bytes is empty");
        }
        if(fromIndex < 0){
            throw new IllegalArgumentException("fromIndex < 0");
        }
        if(!request(byteString.getData().length)){
            return -1;
        }
        for(int i = fromIndex; i <= buffer.size() - byteString.getData().length; i++){
            if(bufferMatchIndex(byteString, i)){
                return i;
            }
        }
        while(source.read(buffer, 1) != -1){
            if(bufferMatchIndex(byteString, buffer.size() - byteString.getData().length)){
                return buffer.size() - byteString.getData().length;
            }
        }
        return -1;
    }

    private boolean bufferMatchIndex(ByteString byteString, int index) {
        for(int i = index; i < buffer.size(); i++){
            if(i - index >= byteString.getData().length){
                return true;
            }
            if(buffer.getByte(i) != byteString.getData()[i - index]){
                return false;
            }
        }
        return true;
    }

    @Override
    public int indexOfElement(ByteString byteString) throws IOException {
        return indexOfElement(byteString, 0);
    }

    @Override
    public int indexOfElement(ByteString byteString, int fromIndex) throws IOException {
        if(!request(fromIndex)){
            return -1;
        }
        Set<Byte> bytesSet = new HashSet<>();
        for(byte b : byteString.getData()){
            bytesSet.add(b);
        }
        for(int i = fromIndex; i < buffer.size(); i++){
            if(bytesSet.contains(buffer.getByte(i))){
                return i;
            }
        }
        while(source.read(buffer, 1) != -1){
            if(bytesSet.contains(buffer.getByte(buffer.size() - 1))){
                return buffer.size() - 1;
            }
        }
        return -1;
    }

    @Override
    public boolean request(long byteCount) throws IOException {
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
        while (buffer.size() < byteCount) {
            if (source.read(buffer, Segment.SIZE) == -1) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void require(long count) throws IOException {
        if (!request(count)) {
            throw new EOFException();
        }
    }

    @Override
    public InputStream inputStream() {
        return new InputStream() {
            @Override public int read() throws IOException {
                System.out.println("read");
                if (closed) {
                    throw new IOException("closed");
                }
                if(!request(1)){
                    return -1;
                }
                return buffer.readByte() & 0xff;
            }

            @Override public int read(byte[] data, int offset, int byteCount) throws IOException {
                if (closed) {
                    throw new IOException("closed");
                }
                checkOffsetAndCount(data.length, offset, byteCount);
                if(!request(1)){
                    return -1;
                }

                return buffer.read(data, offset, byteCount);
            }

            @Override public int available() throws IOException {
                if (closed) {
                    throw new IOException("closed");
                }
                return buffer.size();
            }

            @Override public void close() throws IOException {
                RealBufferedSource.this.close();
            }

            @Override public String toString() {
                return RealBufferedSource.this + ".inputStream()";
            }
        };
    }

    @Override
    public long readHexadecimalUnsignedLong() throws IOException {
        require(1);
        for (int pos = 0; request(pos + 1); pos++) {
            byte b = buffer.getByte(pos);
            if ((b < '0' || b > '9') && (b < 'a' || b > 'f') && (b < 'A' || b > 'F')) {
                if (pos == 0) {
                    throw new NumberFormatException(String.format(
                            "Expected leading [0-9a-fA-F] character but was %#x", b));
                }
                break;
            }
        }
        return buffer.readHexadecimalUnsignedLong();
    }

    @Override
    public long readDecimalLong() throws IOException {
        require(1);
        for (int pos = 0; request(pos + 1); pos++) {
            byte b = buffer.getByte(pos);
            if ((b < '0' || b > '9') && (pos != 0 || b != '-')) {
                if (pos == 0) {
                    throw new NumberFormatException(String.format(
                            "Expected leading [0-9] or '-' character but was %#x", b));
                }
                break;
            }
        }
        return buffer.readDecimalLong();
    }

    @Override
    public boolean rangeEquals(int offset, ByteString byteString) throws IOException {
        return rangeEquals(offset, byteString, 0, byteString.getData().length);
    }

    @Override
    public boolean rangeEquals(int offset, ByteString bytes, int bytesOffset, int byteCount) throws IOException {
        if(bytesOffset < 0 || byteCount < 0){
            return false;
        }
        if(byteCount > bytes.getData().length - bytesOffset){
            return false;
        }
        if(!request(offset + byteCount)){
            return false;
        }
        return buffer.rangeEqualsChecked(offset, bytes, bytesOffset, byteCount);
    }

    @Override
    public int select(Options options) throws IOException {
        while (true) {
            int index = buffer.selectPrefix(options);
            if (index == -1) {
                return -1;
            }

            int selectedSize = options.byteStrings[index].getData().length;
            if (selectedSize <= buffer.size()) {
                buffer.skip(selectedSize);
                return index;
            }
            if (source.read(buffer, Segment.SIZE) == -1) {
                return -1;
            }
        }
    }

    @Override public String readUtf8LineStrict() throws IOException {
        return readUtf8LineStrict(Long.MAX_VALUE);
    }

    @Override public String readUtf8LineStrict(long limit) throws IOException {
        if (limit < 0) {
            throw new IllegalArgumentException("limit < 0: " + limit);
        }
        long scanLength = limit == Long.MAX_VALUE ? Long.MAX_VALUE : limit + 1;
        long newline = indexOf((byte) '\n', 0, (int) scanLength);
        if (newline != -1) return buffer.readUtf8Line(newline);
        if (scanLength < Long.MAX_VALUE
                && request(scanLength) && buffer.getByte(scanLength - 1) == '\r'
                && request(scanLength + 1) && buffer.getByte(scanLength) == '\n') {
            return buffer.readUtf8Line(scanLength);
        }
        Buffer data = new Buffer();
        buffer.copyTo(data, 0, Math.min(32, buffer.size()));
        throw new EOFException("\\n not found: limit=" + Math.min(buffer.size(), limit)
                + " content=" + data.readByteString().hex() + '…');
    }

    @Override
    public String readUtf8Line() throws IOException {
        long newline = indexOf((byte) '\n');
        if (newline == -1) {
            return buffer.size() != 0 ? readUtf8(buffer.size()) : null;
        }

        return buffer.readUtf8Line(newline);
    }

    @Override
    public String toString() {
        return "RealBufferedSource{" +
                "source=" + source +
                ", buffer=" + buffer +
                ", closed=" + closed +
                '}';
    }
}
