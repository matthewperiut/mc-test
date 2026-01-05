package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

public class FarmTile extends Tile {
   protected FarmTile(int var1) {
      super(var1, Material.dirt);
      this.tex = 87;
      this.setTicking(true);
      this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 0.9375F, 1.0F);
      this.setLightBlock(255);
   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      return AABB.newTemp((double)(var2 + 0), (double)(var3 + 0), (double)(var4 + 0), (double)(var2 + 1), (double)(var3 + 1), (double)(var4 + 1));
   }

   public boolean isSolidRender() {
      return false;
   }

   public boolean isCubeShaped() {
      return false;
   }

   public int getTexture(int var1, int var2) {
      if (var1 == 1 && var2 > 0) {
         return this.tex - 1;
      } else {
         return var1 == 1 ? this.tex : 2;
      }
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      if (var5.nextInt(5) == 0) {
         if (this.isNearWater(var1, var2, var3, var4)) {
            var1.setData(var2, var3, var4, 7);
         } else {
            int var6 = var1.getData(var2, var3, var4);
            if (var6 > 0) {
               var1.setData(var2, var3, var4, var6 - 1);
            } else if (!this.isUnderCrops(var1, var2, var3, var4)) {
               var1.setTile(var2, var3, var4, Tile.dirt.id);
            }
         }
      }

   }

   public void stepOn(Level var1, int var2, int var3, int var4, Entity var5) {
      if (var1.random.nextInt(4) == 0) {
         var1.setTile(var2, var3, var4, Tile.dirt.id);
      }

   }

   private boolean isUnderCrops(Level var1, int var2, int var3, int var4) {
      byte var5 = 0;

      for(int var6 = var2 - var5; var6 <= var2 + var5; ++var6) {
         for(int var7 = var4 - var5; var7 <= var4 + var5; ++var7) {
            if (var1.getTile(var6, var3 + 1, var7) == Tile.crops.id) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean isNearWater(Level var1, int var2, int var3, int var4) {
      for(int var5 = var2 - 4; var5 <= var2 + 4; ++var5) {
         for(int var6 = var3; var6 <= var3 + 1; ++var6) {
            for(int var7 = var4 - 4; var7 <= var4 + 4; ++var7) {
               if (var1.getMaterial(var5, var6, var7) == Material.water) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      super.neighborChanged(var1, var2, var3, var4, var5);
      Material var6 = var1.getMaterial(var2, var3 + 1, var4);
      if (var6.isSolid()) {
         var1.setTile(var2, var3, var4, Tile.dirt.id);
      }

   }

   public boolean blocksLight() {
      return true;
   }

   public int getResource(int var1, Random var2) {
      return Tile.dirt.getResource(0, var2);
   }
}
