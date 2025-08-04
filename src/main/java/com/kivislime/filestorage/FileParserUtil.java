package com.kivislime.filestorage;

public class FileParserUtil {
    public static String getNameFromPath(String path) {
        int index = path.lastIndexOf('/');
        if (index == -1) {
            return "";
        }
        if (index == path.length() - 1) {
            return path.substring(path.lastIndexOf('/', index - 1) + 1, index);
        }
        return path.substring(index + 1);
    }

    public static String getParentPath(String objectKey) {
        int index = objectKey.lastIndexOf('/');
        return index == -1 ? "" : objectKey.substring(0, index + 1);
    }
}
