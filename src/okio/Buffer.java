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

    @Override
    public Buffer writeUtf8(String a) {
        write(a.getBytes());
        return this;


    }

    public int write(Source source, int length) throws IOException {
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

    private void remove(int length) {
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
        if(buffer.size() == 0){
            return -1;
        }
        return buffer.remove(0);
    }

    @Override
    public boolean exhausted() {
        return buffer.size() == 0;
    }

    @Override
    public short readShort() throws IOException {
        if(buffer.size() < 2){
            return -1;
        }
        byte first = buffer.remove(0);
        byte second = buffer.remove(0);
        return (short) ((first & 0xff) << 8 |  (second & 0xff));
    }

    @Override
    public short readShortLe() throws IOException {
        if(buffer.size() < 2){
            return -1;
        }
        byte first = buffer.remove(0);
        byte second = buffer.remove(0);
        return (short) ((second & 0xff) << 8 |  (first & 0xff));
    }

    @Override
    public void skip(int count) throws IOException {
        if(buffer.size() < count){
            remove(count);
            throw new EOFException();
        }
        remove(count);
    }

    @Override
    public int readInt() throws IOException {
        if(buffer.size() < 4){
            return -1;
        }
        return ((buffer.remove(0) & 0xff) << 24 |  (buffer.remove(0) & 0xff) << 16
                |  (buffer.remove(0) & 0xff) << 8 |  (buffer.remove(0) & 0xff));
    }

    @Override
    public int readIntLe() throws IOException {
        if(buffer.size() < 4){
            return -1;
        }
        return ((buffer.remove(0) & 0xff) |  (buffer.remove(0) & 0xff) << 8
                |  (buffer.remove(0) & 0xff) << 16 |  (buffer.remove(0) & 0xff) << 24 );
    }

    @Override
    public long readLong() throws IOException {
        if(buffer.size() < 8){
            return -1;
        }
        long result = 0;
        for(int i = 0; i < 8; i++){
            result |= (buffer.remove(0) & 0xffL) << (8 * (7 - i));
        }
        return result;
    }

    @Override
    public long readLongLe() throws IOException {
        if(buffer.size() < 8){
            return -1;
        }
        long result = 0;
        for(int i = 0; i < 8; i++){
            result |= (buffer.remove(0) & 0xffL) << (8 * i);
        }
        return result;
    }

    @Override
    public Buffer buffer() {
        return this;
    }

    @Override
    public int readAll(Sink sink) throws IOException {
        int result = buffer.size();
        if(result == 0){
            return 0;
        }
        sink.write(this, buffer.size());
        return result;
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
        int result = Math.min(buffer.size(), sink.length);
        for(int i = 0; i < result; i++){
            sink[i] = buffer.get(0);
            buffer.remove(0);
        }
        return result;
    }

    @Override
    public byte[] readByteArray() {
        byte[] result = toArray(buffer);
        buffer.clear();
        return result;
    }

    @Override
    public int read(byte[] sink, int offset, int byteCount) {
        int count = 0;
        for(int i = offset; i < offset + byteCount && i < sink.length && buffer.size() > 0; i++){
            count++;
            sink[i] = buffer.get(0);
            buffer.remove(0);
        }
        return count;
    }

    @Override
    public byte[] readByteArray(int count) {
        byte[] result = new byte[count];
        for(int i = 0; i < count && buffer.size() > 0; i++){
            result[i] = buffer.remove(0);
        }
        return result;
    }

    @Override
    public ByteString readByteString() {
        ByteString result = new ByteString(toArray(buffer));
        buffer.clear();
        return result;
    }

    @Override
    public ByteString readByteString(int count) {
        count = Math.min(buffer.size(), count);
        List<Byte> toRemove = buffer.subList(0, count);
        byte[] result = toArray(toRemove);
        buffer.removeAll(toRemove);
        return new ByteString(result);
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
    public int indexOf(byte a) {
        int i = 0;
        for(byte b : buffer){
            if(b == a){
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public int indexOf(byte a, int fromIndex) {
        if(fromIndex < 0){
            throw new IllegalArgumentException("fromIndex < 0");
        }
        for(int i = fromIndex; i < buffer.size(); i++){
            if(buffer.get(i) == a){
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public int indexOf(byte b, int fromIndex, int toIndex) {
        if(fromIndex < 0){
            throw new IllegalArgumentException("fromIndex < 0");
        }
        if(fromIndex > toIndex){
            throw new IllegalArgumentException("Expected failure: fromIndex > toIndex");
        }
        for(int i = fromIndex; i < buffer.size() && i < toIndex; i++){
            if(buffer.get(i) == b){
                return i;
            }
        }
        return -1;
    }

    @Override
    public int indexOf(ByteString byteString) {
        return indexOf(byteString, 0);
    }

    @Override
    public int indexOf(ByteString byteString, int fromIndex) {
        if(byteString == null || byteString.getData().length == 0){
            throw new IllegalArgumentException("bytes is empty");
        }
        if(fromIndex < 0){
            throw new IllegalArgumentException("fromIndex < 0");
        }
        outer: for(int i = fromIndex; i <= buffer.size() - byteString.getData().length; i++){
            if(buffer.get(i) == byteString.getData()[0]){
                for(int j = i + 1; j < i + byteString.getData().length; j++){
                    if(buffer.get(j) != byteString.getData()[j - i]){
                        continue outer;
                    }
                }
                return i;
            }
        }
        return -1;
    }

    @Override
    public int indexOfElement(ByteString byteString){
        return indexOfElement(byteString, 0);
    }

    @Override
    public int indexOfElement(ByteString byteString, int fromIndex) {
        if(byteString == null || byteString.getData().length == 0){
            return -1;
        }
        Set<Byte> set = new HashSet<>();
        for (byte b : byteString.getData()){
            set.add(b);
        }
        for(int i = fromIndex; i < buffer.size(); i++){
            if(set.contains(buffer.get(i))){
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean request(int count) {
        return buffer.size() >= count;
    }

    @Override
    public void require(int count) throws EOFException {
        if(buffer.size() < count){
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
    public long readHexadecimalUnsignedLong() {
        long result = 0;

        while(buffer.size() > 0){
            if ((result & 0xf000000000000000L) != 0) {
                throw new NumberFormatException("Number too large");
            }
            int digit=0;
            byte b = buffer.remove(0);
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
        String result = new String(toArray(buffer));
        buffer.clear();
        return result;
    }

    @Override
    public int read(Buffer data, int length) throws IOException {
        if(length > buffer.size()){
            length = buffer.size();
        }
        if(length == 0){
            return -1;
        }
        data.writeUtf8(new String(toArray(buffer.subList(0, length))));
        remove(length);
        return length;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void write(Buffer data, int i) {
        buffer.addAll(toList(data.pop(i)));
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
        return buffer.size();
    }

    public String pop(int length) {
        if(length > buffer.size()){
            length = buffer.size();
        }
        String result = new String(toArray(buffer.subList(0, length)));
        remove(length);
        return result;
    }
}
