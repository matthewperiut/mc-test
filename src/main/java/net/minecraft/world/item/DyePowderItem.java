package net.minecraft.world.item;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.ClothTile;
import net.minecraft.world.level.tile.CropTile;
import net.minecraft.world.level.tile.Sapling;
import net.minecraft.world.level.tile.Tile;

public class DyePowderItem extends Item {
   public static final String[] COLOR_DESCS = new String[]{"black", "red", "green", "brown", "blue", "purple", "cyan", "silver", "gray", "pink", "lime", "yellow", "lightBlue", "magenta", "orange", "white"};
   public static final int BLACK = 0;
   public static final int RED = 1;
   public static final int GREEN = 2;
   public static final int BROWN = 3;
   public static final int BLUE = 4;
   public static final int PURPLE = 5;
   public static final int CYAN = 6;
   public static final int SILVER = 7;
   public static final int GRAY = 8;
   public static final int PINK = 9;
   public static final int LIME = 10;
   public static final int YELLOW = 11;
   public static final int LIGHT_BLUE = 12;
   public static final int MAGENTA = 13;
   public static final int ORANGE = 14;
   public static final int WHITE = 15;

   public DyePowderItem(int var1) {
      super(var1);
      this.setStackedByData(true);
      this.setMaxDamage(0);
   }

   public int getIcon(ItemInstance var1) {
      int var2 = var1.getAuxValue();
      return this.icon + var2 % 8 * 16 + var2 / 8;
   }

   public String getDescriptionId(ItemInstance var1) {
      return super.getDescriptionId() + "." + COLOR_DESCS[var1.getAuxValue()];
   }

   public boolean useOn(ItemInstance var1, Player var2, Level var3, int var4, int var5, int var6, int var7) {
      if (var1.getAuxValue() == 15) {
         int var8 = var3.getTile(var4, var5, var6);
         if (var8 == Tile.sapling.id) {
            ((Sapling)Tile.sapling).growTree(var3, var4, var5, var6, var3.random);
            --var1.count;
            return true;
         }

         if (var8 == Tile.crops.id) {
            ((CropTile)Tile.crops).growCropsToMax(var3, var4, var5, var6);
            --var1.count;
            return true;
         }
      }

      return false;
   }

   public void interactEnemy(ItemInstance var1, Mob var2) {
      if (var2 instanceof Sheep) {
         Sheep var3 = (Sheep)var2;
         int var4 = ClothTile.getTileDataForItemAuxValue(var1.getAuxValue());
         if (!var3.isSheared() && var3.getColor() != var4) {
            var3.setColor(var4);
            --var1.count;
         }
      }

   }
}
