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

package org.apache.commons.io.file;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.test.TestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemProperties;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link PathUtils}.
 */
class PathUtilsTest extends AbstractTempDirTest {

    private static final String STRING_FIXTURE = "Hello World";

    private static final byte[] BYTE_ARRAY_FIXTURE = STRING_FIXTURE.getBytes(StandardCharsets.UTF_8);

    private static final String TEST_JAR_NAME = "test.jar";

    private static final String TEST_JAR_PATH = "src/test/resources/org/apache/commons/io/test.jar";

    private static final String PATH_FIXTURE = "NOTICE.txt";

    private Path current() {
        return PathUtils.current();
    }

    private Long getLastModifiedMillis(final Path file) throws IOException {
        return Files.getLastModifiedTime(file).toMillis();
    }

    private Path getNonExistentPath() {
        return Paths.get("/does not exist/for/certain");
    }

    private FileSystem openArchive(final Path p, final boolean createNew) throws IOException {
        if (createNew) {
            final Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            final URI fileUri = p.toAbsolutePath().toUri();
            final URI uri = URI.create("jar:" + fileUri.toASCIIString());
            return FileSystems.newFileSystem(uri, env, null);
        }
        return FileSystems.newFileSystem(p, (ClassLoader) null);
    }

    private void setLastModifiedMillis(final Path file, final long millis) throws IOException {
        Files.setLastModifiedTime(file, FileTime.fromMillis(millis));
    }

    @Test
    void testCopyDirectoryForDifferentFilesystemsWithAbsolutePath() throws IOException {
        final Path archivePath = Paths.get(TEST_JAR_PATH);
        try (FileSystem archive = openArchive(archivePath, false)) {
            // relative jar -> absolute dir
            Path sourceDir = archive.getPath("dir1");
            PathUtils.copyDirectory(sourceDir, tempDirPath);
            assertTrue(Files.exists(tempDirPath.resolve("f1")));

            // absolute jar -> absolute dir
            sourceDir = archive.getPath("/next");
            PathUtils.copyDirectory(sourceDir, tempDirPath);
            assertTrue(Files.exists(tempDirPath.resolve("dir")));
        }
    }

    @Test
    void testCopyDirectoryForDifferentFilesystemsWithAbsolutePathReverse() throws IOException {
        try (FileSystem archive = openArchive(tempDirPath.resolve(TEST_JAR_NAME), true)) {
            // absolute dir -> relative jar
            Path targetDir = archive.getPath("target");
            Files.createDirectory(targetDir);
            final Path sourceDir = Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2").toAbsolutePath();
            PathUtils.copyDirectory(sourceDir, targetDir);
            assertTrue(Files.exists(targetDir.resolve("dirs-a-file-size-1")));

            // absolute dir -> absolute jar
            targetDir = archive.getPath("/");
            PathUtils.copyDirectory(sourceDir, targetDir);
            assertTrue(Files.exists(targetDir.resolve("dirs-a-file-size-1")));
        }
    }

    @Test
    void testCopyDirectoryForDifferentFilesystemsWithRelativePath() throws IOException {
        final Path archivePath = Paths.get(TEST_JAR_PATH);
        try (FileSystem archive = openArchive(archivePath, false);
                FileSystem targetArchive = openArchive(tempDirPath.resolve(TEST_JAR_NAME), true)) {
            final Path targetDir = targetArchive.getPath("targetDir");
            Files.createDirectory(targetDir);
            // relative jar -> relative dir
            Path sourceDir = archive.getPath("next");
            PathUtils.copyDirectory(sourceDir, targetDir);
            assertTrue(Files.exists(targetDir.resolve("dir")));

            // absolute jar -> relative dir
            sourceDir = archive.getPath("/dir1");
            PathUtils.copyDirectory(sourceDir, targetDir);
            assertTrue(Files.exists(targetDir.resolve("f1")));
        }
    }

