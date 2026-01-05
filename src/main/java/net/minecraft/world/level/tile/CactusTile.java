package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

public class CactusTile extends Tile {
   protected CactusTile(int var1, int var2) {
      super(var1, var2, Material.cactus);
      this.setTicking(true);
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      if (var1.isEmptyTile(var2, var3 + 1, var4)) {
         int var6;
         for(var6 = 1; var1.getTile(var2, var3 - var6, var4) == this.id; ++var6) {
         }

         if (var6 < 3) {
            int var7 = var1.getData(var2, var3, var4);
            if (var7 == 15) {
               var1.setTile(var2, var3 + 1, var4, this.id);
               var1.setData(var2, var3, var4, 0);
            } else {
               var1.setData(var2, var3, var4, var7 + 1);
            }
         }
      }

   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      float var5 = 0.0625F;
      return AABB.newTemp((double)((float)var2 + var5), (double)var3, (double)((float)var4 + var5), (double)((float)(var2 + 1) - var5), (double)((float)(var3 + 1) - var5), (double)((float)(var4 + 1) - var5));
   }

   public AABB getTileAABB(Level var1, int var2, int var3, int var4) {
      float var5 = 0.0625F;
      return AABB.newTemp((double)((float)var2 + var5), (double)var3, (double)((float)var4 + var5), (double)((float)(var2 + 1) - var5), (double)(var3 + 1), (double)((float)(var4 + 1) - var5));
   }

   public int getTexture(int var1) {
      if (var1 == 1) {
         return this.tex - 1;
      } else {
         return var1 == 0 ? this.tex + 1 : this.tex;
      }
   }

   public boolean isCubeShaped() {
      return false;
   }

   public boolean isSolidRender() {
      return false;
   }

   public int getRenderShape() {
      return 13;
   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      return !super.mayPlace(var1, var2, var3, var4) ? false : this.canSurvive(var1, var2, var3, var4);
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      if (!this.canSurvive(var1, var2, var3, var4)) {
         this.spawnResources(var1, var2, var3, var4, var1.getData(var2, var3, var4));
         var1.setTile(var2, var3, var4, 0);
      }

   }

   public boolean canSurvive(Level var1, int var2, int var3, int var4) {
      if (var1.getMaterial(var2 - 1, var3, var4).isSolid()) {
         return false;
      } else if (var1.getMaterial(var2 + 1, var3, var4).isSolid()) {
         return false;
      } else if (var1.getMaterial(var2, var3, var4 - 1).isSolid()) {
         return false;
      } else if (var1.getMaterial(var2, var3, var4 + 1).isSolid()) {
         return false;
      } else {
         int var5 = var1.getTile(var2, var3 - 1, var4);
         return var5 == Tile.cactus.id || var5 == Tile.sand.id;
      }
   }

   public void entityInside(Level var1, int var2, int var3, int var4, Entity var5) {
      var5.hurt((Entity)null, 1);
   }
}
