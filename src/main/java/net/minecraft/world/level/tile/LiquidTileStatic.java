package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;

public class LiquidTileStatic extends LiquidTile {
   protected LiquidTileStatic(int var1, Material var2) {
      super(var1, var2);
      this.setTicking(false);
      if (var2 == Material.lava) {
         this.setTicking(true);
      }

   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      super.neighborChanged(var1, var2, var3, var4, var5);
      if (var1.getTile(var2, var3, var4) == this.id) {
         this.setDynamic(var1, var2, var3, var4);
      }

   }

   private void setDynamic(Level var1, int var2, int var3, int var4) {
      int var5 = var1.getData(var2, var3, var4);
      var1.noNeighborUpdate = true;
      var1.setTileAndDataNoUpdate(var2, var3, var4, this.id - 1, var5);
      var1.setTilesDirty(var2, var3, var4, var2, var3, var4);
      var1.addToTickNextTick(var2, var3, var4, this.id - 1);
      var1.noNeighborUpdate = false;
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      if (this.material == Material.lava) {
         int var6 = var5.nextInt(3);

         for(int var7 = 0; var7 < var6; ++var7) {
            var2 += var5.nextInt(3) - 1;
            ++var3;
            var4 += var5.nextInt(3) - 1;
            int var8 = var1.getTile(var2, var3, var4);
            if (var8 == 0) {
               if (this.isFlammable(var1, var2 - 1, var3, var4) || this.isFlammable(var1, var2 + 1, var3, var4) || this.isFlammable(var1, var2, var3, var4 - 1) || this.isFlammable(var1, var2, var3, var4 + 1) || this.isFlammable(var1, var2, var3 - 1, var4) || this.isFlammable(var1, var2, var3 + 1, var4)) {
                  var1.setTile(var2, var3, var4, Tile.fire.id);
                  return;
               }
            } else if (Tile.tiles[var8].material.blocksMotion()) {
               return;
            }
         }
      }

   }

   private boolean isFlammable(Level var1, int var2, int var3, int var4) {
      return var1.getMaterial(var2, var3, var4).isFlammable();
   }
}
