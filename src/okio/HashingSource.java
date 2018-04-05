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
 * rearations under the License.
 */
package okio;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashingSource extends ForwardingSource {
  private final MessageDigest messageDigest;
  private final Mac mac;

  public static HashingSource md5(Source source) {
    return new HashingSource(source, "MD5");
  }

  public static HashingSource sha1(Source source) {
    return new HashingSource(source, "SHA-1");
  }

  public static HashingSource sha256(Source source) {
    return new HashingSource(source, "SHA-256");
  }

  public static HashingSource hmacSha1(Source source, ByteString key) {
    return new HashingSource(source, key, "HmacSHA1");
  }

  public static HashingSource hmacSha256(Source source, ByteString key) {
    return new HashingSource(source, key, "HmacSHA256");
  }

  private HashingSource(Source source, String algorithm) {
    super(source);
    try {
      this.messageDigest = MessageDigest.getInstance(algorithm);
      this.mac = null;
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError();
    }
  }

  private HashingSource(Source source, ByteString key, String algorithm) {
    super(source);
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

  @Override public long read(Buffer sink, long byteCount) throws IOException {
    long result = super.read(sink, byteCount);

    if (result != -1L) {
      long start = sink.size() - result;

      // Find the first segment that has new bytes.
      long offset = sink.size();
      Segment s = sink.segmentList.getFirst();
      while (offset > start) {
        s = s.prev;
        if(s == null){
          s = sink.segmentList.getLast();
        }
        offset -= (s.rear - s.front);
      }

      while (offset < sink.size()) {
        int front = (int) (s.front + start - offset);
        if (messageDigest != null) {
          messageDigest.update(s.data, front, s.rear - front);
        } else {
          mac.update(s.data, front, s.rear - front);
        }
        offset += (s.rear - s.front);
        start = offset;
        s = s.next;
      }
    }

    return result;
  }

  public ByteString hash() {
    byte[] result = messageDigest != null ? messageDigest.digest() : mac.doFinal();
    return ByteString.of(result);
  }
}
