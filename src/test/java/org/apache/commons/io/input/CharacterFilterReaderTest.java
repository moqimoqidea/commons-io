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
package org.apache.commons.io.input;

import static org.apache.commons.io.IOUtils.EOF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.junit.jupiter.api.Test;

class CharacterFilterReaderTest {

    private static final String STRING_FIXTURE = "ababcabcd";

    @Test
    void testInputSize0FilterSize1() throws IOException {
        final StringReader input = new StringReader("");
        final HashSet<Integer> codePoints = new HashSet<>();
        codePoints.add(Integer.valueOf('a'));
        try (CharacterFilterReader reader = new CharacterFilterReader(input, 'A')) {
            assertEquals(-1, reader.read());
        }
    }

    @Test
    void testInputSize1FilterSize1() throws IOException {
        try (StringReader input = new StringReader("a");
            CharacterFilterReader reader = new CharacterFilterReader(input, 'a')) {
            assertEquals(-1, reader.read());
        }
    }

    @Test
    void testInputSize2FilterSize1FilterAll() throws IOException {
        final StringReader input = new StringReader("aa");
        try (CharacterFilterReader reader = new CharacterFilterReader(input, 'a')) {
            assertEquals(-1, reader.read());
        }
    }

    @Test
    void testInputSize2FilterSize1FilterFirst() throws IOException {
        final StringReader input = new StringReader("ab");
        try (CharacterFilterReader reader = new CharacterFilterReader(input, 'a')) {
            assertEquals('b', reader.read());
            assertEquals(-1, reader.read());
        }
    }

    @Test
    void testInputSize2FilterSize1FilterLast() throws IOException {
        final StringReader input = new StringReader("ab");
        try (CharacterFilterReader reader = new CharacterFilterReader(input, 'b')) {
            assertEquals('a', reader.read());
            assertEquals(-1, reader.read());
        }
    }

    @Test
    void testReadFilteringEOF() {
        final StringReader input = new StringReader(STRING_FIXTURE);
        assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
            try (StringBuilderWriter output = new StringBuilderWriter();
                CharacterFilterReader reader = new CharacterFilterReader(input, EOF)) {
                int c;
                while ((c = reader.read()) != EOF) {
                    output.write(c);
                }
                assertEquals(STRING_FIXTURE, output.toString());
            }
        });
    }

    @Test
    void testReadIntoBuffer() throws IOException {
        final StringReader input = new StringReader(STRING_FIXTURE);
        try (CharacterFilterReader reader = new CharacterFilterReader(input, 'b')) {
            final char[] buff = new char[9];
            final int charCount = reader.read(buff);
            assertEquals(6, charCount);
            assertEquals("aacacd", new String(buff, 0, charCount));
        }
    }

    @Test
    void testReadUsingReader() throws IOException {
        final StringReader input = new StringReader(STRING_FIXTURE);
        try (StringBuilderWriter output = new StringBuilderWriter();
            CharacterFilterReader reader = new CharacterFilterReader(input, 'b')) {
            IOUtils.copy(reader, output);
            assertEquals("aacacd", output.toString());
        }
    }

}