    @Test
    void testCopyDirectoryForDifferentFilesystemsWithRelativePathReverse() throws IOException {
        try (FileSystem archive = openArchive(tempDirPath.resolve(TEST_JAR_NAME), true)) {
            // relative dir -> relative jar
            Path targetDir = archive.getPath("target");
            Files.createDirectory(targetDir);
            final Path sourceDir = Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2");
            PathUtils.copyDirectory(sourceDir, targetDir);
            assertTrue(Files.exists(targetDir.resolve("dirs-a-file-size-1")));

            // relative dir -> absolute jar
            targetDir = archive.getPath("/");
            PathUtils.copyDirectory(sourceDir, targetDir);
            assertTrue(Files.exists(targetDir.resolve("dirs-a-file-size-1")));
        }
    }

    @Test
    void testCopyFile() throws IOException {
        final Path sourceFile = Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/file-size-1.bin");
        final Path targetFile = PathUtils.copyFileToDirectory(sourceFile, tempDirPath);
        assertTrue(Files.exists(targetFile));
        assertEquals(Files.size(sourceFile), Files.size(targetFile));
    }

    @Test
    void testCopyFileTwoFileSystem() throws IOException {
        try (FileSystem archive = openArchive(Paths.get(TEST_JAR_PATH), false)) {
            final Path sourceFile = archive.getPath("next/dir/test.log");
            final Path targetFile = PathUtils.copyFileToDirectory(sourceFile, tempDirPath);
            assertTrue(Files.exists(targetFile));
            assertEquals(Files.size(sourceFile), Files.size(targetFile));
        }
    }

    @Test
    void testCopyURL() throws IOException {
        final Path sourceFile = Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/file-size-1.bin");
        final URL url = new URL("file:///" + FilenameUtils.getPath(sourceFile.toAbsolutePath().toString()) + sourceFile.getFileName());
        final Path targetFile = PathUtils.copyFileToDirectory(url, tempDirPath);
        assertTrue(Files.exists(targetFile));
        assertEquals(Files.size(sourceFile), Files.size(targetFile));
    }

    @Test
    void testCreateDirectoriesAlreadyExists() throws IOException {
        assertEquals(tempDirPath.getParent(), PathUtils.createParentDirectories(tempDirPath));
    }

    @SuppressWarnings("resource") // FileSystems.getDefault() is a singleton
    @Test
    void testCreateDirectoriesForRoots() throws IOException {
        for (final Path path : FileSystems.getDefault().getRootDirectories()) {
            final Path parent = path.getParent();
            assertNull(parent);
            assertEquals(parent, PathUtils.createParentDirectories(path));
        }
    }

    @Test
    void testCreateDirectoriesForRootsLinkOptionNull() throws IOException {
        for (final File f : File.listRoots()) {
            final Path path = f.toPath();
            assertEquals(path.getParent(), PathUtils.createParentDirectories(path, (LinkOption) null));
        }
    }

    @Test
    void testCreateDirectoriesNew() throws IOException {
        assertEquals(tempDirPath, PathUtils.createParentDirectories(tempDirPath.resolve("child")));
    }

    @Test
    void testCreateDirectoriesSymlink() throws IOException {
        final Path symlinkedDir = createTempSymbolicLinkedRelativeDir(tempDirPath);
        final String leafDirName = "child";
        final Path newDirFollowed = PathUtils.createParentDirectories(symlinkedDir.resolve(leafDirName), PathUtils.NULL_LINK_OPTION);
        assertEquals(Files.readSymbolicLink(symlinkedDir), newDirFollowed);
    }

    @Test
    void testCreateDirectoriesSymlinkClashing() throws IOException {
        final Path symlinkedDir = createTempSymbolicLinkedRelativeDir(tempDirPath);
        assertEquals(symlinkedDir, PathUtils.createParentDirectories(symlinkedDir.resolve("child")));
    }

