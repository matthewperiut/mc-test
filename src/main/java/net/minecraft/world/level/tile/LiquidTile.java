package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class LiquidTile extends Tile {
   protected LiquidTile(int var1, Material var2) {
      super(var1, (var2 == Material.lava ? 14 : 12) * 16 + 13, var2);
      float var3 = 0.0F;
      float var4 = 0.0F;
      this.setShape(0.0F + var4, 0.0F + var3, 0.0F + var4, 1.0F + var4, 1.0F + var3, 1.0F + var4);
      this.setTicking(true);
   }

   public static float getHeight(int var0) {
      if (var0 >= 8) {
         var0 = 0;
      }

      float var1 = (float)(var0 + 1) / 9.0F;
      return var1;
   }

   public int getTexture(int var1) {
      return var1 != 0 && var1 != 1 ? this.tex + 1 : this.tex;
   }

   protected int getDepth(Level var1, int var2, int var3, int var4) {
      return var1.getMaterial(var2, var3, var4) != this.material ? -1 : var1.getData(var2, var3, var4);
   }

   protected int getRenderedDepth(LevelSource var1, int var2, int var3, int var4) {
      if (var1.getMaterial(var2, var3, var4) != this.material) {
         return -1;
      } else {
         int var5 = var1.getData(var2, var3, var4);
         if (var5 >= 8) {
            var5 = 0;
         }

         return var5;
      }
   }

   public boolean isCubeShaped() {
      return false;
   }

   public boolean isSolidRender() {
      return false;
   }

   public boolean mayPick(int var1, boolean var2) {
      return var2 && var1 == 0;
   }

   public boolean shouldRenderFace(LevelSource var1, int var2, int var3, int var4, int var5) {
      Material var6 = var1.getMaterial(var2, var3, var4);
      if (var6 == this.material) {
         return false;
      } else if (var6 == Material.ice) {
         return false;
      } else {
         return var5 == 1 ? true : super.shouldRenderFace(var1, var2, var3, var4, var5);
      }
   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      return null;
   }

   public int getRenderShape() {
      return 4;
   }

   public int getResource(int var1, Random var2) {
      return 0;
   }

   public int getResourceCount(Random var1) {
      return 0;
   }

   private Vec3 getFlow(LevelSource var1, int var2, int var3, int var4) {
      Vec3 var5 = Vec3.newTemp((double)0.0F, (double)0.0F, (double)0.0F);
      int var6 = this.getRenderedDepth(var1, var2, var3, var4);

      for(int var7 = 0; var7 < 4; ++var7) {
         int var8 = var2;
         int var10 = var4;
         if (var7 == 0) {
            var8 = var2 - 1;
         }

         if (var7 == 1) {
            var10 = var4 - 1;
         }

         if (var7 == 2) {
            ++var8;
         }

         if (var7 == 3) {
            ++var10;
         }

         int var11 = this.getRenderedDepth(var1, var8, var3, var10);
         if (var11 < 0) {
            if (!var1.getMaterial(var8, var3, var10).blocksMotion()) {
               var11 = this.getRenderedDepth(var1, var8, var3 - 1, var10);
               if (var11 >= 0) {
                  int var12 = var11 - (var6 - 8);
                  var5 = var5.add((double)((var8 - var2) * var12), (double)((var3 - var3) * var12), (double)((var10 - var4) * var12));
               }
            }
         } else if (var11 >= 0) {
            int var16 = var11 - var6;
            var5 = var5.add((double)((var8 - var2) * var16), (double)((var3 - var3) * var16), (double)((var10 - var4) * var16));
         }
      }

      if (var1.getData(var2, var3, var4) >= 8) {
         boolean var14 = false;
         if (var14 || this.shouldRenderFace(var1, var2, var3, var4 - 1, 2)) {
            var14 = true;
         }

         if (var14 || this.shouldRenderFace(var1, var2, var3, var4 + 1, 3)) {
            var14 = true;
         }

         if (var14 || this.shouldRenderFace(var1, var2 - 1, var3, var4, 4)) {
            var14 = true;
         }

         if (var14 || this.shouldRenderFace(var1, var2 + 1, var3, var4, 5)) {
            var14 = true;
         }

         if (var14 || this.shouldRenderFace(var1, var2, var3 + 1, var4 - 1, 2)) {
            var14 = true;
         }

         if (var14 || this.shouldRenderFace(var1, var2, var3 + 1, var4 + 1, 3)) {
            var14 = true;
         }

         if (var14 || this.shouldRenderFace(var1, var2 - 1, var3 + 1, var4, 4)) {
            var14 = true;
         }

         if (var14 || this.shouldRenderFace(var1, var2 + 1, var3 + 1, var4, 5)) {
            var14 = true;
         }

         if (var14) {
            var5 = var5.normalize().add((double)0.0F, (double)-6.0F, (double)0.0F);
         }
      }

      var5 = var5.normalize();
      return var5;
   }

   public void handleEntityInside(Level var1, int var2, int var3, int var4, Entity var5, Vec3 var6) {
      Vec3 var7 = this.getFlow(var1, var2, var3, var4);
      var6.x += var7.x;
      var6.y += var7.y;
      var6.z += var7.z;
   }

   public int getTickDelay() {
      if (this.material == Material.water) {
         return 5;
      } else {
         return this.material == Material.lava ? 30 : 0;
      }
   }

   public float getBrightness(LevelSource var1, int var2, int var3, int var4) {
      float var5 = var1.getBrightness(var2, var3, var4);
      float var6 = var1.getBrightness(var2, var3 + 1, var4);
      return var5 > var6 ? var5 : var6;
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      super.tick(var1, var2, var3, var4, var5);
   }

   public int getRenderLayer() {
      return this.material == Material.water ? 1 : 0;
   }

   public void animateTick(Level var1, int var2, int var3, int var4, Random var5) {
      if (this.material == Material.water && var5.nextInt(64) == 0) {
         int var6 = var1.getData(var2, var3, var4);
         if (var6 > 0 && var6 < 8) {
            var1.playSound((double)((float)var2 + 0.5F), (double)((float)var3 + 0.5F), (double)((float)var4 + 0.5F), "liquid.water", var5.nextFloat() * 0.25F + 0.75F, var5.nextFloat() * 1.0F + 0.5F);
         }
      }

      if (this.material == Material.lava && var1.getMaterial(var2, var3 + 1, var4) == Material.air && !var1.isSolidTile(var2, var3 + 1, var4) && var5.nextInt(100) == 0) {
         double var12 = (double)((float)var2 + var5.nextFloat());
         double var8 = (double)var3 + this.yy1;
         double var10 = (double)((float)var4 + var5.nextFloat());
         var1.addParticle("lava", var12, var8, var10, (double)0.0F, (double)0.0F, (double)0.0F);
      }

   }

   public static double getSlopeAngle(LevelSource var0, int var1, int var2, int var3, Material var4) {
      Vec3 var5 = null;
      if (var4 == Material.water) {
         var5 = ((LiquidTile)Tile.water).getFlow(var0, var1, var2, var3);
      }

      if (var4 == Material.lava) {
         var5 = ((LiquidTile)Tile.lava).getFlow(var0, var1, var2, var3);
      }

      return var5.x == (double)0.0F && var5.z == (double)0.0F ? (double)-1000.0F : Math.atan2(var5.z, var5.x) - (Math.PI / 2D);
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
      this.updateLiquid(var1, var2, var3, var4);
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      this.updateLiquid(var1, var2, var3, var4);
   }

   private void updateLiquid(Level var1, int var2, int var3, int var4) {
      if (var1.getTile(var2, var3, var4) == this.id) {
         if (this.material == Material.lava) {
            boolean var5 = false;
            if (var5 || var1.getMaterial(var2, var3, var4 - 1) == Material.water) {
               var5 = true;
            }

            if (var5 || var1.getMaterial(var2, var3, var4 + 1) == Material.water) {
               var5 = true;
            }

            if (var5 || var1.getMaterial(var2 - 1, var3, var4) == Material.water) {
               var5 = true;
            }

            if (var5 || var1.getMaterial(var2 + 1, var3, var4) == Material.water) {
               var5 = true;
            }

            if (var5 || var1.getMaterial(var2, var3 + 1, var4) == Material.water) {
               var5 = true;
            }

            if (var5) {
               int var6 = var1.getData(var2, var3, var4);
               if (var6 == 0) {
                  var1.setTile(var2, var3, var4, Tile.obsidian.id);
               } else if (var6 <= 4) {
                  var1.setTile(var2, var3, var4, Tile.stoneBrick.id);
               }

               this.fizz(var1, var2, var3, var4);
            }
         }

      }
   }

   protected void fizz(Level var1, int var2, int var3, int var4) {
      var1.playSound((double)((float)var2 + 0.5F), (double)((float)var3 + 0.5F), (double)((float)var4 + 0.5F), "random.fizz", 0.5F, 2.6F + (var1.random.nextFloat() - var1.random.nextFloat()) * 0.8F);

      for(int var5 = 0; var5 < 8; ++var5) {
         var1.addParticle("largesmoke", (double)var2 + Math.random(), (double)var3 + 1.2, (double)var4 + Math.random(), (double)0.0F, (double)0.0F, (double)0.0F);
      }

   }
}
