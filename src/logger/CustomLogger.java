package logger;

import utils.StringUtils;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class CustomLogger {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Logger log;
    private final String className;

    public CustomLogger(String className) {
        this.className = className;

        //Logger Config
        StreamHandler consoleHandler = new StreamHandler(new FileOutputStream(FileDescriptor.out), new CustomFormatter()) {
            @Override
            public synchronized void publish(LogRecord logRecord) {
                super.publish(logRecord);
                flush();
            }
        };

        log = Logger.getLogger(className);
        log.addHandler(consoleHandler);
        log.setUseParentHandlers(false);
    }


    public void log(LoggerLevel level, String message, Object... args) {
        logMessage(message, level, args);
    }

    public void info(String message, Object... args) {
        logMessage(message, LoggerLevel.INFO, args);
    }

    public void warn(String message, Object... args) {
        logMessage(message, LoggerLevel.WARNING, args);
    }

    public void error(String message, Object... args) {
        logMessage(message, LoggerLevel.ERROR, args);
    }

    public void debug(String message, Object... args) {
        logMessage(message, LoggerLevel.DEBUG, args);
    }

    private void logMessage(String message, LoggerLevel level, Object... args) {
        String logMessage = getFormattedMessage(message, level, args);
        log.info(logMessage);
        printStackTrace(new PrintStream(new FileOutputStream(FileDescriptor.out)), args);
    }

    private String getFormattedMessage(String message, LoggerLevel level, Object... args) {
        String timeStamp = LocalDateTime.now().format(formatter);
        return timeStamp + " " + level.name() + " --- " + className + ": " + StringUtils.format(message, args);
    }

    private void printStackTrace(PrintStream printStream, Object... args) {
        if (null != args) {
            for (Object arg : args) {
                if (arg instanceof Throwable e) {
                    e.printStackTrace(printStream);
                }
            }
        }
    }

    private static class CustomFormatter extends Formatter {

        @Override
        public String format(LogRecord logRecord) {
            return logRecord.getMessage() + System.lineSeparator();
        }
    }
}