    @Test
    void testGetBaseNamePathBaseCases() {
        assertEquals("bar", PathUtils.getBaseName(Paths.get("a/b/c/bar.foo")));
        assertEquals("foo", PathUtils.getBaseName(Paths.get("foo")));
        assertEquals("", PathUtils.getBaseName(Paths.get("")));
        assertEquals("", PathUtils.getBaseName(Paths.get(".")));
        for (final File f : File.listRoots()) {
            assertNull(PathUtils.getBaseName(f.toPath()));
        }
        if (SystemUtils.IS_OS_WINDOWS) {
            assertNull(PathUtils.getBaseName(Paths.get("C:\\")));
        }
    }

    @Test
    void testGetBaseNamePathCornerCases() {
        assertNull(PathUtils.getBaseName((Path) null));
        assertEquals("foo", PathUtils.getBaseName(Paths.get("foo.")));
        assertEquals("", PathUtils.getBaseName(Paths.get("bar/.foo")));
    }

    @Test
    void testGetDosFileAttributeView() {
        // dir
        final DosFileAttributeView dosFileAttributeView = PathUtils.getDosFileAttributeView(current());
        final Path path = Paths.get("this-file-does-not-exist-at.all");
        assertFalse(Files.exists(path));
        if (SystemUtils.IS_OS_MAC) {
            assertNull(dosFileAttributeView);
            // missing file
            assertNull(PathUtils.getDosFileAttributeView(path));
        } else {
            assertNotNull(dosFileAttributeView);
            // missing file
            assertNotNull(PathUtils.getDosFileAttributeView(path));
        }
        // null
        assertThrows(NullPointerException.class, () -> PathUtils.getDosFileAttributeView(null));
    }

    @Test
    void testGetExtension() {
        assertNull(PathUtils.getExtension(null));
        assertEquals("ext", PathUtils.getExtension(Paths.get("file.ext")));
        assertEquals("", PathUtils.getExtension(Paths.get("README")));
        assertEquals("com", PathUtils.getExtension(Paths.get("domain.dot.com")));
        assertEquals("jpeg", PathUtils.getExtension(Paths.get("image.jpeg")));
        assertEquals("", PathUtils.getExtension(Paths.get("a.b/c")));
        assertEquals("txt", PathUtils.getExtension(Paths.get("a.b/c.txt")));
        assertEquals("", PathUtils.getExtension(Paths.get("a/b/c")));
        assertEquals("", PathUtils.getExtension(Paths.get("a.b\\c")));
        assertEquals("txt", PathUtils.getExtension(Paths.get("a.b\\c.txt")));
        assertEquals("", PathUtils.getExtension(Paths.get("a\\b\\c")));
        assertEquals("", PathUtils.getExtension(Paths.get("C:\\temp\\foo.bar\\README")));
        assertEquals("ext", PathUtils.getExtension(Paths.get("../filename.ext")));

        if (File.separatorChar != '\\') {
            // Upwards compatibility:
            assertEquals("txt", PathUtils.getExtension(Paths.get("foo.exe:bar.txt")));
        }
    }

    @Test
    void testGetFileName() {
        assertNull(PathUtils.getFileName(null, null));
        assertNull(PathUtils.getFileName(null, Path::toString));
        assertNull(PathUtils.getFileName(Paths.get("/"), Path::toString));
        assertNull(PathUtils.getFileName(Paths.get("/"), Path::toString));
        assertEquals("", PathUtils.getFileName(Paths.get(""), Path::toString));
        assertEquals("a", PathUtils.getFileName(Paths.get("a"), Path::toString));
        assertEquals("a", PathUtils.getFileName(Paths.get("p", "a"), Path::toString));
    }

    @Test
    void testGetFileNameString() {
        assertNull(PathUtils.getFileNameString(Paths.get("/")));
        assertEquals("", PathUtils.getFileNameString(Paths.get("")));
        assertEquals("a", PathUtils.getFileNameString(Paths.get("a")));
        assertEquals("a", PathUtils.getFileNameString(Paths.get("p", "a")));
    }

