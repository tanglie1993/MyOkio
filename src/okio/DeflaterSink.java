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
import java.util.zip.Deflater;

import static okio.Util.checkOffsetAndCount;

/** A {@link Sink} which forwards calls to another. Useful for subclassing. */
public class DeflaterSink implements Sink {
  private final BufferedSink sink;
  private final Deflater deflater;

  public DeflaterSink(BufferedSink sink, Deflater deflater) {
    this.sink = sink;
    this.deflater = deflater;
  }

  @Override
  public void write(Buffer source, long byteCount) throws IOException {
    checkOffsetAndCount(source.size(), 0, byteCount);
    while (byteCount > 0) {
      Segment sourceSegment = source.segmentList.getFirst();
      if(sourceSegment == null){
        return;
      }
      int toDeflate = (int) Math.min(byteCount, sourceSegment.data.length - sourceSegment.rear);
      deflater.setInput(sourceSegment.data, sourceSegment.front, toDeflate);
      deflate(false);
      byteCount -= toDeflate;
    }
  }

  private void deflate(boolean syncFlush) throws IOException {
    Buffer buffer = sink.buffer();
    while (true) {
      Segment toWrite = buffer.writableSegment(1);
      int deflated;
      if(syncFlush) {
        deflated = deflater.deflate(toWrite.data, toWrite.rear, toWrite.data.length - toWrite.rear, Deflater.SYNC_FLUSH);
      }else {
        deflated = deflater.deflate(toWrite.data, toWrite.rear, toWrite.data.length - toWrite.rear);
      }
      if (deflated > 0) {
        toWrite.rear += deflated;
      }else{
        return;
      }
    }
  }

  @Override public void flush() throws IOException {
    sink.flush();
  }

  @Override
  public Timeout timeout() {
    return sink.timeout();
  }

  @Override public void close() throws IOException {
    deflater.finish();
    deflate(false);
    deflater.end();
    sink.close();
  }

  @Override public String toString() {
    return getClass().getSimpleName() + "(" + sink.toString() + ")";
  }
}
