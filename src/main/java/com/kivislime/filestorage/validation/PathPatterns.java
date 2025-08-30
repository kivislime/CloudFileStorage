package com.kivislime.filestorage.validation;

public final class PathPatterns {
    private PathPatterns() {}

    private static final String NO_DOT_DOT = "(?!.*(?:^|/)\\.\\.($|/))";
    private static final String NO_DOUBLE_SLASH = "(?!.*//)";
    private static final String NO_LEADING = "(?!\\.?/).*";

    private static final String SEGMENT = "[^/]+";

    public static final String DIRECTORY = "^" + NO_DOT_DOT + NO_DOUBLE_SLASH + NO_LEADING + "(?:" + SEGMENT + "/)+$";
    public static final String FILE = "^" + NO_DOT_DOT + NO_DOUBLE_SLASH + NO_LEADING + "(?:" + SEGMENT + "/)*" + SEGMENT + "(?:\\.[^/]+)*$";
    public static final String RESOURCE = "^(?:" + FILE + "|" + DIRECTORY + ")$";

    public static final String DIRECTORY_PARAM = "^" + NO_DOT_DOT + NO_DOUBLE_SLASH + NO_LEADING + "(?:|(?:" + SEGMENT + "/)+)$";
}
