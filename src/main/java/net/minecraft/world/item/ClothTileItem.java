package net.minecraft.world.item;

import net.minecraft.world.level.tile.ClothTile;
import net.minecraft.world.level.tile.Tile;

public class ClothTileItem extends TileItem {
   public ClothTileItem(int var1) {
      super(var1);
      this.setMaxDamage(0);
      this.setStackedByData(true);
   }

   public int getIcon(ItemInstance var1) {
      return Tile.cloth.getTexture(2, ClothTile.getTileDataForItemAuxValue(var1.getAuxValue()));
   }

   public int getLevelDataForAuxValue(int var1) {
      return var1;
   }

   public String getDescriptionId(ItemInstance var1) {
      return super.getDescriptionId() + "." + DyePowderItem.COLOR_DESCS[ClothTile.getTileDataForItemAuxValue(var1.getAuxValue())];
   }
}
