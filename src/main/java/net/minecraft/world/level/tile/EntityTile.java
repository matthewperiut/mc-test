package net.minecraft.world.level.tile;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.entity.TileEntity;

public abstract class EntityTile extends Tile {
   protected EntityTile(int var1, Material var2) {
      super(var1, var2);
      isEntityTile[var1] = true;
   }

   protected EntityTile(int var1, int var2, Material var3) {
      super(var1, var2, var3);
      isEntityTile[var1] = true;
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
      super.onPlace(var1, var2, var3, var4);
      var1.setTileEntity(var2, var3, var4, this.newTileEntity());
   }

   public void onRemove(Level var1, int var2, int var3, int var4) {
      super.onRemove(var1, var2, var3, var4);
      var1.removeTileEntity(var2, var3, var4);
   }

   protected abstract TileEntity newTileEntity();
}
