/*
 * Copyright (C) 2014 Square, Inc.
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

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Inflater;

public final class GzipSource implements Source {
  private static final byte FHCRC = 1;
  private static final byte FEXTRA = 2;
  private static final byte FNAME = 3;
  private static final byte FCOMMENT = 4;

  private static final byte SECTION_HEADER = 0;
  private static final byte SECTION_BODY = 1;
  private static final byte SECTION_TRAILER = 2;
  private static final byte SECTION_DONE = 3;

  private int section = SECTION_HEADER;

  private final BufferedSource source;

  private final Inflater inflater;

  private final InflaterSource inflaterSource;

  private final CRC32 crc = new CRC32();

  public GzipSource(Source source) {
    if (source == null) throw new IllegalArgumentException("source == null");
    this.inflater = new Inflater(true);
    this.source = Okio.buffer(source);
    this.inflaterSource = new InflaterSource(this.source, inflater);
  }

  @Override public long read(Buffer sink, long byteCount) throws IOException {
    if (byteCount < 0) {
      throw new IllegalArgumentException("byteCount < 0: " + byteCount);
    }
    if (byteCount == 0) {
      return 0;
    }

    if (section == SECTION_HEADER) {
      consumeHeader();
      section = SECTION_BODY;
    }

    if (section == SECTION_BODY) {
      long offset = sink.size();
      long result = inflaterSource.read(sink, byteCount);
      if (result != -1) {
        updateCrc(sink, offset, result);
        return result;
      }
      section = SECTION_TRAILER;
    }

    if (section == SECTION_TRAILER) {
      consumeTrailer();
      section = SECTION_DONE;

      if (!source.exhausted()) {
        throw new IOException("gzip finished without exhausting source");
      }
    }

    return -1;
  }

  private void consumeHeader() throws IOException {
    source.require(10);
    byte flags = source.buffer().getByte(3);
    boolean fhcrc = ((flags >> FHCRC) & 1) == 1;
    if (fhcrc) updateCrc(source.buffer(), 0, 10);

    short id1id2 = source.readShort();
    checkEqual("ID1ID2", (short) 0x1f8b, id1id2);
    source.skip(8);

    if (((flags >> FEXTRA) & 1) == 1) {
      source.require(2);
      if (fhcrc) updateCrc(source.buffer(), 0, 2);
      int xlen = source.buffer().readShortLe();
      source.require(xlen);
      if (fhcrc) updateCrc(source.buffer(), 0, xlen);
      source.skip(xlen);
    }
    if (((flags >> FNAME) & 1) == 1) {
      long index = source.indexOf((byte) 0);
      if (index == -1) throw new EOFException();
      if (fhcrc) updateCrc(source.buffer(), 0, index + 1);
      source.skip(index + 1);
    }

    if (((flags >> FCOMMENT) & 1) == 1) {
      long index = source.indexOf((byte) 0);
      if (index == -1) throw new EOFException();
      if (fhcrc) updateCrc(source.buffer(), 0, index + 1);
      source.skip(index + 1);
    }

    if (fhcrc) {
      checkEqual("FHCRC", source.readShortLe(), (short) crc.getValue());
      crc.reset();
    }
  }

  private void consumeTrailer() throws IOException {
    checkEqual("CRC", source.readIntLe(), (int) crc.getValue());
    checkEqual("ISIZE", source.readIntLe(), (int) inflater.getBytesWritten());
  }

  @Override public Timeout timeout() {
    return source.timeout();
  }

  @Override public void close() throws IOException {
    inflaterSource.close();
  }

  private void updateCrc(Buffer buffer, long offset, long byteCount) {
    Segment s = buffer.segmentList.getFirst();
    for (; offset >= (s.rear - s.front); s = s.next) {
      offset -= (s.rear - s.front);
    }
    for (; byteCount > 0; s = s.next) {
      int pos = (int) (s.front + offset);
      int toUpdate = (int) Math.min(s.rear - pos, byteCount);
      crc.update(s.data, pos, toUpdate);
      byteCount -= toUpdate;
      offset = 0;
    }
  }

  private void checkEqual(String name, int expected, int actual) throws IOException {
    if (actual != expected) {
      throw new IOException(String.format(
          "%s: actual 0x%08x != expected 0x%08x", name, actual, expected));
    }
  }
}
