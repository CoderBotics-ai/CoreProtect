package net.coreprotect.bukkit;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public interface BukkitInterface {

    ItemStack adjustIngredient(MerchantRecipe recipe, ItemStack itemStack);

    Material getBucketContents(Material material);

    Material getFrameType(Entity entity);

    Material getFrameType(EntityType type);

    Class<?> getFrameClass(Material material);

    String parseLegacyName(String name);

    boolean getEntityMeta(LivingEntity entity, List<Object> info);

    boolean setEntityMeta(Entity entity, Object value, int count);

    boolean getItemMeta(ItemMeta itemMeta, List<Map<String, Object>> list, List<List<Map<String, Object>>> metadata, int slot);

    boolean setItemMeta(Material rowType, ItemStack itemstack, List<Map<String, Object>> map);

    boolean isAttached(Block block, Block scanBlock, BlockData blockData, int scanMin);

    boolean isWall(BlockData blockData);

    boolean isItemFrame(Material material);

    boolean isGlowing(Sign sign, boolean isFront);

    boolean isInvisible(Material material);

    boolean isWaxed(Sign sign);

    int getMinHeight(World world);

    int getLegacyBlockId(Material material);

    void setGlowing(Sign sign, boolean isFront, boolean isGlowing);

    void setColor(Sign sign, boolean isFront, int color);

    void setWaxed(Sign sign, boolean isWaxed);

    int getColor(Sign sign, boolean isFront);

    Material getPlantSeeds(Material material);

    boolean isDecoratedPot(Material material);

    boolean isSuspiciousBlock(Material material);

    boolean isSign(Material material);

    boolean isChiseledBookshelf(Material material);

    boolean isBookshelfBook(Material material);

    ItemStack getChiseledBookshelfBook(BlockState blockState, PlayerInteractEvent event);

    String getLine(Sign sign, int line);

    void setLine(Sign sign, int line, String string);

    boolean isSignFront(SignChangeEvent event);

    ItemStack getArrowMeta(Arrow arrow, ItemStack itemStack);

    EntityType getEntityType(Material material);

    Object getRegistryKey(Object value);

    Object getRegistryValue(String key, Object tClass);
}