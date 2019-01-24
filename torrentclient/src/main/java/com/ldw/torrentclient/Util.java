package com.ldw.torrentclient;

import java.util.Locale;
import java.util.concurrent.ThreadFactory;

/**
 * Created by ldw
 * time :2019/1/23.
 */
public class Util {
    /** Returns a {@link Locale#CHINA} formatted {@link String}. */
    public static String format(String format, Object... args) {
        return String.format(Locale.CHINA, format, args);
    }
    public static ThreadFactory threadFactory(final String name, final boolean daemon) {
        return new ThreadFactory() {
            @Override public Thread newThread(Runnable runnable) {
                Thread result = new Thread(runnable, name);
                result.setDaemon(daemon);
                return result;
            }
        };
    }

}
