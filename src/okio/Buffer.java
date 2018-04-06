package okio;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by pc on 2018/1/20.
 */
public class Buffer implements BufferedSource, BufferedSink, Cloneable {

    private static final byte[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    SegmentList segmentList = new SegmentList();

    public Buffer(SegmentList segmentList) {
        this.segmentList = new SegmentList(segmentList);
    }

    public Buffer() {

    }

    @Override
    public Buffer writeUtf8(String a) {
        write(a.getBytes());
        return this;
    }

    public long write(Source source, long length) throws IOException {
        long accumulatedRead = 0;
        while(accumulatedRead < length){
            long read = source.read(this, length);
            if(read == -1){
                if(accumulatedRead < length){
                    throw new EOFException();
                }
                return accumulatedRead;
            }
            accumulatedRead += read;
        }
        return length;
    }

    @Override
    public OutputStream outputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                writeByte((byte) b);
            }

            @Override
            public void write(byte b[], int off, int len) throws IOException {
                if (b == null) {
                    throw new NullPointerException();
                } else if ((off < 0) || (off > b.length) || (len < 0) ||
                        ((off + len) > b.length) || ((off + len) < 0)) {
                    throw new ArrayIndexOutOfBoundsException();
                } else if (len == 0) {
                    return;
                }
                for (int i = 0 ; i < len ; i++) {
                    write(b[off + i]);
                }
            }
        };
    }

    @Override
    public Buffer writeDecimalLong(long v) throws IOException {
        if (v == 0) {
            writeByte((byte) '0');
            return this;
        }

        boolean negative = false;
        if (v < 0) {
            v = -v;
            if (v < 0) {
                writeUtf8("-9223372036854775808");
                return this;
            }
            negative = true;
        }

        int width = //
                v < 100000000L
                        ? v < 10000L
                        ? v < 100L
                        ? v < 10L ? 1 : 2
                        : v < 1000L ? 3 : 4
                        : v < 1000000L
                        ? v < 100000L ? 5 : 6
                        : v < 10000000L ? 7 : 8
                        : v < 1000000000000L
                        ? v < 10000000000L
                        ? v < 1000000000L ? 9 : 10
                        : v < 100000000000L ? 11 : 12
                        : v < 1000000000000000L
                        ? v < 10000000000000L ? 13
                        : v < 100000000000000L ? 14 : 15
                        : v < 100000000000000000L
                        ? v < 10000000000000000L ? 16 : 17
                        : v < 1000000000000000000L ? 18 : 19;
        if (negative) {
            ++width;
        }

        Segment tail = segmentList.getWritableSegment(width);
        byte[] data = tail.data;
        int pos = tail.rear + width; // We write backwards from right to left.
        while (v != 0) {
            int digit = (int) (v % 10);
            data[--pos] = DIGITS[digit];
            v /= 10;
        }
        if (negative) {
            data[--pos] = '-';
        }

        tail.rear += width;
        return this;
    }

    @Override
    public Buffer writeHexadecimalUnsignedLong(long v) {
        if (v == 0) {
            writeByte((byte) '0');
            return this;
        }

        int width = Long.numberOfTrailingZeros(Long.highestOneBit(v)) / 4 + 1;

        Segment tail = segmentList.getWritableSegment(width);
        byte[] data = tail.data;
        for (int pos = tail.rear + width - 1, start = tail.rear; pos >= start; pos--) {
            data[pos] = DIGITS[(int) (v & 0xF)];
            v >>>= 4;
        }
        tail.rear += width;
        return this;
    }

    @Override
    public long writeAll(Source source) throws IOException {
        if (source == null) throw new IllegalArgumentException("source == null");
        long result = 0;
        while(true){
            if(source.read(this, 1) <= 0){
                break;
            }else{
                result++;
            }
        }
//        if(result == 0){
//            return -1;
//        }
        return result;
    }

    @Override
    public Buffer write(byte[] bytes) {
        segmentList.write(bytes);
        return this;
    }


    public Buffer write(byte[] data, int pos, int byteCount) {
        segmentList.write(data, pos, pos + byteCount);
        return this;
    }

    @Override
    public void writeByte(byte b) {
        segmentList.write(b);
    }

    @Override
    public void writeShort(short s) {
        byte second = (byte) (s & 0xff);
        byte first = (byte) (s >> 8 & 0xff);
        segmentList.write(first);
        segmentList.write(second);
    }

    @Override
    public void writeShortLe(short s) {
        byte first = (byte) (s & 0xff);
        byte second = (byte) (s >> 8 & 0xff);
        segmentList.write(first);
        segmentList.write(second);
    }

    @Override
    public void writeInt(int i) {
        segmentList.write((byte) (i >> 24 & 0xff));
        segmentList.write((byte) (i >> 16 & 0xff));
        segmentList.write((byte) (i >> 8 & 0xff));
        segmentList.write((byte) (i & 0xff));
    }

    @Override
    public void writeIntLe(int i) {
        segmentList.write((byte) (i & 0xff));
        segmentList.write((byte) (i >> 8 & 0xff));
        segmentList.write((byte) (i >> 16 & 0xff));
        segmentList.write((byte) (i >> 24 & 0xff));
    }

    @Override
    public void writeLong(long l) {
        for(int i = 8; i > 0; i--){
            segmentList.write((byte) ((l >>> 8 * (i - 1)) & 0xff));
        }
    }

    @Override
    public void writeLongLe(long l) {
        writeLong(Util.reverseBytesLong(l));
    }

    @Override
    public Buffer write(ByteString byteString) {
        segmentList.write(byteString.getData());
        return this;
    }

    @Override
    public void writeUtf8(String string, int startIndex, int endIndex) {
        segmentList.write(string.substring(startIndex, endIndex).getBytes());
    }

    @Override
    public void writeString(String string, Charset charset) {
        write(string.getBytes(charset));
    }

    @Override
    public void writeString(String string, int start, int end, Charset charset) {
        write(string.substring(start, end).getBytes(charset));
    }

    @Override
    public void write(Buffer clone, long byteCount) throws IOException {
        clone.read(this, Math.min(clone.size(), byteCount));
    }

    @Override
    public String readUtf8(int length) {
        if(segmentList.available() < length){
            throw new ArrayIndexOutOfBoundsException();
        }
        return new String(readByteArray(length));
    }

    @Override
    public Buffer clone() {
        return new Buffer(this.segmentList.clone());
    }

    private void remove(long length) {
        segmentList.remove(length);
    }

    @Override
    public byte readByte() {
        return segmentList.read();
    }

    @Override
    public boolean exhausted() {
        return segmentList.available() == 0;
    }

    @Override
    public short readShort() throws IOException {
        if(!segmentList.has(2)){
            throw new IllegalStateException("size < 2");
        }
        byte first = segmentList.read();
        byte second = segmentList.read();
        return (short) ((first & 0xff) << 8 |  (second & 0xff));
    }

    @Override
    public short readShortLe() throws IOException {
        if(!segmentList.has(2)){
            throw new IllegalStateException("size < 2");
        }
        byte first = segmentList.read();
        byte second = segmentList.read();
        return (short) ((second & 0xff) << 8 |  (first & 0xff));
    }

    @Override
    public void skip(long count) throws IOException {
        remove(count);
    }

    @Override
    public int readInt() throws IOException {
        if(!segmentList.has(4)){
            throw new IllegalStateException("size < 4");
        }
        return ((segmentList.read() & 0xff) << 24 |  (segmentList.read() & 0xff) << 16
                |  (segmentList.read() & 0xff) << 8 |  (segmentList.read() & 0xff));
    }

    @Override
    public int readIntLe() throws IOException {
        if(!segmentList.has(4)){
            return -1;
        }
        return ((segmentList.read() & 0xff) |  (segmentList.read() & 0xff) << 8
                |  (segmentList.read() & 0xff) << 16 |  (segmentList.read() & 0xff) << 24 );
    }

    @Override
    public long readLong() throws IOException {
        if(!segmentList.has(8)){
            return -1;
        }
        long result = 0;
        for(int i = 0; i < 8; i++){
            result |= (segmentList.read() & 0xffL) << (8 * (7 - i));
        }
        return result;
    }

    @Override
    public long readLongLe() throws IOException {
        if(!segmentList.has(8)){
            return -1;
        }
        long result = 0;
        for(int i = 0; i < 8; i++){
            result |= (segmentList.read() & 0xffL) << (8 * i);
        }
        return result;
    }

    @Override
    public Buffer buffer() {
        return this;
    }

    @Override
    public int readAll(Sink sink) throws IOException {
        int available = segmentList.available();
        if(available == 0){
            return 0;
        }
        sink.write(this, available);
        return available;
    }

    @Override
    public void readFully(Buffer sink, long length) throws IOException {
        if(size() < length){
            read(sink, size());
            throw new EOFException();
        }
        read(sink, length);
    }

    @Override
    public void readFully(byte[] sink) throws EOFException {
        if(size() < sink.length){
            read(sink);
            throw new EOFException();
        }
        read(sink);
    }

    @Override
    public int read(byte[] sink) {
        int result = Math.min(segmentList.available(), sink.length);
        segmentList.read(sink);
        return result;
    }

    @Override
    public byte[] readByteArray() {
        byte[] bytes = new byte[segmentList.available()];
        segmentList.read(bytes);
        return bytes;
    }

    @Override
    public int read(byte[] sink, int offset, int byteCount) {
        return segmentList.read(sink, offset, byteCount);
    }

    @Override
    public byte[] readByteArray(int count) {
        byte[] bytes = new byte[count];
        segmentList.read(bytes);
        return bytes;
    }

    @Override
    public ByteString readByteString() {
        return new ByteString(readByteArray());
    }

    @Override
    public ByteString readByteString(int count) {
        return new ByteString(readByteArray(count));
    }

    @Override
    public String readString(int count, Charset charset) {
        return new ByteString(readByteArray(count)).toString(charset);
    }

    @Override
    public String readString(Charset charset) {
        return new ByteString(readByteArray()).toString(charset);
    }

    @Override
    public int indexOf(byte target) {
        return segmentList.indexOf(target, 0, segmentList.available());
    }

    @Override
    public int indexOf(byte target, int fromIndex) {
        return segmentList.indexOf(target, fromIndex, segmentList.available());
    }

    @Override
    public int indexOf(byte target, int fromIndex, int toIndex) {
        return segmentList.indexOf(target, fromIndex, toIndex);
    }

    @Override
    public int indexOf(ByteString byteString) {
        return indexOf(byteString, 0);
    }

    @Override
    public int indexOf(ByteString byteString, int fromIndex) {
        return segmentList.indexOf(byteString, fromIndex);
    }

    @Override
    public int indexOfElement(ByteString byteString){
        return indexOfElement(byteString, 0);
    }

    @Override
    public int indexOfElement(ByteString byteString, int fromIndex) {
        return segmentList.indexOfElement(byteString, fromIndex);
    }

    @Override
    public boolean request(long byteCount) {
        return segmentList.available() >= byteCount;
    }

    @Override
    public void require(long count) throws EOFException {
        if(!segmentList.has(count)){
            throw new EOFException();
        }
    }

    @Override
    public InputStream inputStream() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                if (size() > 0) {
                    return readByte() & 0xff;
                }
                return -1;
            }

            @Override
            public int read(byte[] sink, int offset, int byteCount) {
                return Buffer.this.read(sink, offset, byteCount);
            }

            @Override public int available() {
                return (int) Math.min(size(), Integer.MAX_VALUE);
            }

            @Override public void close() {
            }

            @Override public String toString() {
                return Buffer.this + ".inputStream()";
            }
        };
    }

    @Override
    public long readHexadecimalUnsignedLong() throws EOFException {
        long result = 0;
        require(1);
        while(segmentList.has(1)){
            if ((result & 0xf000000000000000L) != 0) {
                throw new NumberFormatException("Number too large");
            }
            int digit=0;
            byte b = segmentList.read();
            if (b >= '0' && b <= '9') {
                digit = b - '0';
            } else if (b >= 'a' && b <= 'f') {
                digit = b - 'a' + 10;
            } else if (b >= 'A' && b <= 'F') {
                digit = b - 'A' + 10; // We never write uppercase, but we support reading it.
            } else {
                throw new NumberFormatException(
                        "Expected leading [0-9a-fA-F] character but was 0x" + Integer.toHexString(b));
            }

            result <<= 4;
            result |= digit;
        }

        return result;
    }

    @Override
    public long readDecimalLong() throws EOFException {
        long result = 0;
        require(1);
        long sign = 1;
        boolean isFirst = true;

        long underflowZone = Long.MIN_VALUE / 10;
        long underflowDigit = (Long.MIN_VALUE % 10);

        long overflowZone = Long.MAX_VALUE / 10;
        long overflowDigit = (Long.MAX_VALUE % 10);

        while(segmentList.has(1)){
            int digit = 0;
            byte b = segmentList.peek();
            if(isFirst && b == '-'){
                sign = -1;
            } else if (b >= '0' && b <= '9') {
                digit = b - '0';
                if(sign == -1){
                    if (-result < underflowZone || -result == underflowZone && -digit < underflowDigit) {
                        throw new NumberFormatException("Number too small");
                    }
                }else if(!isFirst){
                    if (result > overflowZone || result == overflowZone && digit > overflowDigit) {
                        throw new NumberFormatException("Number too large");
                    }
                }
            } else {
                if (isFirst) {
                    throw new NumberFormatException(
                            "Expected leading [0-9] or '-' character but was 0x" + Integer.toHexString(b));
                } else {
                    break;
                }
            }
            segmentList.read();
            isFirst = false;
            result *= 10;
            result += digit;
        }

        return result * sign;
    }

    @Override
    public boolean rangeEquals(int offset, ByteString byteString) {
        return rangeEquals(offset, byteString, 0, byteString.getData().length);
    }

    @Override
    public boolean rangeEquals(int offset, ByteString bytes, int bytesOffset, int byteCount) {
        if(bytesOffset < 0 || byteCount <0){
            return false;
        }
        if(byteCount > bytes.getData().length - bytesOffset){
            return false;
        }
        if(!request(offset + byteCount)){
            return false;
        }
        return rangeEqualsChecked(offset, bytes, bytesOffset, byteCount);
    }

    @Override
    public int select(Options options) throws IOException {
        Segment firstSegment = null;
        if(segmentList.has(1)){
            firstSegment = segmentList.getFirst();
        }else {
            return options.indexOf(ByteString.EMPTY);
        }

        ByteString[] byteStrings = options.byteStrings;
        for (int i = 0, listSize = byteStrings.length; i < listSize; i++) {
            ByteString byteString = byteStrings[i];
            if (size() >= byteString.getData().length &&
                    rangeEquals(firstSegment, firstSegment.front, byteString, 0, byteString.getData().length)) {
                skip(byteString.getData().length);
                return i;
            }
        }
        return -1;
    }

    int selectPrefix(Options options) {
        Segment s = null;
        if(segmentList.has(1)){
            s = segmentList.getFirst();
        }
        ByteString[] byteStrings = options.byteStrings;
        for (int i = 0, listSize = byteStrings.length; i < listSize; i++) {
            ByteString b = byteStrings[i];
            int bytesLimit = (int) Math.min(size(), b.getData().length);
            if (bytesLimit == 0 || rangeEquals(s, s.front, b, 0, bytesLimit)) {
                return i;
            }
        }
        return -1;
    }

    private boolean rangeEquals(Segment segment, int segmentFront, ByteString bytes, int bytesOffset, int bytesLimit) {
        int segmentRear = segment.rear;
        byte[] data = segment.data;
        for (int i = bytesOffset; i < bytesLimit; ) {
            if (segmentFront == segmentRear) {
                segment = segment.next;
                data = segment.data;
                segmentFront = segment.front;
                segmentRear = segment.rear;
            }
            if (data[segmentFront] != bytes.getByte(i)) {
                return false;
            }
            segmentFront++;
            i++;
        }
        return true;
    }

    boolean rangeEqualsChecked(int offset, ByteString bytes, int bytesOffset, int byteCount) {
        for(int i = offset; i < offset + byteCount; i++){
            if(i - offset + bytesOffset >= bytes.getData().length){
                return false;
            }
            if(getByte(i) != bytes.getData()[i - offset + bytesOffset]){
                return false;
            }
        }
        return true;
    }

    @Override
    public String readUtf8() throws IOException {
        byte[] bytes = new byte[segmentList.available()];
        segmentList.read(bytes);
        return new String(bytes);
    }

    @Override
    public long read(Buffer data, long length) throws IOException {
        int available = segmentList.available();
        if(length > available){
            length = available;
        }
        if(length == 0){
            return -1;
        }
        // TODO
        byte[] bytes = new byte[(int) length];
        segmentList.read(bytes);
        data.write(bytes);
        return length;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void flush() throws IOException {

    }

    @Override public Timeout timeout() {
        return Timeout.NONE;
    }

    public int size() {
        return segmentList.available();
    }

    public String pop(long length) {
        int available = segmentList.available();
        if(length > segmentList.available()){
            length = available;
        }
        // TODO
        byte[] bytes = new byte[(int) length];
        segmentList.read(bytes);
        String result = new String(bytes);
        remove(length);
        return result;
    }

    public byte getByte(long index) {
        if(index >= segmentList.available()){
            throw new IndexOutOfBoundsException();
        }
        return segmentList.getByte(index);
    }

    @Override
    public String toString() {
        return "[hex=" + segmentList + ']';
    }

    public List<Integer> segmentSizes() {
        return segmentList.segmentSizes();
    }

    public long completeSegmentByteCount() {
        return segmentList.completeSegmentByteCount();
    }

    public void copyTo(Buffer target, long startIndex, long length) {
        for(long i = startIndex; i < startIndex + length; i++){
            byte b = getByte(i);
            if(b == -1){
                break;
            }
            target.writeByte(getByte(i));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Buffer buffer = (Buffer) o;

        return segmentList != null ? segmentList.equals(buffer.segmentList) : buffer.segmentList == null;
    }

    @Override
    public int hashCode() {
        return segmentList != null ? segmentList.hashCode() : 0;
    }

    public void readFrom(InputStream in, int count) throws IOException {
        for (int i = 0; i < count; i++) {
            segmentList.write((byte) in.read());
        }
    }

    public void readFrom(InputStream in) throws IOException {
        while (true){
            int read = in.read();
            if(read == -1){
                return;
            }
            segmentList.write((byte) read);
        }
    }

    public void writeTo(ByteArrayOutputStream out, int length) {
        for(int i = 0; i < length; i++){
            if(!segmentList.has(1)){
                return;
            }
            out.write(segmentList.read());
        }
    }

    public void writeTo(ByteArrayOutputStream out) throws IOException {
        byte[] bytes = new byte[segmentList.available()];
        segmentList.read(bytes);
        out.write(bytes);
    }

    public void copyTo(ByteArrayOutputStream out) {
        int available = segmentList.available();
        for(int i = 0; i < available; i++){
            out.write(segmentList.getByte(i));
        }
    }

    public void copyTo(ByteArrayOutputStream out, int startIndex, int length) {
        int available = segmentList.available();
        for(long i = startIndex; i < startIndex + length && i < available; i++){
            out.write(segmentList.getByte(i));
        }
    }

    public Segment writableSegment(int size) {
        return segmentList.getWritableSegment(size);
    }

    public ByteString snapshot() {
        byte[] bytes = new byte[segmentList.available()];
        for(int i = 0; i < bytes.length; i++){
            bytes[i] = segmentList.getByte(i);
        }
        return new ByteString(bytes);
    }

    /** Returns the 128-bit MD5 hash of this buffer. */
    public ByteString md5() {
        return digest("MD5");
    }

    /** Returns the 160-bit SHA-1 hash of this buffer. */
    public ByteString sha1() {
        return digest("SHA-1");
    }

    /** Returns the 256-bit SHA-256 hash of this buffer. */
    public ByteString sha256() {
        return digest("SHA-256");
    }

    /** Returns the 512-bit SHA-512 hash of this buffer. */
    public ByteString sha512() {
        return digest("SHA-512");
    }

    private ByteString digest(String algorithm) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            if (segmentList.getFirst() != null) {
                messageDigest.update(segmentList.getFirst().data, segmentList.getFirst().front, segmentList.getFirst().rear - segmentList.getFirst().front);
                for (Segment s = segmentList.getFirst().next; s != null; s = s.next) {
                    messageDigest.update(s.data, s.front, s.rear - s.front);
                }
            }
            return ByteString.of(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }

    /** Returns the 160-bit SHA-1 HMAC of this buffer. */
    public ByteString hmacSha1(ByteString key) {
        return hmac("HmacSHA1", key);
    }

    /** Returns the 256-bit SHA-256 HMAC of this buffer. */
    public ByteString hmacSha256(ByteString key) {
        return hmac("HmacSHA256", key);
    }

    /** Returns the 512-bit SHA-512 HMAC of this buffer. */
    public ByteString hmacSha512(ByteString key) {
        return hmac("HmacSHA512", key);
    }

    private ByteString hmac(String algorithm, ByteString key) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key.toByteArray(), algorithm));
            if (segmentList.getFirst() != null) {
                mac.update(segmentList.getFirst().data, segmentList.getFirst().front, segmentList.getFirst().rear - segmentList.getFirst().front);
                for (Segment s = segmentList.getFirst().next; s != null; s = s.next) {
                    mac.update(s.data, s.front, s.rear - s.front);
                }
            }
            return ByteString.of(mac.doFinal());
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void clear() throws IOException {
        try {
            skip(size());
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }
}
