package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

public class ReedTile extends Tile {
   protected ReedTile(int var1, int var2) {
      super(var1, Material.plant);
      this.tex = var2;
      float var3 = 0.375F;
      this.setShape(0.5F - var3, 0.0F, 0.5F - var3, 0.5F + var3, 1.0F, 0.5F + var3);
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

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      int var5 = var1.getTile(var2, var3 - 1, var4);
      if (var5 == this.id) {
         return true;
      } else if (var5 != Tile.grass.id && var5 != Tile.dirt.id) {
         return false;
      } else if (var1.getMaterial(var2 - 1, var3 - 1, var4) == Material.water) {
         return true;
      } else if (var1.getMaterial(var2 + 1, var3 - 1, var4) == Material.water) {
         return true;
      } else if (var1.getMaterial(var2, var3 - 1, var4 - 1) == Material.water) {
         return true;
      } else {
         return var1.getMaterial(var2, var3 - 1, var4 + 1) == Material.water;
      }
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      this.checkAlive(var1, var2, var3, var4);
   }

   protected final void checkAlive(Level var1, int var2, int var3, int var4) {
      if (!this.canSurvive(var1, var2, var3, var4)) {
         this.spawnResources(var1, var2, var3, var4, var1.getData(var2, var3, var4));
         var1.setTile(var2, var3, var4, 0);
      }

   }

   public boolean canSurvive(Level var1, int var2, int var3, int var4) {
      return this.mayPlace(var1, var2, var3, var4);
   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      return null;
   }

   public int getResource(int var1, Random var2) {
      return Item.reeds.id;
   }

   public boolean blocksLight() {
      return false;
   }

   public boolean isSolidRender() {
      return false;
   }

   public boolean isCubeShaped() {
      return false;
   }

   public int getRenderShape() {
      return 1;
   }
}
