/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.IOIndexedException;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link TeeWriter}.
 */
@SuppressWarnings("resource") // not necessary to close these resources
class TeeWriterTest {

    @Test
    void testArrayIOExceptionOnAppendChar1() {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        final char data = 'A';

        final IOExceptionList e = assertThrows(IOExceptionList.class, () -> tw.append(data));
        verify(goodW).append(data);
        assertEquals(1, e.getCauseList().size());
        assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnAppendChar2() {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);
        final char data = 'A';

        final IOExceptionList e = assertThrows(IOExceptionList.class, () -> tw.append(data));
        verify(goodW).append(data);
        assertEquals(1, e.getCauseList().size());
        assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnAppendCharSequence1() {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        final String data = "A";

        final IOExceptionList e = assertThrows(IOExceptionList.class, () -> tw.append(data));
        verify(goodW).append(data);
        assertEquals(1, e.getCauseList().size());
        assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnAppendCharSequence2() {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);
        final String data = "A";

        final IOExceptionList e = assertThrows(IOExceptionList.class, () -> tw.append(data));
        verify(goodW).append(data);
        assertEquals(1, e.getCauseList().size());
        assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnAppendCharSequenceIntInt1() {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        final String data = "A";

        final IOExceptionList e = assertThrows(IOExceptionList.class, () -> tw.append(data, 0, 0));
        verify(goodW).append(data, 0, 0);
        assertEquals(1, e.getCauseList().size());
        assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnAppendCharSequenceIntInt2() {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);
        final String data = "A";

        final IOExceptionList e = assertThrows(IOExceptionList.class, () -> tw.append(data, 0, 0));
        verify(goodW).append(data, 0, 0);
        assertEquals(1, e.getCauseList().size());
        assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnClose1() throws IOException {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);

        final IOExceptionList e = assertThrows(IOExceptionList.class, tw::close);
        verify(goodW).close();
        assertEquals(1, e.getCauseList().size());
        assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnClose2() throws IOException {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);

        final IOExceptionList e = assertThrows(IOExceptionList.class, tw::close);
        verify(goodW).close();
        assertEquals(1, e.getCauseList().size());
        assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnFlush1() {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);

        final IOExceptionList e = assertThrows(IOExceptionList.class, tw::flush);
        verify(goodW).flush();
        assertEquals(1, e.getCauseList().size());
        assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnFlush2() {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);

        final IOExceptionList e = assertThrows(IOExceptionList.class, tw::flush);
        verify(goodW).flush();
        assertEquals(1, e.getCauseList().size());
        assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnWriteCharArray1() throws IOException {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        final char[] data = { 'a' };

        final IOExceptionList e = assertThrows(IOExceptionList.class, () -> tw.write(data));
        verify(goodW).write(data);
        assertEquals(1, e.getCauseList().size());
        assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnWriteCharArray2() throws IOException {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);
        final char[] data = { 'a' };

        final IOExceptionList e = assertThrows(IOExceptionList.class, () -> tw.write(data));
        verify(goodW).write(data);
        assertEquals(1, e.getCauseList().size());
        assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnWriteCharArrayIntInt1() {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        final char[] data = { 'a' };

        final IOExceptionList e = assertThrows(IOExceptionList.class, () -> tw.write(data, 0, 0));
        verify(goodW).write(data, 0, 0);
        assertEquals(1, e.getCauseList().size());
        assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnWriteCharArrayIntInt2() {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);
        final char[] data = { 'a' };

        final IOExceptionList e = assertThrows(IOExceptionList.class, () -> tw.write(data, 0, 0));
        verify(goodW).write(data, 0, 0);
        assertEquals(1, e.getCauseList().size());
        assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnWriteInt1() {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        final int data = 32;

        final IOExceptionList e = assertThrows(IOExceptionList.class, () -> tw.write(data));
        verify(goodW).write(data);
        assertEquals(1, e.getCauseList().size());
        assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnWriteInt2() {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);

        final IOExceptionList e = assertThrows(IOExceptionList.class, () -> tw.write(32));
        verify(goodW).write(32);
        assertEquals(1, e.getCauseList().size());
        assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnWriteString1() {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        final String data = "A";

        final IOExceptionList e = assertThrows(IOExceptionList.class, () -> tw.write(data));
        verify(goodW).write(data);
        assertEquals(1, e.getCauseList().size());
        assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnWriteString2() {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);
        final String data = "A";

        final IOExceptionList e = assertThrows(IOExceptionList.class, () -> tw.write(data));
        verify(goodW).write(data);
        assertEquals(1, e.getCauseList().size());
        assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnWriteStringIntInt1() {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        final String data = "A";

        final IOExceptionList e = assertThrows(IOExceptionList.class, () -> tw.write(data, 0, 0));
        verify(goodW).write(data, 0, 0);
        assertEquals(1, e.getCauseList().size());
        assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testArrayIOExceptionOnWriteStringIntInt2() {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);
        final String data = "A";

        final IOExceptionList e = assertThrows(IOExceptionList.class, () -> tw.write(data, 0, 0));
        verify(goodW).write(data, 0, 0);
        assertEquals(1, e.getCauseList().size());
        assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testCollectionCloseBranchIOException() throws IOException {
        final Writer badW = BrokenWriter.INSTANCE;
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(Arrays.asList(goodW, badW, null));

        final IOExceptionList e = assertThrows(IOExceptionList.class, tw::close);
        verify(goodW).close();
        assertEquals(1, e.getCauseList().size());
        assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
    }

    @Test
    void testConstructorsNull() throws IOException {
        try (TeeWriter teeWriter = new TeeWriter((Writer[]) null)) {
            // Call any method, should not throw
            teeWriter.append('a');
            teeWriter.flush();
        }
        try (TeeWriter teeWriter = new TeeWriter((Collection<Writer>) null)) {
            // Call any method, should not throw
            teeWriter.append('a');
            teeWriter.flush();
        }
        assertTrue(true, "Dummy to show test completed OK");
    }

    @Test
    void testTee() throws IOException {
        final StringBuilderWriter sbw1 = new StringBuilderWriter();
        final StringBuilderWriter sbw2 = new StringBuilderWriter();
        final StringBuilderWriter expected = new StringBuilderWriter();

        try (TeeWriter tw = new TeeWriter(sbw1, sbw2, null)) {
            for (int i = 0; i < 20; i++) {
                tw.write(i);
                expected.write(i);
            }
            assertEquals(expected.toString(), sbw1.toString(), "TeeWriter.write(int)");
            assertEquals(expected.toString(), sbw2.toString(), "TeeWriter.write(int)");

            final char[] array = new char[10];
            for (int i = 20; i < 30; i++) {
                array[i - 20] = (char) i;
            }
            tw.write(array);
            expected.write(array);
            assertEquals(expected.toString(), sbw1.toString(), "TeeWriter.write(char[])");
            assertEquals(expected.toString(), sbw2.toString(), "TeeWriter.write(char[])");

            for (int i = 25; i < 35; i++) {
                array[i - 25] = (char) i;
            }
            tw.write(array, 5, 5);
            expected.write(array, 5, 5);
            assertEquals(expected.toString(), sbw1.toString(), "TeeOutputStream.write(byte[], int, int)");
            assertEquals(expected.toString(), sbw2.toString(), "TeeOutputStream.write(byte[], int, int)");

            for (int i = 0; i < 20; i++) {
                tw.append((char) i);
                expected.append((char) i);
            }
            assertEquals(expected.toString(), sbw1.toString(), "TeeWriter.append(char)");
            assertEquals(expected.toString(), sbw2.toString(), "TeeWriter.append(char)");

            for (int i = 20; i < 30; i++) {
                array[i - 20] = (char) i;
            }
            tw.append(new String(array));
            expected.append(new String(array));
            assertEquals(expected.toString(), sbw1.toString(), "TeeWriter.append(CharSequence)");
            assertEquals(expected.toString(), sbw2.toString(), "TeeWriter.append(CharSequence)");

            for (int i = 25; i < 35; i++) {
                array[i - 25] = (char) i;
            }
            tw.append(new String(array), 5, 5);
            expected.append(new String(array), 5, 5);
            assertEquals(expected.toString(), sbw1.toString(), "TeeWriter.append(CharSequence, int, int)");
            assertEquals(expected.toString(), sbw2.toString(), "TeeWriter.append(CharSequence, int, int)");

            expected.flush();
            expected.close();

            tw.flush();
        }
    }

}
