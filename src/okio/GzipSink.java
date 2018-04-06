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

import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import static java.util.zip.Deflater.DEFAULT_COMPRESSION;

public final class GzipSink implements Sink {

  private final BufferedSink sink;
  private final Deflater deflater;
  private final DeflaterSink deflaterSink;

  private boolean closed;

  private final CRC32 crc = new CRC32();

  public GzipSink(Sink sink) {
    if (sink == null) throw new IllegalArgumentException("sink == null");
    this.deflater = new Deflater(DEFAULT_COMPRESSION, true /* No wrap */);
    this.sink = Okio.buffer(sink);
    this.deflaterSink = new DeflaterSink(this.sink, deflater);
    writeHeader();
  }

  long totalWrite = 0;
  @Override
  public void write(Buffer source, long byteCount) throws IOException {
    System.out.println("write 1");
    if (byteCount < 0) {
      throw new IllegalArgumentException("byteCount < 0: " + byteCount);
    }
    if (byteCount == 0) {
      return;
    }
    updateCrc(source, byteCount);
    totalWrite += byteCount;
    deflaterSink.write(source, byteCount);
    System.out.println("" +totalWrite);
  }

  @Override
  public void flush() throws IOException {
    deflaterSink.flush();
  }

  @Override
  public Timeout timeout() {
    return sink.timeout();
  }

  @Override public void close() throws IOException {
    if (closed) return;

    Throwable thrown = null;
    try {
      deflaterSink.finishDeflate();
      writeFooter();
    } catch (Throwable e) {
      thrown = e;
    }

    try {
      deflater.end();
    } catch (Throwable e) {
      if (thrown == null) thrown = e;
    }

    try {
      sink.close();
    } catch (Throwable e) {
      if (thrown == null) thrown = e;
    }
    closed = true;

    if (thrown != null) Util.sneakyRethrow(thrown);
  }

  public Deflater deflater() {
    return deflater;
  }

  private void writeHeader() {
    Buffer buffer = this.sink.buffer();
    buffer.writeShort((short) 0x1f8b); // Two-byte Gzip ID.
    buffer.writeByte((byte) 0x08); // 8 == Deflate compression method.
    buffer.writeByte((byte) 0x00); // No flags.
    buffer.writeInt(0x00); // No modification time.
    buffer.writeByte((byte) 0x00); // No extra flags.
    buffer.writeByte((byte) 0x00); // No OS.
  }

  private void writeFooter() throws IOException {
    sink.writeIntLe((int) crc.getValue()); // CRC of original data.
    sink.writeIntLe((int) deflater.getBytesRead()); // Length of original data.
  }

  private void updateCrc(Buffer buffer, long byteCount) {
    for (Segment head = buffer.segmentList.getFirst(); byteCount > 0; head = head.next) {
      int segmentLength = (int) Math.min(byteCount, head.rear - head.front);
      crc.update(head.data, head.front, segmentLength);
      byteCount -= segmentLength;
    }
  }
}
