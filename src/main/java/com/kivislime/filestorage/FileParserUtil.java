package com.kivislime.filestorage;

import org.springframework.stereotype.Component;

@Component
//TODO: надо ли его вообще помечаьт компонентом?
public class FileParserUtil {
    public static String findFileName(String objectKey) {
        int index = objectKey.lastIndexOf('/');
        return (index == -1 || index == objectKey.length() - 1)
                ? "" : objectKey.substring(index + 1);
    }

    public static String findPathToFile(String objectKey) {
        int index = objectKey.lastIndexOf('/');
        return index < 0 ? "" : objectKey.substring(0, index + 1);
    }
}
