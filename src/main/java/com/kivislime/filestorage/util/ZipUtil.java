package com.kivislime.filestorage.util;

import com.kivislime.filestorage.exception.ZipCreateArchiveHierarchyException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    private ZipUtil() {
    }

    public static ZipResult createArchiveHierarchy(Map<String, Supplier<InputStream>> downloadedFileStreams,
                                                   List<String> allObjectKeys) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(baos)) {

            for (String objectKey : allObjectKeys) {
                Supplier<InputStream> supplier = downloadedFileStreams.get(objectKey);
                if (supplier == null) {
                    addEntry(zipOut, objectKey, null);
                    continue;
                }
                try (InputStream is = supplier.get()) {
                    addEntry(zipOut, objectKey, is);
                }
            }
            zipOut.finish();

            byte[] bytes = baos.toByteArray();
            return new ZipResult(bytes, bytes.length);
        } catch (IOException e) {
            throw new ZipCreateArchiveHierarchyException("Cannot create archive hierarchy I/O", e);
        }
    }

    private static void addEntry(ZipOutputStream zos,
                                 String entryName,
                                 InputStream in) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);

        zos.putNextEntry(entry);
        if (in != null) {
            try (in) {
                in.transferTo(zos);
            }
        }
        zos.closeEntry();
    }
}
