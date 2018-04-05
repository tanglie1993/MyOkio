/*
 * Copyright (C) 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package okio;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static okio.Util.checkOffsetAndCount;

public final class HashingSink extends ForwardingSink {
  private final MessageDigest messageDigest;
  private final Mac mac;

  public static HashingSink md5(Sink sink) {
    return new HashingSink(sink, "MD5");
  }

  public static HashingSink sha1(Sink sink) {
    return new HashingSink(sink, "SHA-1");
  }

  public static HashingSink sha256(Sink sink) {
    return new HashingSink(sink, "SHA-256");
  }

  public static HashingSink sha512(Sink sink) {
    return new HashingSink(sink, "SHA-512");
  }

  public static HashingSink hmacSha1(Sink sink, ByteString key) {
    return new HashingSink(sink, key, "HmacSHA1");
  }

  public static HashingSink hmacSha256(Sink sink, ByteString key) {
    return new HashingSink(sink, key, "HmacSHA256");
  }

  public static HashingSink hmacSha512(Sink sink, ByteString key) {
    return new HashingSink(sink, key, "HmacSHA512");
  }

  private HashingSink(Sink sink, String algorithm) {
    super(sink);
    try {
      this.messageDigest = MessageDigest.getInstance(algorithm);
      this.mac = null;
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError();
    }
  }

  private HashingSink(Sink sink, ByteString key, String algorithm) {
    super(sink);
    try {
      this.mac = Mac.getInstance(algorithm);
      this.mac.init(new SecretKeySpec(key.toByteArray(), algorithm));
      this.messageDigest = null;
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError();
    } catch (InvalidKeyException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override public void write(Buffer source, long byteCount) throws IOException {
    checkOffsetAndCount(source.size(), 0, byteCount);
    long hashedCount = 0;
    for (Segment s = source.segmentList.getFirst(); hashedCount < byteCount; s = s.next) {
      int toHash = (int) Math.min(byteCount - hashedCount, s.rear - s.front);
      if (messageDigest != null) {
        messageDigest.update(s.data, s.front, toHash);
      } else {
        mac.update(s.data, s.front, toHash);
      }
      hashedCount += toHash;
    }
    super.write(source, byteCount);
  }

  public ByteString hash() {
    byte[] result = messageDigest != null ? messageDigest.digest() : mac.doFinal();
    return ByteString.of(result);
  }
}
