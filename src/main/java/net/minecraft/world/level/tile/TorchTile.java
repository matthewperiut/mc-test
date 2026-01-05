package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class TorchTile extends Tile {
   protected TorchTile(int var1, int var2) {
      super(var1, var2, Material.decoration);
      this.setTicking(true);
   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      return null;
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
      return 2;
   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      if (var1.isSolidTile(var2 - 1, var3, var4)) {
         return true;
      } else if (var1.isSolidTile(var2 + 1, var3, var4)) {
         return true;
      } else if (var1.isSolidTile(var2, var3, var4 - 1)) {
         return true;
      } else if (var1.isSolidTile(var2, var3, var4 + 1)) {
         return true;
      } else {
         return var1.isSolidTile(var2, var3 - 1, var4);
      }
   }

   public void setPlacedOnFace(Level var1, int var2, int var3, int var4, int var5) {
      int var6 = var1.getData(var2, var3, var4);
      if (var5 == 1 && var1.isSolidTile(var2, var3 - 1, var4)) {
         var6 = 5;
      }

      if (var5 == 2 && var1.isSolidTile(var2, var3, var4 + 1)) {
         var6 = 4;
      }

      if (var5 == 3 && var1.isSolidTile(var2, var3, var4 - 1)) {
         var6 = 3;
      }

      if (var5 == 4 && var1.isSolidTile(var2 + 1, var3, var4)) {
         var6 = 2;
      }

      if (var5 == 5 && var1.isSolidTile(var2 - 1, var3, var4)) {
         var6 = 1;
      }

      var1.setData(var2, var3, var4, var6);
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      super.tick(var1, var2, var3, var4, var5);
      if (var1.getData(var2, var3, var4) == 0) {
         this.onPlace(var1, var2, var3, var4);
      }

   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
      if (var1.isSolidTile(var2 - 1, var3, var4)) {
         var1.setData(var2, var3, var4, 1);
      } else if (var1.isSolidTile(var2 + 1, var3, var4)) {
         var1.setData(var2, var3, var4, 2);
      } else if (var1.isSolidTile(var2, var3, var4 - 1)) {
         var1.setData(var2, var3, var4, 3);
      } else if (var1.isSolidTile(var2, var3, var4 + 1)) {
         var1.setData(var2, var3, var4, 4);
      } else if (var1.isSolidTile(var2, var3 - 1, var4)) {
         var1.setData(var2, var3, var4, 5);
      }

      this.checkCanSurvive(var1, var2, var3, var4);
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      if (this.checkCanSurvive(var1, var2, var3, var4)) {
         int var6 = var1.getData(var2, var3, var4);
         boolean var7 = false;
         if (!var1.isSolidTile(var2 - 1, var3, var4) && var6 == 1) {
            var7 = true;
         }

         if (!var1.isSolidTile(var2 + 1, var3, var4) && var6 == 2) {
            var7 = true;
         }

         if (!var1.isSolidTile(var2, var3, var4 - 1) && var6 == 3) {
            var7 = true;
         }

         if (!var1.isSolidTile(var2, var3, var4 + 1) && var6 == 4) {
            var7 = true;
         }

         if (!var1.isSolidTile(var2, var3 - 1, var4) && var6 == 5) {
            var7 = true;
         }

         if (var7) {
            this.spawnResources(var1, var2, var3, var4, var1.getData(var2, var3, var4));
            var1.setTile(var2, var3, var4, 0);
         }
      }

   }

   private boolean checkCanSurvive(Level var1, int var2, int var3, int var4) {
      if (!this.mayPlace(var1, var2, var3, var4)) {
         this.spawnResources(var1, var2, var3, var4, var1.getData(var2, var3, var4));
         var1.setTile(var2, var3, var4, 0);
         return false;
      } else {
         return true;
      }
   }

   public HitResult clip(Level var1, int var2, int var3, int var4, Vec3 var5, Vec3 var6) {
      int var7 = var1.getData(var2, var3, var4) & 7;
      float var8 = 0.15F;
      if (var7 == 1) {
         this.setShape(0.0F, 0.2F, 0.5F - var8, var8 * 2.0F, 0.8F, 0.5F + var8);
      } else if (var7 == 2) {
         this.setShape(1.0F - var8 * 2.0F, 0.2F, 0.5F - var8, 1.0F, 0.8F, 0.5F + var8);
      } else if (var7 == 3) {
         this.setShape(0.5F - var8, 0.2F, 0.0F, 0.5F + var8, 0.8F, var8 * 2.0F);
      } else if (var7 == 4) {
         this.setShape(0.5F - var8, 0.2F, 1.0F - var8 * 2.0F, 0.5F + var8, 0.8F, 1.0F);
      } else {
         var8 = 0.1F;
         this.setShape(0.5F - var8, 0.0F, 0.5F - var8, 0.5F + var8, 0.6F, 0.5F + var8);
      }

      return super.clip(var1, var2, var3, var4, var5, var6);
   }

   public void animateTick(Level var1, int var2, int var3, int var4, Random var5) {
      int var6 = var1.getData(var2, var3, var4);
      double var7 = (double)((float)var2 + 0.5F);
      double var9 = (double)((float)var3 + 0.7F);
      double var11 = (double)((float)var4 + 0.5F);
      double var13 = (double)0.22F;
      double var15 = (double)0.27F;
      if (var6 == 1) {
         var1.addParticle("smoke", var7 - var15, var9 + var13, var11, (double)0.0F, (double)0.0F, (double)0.0F);
         var1.addParticle("flame", var7 - var15, var9 + var13, var11, (double)0.0F, (double)0.0F, (double)0.0F);
      } else if (var6 == 2) {
         var1.addParticle("smoke", var7 + var15, var9 + var13, var11, (double)0.0F, (double)0.0F, (double)0.0F);
         var1.addParticle("flame", var7 + var15, var9 + var13, var11, (double)0.0F, (double)0.0F, (double)0.0F);
      } else if (var6 == 3) {
         var1.addParticle("smoke", var7, var9 + var13, var11 - var15, (double)0.0F, (double)0.0F, (double)0.0F);
         var1.addParticle("flame", var7, var9 + var13, var11 - var15, (double)0.0F, (double)0.0F, (double)0.0F);
      } else if (var6 == 4) {
         var1.addParticle("smoke", var7, var9 + var13, var11 + var15, (double)0.0F, (double)0.0F, (double)0.0F);
         var1.addParticle("flame", var7, var9 + var13, var11 + var15, (double)0.0F, (double)0.0F, (double)0.0F);
      } else {
         var1.addParticle("smoke", var7, var9, var11, (double)0.0F, (double)0.0F, (double)0.0F);
         var1.addParticle("flame", var7, var9, var11, (double)0.0F, (double)0.0F, (double)0.0F);
      }

   }
}
