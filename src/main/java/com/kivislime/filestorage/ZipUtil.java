package com.kivislime.filestorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    private ZipUtil() {
    }

    public static ZipResult createArchiveHierarchy(Map<String, InputStream> downloadedFileStreams,
                                                   List<UserFile> userFiles) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(baos)) {

            for (UserFile userFile : userFiles) {
                String key = userFile.getObjectKey();
                InputStream is = downloadedFileStreams.get(key);

                addEntry(zipOut, key, userFiles.size(), is);
            }
            zipOut.finish();

            byte[] bytes = baos.toByteArray();
            return new ZipResult(bytes, bytes.length);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create archive hierarchy", e);
        }
    }

    private static void addEntry(ZipOutputStream zos,
                                 String entryName,
                                 long size,
                                 InputStream in) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        if (in != null) {
            entry.setSize(size);
        }
        zos.putNextEntry(entry);
        if (in != null) {
            try (in) {
                in.transferTo(zos);
            }
        }
        zos.closeEntry();
    }
}
