package net.minecraft.world.item;

import net.minecraft.world.level.tile.Tile;

public class ShovelItem extends DiggerItem {
   private static Tile[] diggables;

   public ShovelItem(int var1, Item.Tier var2) {
      super(var1, 1, var2, diggables);
   }

   public boolean canDestroySpecial(Tile var1) {
      if (var1 == Tile.topSnow) {
         return true;
      } else {
         return var1 == Tile.snow;
      }
   }

   static {
      diggables = new Tile[]{Tile.grass, Tile.dirt, Tile.sand, Tile.gravel, Tile.topSnow, Tile.snow, Tile.clay};
   }
}
