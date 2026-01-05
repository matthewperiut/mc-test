package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

public class PortalTile extends HalfTransparentTile {
   public PortalTile(int var1, int var2) {
      super(var1, var2, Material.portal, false);
   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      return null;
   }

   public void updateShape(LevelSource var1, int var2, int var3, int var4) {
      if (var1.getTile(var2 - 1, var3, var4) != this.id && var1.getTile(var2 + 1, var3, var4) != this.id) {
         float var7 = 0.125F;
         float var8 = 0.5F;
         this.setShape(0.5F - var7, 0.0F, 0.5F - var8, 0.5F + var7, 1.0F, 0.5F + var8);
      } else {
         float var5 = 0.5F;
         float var6 = 0.125F;
         this.setShape(0.5F - var5, 0.0F, 0.5F - var6, 0.5F + var5, 1.0F, 0.5F + var6);
      }

   }

   public boolean isSolidRender() {
      return false;
   }

   public boolean isCubeShaped() {
      return false;
   }

   public boolean trySpawnPortal(Level var1, int var2, int var3, int var4) {
      byte var5 = 0;
      byte var6 = 0;
      if (var1.getTile(var2 - 1, var3, var4) == Tile.obsidian.id || var1.getTile(var2 + 1, var3, var4) == Tile.obsidian.id) {
         var5 = 1;
      }

      if (var1.getTile(var2, var3, var4 - 1) == Tile.obsidian.id || var1.getTile(var2, var3, var4 + 1) == Tile.obsidian.id) {
         var6 = 1;
      }

      System.out.println(var5 + ", " + var6);
      if (var5 == var6) {
         return false;
      } else {
         if (var1.getTile(var2 - var5, var3, var4 - var6) == 0) {
            var2 -= var5;
            var4 -= var6;
         }

         for(int var7 = -1; var7 <= 2; ++var7) {
            for(int var8 = -1; var8 <= 3; ++var8) {
               boolean var9 = var7 == -1 || var7 == 2 || var8 == -1 || var8 == 3;
               if (var7 != -1 && var7 != 2 || var8 != -1 && var8 != 3) {
                  int var10 = var1.getTile(var2 + var5 * var7, var3 + var8, var4 + var6 * var7);
                  if (var9) {
                     if (var10 != Tile.obsidian.id) {
                        return false;
                     }
                  } else if (var10 != 0 && var10 != Tile.fire.id) {
                     return false;
                  }
               }
            }
         }

         var1.noNeighborUpdate = true;

         for(int var11 = 0; var11 < 2; ++var11) {
            for(int var12 = 0; var12 < 3; ++var12) {
               var1.setTile(var2 + var5 * var11, var3 + var12, var4 + var6 * var11, Tile.portalTile.id);
            }
         }

         var1.noNeighborUpdate = false;
         return true;
      }
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      byte var6 = 0;
      byte var7 = 1;
      if (var1.getTile(var2 - 1, var3, var4) == this.id || var1.getTile(var2 + 1, var3, var4) == this.id) {
         var6 = 1;
         var7 = 0;
      }

      int var8;
      for(var8 = var3; var1.getTile(var2, var8 - 1, var4) == this.id; --var8) {
      }

      if (var1.getTile(var2, var8 - 1, var4) != Tile.obsidian.id) {
         var1.setTile(var2, var3, var4, 0);
      } else {
         int var9;
         for(var9 = 1; var9 < 4 && var1.getTile(var2, var8 + var9, var4) == this.id; ++var9) {
         }

         if (var9 == 3 && var1.getTile(var2, var8 + var9, var4) == Tile.obsidian.id) {
            boolean var10 = var1.getTile(var2 - 1, var3, var4) == this.id || var1.getTile(var2 + 1, var3, var4) == this.id;
            boolean var11 = var1.getTile(var2, var3, var4 - 1) == this.id || var1.getTile(var2, var3, var4 + 1) == this.id;
            if (var10 && var11) {
               var1.setTile(var2, var3, var4, 0);
            } else if ((var1.getTile(var2 + var6, var3, var4 + var7) != Tile.obsidian.id || var1.getTile(var2 - var6, var3, var4 - var7) != this.id) && (var1.getTile(var2 - var6, var3, var4 - var7) != Tile.obsidian.id || var1.getTile(var2 + var6, var3, var4 + var7) != this.id)) {
               var1.setTile(var2, var3, var4, 0);
            }
         } else {
            var1.setTile(var2, var3, var4, 0);
         }
      }
   }

   public boolean shouldRenderFace(LevelSource var1, int var2, int var3, int var4, int var5) {
      return true;
   }

   public int getResourceCount(Random var1) {
      return 0;
   }

   public int getRenderLayer() {
      return 1;
   }

   public void entityInside(Level var1, int var2, int var3, int var4, Entity var5) {
      if (!var1.isOnline) {
         var5.handleInsidePortal();
      }
   }

   public void animateTick(Level var1, int var2, int var3, int var4, Random var5) {
      if (var5.nextInt(100) == 0) {
         var1.playSound((double)var2 + (double)0.5F, (double)var3 + (double)0.5F, (double)var4 + (double)0.5F, "portal.portal", 1.0F, var5.nextFloat() * 0.4F + 0.8F);
      }

      for(int var6 = 0; var6 < 4; ++var6) {
         double var7 = (double)((float)var2 + var5.nextFloat());
         double var9 = (double)((float)var3 + var5.nextFloat());
         double var11 = (double)((float)var4 + var5.nextFloat());
         double var13 = (double)0.0F;
         double var15 = (double)0.0F;
         double var17 = (double)0.0F;
         int var19 = var5.nextInt(2) * 2 - 1;
         var13 = ((double)var5.nextFloat() - (double)0.5F) * (double)0.5F;
         var15 = ((double)var5.nextFloat() - (double)0.5F) * (double)0.5F;
         var17 = ((double)var5.nextFloat() - (double)0.5F) * (double)0.5F;
         if (var1.getTile(var2 - 1, var3, var4) != this.id && var1.getTile(var2 + 1, var3, var4) != this.id) {
            var7 = (double)var2 + (double)0.5F + (double)0.25F * (double)var19;
            var13 = (double)(var5.nextFloat() * 2.0F * (float)var19);
         } else {
            var11 = (double)var4 + (double)0.5F + (double)0.25F * (double)var19;
            var17 = (double)(var5.nextFloat() * 2.0F * (float)var19);
         }

         var1.addParticle("portal", var7, var9, var11, var13, var15, var17);
      }

   }
}
