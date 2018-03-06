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

import test.MockSink;

import java.io.IOException;
import java.util.zip.Deflater;

import static okio.Util.checkOffsetAndCount;

/** A {@link Sink} which forwards calls to another. Useful for subclassing. */
public class GzipSink implements Sink {
  private final BufferedSink sink;

  public GzipSink(BufferedSink sink) {
    this.sink = sink;
  }

  public GzipSink(MockSink mockSink) {
    this.sink = Okio.buffer(mockSink);
  }

  @Override
  public void write(Buffer source, long byteCount) throws IOException {

  }

  @Override
  public void flush() throws IOException {
    sink.flush();
  }

  @Override
  public Timeout timeout() {
    return sink.timeout();
  }

  @Override
  public void close() throws IOException {
    sink.close();
  }
//
//
//  @Override public String toString() {
//    return getClass().getSimpleName() + "(" + sink.toString() + ")";
//  }
}
