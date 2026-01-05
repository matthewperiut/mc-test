package net.minecraft.world.level.levelgen;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.FlowerFeature;
import net.minecraft.world.level.levelgen.feature.HellFireFeature;
import net.minecraft.world.level.levelgen.feature.HellPortalFeature;
import net.minecraft.world.level.levelgen.feature.HellSpringFeature;
import net.minecraft.world.level.levelgen.feature.LightGemFeature;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.tile.SandTile;
import net.minecraft.world.level.tile.Tile;
import util.ProgressListener;

public class HellRandomLevelSource implements ChunkSource {
   public static final int CHUNK_HEIGHT = 8;
   public static final int CHUNK_WIDTH = 4;
   private Random random;
   private PerlinNoise lperlinNoise1;
   private PerlinNoise lperlinNoise2;
   private PerlinNoise perlinNoise1;
   private PerlinNoise perlinNoise2;
   private PerlinNoise perlinNoise3;
   public PerlinNoise scaleNoise;
   public PerlinNoise depthNoise;
   private Level level;
   private double[] buffer;
   double[] caveBuffer1;
   double[] caveBuffer2;
   double[] caveBuffer3;
   private double[] sandBuffer = new double[256];
   private double[] gravelBuffer = new double[256];
   private double[] depthBuffer = new double[256];
   private LargeFeature caveFeature = new LargeHellCaveFeature();
   double[] pnr;
   double[] ar;
   double[] br;
   double[] sr;
   double[] dr;
   double[] fi;
   double[] fis;

   public HellRandomLevelSource(Level var1, long var2) {
      this.level = var1;
      this.random = new Random(var2);
      this.lperlinNoise1 = new PerlinNoise(this.random, 16);
      this.lperlinNoise2 = new PerlinNoise(this.random, 16);
      this.perlinNoise1 = new PerlinNoise(this.random, 8);
      this.perlinNoise2 = new PerlinNoise(this.random, 4);
      this.perlinNoise3 = new PerlinNoise(this.random, 4);
      this.scaleNoise = new PerlinNoise(this.random, 10);
      this.depthNoise = new PerlinNoise(this.random, 16);
   }

   public void prepareHeights(int var1, int var2, byte[] var3) {
      byte var4 = 4;
      byte var5 = 32;
      int var6 = var4 + 1;
      byte var7 = 17;
      int var8 = var4 + 1;
      this.buffer = this.getHeights(this.buffer, var1 * var4, 0, var2 * var4, var6, var7, var8);

      for(int var9 = 0; var9 < var4; ++var9) {
         for(int var10 = 0; var10 < var4; ++var10) {
            for(int var11 = 0; var11 < 16; ++var11) {
               double var12 = (double)0.125F;
               double var14 = this.buffer[((var9 + 0) * var8 + var10 + 0) * var7 + var11 + 0];
               double var16 = this.buffer[((var9 + 0) * var8 + var10 + 1) * var7 + var11 + 0];
               double var18 = this.buffer[((var9 + 1) * var8 + var10 + 0) * var7 + var11 + 0];
               double var20 = this.buffer[((var9 + 1) * var8 + var10 + 1) * var7 + var11 + 0];
               double var22 = (this.buffer[((var9 + 0) * var8 + var10 + 0) * var7 + var11 + 1] - var14) * var12;
               double var24 = (this.buffer[((var9 + 0) * var8 + var10 + 1) * var7 + var11 + 1] - var16) * var12;
               double var26 = (this.buffer[((var9 + 1) * var8 + var10 + 0) * var7 + var11 + 1] - var18) * var12;
               double var28 = (this.buffer[((var9 + 1) * var8 + var10 + 1) * var7 + var11 + 1] - var20) * var12;

               for(int var30 = 0; var30 < 8; ++var30) {
                  double var31 = (double)0.25F;
                  double var33 = var14;
                  double var35 = var16;
                  double var37 = (var18 - var14) * var31;
                  double var39 = (var20 - var16) * var31;

                  for(int var41 = 0; var41 < 4; ++var41) {
                     int var42 = var41 + var9 * 4 << 11 | 0 + var10 * 4 << 7 | var11 * 8 + var30;
                     short var43 = 128;
                     double var44 = (double)0.25F;
                     double var46 = var33;
                     double var48 = (var35 - var33) * var44;

                     for(int var50 = 0; var50 < 4; ++var50) {
                        int var51 = 0;
                        if (var11 * 8 + var30 < var5) {
                           var51 = Tile.calmLava.id;
                        }

                        if (var46 > (double)0.0F) {
                           var51 = Tile.hellRock.id;
                        }

                        var3[var42] = (byte)var51;
                        var42 += var43;
                        var46 += var48;
                     }

                     var33 += var37;
                     var35 += var39;
                  }

                  var14 += var22;
                  var16 += var24;
                  var18 += var26;
                  var20 += var28;
               }
            }
         }
      }

   }

