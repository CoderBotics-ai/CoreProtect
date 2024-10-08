package net.coreprotect.utility.serialize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;

import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.Util;

public class ItemMetaHandler {

    public static String getEnchantmentName(Enchantment enchantment, int level) {
        String name = enchantment.getKey().getKey();

        switch (name) {
            case "vanishing_curse" -> name = "Curse of Vanishing";
            case "binding_curse" -> name = "Curse of Binding";
            default -> name = Util.capitalize(name.replace("_", " "), true);
        }

        if (enchantment.getMaxLevel() > 1) {
            name = name + " " + getEnchantmentLevel(level);
        }

        return name;
    }

    private static String getEnchantmentLevel(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> Integer.toString(level);
        };
    }

    private static Map<Enchantment, Integer> getEnchantments(ItemMeta itemMeta) {
        if (itemMeta == null) {
            return null;
        }

        return itemMeta instanceof EnchantmentStorageMeta enchantmentStorageEngine 
            ? enchantmentStorageEngine.getStoredEnchants() 
            : itemMeta.getEnchants();
    }

    public static List<String> getEnchantments(ItemStack item, String displayName) {
        List<String> result = new ArrayList<>();
        ItemMeta itemMeta = item.getItemMeta();
        Map<Enchantment, Integer> enchantments = getEnchantments(itemMeta);

        for (Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer level = entry.getValue();

            result.add(getEnchantmentName(enchantment, level));
        }

        if (itemMeta.hasLore()) {
            for (String lore : itemMeta.getLore()) {
                result.add(Color.DARK_PURPLE + Color.ITALIC + lore);
            }
        }

        return result;
    }

    public static List<List<Map<String, Object>>> serialize(ItemStack item, Material type, String faceData, int slot) {
        List<List<Map<String, Object>>> metadata = new ArrayList<>();
        List<Map<String, Object>> list = new ArrayList<>();
        List<Object> modifiers = new ArrayList<>();

        if (item != null && item.hasItemMeta() && item.getItemMeta() != null) {
            ItemMeta itemMeta = item.getItemMeta().clone();

            if (itemMeta.hasAttributeModifiers()) {
                for (Map.Entry<Attribute, AttributeModifier> entry : itemMeta.getAttributeModifiers().entries()) {
                    Map<Attribute, Map<String, Object>> attributeList = new HashMap<>();
                    Attribute attribute = entry.getKey();
                    AttributeModifier modifier = entry.getValue();

                    itemMeta.removeAttributeModifier(attribute, modifier);
                    attributeList.put(attribute, modifier.serialize());
                    modifiers.add(attributeList);
                }
            }

            if (itemMeta instanceof LeatherArmorMeta meta) {
                LeatherArmorMeta subMeta = meta.clone();
                meta.setColor(Bukkit.getServer().getItemFactory().getDefaultLeatherColor());
                list.add(meta.serialize());
                metadata.add(list);

                list = new ArrayList<>();
                list.add(subMeta.getColor().serialize());
                metadata.add(list);
            } else if (itemMeta instanceof PotionMeta meta) {
                PotionMeta subMeta = meta.clone();
                meta.setColor(null);
                meta.clearCustomEffects();
                list.add(meta.serialize());

                if (subMeta.hasColor()) {
                    list.add(subMeta.getColor().serialize());
                }
                metadata.add(list);

                if (subMeta.hasCustomEffects()) {
                    for (PotionEffect effect : subMeta.getCustomEffects()) {
                        list = new ArrayList<>();
                        list.add(effect.serialize());
                        metadata.add(list);
                    }
                }
            } else if (itemMeta instanceof FireworkMeta meta) {
                FireworkMeta subMeta = meta.clone();
                meta.clearEffects();
                list.add(meta.serialize());
                metadata.add(list);

                if (subMeta.hasEffects()) {
                    for (FireworkEffect effect : subMeta.getEffects()) {
                        deserializeFireworkEffect(effect, metadata);
                    }
                }
            } else if (itemMeta instanceof FireworkEffectMeta meta) {
                FireworkEffectMeta subMeta = meta.clone();
                meta.setEffect(null);
                list.add(meta.serialize());
                metadata.add(list);

                if (subMeta.hasEffect()) {
                    FireworkEffect effect = subMeta.getEffect();
                    deserializeFireworkEffect(effect, metadata);
                }
            } else if (itemMeta instanceof BannerMeta meta) {
                BannerMeta subMeta = (BannerMeta) itemMeta.clone();
                meta.setPatterns(new ArrayList<>());
                list.add(meta.serialize());
                metadata.add(list);

                for (Pattern pattern : subMeta.getPatterns()) {
                    list = new ArrayList<>();
                    list.add(pattern.serialize());
                    metadata.add(list);
                }
            } else if (itemMeta instanceof CrossbowMeta meta) {
                CrossbowMeta subMeta = meta.clone();
                meta.setChargedProjectiles(null);
                list.add(meta.serialize());
                metadata.add(list);

                if (subMeta.hasChargedProjectiles()) {
                    list = new ArrayList<>();

                    for (ItemStack chargedProjectile : subMeta.getChargedProjectiles()) {
                        Map<String, Object> itemMap = Util.serializeItemStack(chargedProjectile, null, slot);
                        if (!itemMap.isEmpty()) {
                            list.add(itemMap);
                        }
                    }

                    metadata.add(list);
                }
            } else if (itemMeta instanceof MapMeta meta) {
                MapMeta subMeta = meta.clone();
                meta.setColor(null);
                list.add(meta.serialize());
                metadata.add(list);

                if (subMeta.hasColor()) {
                    list = new ArrayList<>();
                    list.add(subMeta.getColor().serialize());
                    metadata.add(list);
                }
            } else if (itemMeta instanceof SuspiciousStewMeta meta) {
                SuspiciousStewMeta subMeta = meta.clone();
                meta.clearCustomEffects();
                list.add(meta.serialize());
                metadata.add(list);

                if (subMeta.hasCustomEffects()) {
                    for (PotionEffect effect : subMeta.getCustomEffects()) {
                        list = new ArrayList<>();
                        list.add(effect.serialize());
                        metadata.add(list);
                    }
                }
            } else if (!BukkitAdapter.ADAPTER.getItemMeta(itemMeta, list, metadata, slot)) {
                list.add(itemMeta.serialize());
                metadata.add(list);
            }
        }

        if (type != null && type.equals(Material.ARMOR_STAND)) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("slot", slot);
            list = new ArrayList<>();
            list.add(meta);
            metadata.add(list);
        }

        if (faceData != null && !faceData.isEmpty()) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("facing", faceData);
            list = new ArrayList<>();
            list.add(meta);
            metadata.add(list);
        }

        if (!modifiers.isEmpty()) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("modifiers", modifiers);
            list = new ArrayList<>();
            list.add(meta);
            metadata.add(list);
        }

        return metadata;
    }

    private static void deserializeFireworkEffect(FireworkEffect effect, List<List<Map<String, Object>>> metadata) {
        List<Map<String, Object>> colorList = new ArrayList<>();
        List<Map<String, Object>> fadeList = new ArrayList<>();
        List<Map<String, Object>> list = new ArrayList<>();

        for (org.bukkit.Color color : effect.getColors()) {
            colorList.add(color.serialize());
        }

        for (org.bukkit.Color color : effect.getFadeColors()) {
            fadeList.add(color.serialize());
        }

        Map<String, Object> hasCheck = new HashMap<>();
        hasCheck.put("type", effect.getType());
        hasCheck.put("flicker", effect.hasFlicker());
        hasCheck.put("trail", effect.hasTrail());
        list.add(hasCheck);
        metadata.add(list);
        metadata.add(colorList);
        metadata.add(fadeList);
    }
}