<OUTPUT>
```
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

        return new Integer[]{major, minor, revision};
    }

    public static String getPluginName() {
        String name = CoreProtect.getInstance().getDescription().getName();
        String branch = ConfigHandler.EDITION_BRANCH;

        if (branch.startsWith("-edge")) {
            name = name + " " + branch.substring(1, 2).toUpperCase() + branch.substring(2, 5);
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

            int displayStart = Math.max(1, page - 2);
            int displayEnd = Math.min(totalPages, page + 2);
            if (page > 999 || (page > 101 && totalPages > 99999)) { // limit to max 5 page numbers
                displayStart = Math.max(displayStart + 1, displayEnd);
                displayEnd = Math.min(displayEnd - 1, displayEnd);
                if (displayStart > (totalPages - 3)) {
                    displayStart = Math.max(1, totalPages - 3);
                }
            } else { // display at least 7 page numbers
                if (displayStart > (totalPages - 5)) {
                    displayStart = Math.max(1, totalPages - 5);
                }
                if (displayEnd < 6) {
                    displayEnd = Math.min(6, totalPages);
                }
            }

            if (page > 99999) { // limit to max 3 page numbers
                displayStart = Math.max(displayStart + 1, displayEnd);
                displayEnd = Math.min(displayEnd - 1, displayEnd);
                if (page == (totalPages - 1)) {
                    displayEnd = totalPages - 1;
                }
                if (displayStart < displayEnd) {
                    displayStart = displayEnd;
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

    public static int getBlockId(String name, boolean internal) {
        int id = -1;

        name = name.toLowerCase(Locale.ROOT).trim();
        if (!name.contains(":")) {
            name = NAMESPACE + name;
        }

        if (ConfigHandler.materials.get(name) != null) {
            id = ConfigHandler.materials.get(name);
        } else if (internal) {
            int mid = ConfigHandler.materialId + 1;
            ConfigHandler.materials.put(name, mid);
            ConfigHandler.materialsReversed.put(mid, name);
            ConfigHandler.materialId = mid;
            Queue.queueMaterialInsert(mid, name);
            id = ConfigHandler.materials.get(name);
        }

        return id;
    }

    public static int getBlockdataId(String data, boolean internal) {
        int id = -1;
        data = data.toLowerCase(Locale.ROOT).trim();

        if (ConfigHandler.blockdata.get(data) != null) {
            id = ConfigHandler.blockdata.get(data);
        } else if (internal) {
            int bid = ConfigHandler.blockdataId + 1;
            ConfigHandler.blockdata.put(data, bid);
            ConfigHandler.blockdataReversed.put(bid, data);
            ConfigHandler.blockdataId = bid;
            Queue.queueBlockDataInsert(bid, data);
            id = ConfigHandler.blockdata.get(data);
        }

        return id;
    }

    public static String getBlockDataString(int id) {
        // Internal ID pulled from DB
        String blockdata = "";
        if (ConfigHandler.blockdataReversed.get(id) != null) {
            blockdata = ConfigHandler.blockdataReversed.get(id);
        }
        return blockdata;
    }

    public static String getBlockName(int id) {
        String name = "";
        if (ConfigHandler.materialsReversed.get(id) != null) {
            name = ConfigHandler.materialsReversed.get(id);
        }
        return name;
    }

    public static String getBlockNameShort(int id) {
        String name = getBlockName(id);
        if (name.contains(":")) {
            name = name.split(":")[1];
        }

        return name;
    }

    public static void mergeItems(Material material, ItemStack[] items) {
        if (material != null && (material.equals(Material.ARMOR_STAND) || BukkitAdapter.ADAPTER.isItemFrame(material))) {
            return;
        }
        try {
            int c1 = 0;
            for (ItemStack o1 : items) {
                if (o1 != null && o1.getAmount() > 0) {
                    int c2 = 0;
                    for (ItemStack o2 : items) {
                        if (o2 != null && c2 > c1 && o1.isSimilar(o2) && !Util.isAir(o1.getType())) { // Ignores amount
                            int namount = o1.getAmount() + o2.getAmount();
                            o1.setAmount(namount);
                            o2.setAmount(0);
                        }
                        c2++;
                    }
                }
                c1++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Integer[] convertArray(String[] array) {
        return Arrays.stream(array)
                .map(Integer::parseInt)
                .toArray(Integer[]::new);
    }

    public static byte[] stringToByteData(String string, int type) {
        byte[] result = null;
        if (string != null) {
            Material material = Util.getType(type);
            if (material == null) {
                return result;
            }

            if (material.isBlock() && !createBlockData(material).getAsString().equals(string) && string.startsWith(NAMESPACE + material.name().toLowerCase(Locale.ROOT) + "[") && string.endsWith("]")) {
                String substring = string.substring(material.name().length() + 11, string.length() - 1);
                String[] blockDataSplit = substring.split(",");
                ArrayList<String> blockDataArray = new ArrayList<>();
                for (String data : blockDataSplit) {
                    int id = getBlockdataId(data, true);
                    if (id > -1) {
                        blockDataArray.add(Integer.toString(id));
                    }
                }
                string = String.join(",", blockDataArray);
            } else if (!string.contains(":") && (material == Material.PAINTING || BukkitAdapter.ADAPTER.isItemFrame(material))) {
                int id = getBlockdataId(string, true);
                if (id > -1) {
                    string = Integer.toString(id);
                } else {
                    return result;
                }
            } else {
                return result;
            }

            result = string.getBytes(StandardCharsets.UTF_8);
        }

        return result;
    }

    public static String byteDataToString(byte[] data, int type) {
        String result = "";
        if (data != null) {
            Material material = Util.getType(type);
            if (material == null) {
                return result;
            }

            result = new String(data, StandardCharsets.UTF_8);
            if (result.length() > 0) {
                if (result.matches("\\d+")) {
                    result += ",";
                }
                if (result.contains(",")) {
                    String[] blockDataSplit = result.split(",");
                    ArrayList<String> blockDataArray = new ArrayList<>();
                    for (String blockData : blockDataSplit) {
                        String block = getBlockDataString(Integer.parseInt(blockData));
                        if (block.length() > 0) {
                            blockDataArray.add(block);
                        }
                    }

                    if (material == Material.PAINTING || BukkitAdapter.ADAPTER.isItemFrame(material)) {
                        result = String.join(",", blockDataArray);
                    } else {
                        result = NAMESPACE + material.name().toLowerCase(Locale.ROOT) + "[" + String.join(",", blockDataArray) + "]";
                    }
                } else {
                    result = "";
                }
            }
        }

        return result;
    }

    public static byte[] convertByteData(Object data) {
        byte[] result = null;
        if (data == null) {
            return result;
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             BukkitObjectOutputStream oos = new BukkitObjectOutputStream(bos)) {
            oos.writeObject(data);
            oos.flush();
            result = bos.toByteArray();
        } catch (Exception e) { // only display exception on development branch
            if (!ConfigHandler.EDITION_BRANCH.contains("-dev")) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static ItemMeta deserializeItemMeta(Class<? extends ItemMeta> itemMetaClass, Map<String, Object> args) {
        try {
            DelegateDeserialization delegate = itemMetaClass.getAnnotation(DelegateDeserialization.class);
            return (ItemMeta) ConfigurationSerialization.deserializeObject(args, delegate.value());
        } catch (Exception e) { // only display exception on development branch
            if (!ConfigHandler.EDITION_BRANCH.contains("-dev")) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<>((e1, e2) -> {
            int res = e1.getValue().compareTo(e2.getValue());
            return res != 0 ? res : 1;
        });
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    public static Waterlogged checkWaterlogged(BlockData blockData, BlockState blockReplacedState) {
        if (blockReplacedState.getType().equals(Material.WATER) && blockData instanceof Waterlogged) {
            if (blockReplacedState.getBlockData().equals(Material.WATER.createBlockData())) {
                Waterlogged waterlogged = (Waterlogged) blockData;
                waterlogged.setWaterlogged(true);
                return waterlogged;
            }
        }
        return null;
    }

    public static ItemStack[] getContainerState(ItemStack[] array) {
        ItemStack[] result = array == null ? null : array.clone();
        if (result == null) {
            return result;
        }

        int count = 0;
        for (ItemStack itemStack : array) {
            ItemStack clonedItem = null;
            if (itemStack != null) {
                clonedItem = itemStack.clone();
            }
            result[count] = clonedItem;
            count++;
        }

        return result;
    }

    public static ItemStack[] sortContainerState(ItemStack[] array) {
        if (array == null) {
            return null;
        }

        ItemStack[] sorted = new ItemStack[array.length];
        Map<String, ItemStack> map = new HashMap<>();
        for (ItemStack itemStack : array) {
            if (itemStack == null) {
                continue;
            }

            map.put(itemStack.toString(), itemStack);
        }

        ArrayList<String> sortedKeys = new ArrayList<>(map.keySet());
        Collections.sort(sortedKeys);

        int i = 0;
        for (String key : sortedKeys) {
            sorted[i] = map.get(key);
            i++;
        }

        return sorted;
    }

    /* return true if ItemStack[] contents are identical */
    public static boolean compareContainers(ItemStack[] oldContainer, ItemStack[] newContainer) {
        if (oldContainer.length != newContainer.length) {
            return false;
        }

        for (int i = 0; i < oldContainer.length; i++) {
            ItemStack oldItem = oldContainer[i];
            ItemStack newItem = newContainer[i];

            if (oldItem == null && newItem == null) {
                continue;
            }

            if (oldItem == null || !oldItem.equals(newItem)) {
                return false;
            }
        }

        return true;
    }

    /* return true if newContainer contains new items */
    public static boolean addedContainer(ItemStack[] oldContainer, ItemStack[] newContainer) {
        if (oldContainer.length != newContainer.length) {
            return false;
        }

        for (int i = 0; i < oldContainer.length; i++) {
            ItemStack oldItem = oldContainer[i];
            ItemStack newItem = newContainer[i];

            if (oldItem == null && newItem == null) {
                continue;
            }

            if (oldItem != null && newItem == null) {
                return false;
            }

            if (oldItem == null) {
                return true;
            }

            if (!newItem.equals(oldItem)) {
                return (newItem.isSimilar(oldItem) && newItem.getAmount() > oldItem.getAmount());
            }
        }

        return false;
    }

    /* return true if item can be added to container */
    public static boolean canAddContainer(ItemStack[] container, ItemStack item, int forceMaxStack) {
        for (ItemStack containerItem : container) {
            if (containerItem == null || containerItem.getType() == Material.AIR) {
                return true;
            }

            int maxStackSize = containerItem.getMaxStackSize();
            if (forceMaxStack > 0 && (forceMaxStack < maxStackSize || maxStackSize == -1)) {
                maxStackSize = forceMaxStack;
            }

            if (maxStackSize == -1) {
                maxStackSize = 1;
            }

            if (containerItem.isSimilar(item) && containerItem.getAmount() < maxStackSize) {
                return true;
            }
        }

        return false;
    }

    public static int getArtId(String name, boolean internal) {
        int id = -1;
        name = name.toLowerCase(Locale.ROOT).trim();

        if (ConfigHandler.art.get(name) != null) {
            id = ConfigHandler.art.get(name);
        } else if (internal) {
            int artID = ConfigHandler.artId + 1;
            ConfigHandler.art.put(name, artID);
            ConfigHandler.artReversed.put(artID, name);
            ConfigHandler.artId = artID;
            Queue.queueArtInsert(artID, name);
            id = ConfigHandler.art.get(name);
        }

        return id;
    }

    public static String getArtName(int id) {
        // Internal ID pulled from DB
        String artname = "";
        if (ConfigHandler.artReversed.get(id) != null) {
            artname = ConfigHandler.artReversed.get(id);
        }
        return artname;
    }

    public static int setPlayerArmor(PlayerInventory inventory, ItemStack itemStack) {
        String itemName = itemStack.getType().name();
        boolean isHelmet = (itemName.endsWith("_HELMET") || itemName.endsWith("_HEAD") || itemName.endsWith("_SKULL") || itemName.endsWith("_PUMPKIN"));
        boolean isChestplate = (itemName.endsWith("_CHESTPLATE"));
        boolean isLeggings = (itemName.endsWith("_LEGGINGS"));
        boolean isBoots = (itemName.endsWith("_BOOTS"));

        if (isHelmet && inventory.getHelmet() == null) {
            inventory.setHelmet(itemStack);
            return 3;
        } else if (isChestplate && inventory.getChestplate() == null) {
            inventory.setChestplate(itemStack);
            return 2;
        } else if (isLeggings && inventory.getLeggings() == null) {
            inventory.setLeggings(itemStack);
            return 1;
        } else if (isBoots && inventory.getBoots() == null) {
            inventory.setBoots(itemStack);
            return 0;
        }

        return -1;
    }

    public static ItemStack[] getArmorStandContents(EntityEquipment equipment) {
        ItemStack[] contents = new ItemStack[6];
        if (equipment != null) {
            // 0: BOOTS, 1: LEGGINGS, 2: CHESTPLATE, 3: HELMET
            ItemStack[] armorContent = equipment.getArmorContents();
            System.arraycopy(armorContent, 0, contents, 0, 4);
            contents[4] = equipment.getItemInMainHand();
            contents[5] = equipment.getItemInOffHand();
        } else {
            Arrays.fill(contents, new ItemStack(Material.AIR));
        }

        return contents;
    }

    public static ItemStack[] getContainerContents(Material type, Object container, Location location) {
        ItemStack[] contents = null;
        if (Config.getConfig(location.getWorld()).ITEM_TRANSACTIONS && BlockGroup.CONTAINERS.contains(type)) {
            try {
                // container may be null if called from within WorldEdit logger
                if (container == null) {
                    container = location.getBlock();
                }

                if (type == Material.ARMOR_STAND) {
                    LivingEntity entity = (LivingEntity) container;
                    EntityEquipment equipment = Util.getEntityEquipment(entity);
                    if (equipment != null) {
                        contents = getArmorStandContents(equipment);
                    }
                } else if (type == Material.ITEM_FRAME) {
                    ItemFrame entity = (ItemFrame) container;
                    contents = Util.getItemFrameItem(entity);
                } else if (type == Material.JUKEBOX) {
                    Jukebox blockState = (Jukebox) ((Block) container).getState();
                    contents = Util.getJukeboxItem(blockState);
                } else {
                    Block block = (Block) container;
                    Inventory inventory = Util.getContainerInventory(block.getState(), true);
                    if (inventory != null) {
                        contents = inventory.getContents();
                    }
                }

                if (type == Material.ARMOR_STAND || type == Material.ITEM_FRAME) {
                    boolean hasItem = false;
                    for (ItemStack item : contents) {
                        if (item != null && !item.getType().equals(Material.AIR)) {
                            hasItem = true;
                            break;
                        }
                    }
                    if (!hasItem) {
                        contents = null;
                    }
                }

                if (contents != null) {
                    contents = Util.getContainerState(contents);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return contents;
    }

    public static Inventory getContainerInventory(BlockState blockState, boolean singleBlock) {
        Inventory inventory = null;
        try {
            if (blockState instanceof BlockInventoryHolder) {
                if (singleBlock) {
                    List<Material> chests = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST);
                    Material type = blockState.getType();
                    if (chests.contains(type)) {
                        inventory = ((Chest) blockState).getBlockInventory();
                    }
                }
                if (inventory == null) {
                    inventory = ((BlockInventoryHolder) blockState).getInventory();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inventory;
    }

    public static EntityEquipment getEntityEquipment(LivingEntity entity) {
        EntityEquipment equipment = null;
        try {
            equipment = entity.getEquipment();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return equipment;
    }

    public static ItemStack[] getItemFrameItem(ItemFrame entity) {
        ItemStack[] contents = null;
        try {
            contents = new ItemStack[]{entity.getItem()};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contents;
    }

    public static ItemStack[] getJukeboxItem(Jukebox blockState) {
        ItemStack[] contents = null;
        try {
            contents = new ItemStack[]{blockState.getRecord()};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contents;
    }

    public static int getEntityId(EntityType type) {
        if (type == null) {
            return -1;
        }

        return getEntityId(type.name(), true);
    }

    public static int getEntityId(String name, boolean internal) {
        int id = -1;
        name = name.toLowerCase(Locale.ROOT).trim();

        if (ConfigHandler.entities.get(name) != null) {
            id = ConfigHandler.entities.get(name);
        } else if (internal) {
            int entityID = ConfigHandler.entityId + 1;
            ConfigHandler.entities.put(name, entityID);
            ConfigHandler.entitiesReversed.put(entityID, name);
            ConfigHandler.entityId = entityID;
            Queue.queueEntityInsert(entityID, name);
            id = ConfigHandler.entities.get(name);
        }

        return id;
    }

    public static Material getEntityMaterial(EntityType type) {
        switch (type.name()) {
            case "ARMOR_STAND":
                return Material.ARMOR_STAND;
            case "ITEM_FRAME":
                return Material.ITEM_FRAME;
            case "END_CRYSTAL":
            case "ENDER_CRYSTAL":
                return Material.END_CRYSTAL;
            case "ENDER_PEARL":
                return Material.ENDER_PEARL;
            case "POTION":
            case "SPLASH_POTION":
                return Material.SPLASH_POTION;
            case "EXPERIENCE_BOTTLE":
            case "THROWN_EXP_BOTTLE":
                return Material.EXPERIENCE_BOTTLE;
            case "TRIDENT":
                return Material.TRIDENT;
            case "FIREWORK_ROCKET":
            case "FIREWORK":
                return Material.FIREWORK_ROCKET;
            case "EGG":
                return Material.EGG;
            case "SNOWBALL":
                return Material.SNOWBALL;
            case "WIND_CHARGE":
                return Material.valueOf("WIND_CHARGE");
            default:
                return BukkitAdapter.ADAPTER.getFrameType(type);
        }
    }

    public static String getEntityName(int id) {
        // Internal ID pulled from DB
        String entityName = "";
        if (ConfigHandler.entitiesReversed.get(id) != null) {
            entityName = ConfigHandler.entitiesReversed.get(id);
        }
        return entityName;
    }

    public static EntityType getEntityType(int id) {
        // Internal ID pulled from DB
        EntityType entitytype = null;
        if (ConfigHandler.entitiesReversed.get(id) != null) {
            String name = ConfigHandler.entitiesReversed.get(id);
            if (name.contains(NAMESPACE)) {
                name = name.split(":")[1];
            }
            entitytype = EntityType.valueOf(name.toUpperCase(Locale.ROOT));
        }
        return entitytype;
    }

    public static EntityType getEntityType(String name) {
        // Name entered by user
        EntityType type = null;
        name = name.toLowerCase(Locale.ROOT).trim();
        if (name.contains(NAMESPACE)) {
            name = (name.split(":"))[1];
        }

        if (ConfigHandler.entities.get(name) != null) {
            type = EntityType.valueOf(name.toUpperCase(Locale.ROOT));
        }

        return type;
    }

    public static int getItemStackHashCode(ItemStack item) {
        try {
            return item.hashCode();
        } catch (Exception exception) {
            return -1;
        }
    }

    public static int getMaterialId(Material material) {
        return getBlockId(material.name(), true);
    }

    public static int getSpawnerType(EntityType type) {
        int result = Util.getEntityId(type);
        if (result == -1) {
            result = 0; // default to pig
        }

        return result;
    }

    public static EntityType getSpawnerType(int type) {
        EntityType result = Util.getEntityType(type);
        if (result == null) {
            result = EntityType.PIG;
        }

        return result;
    }

    public static boolean isAir(Material type) {
        return (type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR);
    }

    public static boolean solidBlock(Material type) {
        return type.isSolid();
    }

    public static boolean passableBlock(Block block) {
        return block.isPassable();
    }

    public static Material getType(Block block) {
        // Temp code
        return block.getType();
    }

    public static Material getType(int id) {
        // Internal ID pulled from DB
        Material material = null;
        if (ConfigHandler.materialsReversed.get(id) != null && id > 0) {
            String name = ConfigHandler.materialsReversed.get(id).toUpperCase(Locale.ROOT);
            if (name.contains(NAMESPACE.toUpperCase(Locale.ROOT))) {
                name = name.split(":")[1];
            }

            name = BukkitAdapter.ADAPTER.parseLegacyName(name);
            material = Material.getMaterial(name);

            if (material == null) {
                material = Material.getMaterial(name, true);
            }
        }

        return material;
    }

    public static Material getType(String name) {
        // Name entered by user
       