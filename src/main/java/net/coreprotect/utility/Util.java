package net.coreprotect.utility;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Jukebox;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jutils.jhardware.HardwareInfo;
import org.jutils.jhardware.model.ProcessorInfo;

import net.coreprotect.CoreProtect;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.rollback.Rollback;
import net.coreprotect.language.Phrase;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.thread.CacheHandler;
import net.coreprotect.thread.Scheduler;
import net.coreprotect.utility.serialize.ItemMetaHandler;
import net.coreprotect.worldedit.CoreProtectEditSessionEvent;

public class Util extends Queue {

    public static final java.util.regex.Pattern tagParser = java.util.regex.Pattern.compile(Chat.COMPONENT_TAG_OPEN + "(.+?)" + Chat.COMPONENT_TAG_CLOSE + "|(.+?)", java.util.regex.Pattern.DOTALL);
    private static final String NAMESPACE = "minecraft:";

    private Util() {
        throw new IllegalStateException("Utility class");
    }

    public static String getPluginVersion() {
        String version = CoreProtect.getInstance().getDescription().getVersion();
        if (version.contains("-")) {
            version = version.split("-")[0];
        }

        return version;
    }

    public static Integer[] getInternalPluginVersion() {
        int major = ConfigHandler.EDITION_VERSION;
        int minor = 0;
        int revision = 0;

        String pluginVersion = getPluginVersion();
        if (pluginVersion.contains(".")) {
            String[] versionSplit = pluginVersion.split("\\.");
            minor = Integer.parseInt(versionSplit[0]);
            revision = Integer.parseInt(versionSplit[1]);
        } else {
            minor = Integer.parseInt(pluginVersion);
        }

        return new Integer[] { major, minor, revision };
    }

    public static String getPluginName() {
        String name = CoreProtect.getInstance().getDescription().getName();
        String branch = ConfigHandler.EDITION_BRANCH;

        if (branch.startsWith("-")) {
            branch = branch.substring(1);
        }
        if (!branch.isEmpty()) {
            name += " " + branch.substring(0, 1).toUpperCase() + branch.substring(1);
        }

        return name;
    }

    public static ProcessorInfo getProcessorInfo() {
        ProcessorInfo result = null;
        try {
            Configurator.setLevel("com.profesorfalken.jsensors.manager.unix.UnixSensorsManager", Level.OFF);
            result = HardwareInfo.getProcessorInfo();
        } catch (Exception e) {
            // unable to read processor information
        }

        return result;
    }

    public static int getBlockId(Material material) {
        if (material == null) {
            material = Material.AIR;
        }
        return getBlockId(material.name(), true);
    }

