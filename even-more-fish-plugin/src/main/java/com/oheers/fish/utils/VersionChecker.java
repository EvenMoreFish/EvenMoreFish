package com.oheers.fish.utils;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.NotNull;

public class VersionChecker {

    public static boolean isNewerThan(@NotNull String ver1, @NotNull String ver2) {
        return compareVersions(ver1, ver2) > 0;
    }

    public static boolean isNewerThanOrEqualTo(@NotNull String ver1, @NotNull String ver2) {
        int value = compareVersions(ver1, ver2);
        return value >= 0;
    }

    public static boolean isOlderThan(@NotNull String ver1, @NotNull String ver2) {
        return compareVersions(ver1, ver2) < 0;
    }

    public static boolean isOlderThanOrEqualTo(@NotNull String ver1, @NotNull String ver2) {
        int value = compareVersions(ver1, ver2);
        return value <= 0;
    }

    public static boolean isEqualTo(@NotNull String ver1, @NotNull String ver2) {
        return compareVersions(ver1, ver2) == 0;
    }

    private static int compareVersions(@NotNull String ver1, @NotNull String ver2) {
        ComparableVersion one = new ComparableVersion(ver1);
        ComparableVersion two = new ComparableVersion(ver2);
        return one.compareTo(two);
    }

}
