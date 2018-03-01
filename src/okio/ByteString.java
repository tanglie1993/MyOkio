package okio;

import test.TestUtil;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import static okio.Util.arrayRangeEquals;

/**
 * Created by pc on 2018/1/21.
 */
public class ByteString  implements Serializable {

    public static final ByteString EMPTY = ByteString.of();

    private byte[] data;

    public ByteString(byte[] bytes) {
        this.data = bytes;
    }

    public ByteString(byte[] bytes, int startIndex, int length) {
        this.data = new byte[length];
        System.arraycopy(bytes, startIndex, this.data, 0, length);
    }

    public String utf8() {
        return new String(data);
    }

    public String toString(Charset charset) {
        return new String(data, charset);
    }

    public static ByteString encodeUtf8(String string) {
        return new ByteString(string.getBytes(TestUtil.UTF_8));
    }

    public byte[] getData() {
        return data;
    }

    public static ByteString of(byte... data) {
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }
        return new ByteString(data.clone());
    }

    public static ByteString decodeHex(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("hex == null");
        }
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Unexpected hex string: " + hex);
        }

        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < result.length; i++) {
            int d1 = decodeHexDigit(hex.charAt(i * 2)) << 4;
            int d2 = decodeHexDigit(hex.charAt(i * 2 + 1));
            result[i] = (byte) (d1 + d2);
        }
        return of(result);
    }

    private static int decodeHexDigit(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        if (c >= 'A' && c <= 'F') return c - 'A' + 10;
        throw new IllegalArgumentException("Unexpected hex digit: " + c);
    }

    @Override
    public String toString() {
        if(data == null || data.length == 0){
            return "[size=0]";
        }
        String string = new String(data);
        string = string.substring(0, Math.min(64, string.length()))
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
        if(containsReplacement(string)){
            return "[hex=" + hex() + "]";
        }
        return "[text=" + string + ']';
    }

    private boolean containsReplacement(String string) {
        int c;
        for (int i = 0, length = string.length(); i < length; i += Character.charCount(c)) {
            c = string.codePointAt(i);
            if (c == '\ufffd') {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ByteString that = (ByteString) o;

        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    public byte getByte(int index) {
        return data[index];
    }

    public static ByteString of(byte[] bytes, int startIndex, int length) {
        return new ByteString(bytes, startIndex, length);
    }

    public static ByteString of(ByteBuffer byteBuffer) {
        return new ByteString(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit() - byteBuffer.position());
    }

    public boolean startsWith(ByteString byteString) {
        return startsWith(byteString.getData());
    }

    public final boolean startsWith(byte[] prefix) {
        for(int i = 0; i < prefix.length; i++){
            if(i >= getData().length || getData()[i] != prefix[i]){
                return false;
            }
        }
        return true;
    }

    public boolean endsWith(ByteString byteString) {
        for(int i = 0; i < byteString.getData().length; i++){
            if(getData().length - 1 - i < 0 || getData()[getData().length - 1 - i] != byteString.getData()[byteString.getData().length - 1 - i]){
                return false;
            }
        }
        return true;
    }
    public final boolean endsWith(byte[] suffix) {
        for(int i = 0; i < suffix.length; i++){
            if(getData().length - 1 - i < 0 || getData()[getData().length - 1 - i] != suffix[suffix.length - 1 - i]){
                return false;
            }
        }
        return true;
    }


    public byte[] toByteArray() {
        return data.clone();
    }

    public int indexOf(ByteString byteString) {
        return indexOf(byteString, 0);
    }

    public int indexOf(ByteString byteString, int startIndex) {
        return indexOf(byteString.getData(), startIndex);
    }

    public int indexOf(byte[] bytes) {
        return indexOf(bytes, 0);
    }

    public int indexOf(byte[] bytes, int startIndex) {
        startIndex = Math.max(startIndex, 0);
        outer: for(int i = startIndex; i <= getData().length - bytes.length; i++){
            for(int j = 0; j < bytes.length; j++){
                if(j >= getData().length || getData()[i + j] != bytes[j]){
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public final int lastIndexOf(ByteString byteString) {
        return lastIndexOf(byteString.getData(), getData().length);
    }

    public final int lastIndexOf(ByteString byteString, int fromIndex) {
        return lastIndexOf(byteString.getData(), fromIndex);
    }

    public final int lastIndexOf(byte[] other) {
        return lastIndexOf(other, getData().length);
    }

    public int lastIndexOf(byte[] other, int fromIndex) {
        fromIndex = Math.min(fromIndex, data.length - other.length);
        for (int i = fromIndex; i >= 0; i--) {
            if (arrayRangeEquals(data, i, other, 0, other.length)) {
                return i;
            }
        }
        return -1;
    }

    public static ByteString encodeString(String s, Charset charset) {
        if(s == null || charset == null){
            throw new IllegalArgumentException();
        }
        return new ByteString(s.getBytes(charset));
    }

    public String string(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charset == null");
        }
        return new String(data, charset);
    }

    public static ByteString read(InputStream in, int count) throws IOException {
        byte[] bytes = new byte[count];
        for(int i = 0; i < count; i++){
            bytes[i] = (byte) in.read();
        }
        return new ByteString(bytes);
    }

    public ByteString toAsciiLowercase() {
        // Search for an uppercase character. If we don't find one, return this.
        for (int i = 0; i < data.length; i++) {
            byte c = data[i];
            if (c < 'A' || c > 'Z') continue;

            // If we reach this point, this string is not not lowercase. Create and
            // return a new byte string.
            byte[] lowercase = data.clone();
            lowercase[i++] = (byte) (c - ('A' - 'a'));
            for (; i < lowercase.length; i++) {
                c = lowercase[i];
                if (c < 'A' || c > 'Z') continue;
                lowercase[i] = (byte) (c - ('A' - 'a'));
            }
            return new ByteString(lowercase);
        }
        return this;
    }

    public ByteString toAsciiUppercase() {
        // Search for an lowercase character. If we don't find one, return this.
        for (int i = 0; i < data.length; i++) {
            byte c = data[i];
            if (c < 'a' || c > 'z') continue;

            // If we reach this point, this string is not not uppercase. Create and
            // return a new byte string.
            byte[] lowercase = data.clone();
            lowercase[i++] = (byte) (c - ('a' - 'A'));
            for (; i < lowercase.length; i++) {
                c = lowercase[i];
                if (c < 'a' || c > 'z') continue;
                lowercase[i] = (byte) (c - ('a' - 'A'));
            }
            return new ByteString(lowercase);
        }
        return this;
    }

    public ByteString substring(int startIndex) {
        if(startIndex < 0 || startIndex >= data.length){
            throw new IllegalArgumentException();
        }
        byte[] bytes = new byte[data.length - startIndex];
        System.arraycopy(data, startIndex, bytes, 0, data.length - startIndex);
        return new ByteString(bytes);
    }

    public ByteString substring(int startIndex, int endIndex) {
        if(startIndex < 0 || startIndex >= data.length || endIndex < 0 || endIndex >= data.length || endIndex < startIndex){
            throw new IllegalArgumentException();
        }
        byte[] bytes = new byte[endIndex - startIndex];
        System.arraycopy(data, startIndex, bytes, 0, endIndex - startIndex);
        return new ByteString(bytes);
    }

    public void write(ByteArrayOutputStream out) throws IOException {
        out.write(data);
    }

    public String base64() {
        return Base64.encode(data);
    }

    public String base64Url() {
        return Base64.encodeUrl(data);
    }

    public static ByteString decodeBase64(String base64) {
        if (base64 == null) {
            throw new IllegalArgumentException();
        }
        byte[] decoded = Base64.decode(base64);
        if(decoded == null){
            return null;
        }
        return new ByteString(decoded);
    }

    public String hex() {
        return Hex.hex(data);
    }
}
