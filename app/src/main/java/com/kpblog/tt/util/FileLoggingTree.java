package com.kpblog.tt.util;

import android.content.Context;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.html.HTMLLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.StatusPrinter;
import timber.log.Timber;

public class FileLoggingTree extends Timber.DebugTree {

    /*
    http://www.sureshjoshi.com/mobile/file-logging-in-android-with-timber/
     */
    private static Logger mLogger = LoggerFactory.getLogger(FileLoggingTree.class);

    public FileLoggingTree(Context context) {
        File logFolder = Util.getLocalLogFolder();

        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }
        final String logDirectory = logFolder.getAbsolutePath();
        configureLogger(logDirectory);
    }

    private void configureLogger(String logDirectory) {
        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();

        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setContext(loggerContext);
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setFile(logDirectory + "/" + "latest.txt");

        SizeAndTimeBasedFNATP<ILoggingEvent> fileNamingPolicy = new SizeAndTimeBasedFNATP<>();
        fileNamingPolicy.setContext(loggerContext);
        fileNamingPolicy.setMaxFileSize(FileSize.valueOf("1 mb"));

        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setFileNamePattern(logDirectory + "/" + ".%d{yyyy-MM-dd}.%i.txt");
        rollingPolicy.setMaxHistory(7);
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(fileNamingPolicy);
        rollingPolicy.setParent(rollingFileAppender);  // parent and context required!
        rollingPolicy.start();

        /*HTMLLayout htmlLayout = new HTMLLayout();
        htmlLayout.setContext(loggerContext);
        htmlLayout.setPattern("%d{HH:mm:ss.SSS}%level%thread%msg");
        htmlLayout.start();

        LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
        encoder.setContext(loggerContext);
        encoder.setLayout(htmlLayout);
        encoder.start();*/

        //Alternative text encoder - very clean pattern, takes up less space
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setCharset(Charset.forName("UTF-8"));
        encoder.setPattern("%date %level [%thread] %msg%n");
        encoder.start();

        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setEncoder(encoder);
        rollingFileAppender.start();

        // add the newly created appenders to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);
        root.addAppender(rollingFileAppender);

        // print any status messages (warnings, etc) encountered in logback config
        StatusPrinter.print(loggerContext);
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (priority == Log.VERBOSE) {
            return;
        }

        String logMessage = tag + ": " + message;
        switch (priority) {
            case Log.DEBUG:
                mLogger.debug(logMessage);
                break;
            case Log.INFO:
                mLogger.info(logMessage);
                break;
            case Log.WARN:
                mLogger.warn(logMessage);
                break;
            case Log.ERROR:
                mLogger.error(logMessage);
                break;
        }
    }

    @Override
    protected String createStackElementTag(StackTraceElement element) {
        return String.format("[%s#%s:%s]", super.createStackElementTag(element), element.getMethodName(), element.getLineNumber());
    }

}
