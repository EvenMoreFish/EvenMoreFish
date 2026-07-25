package com.oheers.fish.utils;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jspecify.annotations.NonNull;

public class VersionChecker {

    public static boolean isNewerThan(@NonNull String ver1, @NonNull String ver2) {
        return compareVersions(ver1, ver2) > 0;
    }

    public static boolean isNewerThanOrEqualTo(@NonNull String ver1, @NonNull String ver2) {
        int value = compareVersions(ver1, ver2);
        return value >= 0;
    }

    public static boolean isOlderThan(@NonNull String ver1, @NonNull String ver2) {
        return compareVersions(ver1, ver2) < 0;
    }

    public static boolean isOlderThanOrEqualTo(@NonNull String ver1, @NonNull String ver2) {
        int value = compareVersions(ver1, ver2);
        return value <= 0;
    }

    public static boolean isEqualTo(@NonNull String ver1, @NonNull String ver2) {
        return compareVersions(ver1, ver2) == 0;
    }

    private static int compareVersions(@NonNull String ver1, @NonNull String ver2) {
        ComparableVersion one = new ComparableVersion(ver1);
        ComparableVersion two = new ComparableVersion(ver2);
        return one.compareTo(two);
    }

}
