package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

public class LadderTile extends Tile {
   protected LadderTile(int var1, int var2) {
      super(var1, var2, Material.decoration);
   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      int var5 = var1.getData(var2, var3, var4);
      float var6 = 0.125F;
      if (var5 == 2) {
         this.setShape(0.0F, 0.0F, 1.0F - var6, 1.0F, 1.0F, 1.0F);
      }

      if (var5 == 3) {
         this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, var6);
      }

      if (var5 == 4) {
         this.setShape(1.0F - var6, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      }

      if (var5 == 5) {
         this.setShape(0.0F, 0.0F, 0.0F, var6, 1.0F, 1.0F);
      }

      return super.getAABB(var1, var2, var3, var4);
   }

   public AABB getTileAABB(Level var1, int var2, int var3, int var4) {
      int var5 = var1.getData(var2, var3, var4);
      float var6 = 0.125F;
      if (var5 == 2) {
         this.setShape(0.0F, 0.0F, 1.0F - var6, 1.0F, 1.0F, 1.0F);
      }

      if (var5 == 3) {
         this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, var6);
      }

      if (var5 == 4) {
         this.setShape(1.0F - var6, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      }

      if (var5 == 5) {
         this.setShape(0.0F, 0.0F, 0.0F, var6, 1.0F, 1.0F);
      }

      return super.getTileAABB(var1, var2, var3, var4);
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
      return 8;
   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      if (var1.isSolidTile(var2 - 1, var3, var4)) {
         return true;
      } else if (var1.isSolidTile(var2 + 1, var3, var4)) {
         return true;
      } else if (var1.isSolidTile(var2, var3, var4 - 1)) {
         return true;
      } else {
         return var1.isSolidTile(var2, var3, var4 + 1);
      }
   }

   public void setPlacedOnFace(Level var1, int var2, int var3, int var4, int var5) {
      int var6 = var1.getData(var2, var3, var4);
      if ((var6 == 0 || var5 == 2) && var1.isSolidTile(var2, var3, var4 + 1)) {
         var6 = 2;
      }

      if ((var6 == 0 || var5 == 3) && var1.isSolidTile(var2, var3, var4 - 1)) {
         var6 = 3;
      }

      if ((var6 == 0 || var5 == 4) && var1.isSolidTile(var2 + 1, var3, var4)) {
         var6 = 4;
      }

      if ((var6 == 0 || var5 == 5) && var1.isSolidTile(var2 - 1, var3, var4)) {
         var6 = 5;
      }

      var1.setData(var2, var3, var4, var6);
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      int var6 = var1.getData(var2, var3, var4);
      boolean var7 = false;
      if (var6 == 2 && var1.isSolidTile(var2, var3, var4 + 1)) {
         var7 = true;
      }

      if (var6 == 3 && var1.isSolidTile(var2, var3, var4 - 1)) {
         var7 = true;
      }

      if (var6 == 4 && var1.isSolidTile(var2 + 1, var3, var4)) {
         var7 = true;
      }

      if (var6 == 5 && var1.isSolidTile(var2 - 1, var3, var4)) {
         var7 = true;
      }

      if (!var7) {
         this.spawnResources(var1, var2, var3, var4, var6);
         var1.setTile(var2, var3, var4, 0);
      }

      super.neighborChanged(var1, var2, var3, var4, var5);
   }

   public int getResourceCount(Random var1) {
      return 1;
   }
}
