package net.minecraft.world.item;

import net.minecraft.world.level.tile.Tile;

public class HatchetItem extends DiggerItem {
   private static Tile[] diggables;

   protected HatchetItem(int var1, Item.Tier var2) {
      super(var1, 3, var2, diggables);
   }

   static {
      diggables = new Tile[]{Tile.wood, Tile.bookshelf, Tile.treeTrunk, Tile.chest};
   }
}