   public void buildSurfaces(int var1, int var2, byte[] var3) {
      byte var4 = 64;
      double var5 = (double)0.03125F;
      this.sandBuffer = this.perlinNoise2.getRegion(this.sandBuffer, (double)(var1 * 16), (double)(var2 * 16), (double)0.0F, 16, 16, 1, var5, var5, (double)1.0F);
      this.gravelBuffer = this.perlinNoise2.getRegion(this.gravelBuffer, (double)(var2 * 16), 109.0134, (double)(var1 * 16), 16, 1, 16, var5, (double)1.0F, var5);
      this.depthBuffer = this.perlinNoise3.getRegion(this.depthBuffer, (double)(var1 * 16), (double)(var2 * 16), (double)0.0F, 16, 16, 1, var5 * (double)2.0F, var5 * (double)2.0F, var5 * (double)2.0F);

      for(int var7 = 0; var7 < 16; ++var7) {
         for(int var8 = 0; var8 < 16; ++var8) {
            boolean var9 = this.sandBuffer[var7 + var8 * 16] + this.random.nextDouble() * 0.2 > (double)0.0F;
            boolean var10 = this.gravelBuffer[var7 + var8 * 16] + this.random.nextDouble() * 0.2 > (double)0.0F;
            int var11 = (int)(this.depthBuffer[var7 + var8 * 16] / (double)3.0F + (double)3.0F + this.random.nextDouble() * (double)0.25F);
            int var12 = -1;
            byte var13 = (byte)Tile.hellRock.id;
            byte var14 = (byte)Tile.hellRock.id;

            for(int var15 = 127; var15 >= 0; --var15) {
               int var16 = (var7 * 16 + var8) * 128 + var15;
               if (var15 >= 127 - this.random.nextInt(5)) {
                  var3[var16] = (byte)Tile.unbreakable.id;
               } else if (var15 <= 0 + this.random.nextInt(5)) {
                  var3[var16] = (byte)Tile.unbreakable.id;
               } else {
                  byte var17 = var3[var16];
                  if (var17 == 0) {
                     var12 = -1;
                  } else if (var17 == Tile.hellRock.id) {
                     if (var12 == -1) {
                        if (var11 <= 0) {
                           var13 = 0;
                           var14 = (byte)Tile.hellRock.id;
                        } else if (var15 >= var4 - 4 && var15 <= var4 + 1) {
                           var13 = (byte)Tile.hellRock.id;
                           var14 = (byte)Tile.hellRock.id;
                           if (var10) {
                              var13 = (byte)Tile.gravel.id;
                           }

                           if (var10) {
                              var14 = (byte)Tile.hellRock.id;
                           }

                           if (var9) {
                              var13 = (byte)Tile.hellSand.id;
                           }

                           if (var9) {
                              var14 = (byte)Tile.hellSand.id;
                           }
                        }

                        if (var15 < var4 && var13 == 0) {
                           var13 = (byte)Tile.calmLava.id;
                        }

                        var12 = var11;
                        if (var15 >= var4 - 1) {
                           var3[var16] = var13;
                        } else {
                           var3[var16] = var14;
                        }
                     } else if (var12 > 0) {
                        --var12;
                        var3[var16] = var14;
                     }
                  }
               }
            }
         }
      }

   }