    @Test
    void testGetLastModifiedFileTime_File_Present() throws IOException {
        assertNotNull(PathUtils.getLastModifiedFileTime(current().toFile()));
    }

    @Test
    void testGetLastModifiedFileTime_Path_Absent() throws IOException {
        assertNull(PathUtils.getLastModifiedFileTime(getNonExistentPath()));
    }

    @Test
    void testGetLastModifiedFileTime_Path_FileTime_Absent() throws IOException {
        final FileTime fromMillis = FileTime.fromMillis(0);
        assertEquals(fromMillis, PathUtils.getLastModifiedFileTime(getNonExistentPath(), fromMillis));
    }

    @Test
    void testGetLastModifiedFileTime_Path_Present() throws IOException {
        assertNotNull(PathUtils.getLastModifiedFileTime(current()));
    }

    @Test
    void testGetLastModifiedFileTime_URI_Present() throws IOException {
        assertNotNull(PathUtils.getLastModifiedFileTime(current().toUri()));
    }

    @Test
    void testGetLastModifiedFileTime_URL_Present() throws IOException, URISyntaxException {
        assertNotNull(PathUtils.getLastModifiedFileTime(current().toUri().toURL()));
    }

    @Test
    void testGetTempDirectory() {
        final Path tempDirectory = Paths.get(SystemProperties.getJavaIoTmpdir());
        assertEquals(tempDirectory, PathUtils.getTempDirectory());
    }

    @Test
    void testIsDirectory() throws IOException {
        assertFalse(PathUtils.isDirectory(null));

        assertTrue(PathUtils.isDirectory(tempDirPath));
        try (TempFile testFile1 = TempFile.create(tempDirPath, "prefix", null)) {
            assertFalse(PathUtils.isDirectory(testFile1.get()));

            Path ref = null;
            try (TempDirectory tempDir = TempDirectory.create(getClass().getCanonicalName())) {
                ref = tempDir.get();
                assertTrue(PathUtils.isDirectory(tempDir.get()));
            }
            assertFalse(PathUtils.isDirectory(ref));
        }
    }

    @Test
    void testIsPosix() throws IOException {
        boolean isPosix;
        try {
            Files.getPosixFilePermissions(current());
            isPosix = true;
        } catch (final UnsupportedOperationException e) {
            isPosix = false;
        }
        assertEquals(isPosix, PathUtils.isPosix(current()));
    }

    @Test
    void testIsPosixAbsentFile() {
        assertFalse(PathUtils.isPosix(Paths.get("ImNotHereAtAllEver.never")));
        assertFalse(PathUtils.isPosix(null));
    }

    @Test
    void testIsRegularFile() throws IOException {
        assertFalse(PathUtils.isRegularFile(null));

        assertFalse(PathUtils.isRegularFile(tempDirPath));
        try (TempFile testFile1 = TempFile.create(tempDirPath, "prefix", null)) {
            assertTrue(PathUtils.isRegularFile(testFile1.get()));

            Files.delete(testFile1.get());
            assertFalse(PathUtils.isRegularFile(testFile1.get()));
        }
    }

    @Test
    void testNewDirectoryStream() throws Exception {
        final PathFilter pathFilter = new NameFileFilter(PATH_FIXTURE);
        try (DirectoryStream<Path> stream = PathUtils.newDirectoryStream(current(), pathFilter)) {
            final Iterator<Path> iterator = stream.iterator();
            final Path path = iterator.next();
            assertEquals(PATH_FIXTURE, PathUtils.getFileNameString(path));
            assertFalse(iterator.hasNext());
        }
    }

    @Test
    void testNewOutputStreamExistingFileAppendFalse() throws IOException {
        testNewOutputStreamNewFile(false);
        testNewOutputStreamNewFile(false);
    }

