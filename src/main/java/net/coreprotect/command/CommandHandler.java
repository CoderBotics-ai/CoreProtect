<RESTATEMENT>
The goal is to upgrade the provided Java file content from Java 18 to Java 21, ensuring compliance with the latest syntax, features, and best practices while maintaining the original functionality and structure of the code.
</RESTATEMENT>

<THINKING>
1. Review the current file content for any deprecated methods or libraries that need updating.
2. Identify new features introduced in Java 21 that can enhance the code, such as pattern matching for switch statements, new language features, or improvements in collections.
3. Check for any necessary changes in import statements based on updated libraries or features.
4. Ensure that all comments and documentation are preserved and updated only if they become incorrect due to code changes.
5. Maintain the overall structure and functionality of the code while applying the upgrades.
</THINKING>

<OUTPUT>
```
package net.coreprotect.command;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.language.Phrase;
import net.coreprotect.language.Selector;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.thread.NetworkHandler;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.Util;

public class CommandHandler implements CommandExecutor {
    private static CommandHandler instance;
    private static final ConcurrentHashMap<String, Boolean> versionAlert = new ConcurrentHashMap<>();

    public static CommandHandler getInstance() {
        if (instance == null) {
            instance = new CommandHandler();
        }
        return instance;
    }

    protected static String[] parsePage(String[] argumentArray) {
        if (argumentArray.length == 2) {
            argumentArray[1] = argumentArray[1].replaceFirst("page:", "");
        }
        return argumentArray;
    }

    protected static List<Integer> parseAction(String[] inputArguments) {
        String[] argumentArray = inputArguments.clone();
        List<Integer> result = new ArrayList<>();
        int count = 0;
        int next = 0;
        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim().toLowerCase(Locale.ROOT)
                                   .replaceAll("\\\\", "")
                                   .replaceAll("'", "");

                if (argument.equals("a:") || argument.equals("action:")) {
                    next = 1;
                } else if (next == 1 || argument.startsWith("a:") || argument.startsWith("action:")) {
                    result.clear();
                    argument = argument.replaceAll("action:", "")
                                       .replaceAll("a:", "")
                                       .replaceFirst("#", "");
                    switch (argument) {
                        case "broke", "break", "remove", "destroy", "block-break", "block-remove", "-block", "-blocks", "block-" -> result.add(0);
                        case "placed", "place", "block-place", "+block", "+blocks", "block+" -> result.add(1);
                        case "block", "blocks", "block-change", "change", "changes" -> result.addAll(Arrays.asList(0, 1));
                        case "click", "clicks", "interact", "interaction", "player-interact", "player-interaction", "player-click" -> result.add(2);
                        case "death", "deaths", "entity-death", "entity-deaths", "kill", "kills", "entity-kill", "entity-kills" -> result.add(3);
                        case "container", "container-change", "containers", "chest", "transaction", "transactions" -> result.add(4);
                        case "-container", "container-", "remove-container" -> result.addAll(Arrays.asList(4, 0));
                        case "+container", "container+", "container-add", "add-container" -> result.addAll(Arrays.asList(4, 1));
                        case "chat", "chats" -> result.add(6);
                        case "command", "commands" -> result.add(7);
                        case "logins", "login", "+session", "+sessions", "session+", "+connection", "connection+" -> result.addAll(Arrays.asList(8, 1));
                        case "logout", "logouts", "-session", "-sessions", "session-", "-connection", "connection-" -> result.add(8);
                        case "session", "sessions", "connection", "connections" -> result.add(8);
                        case "username", "usernames", "user", "users", "name", "names", "uuid", "uuids", "username-change", "username-changes", "name-change", "name-changes" -> result.add(9);
                        case "sign", "signs" -> result.add(10);
                        case "inv", "inventory", "inventories" -> result.addAll(Arrays.asList(4, 11));
                        case "-inv", "inv-", "-inventory", "inventory-", "-inventories" -> result.addAll(Arrays.asList(4, 11, 1));
                        case "+inv", "inv+", "+inventory", "inventory+" -> result.addAll(Arrays.asList(4, 11, 0));
                        case "item", "items" -> result.add(11);
                        case "-item", "item-", "-items", "items-", "drop", "drops", "deposit", "deposits", "deposited" -> result.addAll(Arrays.asList(11, 0));
                        case "+item", "item+", "+items", "items+", "pickup", "pickups", "withdraw", "withdraws", "withdrew" -> result.addAll(Arrays.asList(11, 1));
                        default -> result.add(-1);
                    }
                    next = 0;
                } else {
                    next = 0;
                }
            }
            count++;
        }
        return result;
    }

    protected static Location parseCoordinates(Location location, String[] inputArguments, int worldId) {
        String[] argumentArray = inputArguments.clone();
        int count = 0;
        int next = 0;
        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim().toLowerCase(Locale.ROOT)
                                   .replaceAll("\\\\", "")
                                   .replaceAll("'", "");

                if (argument.equals("position:") || argument.equals("location:") || argument.equals("c:") || argument.equals("coord:") || argument.equals("coords:") || argument.equals("cord:") || argument.equals("cords:") || argument.equals("coordinate:") || argument.equals("coordinates:") || argument.equals("cordinate:") || argument.equals("cordinates:")) {
                    next = 2;
                } else if (next == 2 || argument.startsWith("c:") || argument.startsWith("coord:") || argument.startsWith("coords:") || argument.startsWith("cord:") || argument.startsWith("cords:") || argument.startsWith("coordinate:") || argument.startsWith("coordinates:") || argument.startsWith("cordinate:") || argument.startsWith("cordinates:")) {
                    argument = argument.replaceAll("coordinates:", "")
                                       .replaceAll("coordinate:", "")
                                       .replaceAll("cordinates:", "")
                                       .replaceAll("cordinate:", "")
                                       .replaceAll("coords:", "")
                                       .replaceAll("coord:", "")
                                       .replaceAll("cords:", "")
                                       .replaceAll("cord:", "")
                                       .replaceAll("c:", "");
                    if (argument.contains(",")) {
                        String[] i2 = argument.split(",");
                        double x = 0.00;
                        double y = 0.00;
                        double z = 0.00;
                        int cCount = 0;
                        for (String coord : i2) {
                            coord = coord.replaceAll("[^0-9.\\-]", "");
                            if (coord.length() > 0 && !coord.equals(".") && !coord.equals("-") && coord.indexOf('.') == coord.lastIndexOf('.')) {
                                double parsedCoord = Double.parseDouble(coord);
                                switch (cCount) {
                                    case 0 -> x = parsedCoord;
                                    case 1 -> z = parsedCoord;
                                    case 2 -> {
                                        y = z;
                                        z = parsedCoord;
                                    }
                                }
                                cCount++;
                            }
                        }
                        if (cCount > 1) {
                            if (location == null && worldId > 0) {
                                location = new Location(Bukkit.getWorld(Util.getWorldName(worldId)), 0, 0, 0);
                            }
                            if (location != null) {
                                int worldMaxHeight = location.getWorld().getMaxHeight() - 1;
                                int worldMinHeight = BukkitAdapter.ADAPTER.getMinHeight(location.getWorld());

                                if (y < worldMinHeight) {
                                    y = worldMinHeight;
                                }
                                if (y > worldMaxHeight) {
                                    y = worldMaxHeight;
                                }

                                location.setX(x);
                                location.setY(y);
                                location.setZ(z);
                            }
                        }
                    }
                    next = 0;
                } else {
                    next = 0;
                }
            }
            count++;
        }
        return location;
    }

    protected static boolean parseCount(String[] inputArguments) {
        String[] argumentArray = inputArguments.clone();
        boolean result = false;
        int count = 0;
        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim().toLowerCase(Locale.ROOT)
                                   .replaceAll("\\\\", "")
                                   .replaceAll("'", "");
                if (argument.equals("#count") || argument.equals("#sum")) {
                    result = true;
                }
            }
            count++;
        }
        return result;
    }

    protected static Map<Object, Boolean> parseExcluded(CommandSender player, String[] inputArguments, List<Integer> argAction) {
        String[] argumentArray = inputArguments.clone();
        Map<Object, Boolean> excluded = new HashMap<>();
        int count = 0;
        int next = 0;
        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim().toLowerCase(Locale.ROOT)
                                   .replaceAll("\\\\", "")
                                   .replaceAll("'", "");

                if (argument.equals("e:") || argument.equals("exclude:")) {
                    next = 5;
                } else if (next == 5 || argument.startsWith("e:") || argument.startsWith("exclude:")) {
                    argument = argument.replaceAll("exclude:", "")
                                       .replaceAll("e:", "");
                    if (argument.contains(",")) {
                        String[] i2 = argument.split(",");
                        for (String i3 : i2) {
                            if (!checkTags(i3, excluded)) {
                                Material i3_material = Util.getType(i3);
                                if (i3_material != null && (i3_material.isBlock() || argAction.contains(4))) {
                                    excluded.put(i3_material, false);
                                } else {
                                    EntityType i3_entity = Util.getEntityType(i3);
                                    if (i3_entity != null) {
                                        excluded.put(i3_entity, false);
                                    } else if (i3_material != null) {
                                        excluded.put(i3_material, false);
                                    }
                                }
                            }
                        }
                        next = argument.endsWith(",") ? 5 : 0;
                    } else {
                        if (!checkTags(argument, excluded)) {
                            Material iMaterial = Util.getType(argument);
                            if (iMaterial != null && (iMaterial.isBlock() || argAction.contains(4))) {
                                excluded.put(iMaterial, false);
                            } else {
                                EntityType iEntity = Util.getEntityType(argument);
                                if (iEntity != null) {
                                    excluded.put(iEntity, false);
                                } else if (iMaterial != null) {
                                    excluded.put(iMaterial, false);
                                }
                            }
                        }
                        next = 0;
                    }
                } else {
                    next = 0;
                }
            }
            count++;
        }
        return excluded;
    }

    protected static List<String> parseExcludedUsers(CommandSender player, String[] inputArguments) {
        String[] argumentArray = inputArguments.clone();
        List<String> excluded = new ArrayList<>();
        int count = 0;
        int next = 0;
        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim().toLowerCase(Locale.ROOT)
                                   .replaceAll("\\\\", "")
                                   .replaceAll("'", "");

                if (argument.equals("e:") || argument.equals("exclude:")) {
                    next = 5;
                } else if (next == 5 || argument.startsWith("e:") || argument.startsWith("exclude:")) {
                    argument = argument.replaceAll("exclude:", "")
                                       .replaceAll("e:", "");
                    if (argument.contains(",")) {
                        String[] i2 = argument.split(",");
                        for (String i3 : i2) {
                            boolean isBlock = checkTags(i3);
                            if (!isBlock) {
                                Material i3_material = Util.getType(i3);
                                if (i3_material != null) {
                                    isBlock = true;
                                } else {
                                    EntityType i3Entity = Util.getEntityType(i3);
                                    if (i3Entity != null) {
                                        isBlock = true;
                                    }
                                }
                            }
                            if (!isBlock) {
                                excluded.add(i3);
                            }
                        }
                        next = argument.endsWith(",") ? 5 : 0;
                    } else {
                        boolean isBlock = checkTags(argument);
                        if (!isBlock) {
                            Material iMaterial = Util.getType(argument);
                            if (iMaterial != null) {
                                isBlock = true;
                            } else {
                                EntityType entityType = Util.getEntityType(argument);
                                if (entityType != null) {
                                    isBlock = true;
                                }
                            }
                        }
                        if (!isBlock) {
                            excluded.add(argument);
                        }
                        next = 0;
                    }
                } else {
                    next = 0;
                }
            }
            count++;
        }
        return excluded;
    }

    protected static boolean parseForceGlobal(String[] inputArguments) {
        String[] argumentArray = inputArguments.clone();
        boolean result = false;
        int count = 0;
        int next = 0;
        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim().toLowerCase(Locale.ROOT)
                                   .replaceAll("\\\\", "")
                                   .replaceAll("'", "");

                if (argument.equals("r:") || argument.equals("radius:")) {
                    next = 2;
                } else if (next == 2 || argument.startsWith("r:") || argument.startsWith("radius:")) {
                    argument = argument.replaceAll("radius:", "")
                                       .replaceAll("r:", "");
                    if (argument.equals("#global") || argument.equals("global") || argument.equals("off") || argument.equals("-1") || argument.equals("none") || argument.equals("false")) {
                        result = true;
                    } else if (argument.startsWith("#")) {
                        int worldId = Util.matchWorld(argument);
                        if (worldId > 0) {
                            result = true;
                        }
                    }
                    next = 0;
                } else {
                    next = 0;
                }
            }
            count++;
        }
        return result;
    }

    protected static Location parseLocation(CommandSender user, String[] argumentArray) {
        Location location = null;
        if (user instanceof Player) {
            location = ((Player) user).getLocation();
        } else if (user instanceof BlockCommandSender) {
            location = ((BlockCommandSender) user).getBlock().getLocation();
        }

        return parseCoordinates(location, argumentArray, parseWorld(argumentArray, true, true));
    }

    protected static int parseNoisy(String[] inputArguments) {
        String[] argumentArray = inputArguments.clone();
        int noisy = 0;
        int count = 0;
        if (Config.getGlobal().VERBOSE) {
            noisy = 1;
        }
        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim().toLowerCase(Locale.ROOT)
                                   .replaceAll("\\\\", "")
                                   .replaceAll("'", "");

                if (argument.equals("n") || argument.equals("noisy") || argument.equals("v") || argument.equals("verbose") || argument.equals("#v") || argument.equals("#verbose")) {
                    noisy = 1;
                } else if (argument.equals("#silent")) {
                    noisy = 0;
                }
            }
            count++;
        }
        return noisy;
    }

    protected static int parsePreview(String[] inputArguments) {
        String[] argumentArray = inputArguments.clone();
        int result = 0;
        int count = 0;
        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim().toLowerCase(Locale.ROOT)
                                   .replaceAll("\\\\", "")
                                   .replaceAll("'", "");
                if (argument.equals("#preview")) {
                    result = 1;
                } else if (argument.equals("#preview_cancel")) {
                    result = 2;
                }
            }
            count++;
        }
        return result;
    }

    protected static Integer[] parseRadius(String[] inputArguments, CommandSender user, Location location) {
        String[] argumentArray = inputArguments.clone();
        Integer[] radius = null;
        int count = 0;
        int next = 0;
        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim().toLowerCase(Locale.ROOT)
                                   .replaceAll("\\\\", "")
                                   .replaceAll("'", "");

                if (argument.equals("r:") || argument.equals("radius:")) {
                    next = 2;
                } else if (next == 2 || argument.startsWith("r:") || argument.startsWith("radius:")) {
                    argument = argument.replaceAll("radius:", "")
                                       .replaceAll("r:", "");
                    if (argument.equals("#worldedit") || argument.equals("#we")) {
                        if (user.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                            Integer[] worldEditResult = WorldEditHandler.runWorldEditCommand(user);
                            if (worldEditResult != null) {
                                radius = worldEditResult;
                            }
                        }
                    } else if ((argument.startsWith("#") && argument.length() > 1) || argument.equals("global") || argument.equals("off") || argument.equals("-1") || argument.equals("none") || argument.equals("false")) {
                        // radius = -2;
                    } else {
                        int rcount = 0;
                        int r_x = 0;
                        int r_y = -1;
                        int r_z = 0;
                        String[] r_dat = new String[] { argument };
                        boolean validRadius = false;
                        if (argument.contains("x")) {
                            r_dat = argument.split("x");
                        }
                        for (String value : r_dat) {
                            String i4 = value.replaceAll("[^0-9.]", "");
                            if (i4.length() > 0 && i4.length() == value.length() && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf('.') == i4.lastIndexOf('.')) {
                                double a1 = Double.parseDouble(i4);
                                switch (rcount) {
                                    case 0 -> {
                                        r_x = (int) a1;
                                        r_z = (int) a1;
                                    }
                                    case 1 -> r_y = (int) a1;
                                    case 2 -> r_z = (int) a1;
                                }
                                validRadius = true;
                            }
                            rcount++;
                        }
                        if (location != null) {
                            Integer xmin = location.getBlockX() - r_x;
                            Integer xmax = location.getBlockX() + r_x;
                            Integer ymin = null;
                            Integer ymax = null;
                            Integer zmin = location.getBlockZ() - r_z;
                            Integer zmax = location.getBlockZ() + r_z;
                            int max = r_x;
                            if (r_y > max) {
                                max = r_y;
                            }
                            if (r_z > max) {
                                max = r_z;
                            }
                            if (validRadius) {
                                radius = new Integer[] { max, xmin, xmax, ymin, ymax, zmin, zmax, 0 };
                            } else {
                                radius = new Integer[] { -1 };
                            }
                        }
                    }
                    next = 0;
                } else {
                    next = 0;
                }
            }
            count++;
        }
        return radius;
    }

    protected static List<Object> parseRestricted(CommandSender player, String[] inputArguments, List<Integer> argAction) {
        String[] argumentArray = inputArguments.clone();
        List<Object> restricted = new ArrayList<>();
        int count = 0;
        int next = 0;
        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim().toLowerCase(Locale.ROOT)
                                   .replaceAll("\\\\", "")
                                   .replaceAll("'", "");

                if (argument.equals("i:") || argument.equals("include:") || argument.equals("item:") || argument.equals("items:") || argument.equals("b:") || argument.equals("block:") || argument.equals("blocks:")) {
                    next = 4;
                } else if (next == 4 || argument.startsWith("i:") || argument.startsWith("include:") || argument.startsWith("item:") || argument.startsWith("items:") || argument.startsWith("b:") || argument.startsWith("block:") || argument.startsWith("blocks:")) {
                    argument = argument.replaceAll("include:", "")
                                       .replaceAll("i:", "")
                                       .replaceAll("items:", "")
                                       .replaceAll("item:", "")
                                       .replaceAll("blocks:", "")
                                       .replaceAll("block:", "")
                                       .replaceAll("b:", "");
                    if (argument.contains(",")) {
                        String[] i2 = argument.split(",");
                        for (String i3 : i2) {
                            if (!checkTags(argument, restricted)) {
                                Material i3_material = Util.getType(i3);
                                if (i3_material != null && (i3_material.isBlock() || argAction.contains(4))) {
                                    restricted.add(i3_material);
                                } else {
                                    EntityType i3_entity = Util.getEntityType(i3);
                                    if (i3_entity != null) {
                                        restricted.add(i3_entity);
                                    } else {
                                        Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_INCLUDE, i3));
                                        return null;
                                    }
                                }
                            }
                        }
                        next = argument.endsWith(",") ? 4 : 0;
                    } else {
                        if (!checkTags(argument, restricted)) {
                            Material material = Util.getType(argument);
                            if (material != null && (material.isBlock() || argAction.contains(4))) {
                                restricted.add(material);
                            } else {
                                EntityType entityType = Util.getEntityType(argument);
                                if (entityType != null) {
                                    restricted.add(entityType);
                                } else {
                                    Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_INCLUDE, argument));
                                    return null;
                                }
                            }
                        }
                        next = 0;
                    }
                } else {
                    next = 0;
                }
            }
            count++;
        }
        return restricted;
    }

    protected static long[] parseTime(String[] inputArguments) {
        String[] argumentArray = inputArguments.clone();
        long timeStart = 0;
        long timeEnd = 0;
        int count = 0;
        int next = 0;
        boolean range = false;
        double w = 0;
        double d = 0;
        double h = 0;
        double m = 0;
        double s = 0;
        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim().toLowerCase(Locale.ROOT)
                                   .replaceAll("\\\\", "")
                                   .replaceAll("'", "");

                if (argument.equals("t:") || argument.equals("time:")) {
                    next = 1;
                } else if (next == 1 || argument.startsWith("t:") || argument.startsWith("time:")) {
                    argument = argument.replaceAll("time:", "")
                                       .replaceAll("t:", "")
                                       .replaceAll("y", "y:")
                                       .replaceAll("m", "m:")
                                       .replaceAll("w", "w:")
                                       .replaceAll("d", "d:")
                                       .replaceAll("h", "h:")
                                       .replaceAll("s", "s:");
                    range = argument.contains("-");

                    int argCount = 0;
                    String[] i2 = argument.split(":");
                    for (String i3 : i2) {
                        if (range && argCount > 0 && timeStart == 0 && i3.startsWith("-")) {
                            timeStart = (long) (((w * 7 * 24 * 60 * 60) + (d * 24 * 60 * 60) + (h * 60 * 60) + (m * 60) + s));
                            w = 0;
                            d = 0;
                            h = 0;
                            m = 0;
                            s = 0;
                        }

                        if (i3.endsWith("w") && w == 0) {
                            String i4 = i3.replaceAll("[^0-9.]", "");
                            if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf('.') == i4.lastIndexOf('.')) {
                                w = Double.parseDouble(i4);
                            }
                        } else if (i3.endsWith("d") && d == 0) {
                            String i4 = i3.replaceAll("[^0-9.]", "");
                            if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf('.') == i4.lastIndexOf('.')) {
                                d = Double.parseDouble(i4);
                            }
                        } else if (i3.endsWith("h") && h == 0) {
                            String i4 = i3.replaceAll("[^0-9.]", "");
                            if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf('.') == i4.lastIndexOf('.')) {
                                h = Double.parseDouble(i4);
                            }
                        } else if (i3.endsWith("m") && m == 0) {
                            String i4 = i3.replaceAll("[^0-9.]", "");
                            if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf('.') == i4.lastIndexOf('.')) {
                                m = Double.parseDouble(i4);
                            }
                        } else if (i3.endsWith("s") && s == 0) {
                            String i4 = i3.replaceAll("[^0-9.]", "");
                            if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf('.') == i4.lastIndexOf('.')) {
                                s = Double.parseDouble(i4);
                            }
                        }

                        argCount++;
                    }
                    if (timeStart > 0) {
                        timeEnd = (long) (((w * 7 * 24 * 60 * 60) + (d * 24 * 60 * 60) + (h * 60 * 60) + (m * 60) + s));
                    } else {
                        timeStart = (long) (((w * 7 * 24 * 60 * 60) + (d * 24 * 60 * 60) + (h * 60 * 60) + (m * 60) + s));
                    }
                    next = 0;
                } else {
                    next = 0;
                }
            }
            count++;
        }

        if (timeEnd >= timeStart) {
            return new long[] { timeEnd, timeStart };
        } else {
            return new long[] { timeStart, timeEnd };
        }
    }

    private static String timeString(BigDecimal input) {
        return input.stripTrailingZeros().toPlainString();
    }

    protected static String parseTimeString(String[] inputArguments) {
        String[] argumentArray = inputArguments.clone();
        String time = "";
        int count = 0;
        int next = 0;
        boolean range = false;
        BigDecimal w = BigDecimal.ZERO;
        BigDecimal d = BigDecimal.ZERO;
        BigDecimal h = BigDecimal.ZERO;
        BigDecimal m = BigDecimal.ZERO;
        BigDecimal s = BigDecimal.ZERO;
        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim().toLowerCase(Locale.ROOT)
                                   .replaceAll("\\\\", "")
                                   .replaceAll("'", "");

                if (argument.equals("t:") || argument.equals("time:")) {
                    next = 1;
                } else if (next == 1 || argument.startsWith("t:") || argument.startsWith("time:")) {
                    argument = argument.replaceAll("time:", "")
                                       .replaceAll("t:", "")
                                       .replaceAll("y", "y:")
                                       .replaceAll("m", "m:")
                                       .replaceAll("w", "w:")
                                       .replaceAll("d", "d:")
                                       .replaceAll("h", "h:")
                                       .replaceAll("s", "s:");
                    range = argument.contains("-");

                    int argCount = 0;
                    String[] i2 = argument.split(":");
                    for (String i3 : i2) {
                        if (range && argCount > 0 && !time.contains("-") && i3.startsWith("-")) {
                            w = BigDecimal.ZERO;
                            d = BigDecimal.ZERO;
                            h = BigDecimal.ZERO;
                            m = BigDecimal.ZERO;
                            s = BigDecimal.ZERO;
                            time = time + " -";
                        }

                        if (i3.endsWith("w") && w.intValue() == 0) {
                            String i4 = i3.replaceAll("[^0-9.]", "");
                            if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf('.') == i4.lastIndexOf('.')) {
                                w = new BigDecimal(i4);
                                if (range) {
                                    time = time + " " + timeString(w) + "w";
                                } else {
                                    time = time + " " + Phrase.build(Phrase.TIME_WEEKS, timeString(w), (w.doubleValue() == 1 ? Selector.FIRST : Selector.SECOND));
                                }
                            }
                        } else if (i3.endsWith("d") && d.intValue() == 0) {
                            String i4 = i3.replaceAll("[^0-9.]", "");
                            if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf('.') == i4.lastIndexOf('.')) {
                                d = new BigDecimal(i4);
                                if (range) {
                                    time = time + " " + timeString(d) + "d";
                                } else {
                                    time = time + " " + Phrase.build(Phrase.TIME_DAYS, timeString(d), (d.doubleValue() == 1 ? Selector.FIRST : Selector.SECOND));
                                }
                            }
                        } else if (i3.endsWith("h") && h.intValue() == 0) {
                            String i4 = i3.replaceAll("[^0-9.]", "");
                            if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf('.') == i4.lastIndexOf('.')) {
                                h = new BigDecimal(i4);
                                if (range) {
                                    time = time + " " + timeString(h) + "h";
                                } else {
                                    time = time + " " + Phrase.build(Phrase.TIME_HOURS, timeString(h), (h.doubleValue() == 1 ? Selector.FIRST : Selector.SECOND));
                                }
                            }
                        } else if (i3.endsWith("m") && m.intValue() == 0) {
                            String i4 = i3.replaceAll("[^0-9.]", "");
                            if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf('.') == i4.lastIndexOf('.')) {
                                m = new BigDecimal(i4);
                                if (range) {
                                    time = time + " " + timeString(m) + "m";
                                } else {
                                    time = time + " " + Phrase.build(Phrase.TIME_MINUTES, timeString(m), (m.doubleValue() == 1 ? Selector.FIRST : Selector.SECOND));
                                }
                            }
                        } else if (i3.endsWith("s") && s.intValue() == 0) {
                            String i4 = i3.replaceAll("[^0-9.]", "");
                            if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf('.') == i4.lastIndexOf('.')) {
                                s = new BigDecimal(i4);
                                if (range) {
                                    time = time + " " + timeString(s) + "s";
                                } else {
                                    time = time + " " + Phrase.build(Phrase.TIME_SECONDS, timeString(s), (s.doubleValue() == 1 ? Selector.FIRST : Selector.SECOND));
                                }
                            }
                        }

                        argCount++;
                    }
                    if (time.startsWith(" ")) {
                        time = time.substring(1);
                    }
                    return time;
                }
            }
            count++;
        }

        return time;
    }

    protected static int parseRows(String[] inputArguments) {
        String[] argumentArray = inputArguments.clone();
        int rows = 0;
        int count = 0;
        int next = 0;

        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim().toLowerCase(Locale.ROOT)
                                   .replaceAll("\\\\", "")
                                   .replaceAll("'", "");

                if (argument.equals("rows:")) {
                    next = 1;
                } else if (next == 1 || argument.startsWith("rows:")) {
                    argument = argument.replaceAll("rows:", "").trim();
                    if (!argument.startsWith("-")) {
                        String i2 = argument.replaceAll("[^0-9]", "");
                        if (i2.length() > 0 && i2.length() < 10) {
                            rows = Integer.parseInt(i2);
                        }
                    }

                    next = 0;
                } else {
                    next = 0;
                }
            }
            count++;
        }

        return rows;
    }

    private static void parseUser(List<String> users, String string) {
        string = string.trim();
        if (string.contains(",")) {
            String[] data = string.split(",");
            for (String user : data) {
                validUserCheck(users, user);
            }
        } else {
            validUserCheck(users, string);
        }
    }

    protected static List<String> parseUsers(String[] inputArguments) {
        String[] argumentArray = inputArguments.clone();
        List<String> users = new ArrayList<>();
        int count = 0;
        int next = 0;
        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim().toLowerCase(Locale.ROOT)
                                   .replaceAll("\\\\", "")
                                   .replaceAll("'", "");

                if (next == 2) {
                    if (argument.endsWith(",")) {
                        next = 2;
                    } else {
                        next = 0;
                    }
                } else if (argument.equals("p:") || argument.equals("user:") || argument.equals("users:") || argument.equals("u:")) {
                    next = 1;
                } else if (next == 1 || argument.startsWith("p:") || argument.startsWith("user:") || argument.startsWith("users:") || argument.startsWith("u:")) {
                    argument = argument.replaceAll("user:", "")
                                       .replaceAll("users:", "")
                                       .replaceAll("p:", "")
                                       .replaceAll("u:", "");
                    if (argument.contains(",")) {
                        String[] i2 = argument.split(",");
                        for (String i3 : i2) {
                            parseUser(users, i3);
                        }
                        next = argument.endsWith(",") ? 1 : 0;
                    } else {
                        parseUser(users, argument);
                        next = 0;
                    }
                } else if (argument.endsWith(",") || argument.endsWith(":")) {
                    next = 2;
                } else if (argument.contains(":")) {
                    next = 0;
                } else {
                    parseUser(users, argument);
                    next = 0;
                }
            }
            count++;
        }
        return users;
    }

    protected static int parseWorld(String[] inputArguments, boolean processWorldEdit, boolean requireLoaded) {
        String[] argumentArray = inputArguments.clone();
        int world_id = 0;
        int count = 0;
        int next = 0;
        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim()
                                   .replaceAll("\\\\", "")
                                   .replaceAll("'", "");

