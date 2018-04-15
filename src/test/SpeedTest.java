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
package test;

import okio.BufferedSource;
import okio.Okio;
import okio.RealBufferedSink;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

public final class SpeedTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void readWriteFile() throws Exception {
    File file = temporaryFolder.newFile();
    long start = System.currentTimeMillis();

    RealBufferedSink sink = (RealBufferedSink) Okio.buffer(Okio.sink(file));
    for(int i = 0; i < 10000000; i++){
      sink.writeUtf8("1234567890");
    }
    sink.close();
//    assertTrue(file.exists());
//    assertEquals(20, file.length());
    System.out.println("time: " + (System.currentTimeMillis() - start));
    System.out.println("write time: " + sink.getTotalWriteTime());
    System.out.println("buffer write time: " + sink.getBufferWriteTime());
    System.out.println("buffer writebyte time: " + sink.getBufferWriteByteTime());
    System.out.println("segment list write time: " + sink.getSegmentListWriteTime());
    BufferedSource source = Okio.buffer(Okio.source(file));
//    assertEquals("Hello, java.io file!", source.readUtf8());
    source.close();
  }

}
