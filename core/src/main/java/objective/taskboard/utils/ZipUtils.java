/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

package objective.taskboard.utils;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.walk;
import static org.apache.commons.io.IOUtils.closeQuietly;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static Stream<ZipStreamEntry> stream(Path path) {
        try {
            final InputStream inputStream = Files.newInputStream(path);
            return stream(inputStream)
                    .onClose(() -> closeQuietly(inputStream));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Stream<ZipStreamEntry> stream(File file) {
        try {
            final FileInputStream fileStream = new FileInputStream(file);
            return stream(fileStream)
                    .onClose(() -> closeQuietly(fileStream));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Stream<ZipStreamEntry> stream(InputStream inputStream) {
        ZipEntryIterator it = new ZipEntryIterator(new ZipInputStream(inputStream));
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        it
                        , Spliterator.ORDERED | Spliterator.NONNULL)
                , false);
    }

    public static void unzip(File source, Path destiny) throws IOException {
        unzip(new FileInputStream(source), destiny);
    }

    public static void unzip(InputStream inputStream, Path output) {
        try (Stream<ZipStreamEntry> stream = stream(inputStream)) {
            unzip(stream, output);
        }
    }

    public static void unzip(Stream<ZipStreamEntry> stream, Path output) {
        if (output.toFile().isFile())
            throw new RuntimeException("Output must be a directory");

        try {
            stream.forEach(ze -> {
                        Path entryPath = output.resolve(ze.getName());
                        try {
                            if (ze.isDirectory()) {
                                createDirectories(entryPath);
                            } else {
                                createDirectories(entryPath.getParent());
                                copy(ze.getInputStream(), entryPath);
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
            );
        } finally {
            stream.close();
        }
    }

    public static void zip(Path input, Path output) {
        try (Stream<Path> stream = walk(input)) {
            File outputParent = output.toFile().getParentFile();
            if (outputParent != null && !outputParent.exists())
                createDirectories(outputParent.toPath());
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(output))) {
                stream
                        .filter(path -> path.toFile().isFile())
                        .forEach(path -> {
                            try {
                                Path inputDirectory = input.toFile().isDirectory() ? input : input.getParent();
                                ZipEntry entry = new ZipEntry(inputDirectory.relativize(path).toString());
                                zipOutputStream.putNextEntry(entry);
                                copy(path, zipOutputStream);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void zip(Stream<ZipStreamEntry> stream, Path output) {
        if (output.toFile().isDirectory())
            throw new RuntimeException("Output must be a file");

        try (OutputStream outputStream = Files.newOutputStream(output)) {
            zip(stream, outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void zip(Stream<ZipStreamEntry> stream, OutputStream outputStream) {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            stream.forEach(ze -> {
                ZipEntry newEntry = new ZipEntry(ze.getName());
                try {
                    zipOutputStream.putNextEntry(newEntry);
                    IOUtils.copy(ze.getInputStream(), zipOutputStream);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            stream.close();
        }
    }

    public static byte[] zipToByteArray(Stream<ZipStreamEntry> stream) {
        try(ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            zip(stream, outStream);
            return outStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class ZipEntryIterator implements Iterator<ZipStreamEntry> {

        private final ZipInputStream zipInputStream;
        private boolean checkNext = true;
        private ZipStreamEntry current;

        public ZipEntryIterator(ZipInputStream zipInputStream) {
            this.zipInputStream = zipInputStream;
        }

        @Override
        public boolean hasNext() {
            if (checkNext) {
                try {
                    current = ZipStreamEntry.wrap(zipInputStream, zipInputStream.getNextEntry());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                checkNext = false;
            }
            return current != null;
        }

        @Override
        public ZipStreamEntry next() {
            if (!hasNext())
                throw new NoSuchElementException();
            checkNext = true;
            return current;
        }
    }

    /**
     * Extends default ZipStreamEntry giving access to InputStream that can be used to change to another file.
     */
    public static class ZipStreamEntry extends ZipEntry {

        private InputStream stream;

        public ZipStreamEntry(InputStream stream, ZipEntry e) {
            super(e);
            this.stream = stream;
        }

        public InputStream getInputStream() {
            return stream;
        }

        public void setInputStream(InputStream stream) {
            this.stream = stream;
        }

        private static ZipStreamEntry wrap(ZipInputStream stream, ZipEntry e) {
            return e == null ? null : new ZipStreamEntry(stream, e);
        }
    }

}
