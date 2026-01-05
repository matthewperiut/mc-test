package net.minecraft.world.level.tile;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import util.Mth;

public class PumpkinTile extends Tile {
   private boolean lit;

   protected PumpkinTile(int var1, int var2, boolean var3) {
      super(var1, Material.vegetable);
      this.tex = var2;
      this.setTicking(true);
      this.lit = var3;
   }

   public int getTexture(int var1, int var2) {
      if (var1 == 1) {
         return this.tex;
      } else if (var1 == 0) {
         return this.tex;
      } else {
         int var3 = this.tex + 1 + 16;
         if (this.lit) {
            ++var3;
         }

         if (var2 == 0 && var1 == 2) {
            return var3;
         } else if (var2 == 1 && var1 == 5) {
            return var3;
         } else if (var2 == 2 && var1 == 3) {
            return var3;
         } else {
            return var2 == 3 && var1 == 4 ? var3 : this.tex + 16;
         }
      }
   }

   public int getTexture(int var1) {
      if (var1 == 1) {
         return this.tex;
      } else if (var1 == 0) {
         return this.tex;
      } else {
         return var1 == 3 ? this.tex + 1 + 16 : this.tex + 16;
      }
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
      super.onPlace(var1, var2, var3, var4);
   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      int var5 = var1.getTile(var2, var3, var4);
      return (var5 == 0 || Tile.tiles[var5].material.isLiquid()) && var1.isSolidTile(var2, var3 - 1, var4);
   }

   public void setPlacedBy(Level var1, int var2, int var3, int var4, Mob var5) {
      int var6 = Mth.floor((double)(var5.yRot * 4.0F / 360.0F) + (double)0.5F) & 3;
      var1.setData(var2, var3, var4, var6);
   }
}
