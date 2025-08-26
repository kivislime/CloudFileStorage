package com.kivislime.filestorage.validation;

public final class PathPatterns {
    private PathPatterns() {}

    public static final String DIRECTORY =
            "^(?:[A-Za-z0-9_-]+/)+$";

    public static final String FILE =
            "^(?:[A-Za-z0-9_-]+/)*[A-Za-z0-9_-]+(?:\\.[A-Za-z0-9_-]+)*$";

    public static final String RESOURCE =
            "^(?:" + FILE + "|" + DIRECTORY + ")$";

    public static final String DIRECTORY_PARAM =
            "^(?:|/|(?:[A-Za-z0-9_-]+/)+)$";
}
