package com.kivislime.filestorage.util;

import java.util.ArrayList;
import java.util.List;

public class ResourceParserUtil {
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

    //Using in mapper
    public static String getParentPath(String objectKey) {
        int index = objectKey.lastIndexOf('/');
        return index == -1 ? "" : objectKey.substring(0, index + 1);
    }

    public static String getExtension(String path) {
        int index = path.lastIndexOf('.');
        return index == -1 ? "" : path.substring(index + 1);
    }


    public static List<String> buildPrefixes(String path) {
        String[] parts = path.split("/");
        List<String> prefixes = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(part).append("/");
            prefixes.add(sb.toString());
        }
        return prefixes;
    }
}
