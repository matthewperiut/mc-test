package net.minecraft.world.item;

import net.minecraft.world.level.tile.Tile;

public class TreeTileItem extends TileItem {
   public TreeTileItem(int var1) {
      super(var1);
      this.setMaxDamage(0);
      this.setStackedByData(true);
   }

   public int getIcon(ItemInstance var1) {
      return Tile.treeTrunk.getTexture(2, var1.getAuxValue());
   }

   public int getLevelDataForAuxValue(int var1) {
      return var1;
   }
}
