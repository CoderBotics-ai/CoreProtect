package net.coreprotect.language;

import java.util.concurrent.ConcurrentHashMap;

public class Language {

    private static final ConcurrentHashMap<Phrase, String> phrases = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Phrase, String> userPhrases = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Phrase, String> translatedPhrases = new ConcurrentHashMap<>();

    protected static String getPhrase(Phrase phrase) {
        return phrases.get(phrase);
    }

    protected static String getUserPhrase(Phrase phrase) {
        return userPhrases.get(phrase);
    }

    protected static String getTranslatedPhrase(Phrase phrase) {
        return translatedPhrases.get(phrase);
    }

    protected static void setUserPhrase(Phrase phrase, String value) {
        userPhrases.put(phrase, value);
    }

    protected static void setTranslatedPhrase(Phrase phrase, String value) {
        translatedPhrases.put(phrase, value);
    }

    public static void loadPhrases() {
        phrases.put(Phrase.ACTION_NOT_SUPPORTED, "That action is not supported by the command.");
        phrases.put(Phrase.AMOUNT_BLOCK, "{0} {block|blocks}");
        phrases.put(Phrase.AMOUNT_CHUNK, "{0} {chunk|chunks}");
        phrases.put(Phrase.AMOUNT_ENTITY, "{0} {entity|entities}");
        phrases.put(Phrase.AMOUNT_ITEM, "{0} {item|items}");
        phrases.put(Phrase.API_TEST, "API test successful.");
        phrases.put(Phrase.CACHE_ERROR, "WARNING: Error while validating {0} cache.");
        phrases.put(Phrase.CACHE_RELOAD, "Forcing reload of {mapping|world} caches from database.");
        phrases.put(Phrase.CHECK_CONFIG, "Please check config.yml");
        phrases.put(Phrase.COMMAND_CONSOLE, "Please run the command from the console.");
        phrases.put(Phrase.COMMAND_NOT_FOUND, "Command \"{0}\" not found.");
        phrases.put(Phrase.COMMAND_THROTTLED, "Please wait a moment and try again.");
        phrases.put(Phrase.CONSUMER_ERROR, "Consumer queue processing already {paused|resumed}.");
        phrases.put(Phrase.CONSUMER_TOGGLED, "Consumer queue processing has been {paused|resumed}.");
        phrases.put(Phrase.CONTAINER_HEADER, "Container Transactions");
        phrases.put(Phrase.CPU_CORES, "CPU cores.");
        phrases.put(Phrase.DATABASE_BUSY, "Database busy. Please try again later.");
        phrases.put(Phrase.DATABASE_INDEX_ERROR, "Unable to validate database indexes.");
        phrases.put(Phrase.DATABASE_LOCKED_1, "Database locked. Waiting up to 15 seconds...");
        phrases.put(Phrase.DATABASE_LOCKED_2, "Database is already in use. Please try again.");
        phrases.put(Phrase.DATABASE_LOCKED_3, "To disable database locking, set \"database-lock: false\".");
        phrases.put(Phrase.DATABASE_LOCKED_4, "Disabling database locking can result in data corruption.");
        phrases.put(Phrase.DATABASE_UNREACHABLE, "Database is unreachable. Discarding data and shutting down.");
        phrases.put(Phrase.DEVELOPMENT_BRANCH, "Development branch detected, skipping patch scripts.");
        phrases.put(Phrase.DIRT_BLOCK, "Placed a temporary safety block under you.");
        phrases.put(Phrase.DISABLE_SUCCESS, "Success! Disabled {0}");
        phrases.put(Phrase.ENABLE_FAILED, "{0} was unable to start.");
        phrases.put(Phrase.ENABLE_SUCCESS, "{0} has been successfully enabled!");
        phrases.put(Phrase.ENJOY_COREPROTECT, "Enjoy {0}? Join our Discord!");
        phrases.put(Phrase.FINISHING_CONVERSION, "Finishing up data conversion. Please wait...");
        phrases.put(Phrase.FINISHING_LOGGING, "Finishing up data logging. Please wait...");
        phrases.put(Phrase.FIRST_VERSION, "Initial DB: {0}");
        phrases.put(Phrase.GLOBAL_LOOKUP, "Don't specify a radius to do a global lookup.");
        phrases.put(Phrase.GLOBAL_ROLLBACK, "Use \"{0}\" to do a global {rollback|restore}");
        phrases.put(Phrase.HELP_ACTION_1, "Restrict the lookup to a certain action.");
        phrases.put(Phrase.HELP_ACTION_2, "Examples: [a:block], [a:+block], [a:-block] [a:click], [a:container], [a:inventory], [a:item], [a:kill], [a:chat], [a:command], [a:sign], [a:session], [a:username]");
        phrases.put(Phrase.HELP_COMMAND, "Display more info for that command.");
        // Additional phrases can be added here as needed
    }
}