    @Test
    void testNewOutputStreamExistingFileAppendTrue() throws IOException {
        testNewOutputStreamNewFile(true);
        final Path file = writeToNewOutputStream(true);
        assertArrayEquals(ArrayUtils.addAll(BYTE_ARRAY_FIXTURE, BYTE_ARRAY_FIXTURE), Files.readAllBytes(file));
    }

    void testNewOutputStreamNewFile(final boolean append) throws IOException {
        final Path file = writeToNewOutputStream(append);
        assertArrayEquals(BYTE_ARRAY_FIXTURE, Files.readAllBytes(file));
    }

    @Test
    void testNewOutputStreamNewFileAppendFalse() throws IOException {
        testNewOutputStreamNewFile(false);
    }

    @Test
    void testNewOutputStreamNewFileAppendTrue() throws IOException {
        testNewOutputStreamNewFile(true);
    }

    @Test
    void testNewOutputStreamNewFileInsideExistingSymlinkedDir() throws IOException {
        final Path symlinkDir = createTempSymbolicLinkedRelativeDir(tempDirPath);
        final Path file = symlinkDir.resolve("test.txt");
        try (OutputStream outputStream = PathUtils.newOutputStream(file, new LinkOption[] {})) {
            // empty
        }
        try (OutputStream outputStream = PathUtils.newOutputStream(file, null)) {
            // empty
        }
        try (OutputStream outputStream = PathUtils.newOutputStream(file, true)) {
            // empty
        }
        try (OutputStream outputStream = PathUtils.newOutputStream(file, false)) {
            // empty
        }
    }

    @Test
    void testReadAttributesPosix() throws IOException {
        boolean isPosix;
        try {
            Files.getPosixFilePermissions(current());
            isPosix = true;
        } catch (final UnsupportedOperationException e) {
            isPosix = false;
        }
        assertEquals(isPosix, PathUtils.readAttributes(current(), PosixFileAttributes.class) != null);
    }

    @Test
    void testReadStringEmptyFile() throws IOException {
        final Path path = Paths.get("src/test/resources/org/apache/commons/io/test-file-empty.bin");
        assertEquals(StringUtils.EMPTY, PathUtils.readString(path, StandardCharsets.UTF_8));
        assertEquals(StringUtils.EMPTY, PathUtils.readString(path, null));
    }

    @Test
    void testReadStringSimpleUtf8() throws IOException {
        final Path path = Paths.get("src/test/resources/org/apache/commons/io/test-file-simple-utf8.bin");
        final String expected = "ABC\r\n";
        assertEquals(expected, PathUtils.readString(path, StandardCharsets.UTF_8));
        assertEquals(expected, PathUtils.readString(path, null));
    }

