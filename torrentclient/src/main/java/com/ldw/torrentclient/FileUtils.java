package com.ldw.torrentclient;

import java.io.File;

public final class FileUtils {

    private FileUtils() throws InstantiationException {
        throw new InstantiationException("This class is not created for instantiation");
    }

    /**
     * Delete every item below the File location
     *
     * @param file Location
     * @return {@code true} when successful delete
     */
    public static boolean recursiveDelete(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            if (children == null) return false;
            for (String child : children) {
                recursiveDelete(new File(file, child));
            }
        }

        return file.delete();
    }

}
