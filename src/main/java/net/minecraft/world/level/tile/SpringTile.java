package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;

public class SpringTile extends Tile {
   private int liquidTileId;

   protected SpringTile(int var1, int var2) {
      super(var1, Tile.tiles[var2].tex, Material.water);
      this.liquidTileId = var2;
      this.setTicking(true);
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
      super.onPlace(var1, var2, var3, var4);
      if (var1.isEmptyTile(var2 - 1, var3, var4)) {
         var1.setTile(var2 - 1, var3, var4, this.liquidTileId);
      }

      if (var1.isEmptyTile(var2 + 1, var3, var4)) {
         var1.setTile(var2 + 1, var3, var4, this.liquidTileId);
      }

      if (var1.isEmptyTile(var2, var3, var4 - 1)) {
         var1.setTile(var2, var3, var4 - 1, this.liquidTileId);
      }

      if (var1.isEmptyTile(var2, var3, var4 + 1)) {
         var1.setTile(var2, var3, var4 + 1, this.liquidTileId);
      }

   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      super.tick(var1, var2, var3, var4, var5);
      if (var1.isEmptyTile(var2 - 1, var3, var4)) {
         var1.setTile(var2 - 1, var3, var4, this.liquidTileId);
      }

      if (var1.isEmptyTile(var2 + 1, var3, var4)) {
         var1.setTile(var2 + 1, var3, var4, this.liquidTileId);
      }

      if (var1.isEmptyTile(var2, var3, var4 - 1)) {
         var1.setTile(var2, var3, var4 - 1, this.liquidTileId);
      }

      if (var1.isEmptyTile(var2, var3, var4 + 1)) {
         var1.setTile(var2, var3, var4 + 1, this.liquidTileId);
      }

   }
}