    @Test
    void testSetReadOnlyFile() throws IOException {
        final Path resolved = tempDirPath.resolve("testSetReadOnlyFile.txt");
        // Ask now, as we are allowed before editing parent permissions.
        final boolean isPosix = PathUtils.isPosix(tempDirPath);

        // TEMP HACK
        assumeFalse(SystemUtils.IS_OS_LINUX);

        PathUtils.writeString(resolved, "test", StandardCharsets.UTF_8);
        final boolean readable = Files.isReadable(resolved);
        final boolean writable = Files.isWritable(resolved);
        final boolean regularFile = Files.isRegularFile(resolved);
        final boolean executable = Files.isExecutable(resolved);
        final boolean hidden = Files.isHidden(resolved);
        final boolean directory = Files.isDirectory(resolved);
        final boolean symbolicLink = Files.isSymbolicLink(resolved);
        // Sanity checks
        assertTrue(readable);
        assertTrue(writable);
        // Test A
        PathUtils.setReadOnly(resolved, false);
        assertTrue(Files.isReadable(resolved), "isReadable");
        assertTrue(Files.isWritable(resolved), "isWritable");
        // Again, shouldn't blow up.
        PathUtils.setReadOnly(resolved, false);
        assertTrue(Files.isReadable(resolved), "isReadable");
        assertTrue(Files.isWritable(resolved), "isWritable");
        //
        assertEquals(regularFile, Files.isReadable(resolved));
        assertEquals(executable, Files.isExecutable(resolved));
        assertEquals(hidden, Files.isHidden(resolved));
        assertEquals(directory, Files.isDirectory(resolved));
        assertEquals(symbolicLink, Files.isSymbolicLink(resolved));
        // Test B
        PathUtils.setReadOnly(resolved, true);
        if (isPosix) {
            // On POSIX, now that the parent is not WX, the file is not readable.
            assertFalse(Files.isReadable(resolved), "isReadable");
        } else {
            assertTrue(Files.isReadable(resolved), "isReadable");
        }
        assertFalse(Files.isWritable(resolved), "isWritable");
        final DosFileAttributeView dosFileAttributeView = PathUtils.getDosFileAttributeView(resolved);
        if (dosFileAttributeView != null) {
            assertTrue(dosFileAttributeView.readAttributes().isReadOnly());
        }
        if (isPosix) {
            assertFalse(Files.isReadable(resolved));
        } else {
            assertEquals(regularFile, Files.isReadable(resolved));
        }
        assertEquals(executable, Files.isExecutable(resolved));
        assertEquals(hidden, Files.isHidden(resolved));
        assertEquals(directory, Files.isDirectory(resolved));
        assertEquals(symbolicLink, Files.isSymbolicLink(resolved));
        //
        PathUtils.setReadOnly(resolved, false);
        PathUtils.deleteFile(resolved);
    }

    @Test
    void testSetReadOnlyFileAbsent() {
        assertThrows(IOException.class, () -> PathUtils.setReadOnly(Paths.get("does-not-exist-at-all-ever-never"), true));
    }

    @Test
    void testTouch() throws IOException {
        assertThrows(NullPointerException.class, () -> FileUtils.touch(null));

        final Path file = managedTempDirPath.resolve("touch.txt");
        Files.deleteIfExists(file);
        assertFalse(Files.exists(file), "Bad test: test file still exists");
        PathUtils.touch(file);
        assertTrue(Files.exists(file), "touch() created file");
        try (OutputStream out = Files.newOutputStream(file)) {
            assertEquals(0, Files.size(file), "Created empty file.");
            out.write(0);
        }
        assertEquals(1, Files.size(file), "Wrote one byte to file");
        final long y2k = new GregorianCalendar(2000, 0, 1).getTime().getTime();
        setLastModifiedMillis(file, y2k); // 0L fails on Win98
        assertEquals(y2k, getLastModifiedMillis(file), "Bad test: set lastModified set incorrect value");
        final long nowMillis = System.currentTimeMillis();
        PathUtils.touch(file);
        assertEquals(1, Files.size(file), "FileUtils.touch() didn't empty the file.");
        assertNotEquals(y2k, getLastModifiedMillis(file), "FileUtils.touch() changed lastModified");
        final int delta = 3000;
        assertTrue(getLastModifiedMillis(file) >= nowMillis - delta, "FileUtils.touch() changed lastModified to more than now-3s");
        assertTrue(getLastModifiedMillis(file) <= nowMillis + delta, "FileUtils.touch() changed lastModified to less than now+3s");
    }

    @Test
    void testWriteStringToFile1() throws Exception {
        final Path file = tempDirPath.resolve("write.txt");
        PathUtils.writeString(file, "Hello \u1234", StandardCharsets.UTF_8);
        final byte[] text = "Hello \u1234".getBytes(StandardCharsets.UTF_8);
        TestUtils.assertEqualContent(text, file);
    }

    /**
     * Tests newOutputStream() here and don't use Files.write obviously.
     */
    private Path writeToNewOutputStream(final boolean append) throws IOException {
        final Path file = tempDirPath.resolve("test1.txt");
        try (OutputStream os = PathUtils.newOutputStream(file, append)) {
            os.write(BYTE_ARRAY_FIXTURE);
        }
        return file;
    }

}