   public LevelChunk getChunk(int var1, int var2) {
      this.random.setSeed((long)var1 * 341873128712L + (long)var2 * 132897987541L);
      byte[] var3 = new byte['\u8000'];
      this.prepareHeights(var1, var2, var3);
      this.buildSurfaces(var1, var2, var3);
      this.caveFeature.apply(this, this.level, var1, var2, var3);
      LevelChunk var4 = new LevelChunk(this.level, var3, var1, var2);
      return var4;
   }

   private double[] getHeights(double[] var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      if (var1 == null) {
         var1 = new double[var5 * var6 * var7];
      }

      double var8 = 684.412;
      double var10 = 2053.236;
      this.sr = this.scaleNoise.getRegion(this.sr, (double)var2, (double)var3, (double)var4, var5, 1, var7, (double)1.0F, (double)0.0F, (double)1.0F);
      this.dr = this.depthNoise.getRegion(this.dr, (double)var2, (double)var3, (double)var4, var5, 1, var7, (double)100.0F, (double)0.0F, (double)100.0F);
      this.pnr = this.perlinNoise1.getRegion(this.pnr, (double)var2, (double)var3, (double)var4, var5, var6, var7, var8 / (double)80.0F, var10 / (double)60.0F, var8 / (double)80.0F);
      this.ar = this.lperlinNoise1.getRegion(this.ar, (double)var2, (double)var3, (double)var4, var5, var6, var7, var8, var10, var8);
      this.br = this.lperlinNoise2.getRegion(this.br, (double)var2, (double)var3, (double)var4, var5, var6, var7, var8, var10, var8);
      int var12 = 0;
      int var13 = 0;
      double[] var14 = new double[var6];

      for(int var15 = 0; var15 < var6; ++var15) {
         var14[var15] = Math.cos((double)var15 * Math.PI * (double)6.0F / (double)var6) * (double)2.0F;
         double var16 = (double)var15;
         if (var15 > var6 / 2) {
            var16 = (double)(var6 - 1 - var15);
         }

         if (var16 < (double)4.0F) {
            var16 = (double)4.0F - var16;
            var14[var15] -= var16 * var16 * var16 * (double)10.0F;
         }
      }

      for(int var36 = 0; var36 < var5; ++var36) {
         for(int var38 = 0; var38 < var7; ++var38) {
            double var17 = (this.sr[var13] + (double)256.0F) / (double)512.0F;
            if (var17 > (double)1.0F) {
               var17 = (double)1.0F;
            }

            double var19 = (double)0.0F;
            double var21 = this.dr[var13] / (double)8000.0F;
            if (var21 < (double)0.0F) {
               var21 = -var21;
            }

            var21 = var21 * (double)3.0F - (double)3.0F;
            if (var21 < (double)0.0F) {
               var21 /= (double)2.0F;
               if (var21 < (double)-1.0F) {
                  var21 = (double)-1.0F;
               }

               var21 /= 1.4;
               var21 /= (double)2.0F;
               var17 = (double)0.0F;
            } else {
               if (var21 > (double)1.0F) {
                  var21 = (double)1.0F;
               }

               var21 /= (double)6.0F;
            }

            var17 += (double)0.5F;
            var21 = var21 * (double)var6 / (double)16.0F;
            ++var13;

            for(int var23 = 0; var23 < var6; ++var23) {
               double var24 = (double)0.0F;
               double var26 = var14[var23];
               double var28 = this.ar[var12] / (double)512.0F;
               double var30 = this.br[var12] / (double)512.0F;
               double var32 = (this.pnr[var12] / (double)10.0F + (double)1.0F) / (double)2.0F;
               if (var32 < (double)0.0F) {
                  var24 = var28;
               } else if (var32 > (double)1.0F) {
                  var24 = var30;
               } else {
                  var24 = var28 + (var30 - var28) * var32;
               }

               var24 -= var26;
               if (var23 > var6 - 4) {
                  double var34 = (double)((float)(var23 - (var6 - 4)) / 3.0F);
                  var24 = var24 * ((double)1.0F - var34) + (double)-10.0F * var34;
               }

               if ((double)var23 < var19) {
                  double var47 = (var19 - (double)var23) / (double)4.0F;
                  if (var47 < (double)0.0F) {
                     var47 = (double)0.0F;
                  }

                  if (var47 > (double)1.0F) {
                     var47 = (double)1.0F;
                  }

                  var24 = var24 * ((double)1.0F - var47) + (double)-10.0F * var47;
               }

               var1[var12] = var24;
               ++var12;
            }
         }
      }

      return var1;
   }

