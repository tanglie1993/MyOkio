package okio;

import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by pc on 2018/1/20.
 */
public class Buffer implements BufferedSource, BufferedSink, Cloneable {

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
        return source.read(this, length);
    }

    @Override
    public void writeAll(Source source) throws IOException {
        if (source == null) throw new IllegalArgumentException("source == null");
        while(true){
            if(source.read(this, 1) <= 0){
                break;
            }
        }
    }

    @Override
    public void write(byte[] bytes) {
        segmentList.write(bytes);
    }

    @Override
    public void write(Buffer clone, boolean result) {

    }

    @Override
    public String readUtf8(int length) {
        return new String(readByteArray(length));
    }

    @Override public Buffer clone() {
        return new Buffer(this.segmentList.clone());
    }

    private void remove(long length) {
        segmentList.remove(length);
    }


    private byte[] toArray(List<Byte> bytes) {
        byte[] result = new byte[bytes.size()];
        int index = 0;
        for(Byte b : bytes){
            result[index] = b;
            index++;
        }
        return result;
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
        if(!segmentList.has(count)){
            remove(count);
            throw new EOFException();
        }
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
    public void readFully(Buffer sink, int length) throws IOException {
        if(size() < length){
            read(sink, length);
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
    public boolean request(int byteCount) {
        return segmentList.available() >= byteCount;
    }

    @Override
    public void require(int count) throws EOFException {
        if(!segmentList.has(count)){
            throw new EOFException();
        }
    }

    @Override
    public InputStream inputStream() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return readByte();
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
        long underflowDigit = (Long.MIN_VALUE % 10) + 1;

        long overflowZone = Long.MAX_VALUE / 10;
        long overflowDigit = (Long.MAX_VALUE % 10) - 1;

        while(segmentList.has(1)){
            int digit = 0;
            byte b = segmentList.peek();
            if(isFirst && b == '-'){
                sign = -1;
            } else if (b >= '0' && b <= '9') {
                digit = b - '0';
                if (result < underflowZone || result == underflowZone && digit < underflowDigit) {
                    throw new NumberFormatException("Number too small");
                }
                if (result > overflowZone || result == overflowZone && digit > overflowDigit) {
                    throw new NumberFormatException("Number too large");
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
    public boolean rangeEquals(int offset, ByteString byteString, int start, int end) {
        if(!request(offset + end - start)){
            return false;
        }
        for(int i = start; i < end; i++){
            if(getByte(i - start + offset) != byteString.getData()[i]){
                return false;
            }
        }
        return true;
    }

    private List<Byte> toList(byte[] sink) {
        if(sink == null){
            return new ArrayList<>();
        }
        List<Byte> result = new ArrayList<>();
        for(byte b : sink){
            result.add(b);
        }
        return result;
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
        byte[] bytes = new byte[(int) length];
        segmentList.read(bytes);
        data.write(bytes);
        return length;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void write(Buffer data, long length) {
        byte[] bytes = new byte[(int) Math.min(data.size(), length)];
        data.read(bytes);
        write(bytes);
    }

    private List<Byte> toList(String string) {
        List<Byte> result = new ArrayList<>();
        if(string == null){
            return result;
        }
        return toList(string.getBytes());
    }

    @Override
    public void flush() throws IOException {

    }

    public int size() {
        return segmentList.available();
    }

    public String pop(long length) {
        int available = segmentList.available();
        if(length > segmentList.available()){
            length = available;
        }
        byte[] bytes = new byte[(int) length];
        segmentList.read(bytes);
        String result = new String(bytes);
        remove(length);
        return result;
    }

    public byte getByte(int index) {
        return segmentList.getByte(index);
    }
}
