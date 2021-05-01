package de.eldoria.eldoutilities.logging;

import de.eldoria.eldoutilities.configuration.EldoConfig;
import de.eldoria.eldoutilities.plugin.EldoPlugin;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This class is a hell of a shitload.
 * But it allows us to do some debug logging based on a config setting without requesting a boolean everytime.
 * Since spigot lets all messages disappear above INFO level wee sadly need this workaround.
 */
public class DebugLogger extends Logger {
    private static final String DEBUG = "§b [DEBUG] ";
    private static final String FINE = "§3 [FINE] ";
    private static final String FINER = "§9 [FINER] ";
    private static final String FINEST = "§1 [FINEST] ";
    private final Logger logger;

    public DebugLogger(EldoPlugin plugin, Logger logger) {
        // we still want to use our debugger. so we dont really care about what we are doing here.
        super(plugin.getName(), null);
        this.logger = logger;
        setLevel(EldoConfig.getLogLevel());
        log(getLevel(), "Debug logger initialized. Log Level: " + getLevel().getName());
    }

    @Override
    public void log(LogRecord record) {
        int level = record.getLevel().intValue();
        // check if the level is lover than info.
        // if thats the case we need to change it.
        if (level < 800) {
            // check if we really want to log this level
            if (getLevel().intValue() > level) return;
            // We need to set the level to info to log this shit. Thanks spigot.
            // Lets append another color to differ between them.
            // They will all be displayed as info anyway...
            record.setLevel(Level.INFO);
            if (level == Level.CONFIG.intValue()) {
                appendPrefix(record, DEBUG);
            } else if (level == Level.FINE.intValue()) {
                appendPrefix(record, FINE);
            } else if (level == Level.FINER.intValue()) {
                appendPrefix(record, FINER);
            } else if (level == Level.FINEST.intValue()) {
                appendPrefix(record, FINEST);
            }
        }
        logger.log(record);
    }

    @Override
    public String getName() {
        return getParent().getName();
    }

    @Override
    public Logger getParent() {
        return logger.getParent();
    }

    @Override
    public void setParent(Logger parent) {
        logger.setParent(parent);
    }

    private void appendPrefix(LogRecord record, String prefix) {
        record.setMessage(prefix + record.getMessage());
    }
}
