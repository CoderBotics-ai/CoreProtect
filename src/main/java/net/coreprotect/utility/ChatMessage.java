package net.coreprotect.utility;

import org.bukkit.ChatColor;

public class ChatMessage {

    /**
     * Returns the plugin name with the DARK_AQUA chat color.
     */
    private final String pluginName = Color.DARK_AQUA + "CoreProtect";

    private String message;
    private String textColor = Color.WHITE;
    private String textStyle = "";
    private String separator = "-";
    private boolean useTag = true;
    private boolean useSpaces = true;

    public ChatMessage() {
        this.message = "";
    }

    public ChatMessage setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public ChatMessage setColor(String color) {
        this.textColor = color;
        return this;
    }

    public ChatMessage setStyle(String style) {
        this.textStyle = style;
        return this;
    }

    public ChatMessage useTag(boolean tag) {
        this.useTag = tag;
        return this;
    }

    public ChatMessage useSpaces(boolean spaces) {
        this.useSpaces = spaces;
        return this;
    }

    public ChatMessage append(String color, String string) {
        this.message += color + string; // Using += for better readability
        return this;
    }

    public ChatMessage(String string) {
        this.message = parseQuotes(string, this.textColor);
    }

    public String build(boolean tag) {
        useTag(tag);
        return build();
    }

    public String build(String color) {
        setColor(color);
        return build();
    }

    public String build(boolean tag, String color) {
        useTag(tag);
        setColor(color);
        return build();
    }

    public String build(String color, String style) {
        setColor(color);
        setStyle(style);
        return build();
    }

    public String build(boolean tag, String color, String style) {
        useTag(tag);
        setColor(color);
        setStyle(style);
        return build();
    }

    public String build(String separator, String color, String style) {
        setSeparator(separator);
        setColor(color);
        setStyle(style);
        return build();
    }

    public String build(boolean tag, String separator, String color, String style) {
        useTag(tag);
        setSeparator(separator);
        setColor(color);
        setStyle(style);
        return build();
    }

    public static String parseQuotes(String string, String textColor) {
        int indexFirst = string.indexOf("\"");
        int indexLast = string.lastIndexOf("\"");
        if (indexFirst > -1 && indexLast > indexFirst) {
            String quoteText = string.substring(indexFirst + 1, indexLast);
            string = string.replace(quoteText, Color.DARK_AQUA + ChatColor.stripColor(quoteText) + textColor);
        }

        return string;
    }

    private static String createSpaces(String string, boolean separatorOffset, boolean createSpaces) {
        if (!createSpaces) {
            return "";
        }

        int count = (string.length() - string.replace(String.valueOf(ChatColor.COLOR_CHAR), "").length()) * 2;
        int length = (int) ((string.length() - count) * 1.4);
        if (separatorOffset) {
            length += 2;
        }
        return " ".repeat(length); // Using String.repeat for better readability
    }

    public String build() {
        return (this.useTag ? pluginName : createSpaces(pluginName, true, this.useSpaces)) + this.textColor + " " + this.separator + " " + this.textStyle + this.message;
    }

    @Override
    public String toString() {
        return this.message;
    }
}