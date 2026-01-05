package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;

public class LeafTile extends TransparentTile {
   public static final int REQUIRED_WOOD_RANGE = 4;
   public static final int UPDATE_LEAF_BIT = 4;
   public static final int NORMAL_LEAF = 0;
   public static final int EVERGREEN_LEAF = 1;
   public static final int BIRCH_LEAF = 2;
   private static final int LEAF_TYPE_MASK = 3;
   private int oTex;
   int[] checkBuffer;

   protected LeafTile(int var1, int var2) {
      super(var1, var2, Material.leaves, false);
      this.oTex = var2;
      this.setTicking(true);
   }

   public int getColor(LevelSource var1, int var2, int var3, int var4) {
      int var5 = var1.getData(var2, var3, var4);
      if ((var5 & 1) == 1) {
         return FoliageColor.getEvergreenColor();
      } else if ((var5 & 2) == 2) {
         return FoliageColor.getBirchColor();
      } else {
         var1.getBiomeSource().getBiomeBlock(var2, var4, 1, 1);
         double var6 = var1.getBiomeSource().temperatures[0];
         double var8 = var1.getBiomeSource().downfalls[0];
         return FoliageColor.get(var6, var8);
      }
   }

   public void onRemove(Level var1, int var2, int var3, int var4) {
      byte var5 = 1;
      int var6 = var5 + 1;
      if (var1.hasChunksAt(var2 - var6, var3 - var6, var4 - var6, var2 + var6, var3 + var6, var4 + var6)) {
         for(int var7 = -var5; var7 <= var5; ++var7) {
            for(int var8 = -var5; var8 <= var5; ++var8) {
               for(int var9 = -var5; var9 <= var5; ++var9) {
                  int var10 = var1.getTile(var2 + var7, var3 + var8, var4 + var9);
                  if (var10 == Tile.leaves.id) {
                     int var11 = var1.getData(var2 + var7, var3 + var8, var4 + var9);
                     var1.setDataNoUpdate(var2 + var7, var3 + var8, var4 + var9, var11 | 4);
                  }
               }
            }
         }
      }

   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      if (!var1.isOnline) {
         int var6 = var1.getData(var2, var3, var4);
         if ((var6 & 4) != 0) {
            byte var7 = 4;
            int var8 = var7 + 1;
            byte var9 = 32;
            int var10 = var9 * var9;
            int var11 = var9 / 2;
            if (this.checkBuffer == null) {
               this.checkBuffer = new int[var9 * var9 * var9];
            }

            if (var1.hasChunksAt(var2 - var8, var3 - var8, var4 - var8, var2 + var8, var3 + var8, var4 + var8)) {
               for(int var12 = -var7; var12 <= var7; ++var12) {
                  for(int var13 = -var7; var13 <= var7; ++var13) {
                     for(int var14 = -var7; var14 <= var7; ++var14) {
                        int var15 = var1.getTile(var2 + var12, var3 + var13, var4 + var14);
                        if (var15 == Tile.treeTrunk.id) {
                           this.checkBuffer[(var12 + var11) * var10 + (var13 + var11) * var9 + var14 + var11] = 0;
                        } else if (var15 == Tile.leaves.id) {
                           this.checkBuffer[(var12 + var11) * var10 + (var13 + var11) * var9 + var14 + var11] = -2;
                        } else {
                           this.checkBuffer[(var12 + var11) * var10 + (var13 + var11) * var9 + var14 + var11] = -1;
                        }
                     }
                  }
               }

               for(int var16 = 1; var16 <= 4; ++var16) {
                  for(int var18 = -var7; var18 <= var7; ++var18) {
                     for(int var19 = -var7; var19 <= var7; ++var19) {
                        for(int var20 = -var7; var20 <= var7; ++var20) {
                           if (this.checkBuffer[(var18 + var11) * var10 + (var19 + var11) * var9 + var20 + var11] == var16 - 1) {
                              if (this.checkBuffer[(var18 + var11 - 1) * var10 + (var19 + var11) * var9 + var20 + var11] == -2) {
                                 this.checkBuffer[(var18 + var11 - 1) * var10 + (var19 + var11) * var9 + var20 + var11] = var16;
                              }

                              if (this.checkBuffer[(var18 + var11 + 1) * var10 + (var19 + var11) * var9 + var20 + var11] == -2) {
                                 this.checkBuffer[(var18 + var11 + 1) * var10 + (var19 + var11) * var9 + var20 + var11] = var16;
                              }

                              if (this.checkBuffer[(var18 + var11) * var10 + (var19 + var11 - 1) * var9 + var20 + var11] == -2) {
                                 this.checkBuffer[(var18 + var11) * var10 + (var19 + var11 - 1) * var9 + var20 + var11] = var16;
                              }

                              if (this.checkBuffer[(var18 + var11) * var10 + (var19 + var11 + 1) * var9 + var20 + var11] == -2) {
                                 this.checkBuffer[(var18 + var11) * var10 + (var19 + var11 + 1) * var9 + var20 + var11] = var16;
                              }

                              if (this.checkBuffer[(var18 + var11) * var10 + (var19 + var11) * var9 + (var20 + var11 - 1)] == -2) {
                                 this.checkBuffer[(var18 + var11) * var10 + (var19 + var11) * var9 + (var20 + var11 - 1)] = var16;
                              }

                              if (this.checkBuffer[(var18 + var11) * var10 + (var19 + var11) * var9 + var20 + var11 + 1] == -2) {
                                 this.checkBuffer[(var18 + var11) * var10 + (var19 + var11) * var9 + var20 + var11 + 1] = var16;
                              }
                           }
                        }
                     }
                  }
               }
            }

            int var17 = this.checkBuffer[var11 * var10 + var11 * var9 + var11];
            if (var17 >= 0) {
               var1.setData(var2, var3, var4, var6 & -5);
            } else {
               this.die(var1, var2, var3, var4);
            }
         }

      }
   }

   private void die(Level var1, int var2, int var3, int var4) {
      this.spawnResources(var1, var2, var3, var4, var1.getData(var2, var3, var4));
      var1.setTile(var2, var3, var4, 0);
   }

   public int getResourceCount(Random var1) {
      return var1.nextInt(16) == 0 ? 1 : 0;
   }

   public int getResource(int var1, Random var2) {
      return Tile.sapling.id;
   }

   public boolean isSolidRender() {
      return !this.allowSame;
   }

   public int getTexture(int var1, int var2) {
      return (var2 & 3) == 1 ? this.tex + 80 : this.tex;
   }

   public void setFancy(boolean var1) {
      this.allowSame = var1;
      this.tex = this.oTex + (var1 ? 0 : 1);
   }

   public void stepOn(Level var1, int var2, int var3, int var4, Entity var5) {
      super.stepOn(var1, var2, var3, var4, var5);
   }
}