   public boolean hasChunk(int var1, int var2) {
      return true;
   }

   public void postProcess(ChunkSource var1, int var2, int var3) {
      SandTile.instaFall = true;
      int var4 = var2 * 16;
      int var5 = var3 * 16;

      for(int var6 = 0; var6 < 8; ++var6) {
         int var7 = var4 + this.random.nextInt(16) + 8;
         int var8 = this.random.nextInt(120) + 4;
         int var9 = var5 + this.random.nextInt(16) + 8;
         (new HellSpringFeature(Tile.lava.id)).place(this.level, this.random, var7, var8, var9);
      }

      int var11 = this.random.nextInt(this.random.nextInt(10) + 1) + 1;

      for(int var13 = 0; var13 < var11; ++var13) {
         int var18 = var4 + this.random.nextInt(16) + 8;
         int var23 = this.random.nextInt(120) + 4;
         int var10 = var5 + this.random.nextInt(16) + 8;
         (new HellFireFeature()).place(this.level, this.random, var18, var23, var10);
      }

      var11 = this.random.nextInt(this.random.nextInt(10) + 1);

      for(int var14 = 0; var14 < var11; ++var14) {
         int var19 = var4 + this.random.nextInt(16) + 8;
         int var24 = this.random.nextInt(120) + 4;
         int var28 = var5 + this.random.nextInt(16) + 8;
         (new LightGemFeature()).place(this.level, this.random, var19, var24, var28);
      }

      for(int var15 = 0; var15 < 10; ++var15) {
         int var20 = var4 + this.random.nextInt(16) + 8;
         int var25 = this.random.nextInt(128);
         int var29 = var5 + this.random.nextInt(16) + 8;
         (new HellPortalFeature()).place(this.level, this.random, var20, var25, var29);
      }

      if (this.random.nextInt(1) == 0) {
         int var16 = var4 + this.random.nextInt(16) + 8;
         int var21 = this.random.nextInt(128);
         int var26 = var5 + this.random.nextInt(16) + 8;
         (new FlowerFeature(Tile.mushroom1.id)).place(this.level, this.random, var16, var21, var26);
      }

      if (this.random.nextInt(1) == 0) {
         int var17 = var4 + this.random.nextInt(16) + 8;
         int var22 = this.random.nextInt(128);
         int var27 = var5 + this.random.nextInt(16) + 8;
         (new FlowerFeature(Tile.mushroom2.id)).place(this.level, this.random, var17, var22, var27);
      }

      SandTile.instaFall = false;
   }

   public boolean save(boolean var1, ProgressListener var2) {
      return true;
   }

   public boolean tick() {
      return false;
   }

   public boolean shouldSave() {
      return true;
   }

   public String gatherStats() {
      return "HellRandomLevelSource";
   }
}