    public static String getCoordinates(String command, int worldId, int x, int y, int z, boolean displayWorld, boolean italic) {
        StringBuilder message = new StringBuilder(Chat.COMPONENT_TAG_OPEN + Chat.COMPONENT_COMMAND);

        StringBuilder worldDisplay = new StringBuilder();
        if (displayWorld) {
            worldDisplay.append("/").append(Util.getWorldName(worldId));
        }

        // command
        DecimalFormat decimalFormat = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.ROOT));
        message.append("|/").append(command).append(" teleport wid:").append(worldId).append(" ").append(decimalFormat.format(x + 0.50)).append(" ").append(y).append(" ").append(decimalFormat.format(z + 0.50)).append("|");

        // chat output
        message.append(Color.GREY).append(italic ? Color.ITALIC : "").append("(x").append(x).append("/y").append(y).append("/z").append(z).append(worldDisplay).append(")");

        return message.append(Chat.COMPONENT_TAG_CLOSE).toString();
    }

    public static String getPageNavigation(String command, int page, int totalPages) {
        StringBuilder message = new StringBuilder();

        // back arrow
        String backArrow = "";
        if (page > 1) {
            backArrow = "◀ ";
            backArrow = Chat.COMPONENT_TAG_OPEN + Chat.COMPONENT_COMMAND + "|/" + command + " l " + (page - 1) + "|" + backArrow + Chat.COMPONENT_TAG_CLOSE;
        }

        // next arrow
        String nextArrow = " ";
        if (page < totalPages) {
            nextArrow = " ▶ ";
            nextArrow = Chat.COMPONENT_TAG_OPEN + Chat.COMPONENT_COMMAND + "|/" + command + " l " + (page + 1) + "|" + nextArrow + Chat.COMPONENT_TAG_CLOSE;
        }

        StringBuilder pagination = new StringBuilder();
        if (totalPages > 1) {
            pagination.append(Color.GREY).append("(");
            if (page > 3) {
                pagination.append(Color.WHITE).append(Chat.COMPONENT_TAG_OPEN).append(Chat.COMPONENT_COMMAND).append("|/").append(command).append(" l 1|").append("1 ").append(Chat.COMPONENT_TAG_CLOSE);
                if (page > 4 && totalPages > 7) {
                    pagination.append(Color.GREY).append("... ");
                } else {
                    pagination.append(Color.GREY).append("| ");
                }
            }

            int displayStart = Math.max(page - 2, 1);
            int displayEnd = Math.min(page + 2, totalPages);
            if (page > 999 || (page > 101 && totalPages > 99999)) { // limit to max 5 page numbers
                displayStart = Math.max(displayStart + 1, displayEnd);
                displayEnd = Math.min(displayEnd - 1, displayEnd);
                if (displayStart > (totalPages - 3)) {
                    displayStart = Math.max(totalPages - 3, 1);
                }
            } else { // display at least 7 page numbers
                if (displayStart > (totalPages - 5)) {
                    displayStart = Math.max(totalPages - 5, 1);
                }
                if (displayEnd < 6) {
                    displayEnd = Math.min(6, totalPages);
                }
            }

            if (page > 3 && displayStart == 1) {
                displayStart = 2;
            }

            for (int displayPage = displayStart; displayPage <= displayEnd; displayPage++) {
                if (page != displayPage) {
                    pagination.append(Color.WHITE).append(Chat.COMPONENT_TAG_OPEN).append(Chat.COMPONENT_COMMAND).append("|/").append(command).append(" l ").append(displayPage).append("|").append(displayPage).append((displayPage < totalPages ? " " : "")).append(Chat.COMPONENT_TAG_CLOSE);
                } else {
                    pagination.append(Color.WHITE).append(Color.UNDERLINE).append(displayPage).append(Color.RESET).append((displayPage < totalPages ? " " : ""));
                }
                if (displayPage < displayEnd) {
                    pagination.append(Color.GREY).append("| ");
                }
            }

            if (displayEnd < totalPages) {
                if (displayEnd < (totalPages - 1)) {
                    pagination.append(Color.GREY).append("... ");
                } else {
                    pagination.append(Color.GREY).append("| ");
                }
                if (page != totalPages) {
                    pagination.append(Color.WHITE).append(Chat.COMPONENT_TAG_OPEN).append(Chat.COMPONENT_COMMAND).append("|/").append(command).append(" l ").append(totalPages).append("|").append(totalPages).append(Chat.COMPONENT_TAG_CLOSE);
                } else {
                    pagination.append(Color.WHITE).append(Color.UNDERLINE).append(totalPages);
                }
            }

            pagination.append(Color.GREY).append(")");
        }

        return message.append(Color.WHITE).append(backArrow).append(Color.DARK_AQUA).append(Phrase.build(Phrase.LOOKUP_PAGE, Color.WHITE + page + "/" + totalPages)).append(nextArrow).append(pagination).toString();
    }

    public static String getTimeSince(long resultTime, long currentTime, boolean component) {
        StringBuilder message = new StringBuilder();
        double timeSince = currentTime - (resultTime + 0.00);
        if (timeSince < 0.00) {
            timeSince = 0.00;
        }

        // minutes
        timeSince /= 60;
        if (timeSince < 60.0) {
            message.append(Phrase.build(Phrase.LOOKUP_TIME, new DecimalFormat("0.00").format(timeSince) + "/m"));
        }

        // hours
        if (message.length() == 0) {
            timeSince /= 60;
            if (timeSince < 24.0) {
                message.append(Phrase.build(Phrase.LOOKUP_TIME, new DecimalFormat("0.00").format(timeSince) + "/h"));
            }
        }

        // days
        if (message.length() == 0) {
            timeSince /= 24;
            message.append(Phrase.build(Phrase.LOOKUP_TIME, new DecimalFormat("0.00").format(timeSince) + "/d"));
        }

        if (component) {
            Date logDate = new Date(resultTime * 1000L);
            String formattedTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(logDate);

            return Chat.COMPONENT_TAG_OPEN + Chat.COMPONENT_POPUP + "|" + Color.GREY + formattedTimestamp + "|" + Color.GREY + message.toString() + Chat.COMPONENT_TAG_CLOSE;
        }

        return message.toString();
    }

    public static String getEnchantments(byte[] metadata, int type, int amount) {
        if (metadata == null) {
            return "";
        }

        ItemStack item = new ItemStack(Util.getType(type), amount);
        item = (ItemStack) Rollback.populateItemStack(item, metadata)[2];
        String displayName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : "";
        StringBuilder message = new StringBuilder(Color.ITALIC + displayName + Color.GREY);

        List<String> enchantments = ItemMetaHandler.getEnchantments(item, displayName);
        for (String enchantment : enchantments) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(enchantment);
        }

        if (!displayName.isEmpty()) {
            message.insert(0, enchantments.isEmpty() ? Color.WHITE : Color.AQUA);
        } else if (!enchantments.isEmpty()) {
            String name = Util.capitalize(item.getType().name().replace("_", " "), true);
            message.insert(0, Color.AQUA + Color.ITALIC + name);
        }

        return message.toString();
    }

    public static String createTooltip(String phrase, String tooltip) {
        if (tooltip.isEmpty()) {
            return phrase;
        }

        StringBuilder message = new StringBuilder(Chat.COMPONENT_TAG_OPEN + Chat.COMPONENT_POPUP);

        // tooltip
        message.append("|").append(tooltip.replace("|", Chat.COMPONENT_PIPE)).append("|");

        // chat output
        message.append(phrase);

        return message.append(Chat.COMPONENT_TAG_CLOSE).toString();
    }

    public static String hoverCommandFilter(String string) {
        StringBuilder command = new StringBuilder();

        String[] data = string.toLowerCase().split(" ");
        if (data.length > 2) {
            if (data[1].equals("l")) {
                data[1] = "page";
            }

            if (data[2].startsWith("wid:")) {
                String nameWid = data[2].replaceFirst("wid:", "");
                if (nameWid.length() > 0 && nameWid.equals(nameWid.replaceAll("[^0-9]", ""))) {
                    nameWid = Util.getWorldName(Integer.parseInt(nameWid));
                    if (nameWid.length() > 0) {
                        data[2] = nameWid;
                    }
                }
            }

            if (data[1].equals("teleport") && data.length > 5) {
                data[3] = Integer.toString((int) (Double.parseDouble(data[3]) - 0.50));
                data[4] = Integer.toString(Integer.parseInt(data[4]));
                data[5] = Integer.toString((int) (Double.parseDouble(data[5]) - 0.50));
            }
        }

        for (String s : data) {
            if (s.isEmpty()) {
                continue;
            }

            if (command.length() > 0) {
                command.append(" ");
            }

            command.append(s);
        }

        return command.toString();
    }

    public static String capitalize(String string, boolean allWords) {
        if (string == null || string.isEmpty()) {
            return string;
        }

        if (string.length() <= 1) {
            return string.toUpperCase(Locale.ROOT);
        }

        string = string.toLowerCase(Locale.ROOT);

        if (allWords) {
            StringBuilder builder = new StringBuilder();
            for (String substring : string.split(" ")) {
                if (substring.length() >= 3 && !substring.equals("and") && !substring.equals("the")) {
                    substring = substring.substring(0, 1).toUpperCase(Locale.ROOT) + substring.substring(1);
                }
                if (builder.length() > 0) {
                    builder.append(" ");
                }
                builder.append(substring);
            }

            return builder.toString();
        }

        return string.substring(0, 1).toUpperCase(Locale.ROOT) + string.substring(1);
    }

    public static int getBlockId(Material material) {
        if (material == null) {
            material = Material.AIR;
        }
        return getBlockId(material.name(), true);
    }

    public static String getWorldName(int id) {
        String name = "";
        try {
            if (ConfigHandler.worldsReversed.get(id) != null) {
                name = ConfigHandler.worldsReversed.get(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public static boolean iceBreakCheck(BlockState block, String user, Material type) {
        if (type.equals(Material.ICE)) { // Ice block
            int unixtimestamp = (int) (System.currentTimeMillis() / 1000L);
            int wid = Util.getWorldId(block.getWorld().getName());
            CacheHandler.lookupCache.put("" + block.getX() + "." + block.getY() + "." + block.getZ() + "." + wid + "", new Object[]{unixtimestamp, user, Material.WATER});
            return true;
        }
        return false;
    }

    public static boolean listContains(Set<Material> list, Material value) {
        return list.contains(value);
    }

    public static void loadWorldEdit() {
        try {
            boolean validVersion = true;
            String version = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit").getDescription().getVersion();
            if (version.contains(";") || version.contains("+")) {
                if (version.contains("-beta-")) {
                    version = version.split(";")[0];
                    version = version.split("-beta-")[1];
                    long value = Long.parseLong(version.replaceAll("[^0-9]", ""));
                    if (value < 6) {
                        validVersion = false;
                    }
                } else {
                    if (version.contains("+")) {
                        version = version.split("\\+")[1];
                    } else {
                        version = version.split(";")[1];
                    }

                    if (version.contains("-")) {
                        long value = Long.parseLong(((version.split("-"))[0]).replaceAll("[^0-9]", ""));
                        if (value > 0 && value < 4268) {
                            validVersion = false;
                        }
                    }
                }
            } else if (version.contains(".")) {
                String[] worldEditVersion = version.split("-|\\.");
                if (worldEditVersion.length >= 2) {
                    worldEditVersion[0] = worldEditVersion[0].replaceAll("[^0-9]", "");
                    worldEditVersion[1] = worldEditVersion[1].replaceAll("[^0-9]", "");
                    if (worldEditVersion[0].length() == 0 || worldEditVersion[1].length() == 0 || Util.newVersion(worldEditVersion[0] + "." + worldEditVersion[1], "7.1")) {
                        validVersion = false;
                    }
                }
            } else if (version.equals("unspecified")) { // FAWE
                validVersion = false;
                Plugin fawe = Bukkit.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit");
                if (fawe != null) {
                    String apiVersion = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit").getDescription().getAPIVersion();
                    String faweVersion = fawe.getDescription().getVersion();
                    double apiDouble = Double.parseDouble(apiVersion);
                    double faweDouble = Double.parseDouble(faweVersion);
                    if (apiDouble >= 1.13 && faweDouble >= 1.0) {
                        validVersion = true;
                    }
                }
            } else {
                validVersion = false;
            }

            if (validVersion) {
                CoreProtectEditSessionEvent.register();
            } else {
                Chat.console(Phrase.build(Phrase.INTEGRATION_VERSION, "WorldEdit"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unloadWorldEdit() {
        try {
            CoreProtectEditSessionEvent.unregister();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int matchWorld(String name) {
        int id = -1;
        try {
            // Parse wid:# parameter used internally for /co tp click events
            if (name.startsWith("wid:")) {
                String nameWid = name.replaceFirst("wid:", "");
                if (nameWid.length() > 0 && nameWid.equals(nameWid.replaceAll("[^0-9]", ""))) {
                    nameWid = Util.getWorldName(Integer.parseInt(nameWid));
                    if (nameWid.length() > 0) {
                        name = nameWid;
                    }
                }
            }

            // Determine closest match on world name
            String result = "";
            name = name.replaceFirst("#", "").toLowerCase(Locale.ROOT).trim();
            for (World world : Bukkit.getServer().getWorlds()) {
                String worldName = world.getName();
                if (worldName.toLowerCase(Locale.ROOT).equals(name)) {
                    result = world.getName();
                    break;
                } else if (worldName.toLowerCase(Locale.ROOT).endsWith(name)) {
                    result = world.getName();
                } else if (worldName.toLowerCase(Locale.ROOT).replaceAll("[^a-zA-Z0-9]", "").endsWith(name)) {
                    result = world.getName();
                }
            }

            if (result.length() > 0) {
                id = getWorldId(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return id;
    }

    // This theoretically initializes the component code, to prevent gson adapter errors
    public static void sendConsoleComponentStartup(ConsoleCommandSender consoleSender, String string) {
        Chat.sendComponent(consoleSender, Color.RESET + "[CoreProtect] " + string + Chat.COMPONENT_TAG_OPEN + Chat.COMPONENT_POPUP + "| | " + Chat.COMPONENT_TAG_CLOSE);
    }

    // This filter is only used for a:inventory
    public static Material itemFilter(Material material, boolean blockTable) {
        if (material == null || (!blockTable && material.isItem())) {
            return material;
        }

        material = BukkitAdapter.ADAPTER.getPlantSeeds(material);
        if (material.name().contains("WALL_")) {
            material = Material.valueOf(material.name().replace("WALL_", ""));
        }

        return material;
    }

    public static String nameFilter(String name, int data) {
        if (name.equals("stone")) {
            switch (data) {
                case 1:
                    name = "granite";
                    break;
                case 2:
                    name = "polished_granite";
                    break;
                case 3:
                    name = "diorite";
                    break;
                case 4:
                    name = "polished_diorite";
                    break;
                case 5:
                    name = "andesite";
                    break;
                case 6:
                    name = "polished_andesite";
                    break;
                default:
                    name = "stone";
                    break;
            }
        }

        return name;
    }

    public static ItemStack newItemStack(Material type, int amount) {
        return new ItemStack(type, amount);
    }

    public static boolean isSpigot() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static boolean isPaper() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String getBranch() {
        String branch = "";
        try {
            InputStreamReader reader = new InputStreamReader(CoreProtect.getInstance().getClass().getResourceAsStream("/plugin.yml"));
            branch = YamlConfiguration.loadConfiguration(reader).getString("branch");
            reader.close();

            if (branch == null || branch.equals("${project.branch}")) {
                branch = "";
            }
            if (branch.startsWith("-")) {
                branch = branch.substring(1);
            }
            if (branch.length() > 0) {
                branch = "-" + branch;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return branch;
    }

    public static boolean newVersion(Integer[] oldVersion, Integer[] currentVersion) {
        if (oldVersion[0] < currentVersion[0]) {
            // Major version
            return true;
        } else if (oldVersion[0].equals(currentVersion[0]) && oldVersion[1] < currentVersion[1]) {
            // Minor version
            return true;
        } else if (oldVersion.length < 3 && currentVersion.length >= 3 && oldVersion[0].equals(currentVersion[0]) && oldVersion[1].equals(currentVersion[1]) && 0 < currentVersion[2]) {
            // Revision version (#.# vs #.#.#)
            return true;
        } else if (oldVersion.length >= 3 && currentVersion.length >= 3 && oldVersion[0].equals(currentVersion[0]) && oldVersion[1].equals(currentVersion[1]) && oldVersion[2] < currentVersion[2]) {
            // Revision version (#.#.# vs #.#.#)
            return true;
        }

        return false;
    }

    public static boolean newVersion(Integer[] oldVersion, String currentVersion) {
        String[] currentVersionSplit = currentVersion.split("\\.");
        return newVersion(oldVersion, convertArray(currentVersionSplit));
    }

    public static boolean newVersion(String oldVersion, Integer[] currentVersion) {
        String[] oldVersionSplit = oldVersion.split("\\.");
        return newVersion(convertArray(oldVersionSplit), currentVersion);
    }

    public static boolean newVersion(String oldVersion, String currentVersion) {
        if (!oldVersion.contains(".") || !currentVersion.contains(".")) {
            return false;
        }

        String[] oldVersionSplit = oldVersion.split("\\.");
        String[] currentVersionSplit = currentVersion.split("\\.");
        return newVersion(convertArray(oldVersionSplit), convertArray(currentVersionSplit));
    }

    public static Map<Integer, Object> serializeItemStackLegacy(ItemStack itemStack, String faceData, int slot) {
        Map<Integer, Object> result = new HashMap<>();
        Map<String, Object> itemMap = serializeItemStack(itemStack, faceData, slot);
        if (itemMap.size() > 1) {
            result.put(0, itemMap.get("0"));
            result.put(1, itemMap.get("1"));
        }

        return result;
    }

    public static ItemStack unserializeItemStackLegacy(Object value) {
        ItemStack result = null;
        if (value instanceof Map) {
            Map<String, Object> newMap = new HashMap<>();
            @SuppressWarnings("unchecked")
            Map<Integer, Object> itemMap = (Map<Integer, Object>) value;
            newMap.put("0", itemMap.get(0));
            newMap.put("1", itemMap.get(1));
            result = unserializeItemStack(newMap);
        }

        return result;
    }

    public static Map<String, Object> serializeItemStack(ItemStack itemStack, String faceData, int slot) {
        Map<String, Object> itemMap = new HashMap<>();
        if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
            ItemStack item = itemStack.clone();
            List<List<Map<String, Object>>> metadata = ItemMetaHandler.serialize(item, null, faceData, slot);
            item.setItemMeta(null);
            itemMap.put("0", item.serialize());
            itemMap.put("1", metadata);
        }

        return itemMap;
    }

    public static ItemStack unserializeItemStack(Object value) {
        ItemStack result = null;
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> itemMap = (Map<String, Object>) value;
            @SuppressWarnings("unchecked")
            ItemStack item = ItemStack.deserialize((Map<String, Object>) itemMap.get("0"));
            @SuppressWarnings("unchecked")
            List<List<Map<String, Object>>> metadata = (List<List<Map<String, Object>>) itemMap.get("1");

            Object[] populatedStack = Rollback.populateItemStack(item, metadata);
            result = (ItemStack) populatedStack[2];
        }

        return result;
    }

    public static List<Object> processMeta(BlockState block) {
        List<Object> meta = new ArrayList<>();
        try {
            if (block instanceof CommandBlock) {
                CommandBlock commandBlock = (CommandBlock) block;
                String command = commandBlock.getCommand();
                if (command.length() > 0) {
                    meta.add(command);
                }
            } else if (block instanceof Banner) {
                Banner banner = (Banner) block;
                meta.add(banner.getBaseColor());
                List<Pattern> patterns = banner.getPatterns();
                for (Pattern pattern : patterns) {
                    meta.add(pattern.serialize());
                }
            } else if (block instanceof ShulkerBox) {
                ShulkerBox shulkerBox = (ShulkerBox) block;
                ItemStack[] inventory = shulkerBox.getSnapshotInventory().getStorageContents();
                int slot = 0;
                for (ItemStack itemStack : inventory) {
                    Map<Integer, Object> itemMap = serializeItemStackLegacy(itemStack, null, slot);
                    if (itemMap.size() > 0) {
                        meta.add(itemMap);
                    }
                    slot++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (meta.isEmpty()) {
            meta = null;
        }
        return meta;
    }

    public static void sendBlockChange(Player player, Location location, BlockData blockData) {
        player.sendBlockChange(location, blockData);
    }

    public static BlockData createBlockData(Material material) {
        try {
            BlockData result = material.createBlockData();
            if (result instanceof Waterlogged) {
                ((Waterlogged) result).setWaterlogged(false);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static void prepareTypeAndData(Map<Block, BlockData> map, Block block, Material type, BlockData blockData, boolean update) {
        if (blockData == null) {
            blockData = createBlockData(type);
        }

        if (!update) {
            setTypeAndData(block, type, blockData, update);
            map.remove(block);
        } else {
            map.put(block, blockData);
        }
    }

    public static void setTypeAndData(Block block, Material type, BlockData blockData, boolean update) {
        if (blockData == null && type != null) {
            blockData = createBlockData(type);
        }

        if (blockData != null) {
            block.setBlockData(blockData, update);
        }
    }

    public static boolean successfulQuery(Connection connection, String query) {
        boolean result = false;
        try {
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            ResultSet resultSet = preparedStmt.executeQuery();
            result = resultSet.isBeforeFirst();
            resultSet.close();
            preparedStmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String[] toStringArray(String[] array) {
        int size = array.length;
        if (size == 11) {
            String time = array[0];
            String user = array[1];
            String x = array[2];
            String y = array[3];
            String z = array[4];
            String type = array[5];
            String data = array[6];
            String action = array[7];
            String rolledBack = array[8];
            String wid = array[9];
            String blockData = array[10];
            return new String[]{time, user, x, y, z, type, data, action, rolledBack, wid, "", "", blockData};
        }

        return null;
    }

    public static void updateBlock(final BlockState block) {
        Scheduler.runTask(CoreProtect.getInstance(), () -> {
            try {
                if (block.getBlockData() instanceof Waterlogged) {
                    Block currentBlock = block.getBlock();
                    if (currentBlock.getType().equals(block.getType())) {
                        block.setBlockData(currentBlock.getBlockData());
                    }
                }
                block.update();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, block.getLocation());
    }

    public static void updateInventory(Player player) {
        player.updateInventory();
    }

    public static boolean checkWorldEdit() {
        return Bukkit.getServer().getWorlds().stream().anyMatch(world -> Config.getConfig(world).WORLDEDIT);
    }

    public static String getWidIndex(String queryTable) {
        String index = "";
        boolean isMySQL = Config.getGlobal().MYSQL;
        if (isMySQL) {
            index = "USE INDEX(wid) ";
        } else {
            switch (queryTable) {
                case "block":
                    index = "INDEXED BY block_index ";
                    break;
                case "container":
                    index = "INDEXED BY container_index ";
                    break;
                case "item":
                    index = "INDEXED BY item_index ";
                    break;
                case "sign":
                    index = "INDEXED BY sign_index ";
                    break;
                case "chat":
                    index = "INDEXED BY chat_wid_index ";
                    break;
                case "command":
                    index = "INDEXED BY command_wid_index ";
                    break;
                case "session":
                    index = "INDEXED BY session_index ";
                    break;
                default:
                    break;
            }
        }

        return index;
    }

    public static int rolledBack(int rolledBack, boolean isInventory) {
        switch (rolledBack) {
            case 1: // just block rolled back
                return isInventory ? 0 : 1;
            case 2: // just inventory rolled back
                return isInventory ? 1 : 0;
            case 3: // block and inventory rolled back
                return 1;
            default: // no rollbacks
                return 0;
        }
    }

    public static int toggleRolledBack(int rolledBack, boolean isInventory) {
        switch (rolledBack) {
            case 1: // just block rolled back
                return isInventory ? 3 : 0;
            case 2: // just inventory rolled back
                return isInventory ? 0 : 3;
            case 3: // block and inventory rolled back
                return isInventory ? 1 : 2;
            default: // no rollbacks
                return isInventory ? 2 : 1;
        }
    }

    public static int getSignData(boolean frontGlowing, boolean backGlowing) {
        if (frontGlowing && backGlowing) {
            return 3;
        } else if (backGlowing) {
            return 2;
        } else if (frontGlowing) {
            return 1;
        }

        return 0;
    }

    public static boolean isSideGlowing(boolean isFront, int data) {
        return ((isFront && (data == 1 || data == 3)) || (!isFront && (data == 2 || data == 3)));
    }
}