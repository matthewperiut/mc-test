package net.minecraft.world.level;

import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.tile.Tile;

public class LightUpdate {
   public final LightLayer layer;
   public int x0;
   public int y0;
   public int z0;
   public int x1;
   public int y1;
   public int z1;

   public LightUpdate(LightLayer var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      this.layer = var1;
      this.x0 = var2;
      this.y0 = var3;
      this.z0 = var4;
      this.x1 = var5;
      this.y1 = var6;
      this.z1 = var7;
   }

   public void update(Level var1) {
      int var2 = this.x1 - this.x0 + 1;
      int var3 = this.y1 - this.y0 + 1;
      int var4 = this.z1 - this.z0 + 1;
      int var5 = var2 * var3 * var4;
      if (var5 > 32768) {
         System.out.println("Light too large, skipping!");
      } else {
         int var6 = 0;
         int var7 = 0;
         boolean var8 = false;
         boolean var9 = false;

         for(int var10 = this.x0; var10 <= this.x1; ++var10) {
            for(int var11 = this.z0; var11 <= this.z1; ++var11) {
               int var12 = var10 >> 4;
               int var13 = var11 >> 4;
               boolean var14 = false;
               if (var8 && var12 == var6 && var13 == var7) {
                  var14 = var9;
               } else {
                  var14 = var1.hasChunksAt(var10, 0, var11, 1);
                  if (var14) {
                     LevelChunk var15 = var1.getChunk(var10 >> 4, var11 >> 4);
                     if (var15.isEmpty()) {
                        var14 = false;
                     }
                  }

                  var9 = var14;
                  var6 = var12;
                  var7 = var13;
               }

               if (var14) {
                  if (this.y0 < 0) {
                     this.y0 = 0;
                  }

                  if (this.y1 >= 128) {
                     this.y1 = 127;
                  }

                  for(int var28 = this.y0; var28 <= this.y1; ++var28) {
                     int var16 = var1.getBrightness(this.layer, var10, var28, var11);
                     int var17 = 0;
                     int var18 = var1.getTile(var10, var28, var11);
                     int var19 = Tile.lightBlock[var18];
                     if (var19 == 0) {
                        var19 = 1;
                     }

                     int var20 = 0;
                     if (this.layer == LightLayer.Sky) {
                        if (var1.isSkyLit(var10, var28, var11)) {
                           var20 = 15;
                        }
                     } else if (this.layer == LightLayer.Block) {
                        var20 = Tile.lightEmission[var18];
                     }

                     if (var19 >= 15 && var20 == 0) {
                        var17 = 0;
                     } else {
                        int var21 = var1.getBrightness(this.layer, var10 - 1, var28, var11);
                        int var22 = var1.getBrightness(this.layer, var10 + 1, var28, var11);
                        int var23 = var1.getBrightness(this.layer, var10, var28 - 1, var11);
                        int var24 = var1.getBrightness(this.layer, var10, var28 + 1, var11);
                        int var25 = var1.getBrightness(this.layer, var10, var28, var11 - 1);
                        int var26 = var1.getBrightness(this.layer, var10, var28, var11 + 1);
                        var17 = var21;
                        if (var22 > var21) {
                           var17 = var22;
                        }

                        if (var23 > var17) {
                           var17 = var23;
                        }

                        if (var24 > var17) {
                           var17 = var24;
                        }

                        if (var25 > var17) {
                           var17 = var25;
                        }

                        if (var26 > var17) {
                           var17 = var26;
                        }

                        var17 -= var19;
                        if (var17 < 0) {
                           var17 = 0;
                        }

                        if (var20 > var17) {
                           var17 = var20;
                        }
                     }

                     if (var16 != var17) {
                        var1.setBrightness(this.layer, var10, var28, var11, var17);
                        int var31 = var17 - 1;
                        if (var31 < 0) {
                           var31 = 0;
                        }

                        var1.updateLightIfOtherThan(this.layer, var10 - 1, var28, var11, var31);
                        var1.updateLightIfOtherThan(this.layer, var10, var28 - 1, var11, var31);
                        var1.updateLightIfOtherThan(this.layer, var10, var28, var11 - 1, var31);
                        if (var10 + 1 >= this.x1) {
                           var1.updateLightIfOtherThan(this.layer, var10 + 1, var28, var11, var31);
                        }

                        if (var28 + 1 >= this.y1) {
                           var1.updateLightIfOtherThan(this.layer, var10, var28 + 1, var11, var31);
                        }

                        if (var11 + 1 >= this.z1) {
                           var1.updateLightIfOtherThan(this.layer, var10, var28, var11 + 1, var31);
                        }
                     }
                  }
               }
            }
         }

      }
   }

   public boolean expandToContain(int var1, int var2, int var3, int var4, int var5, int var6) {
      if (var1 >= this.x0 && var2 >= this.y0 && var3 >= this.z0 && var4 <= this.x1 && var5 <= this.y1 && var6 <= this.z1) {
         return true;
      } else {
         byte var7 = 1;
         if (var1 >= this.x0 - var7 && var2 >= this.y0 - var7 && var3 >= this.z0 - var7 && var4 <= this.x1 + var7 && var5 <= this.y1 + var7 && var6 <= this.z1 + var7) {
            int var8 = this.x1 - this.x0;
            int var9 = this.y1 - this.y0;
            int var10 = this.z1 - this.z0;
            if (var1 > this.x0) {
               var1 = this.x0;
            }

            if (var2 > this.y0) {
               var2 = this.y0;
            }

            if (var3 > this.z0) {
               var3 = this.z0;
            }

            if (var4 < this.x1) {
               var4 = this.x1;
            }

            if (var5 < this.y1) {
               var5 = this.y1;
            }

            if (var6 < this.z1) {
               var6 = this.z1;
            }

            int var11 = var4 - var1;
            int var12 = var5 - var2;
            int var13 = var6 - var3;
            int var14 = var8 * var9 * var10;
            int var15 = var11 * var12 * var13;
            if (var15 - var14 <= 2) {
               this.x0 = var1;
               this.y0 = var2;
               this.z0 = var3;
               this.x1 = var4;
               this.y1 = var5;
               this.z1 = var6;
               return true;
            }
         }

         return false;
      }
   }
}
