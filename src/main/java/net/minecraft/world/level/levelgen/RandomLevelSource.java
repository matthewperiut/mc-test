package net.minecraft.world.level.levelgen;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.CactusFeature;
import net.minecraft.world.level.levelgen.feature.ClayFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FlowerFeature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.PumpkinFeature;
import net.minecraft.world.level.levelgen.feature.ReedsFeature;
import net.minecraft.world.level.levelgen.feature.SpringFeature;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.SandTile;
import net.minecraft.world.level.tile.Tile;
import util.ProgressListener;

public class RandomLevelSource implements ChunkSource {
   private static final double SNOW_CUTOFF = (double)0.5F;
   private static final double SNOW_SCALE = 0.3;
   private static final boolean FLOATING_ISLANDS = false;
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
   private PerlinNoise floatingIslandScale;
   private PerlinNoise floatingIslandNoise;
   public PerlinNoise forestNoise;
   private Level level;
   private double[] buffer;
   private double[] sandBuffer = new double[256];
   private double[] gravelBuffer = new double[256];
   private double[] depthBuffer = new double[256];
   private LargeFeature caveFeature = new LargeCaveFeature();
   private Biome[] biomes;
   double[] pnr;
   double[] ar;
   double[] br;
   double[] sr;
   double[] dr;
   double[] fi;
   double[] fis;
   int[][] waterDepths = new int[32][32];
   private double[] temperatures;

   public RandomLevelSource(Level var1, long var2) {
      this.level = var1;
      this.random = new Random(var2);
      this.lperlinNoise1 = new PerlinNoise(this.random, 16);
      this.lperlinNoise2 = new PerlinNoise(this.random, 16);
      this.perlinNoise1 = new PerlinNoise(this.random, 8);
      this.perlinNoise2 = new PerlinNoise(this.random, 4);
      this.perlinNoise3 = new PerlinNoise(this.random, 4);
      this.scaleNoise = new PerlinNoise(this.random, 10);
      this.depthNoise = new PerlinNoise(this.random, 16);
      this.forestNoise = new PerlinNoise(this.random, 8);
   }

   public void prepareHeights(int var1, int var2, byte[] var3, Biome[] var4, double[] var5) {
      byte var6 = 4;
      byte var7 = 64;
      int var8 = var6 + 1;
      byte var9 = 17;
      int var10 = var6 + 1;
      this.buffer = this.getHeights(this.buffer, var1 * var6, 0, var2 * var6, var8, var9, var10);

      for(int var11 = 0; var11 < var6; ++var11) {
         for(int var12 = 0; var12 < var6; ++var12) {
            for(int var13 = 0; var13 < 16; ++var13) {
               double var14 = (double)0.125F;
               double var16 = this.buffer[((var11 + 0) * var10 + var12 + 0) * var9 + var13 + 0];
               double var18 = this.buffer[((var11 + 0) * var10 + var12 + 1) * var9 + var13 + 0];
               double var20 = this.buffer[((var11 + 1) * var10 + var12 + 0) * var9 + var13 + 0];
               double var22 = this.buffer[((var11 + 1) * var10 + var12 + 1) * var9 + var13 + 0];
               double var24 = (this.buffer[((var11 + 0) * var10 + var12 + 0) * var9 + var13 + 1] - var16) * var14;
               double var26 = (this.buffer[((var11 + 0) * var10 + var12 + 1) * var9 + var13 + 1] - var18) * var14;
               double var28 = (this.buffer[((var11 + 1) * var10 + var12 + 0) * var9 + var13 + 1] - var20) * var14;
               double var30 = (this.buffer[((var11 + 1) * var10 + var12 + 1) * var9 + var13 + 1] - var22) * var14;

               for(int var32 = 0; var32 < 8; ++var32) {
                  double var33 = (double)0.25F;
                  double var35 = var16;
                  double var37 = var18;
                  double var39 = (var20 - var16) * var33;
                  double var41 = (var22 - var18) * var33;

                  for(int var43 = 0; var43 < 4; ++var43) {
                     int var44 = var43 + var11 * 4 << 11 | 0 + var12 * 4 << 7 | var13 * 8 + var32;
                     short var45 = 128;
                     double var46 = (double)0.25F;
                     double var48 = var35;
                     double var50 = (var37 - var35) * var46;

                     for(int var52 = 0; var52 < 4; ++var52) {
                        double var53 = var5[(var11 * 4 + var43) * 16 + var12 * 4 + var52];
                        int var55 = 0;
                        if (var13 * 8 + var32 < var7) {
                           if (var53 < (double)0.5F && var13 * 8 + var32 >= var7 - 1) {
                              var55 = Tile.ice.id;
                           } else {
                              var55 = Tile.calmWater.id;
                           }
                        }

                        if (var48 > (double)0.0F) {
                           var55 = Tile.rock.id;
                        }

                        var3[var44] = (byte)var55;
                        var44 += var45;
                        var48 += var50;
                     }

                     var35 += var39;
                     var37 += var41;
                  }

                  var16 += var24;
                  var18 += var26;
                  var20 += var28;
                  var22 += var30;
               }
            }
         }
      }

   }

   public void buildSurfaces(int var1, int var2, byte[] var3, Biome[] var4) {
      byte var5 = 64;
      double var6 = (double)0.03125F;
      this.sandBuffer = this.perlinNoise2.getRegion(this.sandBuffer, (double)(var1 * 16), (double)(var2 * 16), (double)0.0F, 16, 16, 1, var6, var6, (double)1.0F);
      this.gravelBuffer = this.perlinNoise2.getRegion(this.gravelBuffer, (double)(var2 * 16), 109.0134, (double)(var1 * 16), 16, 1, 16, var6, (double)1.0F, var6);
      this.depthBuffer = this.perlinNoise3.getRegion(this.depthBuffer, (double)(var1 * 16), (double)(var2 * 16), (double)0.0F, 16, 16, 1, var6 * (double)2.0F, var6 * (double)2.0F, var6 * (double)2.0F);

      for(int var8 = 0; var8 < 16; ++var8) {
         for(int var9 = 0; var9 < 16; ++var9) {
            Biome var10 = var4[var8 + var9 * 16];
            boolean var11 = this.sandBuffer[var8 + var9 * 16] + this.random.nextDouble() * 0.2 > (double)0.0F;
            boolean var12 = this.gravelBuffer[var8 + var9 * 16] + this.random.nextDouble() * 0.2 > (double)3.0F;
            int var13 = (int)(this.depthBuffer[var8 + var9 * 16] / (double)3.0F + (double)3.0F + this.random.nextDouble() * (double)0.25F);
            int var14 = -1;
            byte var15 = var10.topMaterial;
            byte var16 = var10.material;

            for(int var17 = 127; var17 >= 0; --var17) {
               int var18 = (var8 * 16 + var9) * 128 + var17;
               if (var17 <= 0 + this.random.nextInt(5)) {
                  var3[var18] = (byte)Tile.unbreakable.id;
               } else {
                  byte var19 = var3[var18];
                  if (var19 == 0) {
                     var14 = -1;
                  } else if (var19 == Tile.rock.id) {
                     if (var14 == -1) {
                        if (var13 <= 0) {
                           var15 = 0;
                           var16 = (byte)Tile.rock.id;
                        } else if (var17 >= var5 - 4 && var17 <= var5 + 1) {
                           var15 = var10.topMaterial;
                           var16 = var10.material;
                           if (var12) {
                              var15 = 0;
                           }

                           if (var12) {
                              var16 = (byte)Tile.gravel.id;
                           }

                           if (var11) {
                              var15 = (byte)Tile.sand.id;
                           }

                           if (var11) {
                              var16 = (byte)Tile.sand.id;
                           }
                        }

                        if (var17 < var5 && var15 == 0) {
                           var15 = (byte)Tile.calmWater.id;
                        }

                        var14 = var13;
                        if (var17 >= var5 - 1) {
                           var3[var18] = var15;
                        } else {
                           var3[var18] = var16;
                        }
                     } else if (var14 > 0) {
                        --var14;
                        var3[var18] = var16;
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
      LevelChunk var4 = new LevelChunk(this.level, var3, var1, var2);
      this.biomes = this.level.getBiomeSource().getBiomeBlock(this.biomes, var1 * 16, var2 * 16, 16, 16);
      double[] var5 = this.level.getBiomeSource().temperatures;
      this.prepareHeights(var1, var2, var3, this.biomes, var5);
      this.buildSurfaces(var1, var2, var3, this.biomes);
      this.caveFeature.apply(this, this.level, var1, var2, var3);
      var4.recalcHeightmap();
      return var4;
   }

   private double[] getHeights(double[] var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      if (var1 == null) {
         var1 = new double[var5 * var6 * var7];
      }

      double var8 = 684.412;
      double var10 = 684.412;
      double[] var12 = this.level.getBiomeSource().temperatures;
      double[] var13 = this.level.getBiomeSource().downfalls;
      this.sr = this.scaleNoise.getRegion(this.sr, var2, var4, var5, var7, 1.121, 1.121, (double)0.5F);
      this.dr = this.depthNoise.getRegion(this.dr, var2, var4, var5, var7, (double)200.0F, (double)200.0F, (double)0.5F);
      this.pnr = this.perlinNoise1.getRegion(this.pnr, (double)var2, (double)var3, (double)var4, var5, var6, var7, var8 / (double)80.0F, var10 / (double)160.0F, var8 / (double)80.0F);
      this.ar = this.lperlinNoise1.getRegion(this.ar, (double)var2, (double)var3, (double)var4, var5, var6, var7, var8, var10, var8);
      this.br = this.lperlinNoise2.getRegion(this.br, (double)var2, (double)var3, (double)var4, var5, var6, var7, var8, var10, var8);
      int var14 = 0;
      int var15 = 0;
      int var16 = 16 / var5;

      for(int var17 = 0; var17 < var5; ++var17) {
         int var18 = var17 * var16 + var16 / 2;

         for(int var19 = 0; var19 < var7; ++var19) {
            int var20 = var19 * var16 + var16 / 2;
            double var21 = var12[var18 * 16 + var20];
            double var23 = var13[var18 * 16 + var20] * var21;
            double var25 = (double)1.0F - var23;
            var25 *= var25;
            var25 *= var25;
            var25 = (double)1.0F - var25;
            double var27 = (this.sr[var15] + (double)256.0F) / (double)512.0F;
            var27 *= var25;
            if (var27 > (double)1.0F) {
               var27 = (double)1.0F;
            }

            double var29 = this.dr[var15] / (double)8000.0F;
            if (var29 < (double)0.0F) {
               var29 = -var29 * 0.3;
            }

            var29 = var29 * (double)3.0F - (double)2.0F;
            if (var29 < (double)0.0F) {
               var29 /= (double)2.0F;
               if (var29 < (double)-1.0F) {
                  var29 = (double)-1.0F;
               }

               var29 /= 1.4;
               var29 /= (double)2.0F;
               var27 = (double)0.0F;
            } else {
               if (var29 > (double)1.0F) {
                  var29 = (double)1.0F;
               }

               var29 /= (double)8.0F;
            }

            if (var27 < (double)0.0F) {
               var27 = (double)0.0F;
            }

            var27 += (double)0.5F;
            var29 = var29 * (double)var6 / (double)16.0F;
            double var31 = (double)var6 / (double)2.0F + var29 * (double)4.0F;
            ++var15;

            for(int var33 = 0; var33 < var6; ++var33) {
               double var34 = (double)0.0F;
               double var36 = ((double)var33 - var31) * (double)12.0F / var27;
               if (var36 < (double)0.0F) {
                  var36 *= (double)4.0F;
               }

               double var38 = this.ar[var14] / (double)512.0F;
               double var40 = this.br[var14] / (double)512.0F;
               double var42 = (this.pnr[var14] / (double)10.0F + (double)1.0F) / (double)2.0F;
               if (var42 < (double)0.0F) {
                  var34 = var38;
               } else if (var42 > (double)1.0F) {
                  var34 = var40;
               } else {
                  var34 = var38 + (var40 - var38) * var42;
               }

               var34 -= var36;
               if (var33 > var6 - 4) {
                  double var44 = (double)((float)(var33 - (var6 - 4)) / 3.0F);
                  var34 = var34 * ((double)1.0F - var44) + (double)-10.0F * var44;
               }

               var1[var14] = var34;
               ++var14;
            }
         }
      }

      return var1;
   }

   public boolean hasChunk(int var1, int var2) {
      return true;
   }

   private void calcWaterDepths(ChunkSource var1, int var2, int var3) {
      int var4 = var2 * 16;
      int var5 = var3 * 16;

      for(int var6 = 0; var6 < 16; ++var6) {
         int var7 = this.level.getSeaLevel();

         for(int var8 = 0; var8 < 16; ++var8) {
            int var9 = var4 + var6 + 7;
            int var10 = var5 + var8 + 7;
            int var11 = this.level.getHeightmap(var9, var10);
            if (var11 <= 0 && (this.level.getHeightmap(var9 - 1, var10) > 0 || this.level.getHeightmap(var9 + 1, var10) > 0 || this.level.getHeightmap(var9, var10 - 1) > 0 || this.level.getHeightmap(var9, var10 + 1) > 0)) {
               boolean var12 = false;
               if (var12 || this.level.getTile(var9 - 1, var7, var10) == Tile.calmWater.id && this.level.getData(var9 - 1, var7, var10) < 7) {
                  var12 = true;
               }

               if (var12 || this.level.getTile(var9 + 1, var7, var10) == Tile.calmWater.id && this.level.getData(var9 + 1, var7, var10) < 7) {
                  var12 = true;
               }

               if (var12 || this.level.getTile(var9, var7, var10 - 1) == Tile.calmWater.id && this.level.getData(var9, var7, var10 - 1) < 7) {
                  var12 = true;
               }

               if (var12 || this.level.getTile(var9, var7, var10 + 1) == Tile.calmWater.id && this.level.getData(var9, var7, var10 + 1) < 7) {
                  var12 = true;
               }

               if (var12) {
                  for(int var13 = -5; var13 <= 5; ++var13) {
                     for(int var14 = -5; var14 <= 5; ++var14) {
                        int var15 = (var13 > 0 ? var13 : -var13) + (var14 > 0 ? var14 : -var14);
                        if (var15 <= 5) {
                           var15 = 6 - var15;
                           if (this.level.getTile(var9 + var13, var7, var10 + var14) == Tile.calmWater.id) {
                              int var16 = this.level.getData(var9 + var13, var7, var10 + var14);
                              if (var16 < 7 && var16 < var15) {
                                 this.level.setData(var9 + var13, var7, var10 + var14, var15);
                              }
                           }
                        }
                     }
                  }

                  if (var12) {
                     this.level.setTileAndDataNoUpdate(var9, var7, var10, Tile.calmWater.id, 7);

                     for(int var17 = 0; var17 < var7; ++var17) {
                        this.level.setTileAndDataNoUpdate(var9, var17, var10, Tile.calmWater.id, 8);
                     }
                  }
               }
            }
         }
      }

   }

   public void postProcess(ChunkSource var1, int var2, int var3) {
      SandTile.instaFall = true;
      int var4 = var2 * 16;
      int var5 = var3 * 16;
      Biome var6 = this.level.getBiomeSource().getBiome(var4 + 16, var5 + 16);
      this.random.setSeed(this.level.seed);
      long var7 = this.random.nextLong() / 2L * 2L + 1L;
      long var9 = this.random.nextLong() / 2L * 2L + 1L;
      this.random.setSeed((long)var2 * var7 + (long)var3 * var9 ^ this.level.seed);
      double var11 = (double)0.25F;
      if (this.random.nextInt(4) == 0) {
         int var13 = var4 + this.random.nextInt(16) + 8;
         int var14 = this.random.nextInt(128);
         int var15 = var5 + this.random.nextInt(16) + 8;
         (new LakeFeature(Tile.calmWater.id)).place(this.level, this.random, var13, var14, var15);
      }

      if (this.random.nextInt(8) == 0) {
         int var24 = var4 + this.random.nextInt(16) + 8;
         int var36 = this.random.nextInt(this.random.nextInt(120) + 8);
         int var48 = var5 + this.random.nextInt(16) + 8;
         if (var36 < 64 || this.random.nextInt(10) == 0) {
            (new LakeFeature(Tile.calmLava.id)).place(this.level, this.random, var24, var36, var48);
         }
      }

      for(int var25 = 0; var25 < 8; ++var25) {
         int var37 = var4 + this.random.nextInt(16) + 8;
         int var49 = this.random.nextInt(128);
         int var16 = var5 + this.random.nextInt(16) + 8;
         (new MonsterRoomFeature()).place(this.level, this.random, var37, var49, var16);
      }

      for(int var26 = 0; var26 < 10; ++var26) {
         int var38 = var4 + this.random.nextInt(16);
         int var50 = this.random.nextInt(128);
         int var67 = var5 + this.random.nextInt(16);
         (new ClayFeature(32)).place(this.level, this.random, var38, var50, var67);
      }

      for(int var27 = 0; var27 < 20; ++var27) {
         int var39 = var4 + this.random.nextInt(16);
         int var51 = this.random.nextInt(128);
         int var68 = var5 + this.random.nextInt(16);
         (new OreFeature(Tile.dirt.id, 32)).place(this.level, this.random, var39, var51, var68);
      }

      for(int var28 = 0; var28 < 10; ++var28) {
         int var40 = var4 + this.random.nextInt(16);
         int var52 = this.random.nextInt(128);
         int var69 = var5 + this.random.nextInt(16);
         (new OreFeature(Tile.gravel.id, 32)).place(this.level, this.random, var40, var52, var69);
      }

      for(int var29 = 0; var29 < 20; ++var29) {
         int var41 = var4 + this.random.nextInt(16);
         int var53 = this.random.nextInt(128);
         int var70 = var5 + this.random.nextInt(16);
         (new OreFeature(Tile.coalOre.id, 16)).place(this.level, this.random, var41, var53, var70);
      }

      for(int var30 = 0; var30 < 20; ++var30) {
         int var42 = var4 + this.random.nextInt(16);
         int var54 = this.random.nextInt(64);
         int var71 = var5 + this.random.nextInt(16);
         (new OreFeature(Tile.ironOre.id, 8)).place(this.level, this.random, var42, var54, var71);
      }

      for(int var31 = 0; var31 < 2; ++var31) {
         int var43 = var4 + this.random.nextInt(16);
         int var55 = this.random.nextInt(32);
         int var72 = var5 + this.random.nextInt(16);
         (new OreFeature(Tile.goldOre.id, 8)).place(this.level, this.random, var43, var55, var72);
      }

      for(int var32 = 0; var32 < 8; ++var32) {
         int var44 = var4 + this.random.nextInt(16);
         int var56 = this.random.nextInt(16);
         int var73 = var5 + this.random.nextInt(16);
         (new OreFeature(Tile.redStoneOre.id, 7)).place(this.level, this.random, var44, var56, var73);
      }

      for(int var33 = 0; var33 < 1; ++var33) {
         int var45 = var4 + this.random.nextInt(16);
         int var57 = this.random.nextInt(16);
         int var74 = var5 + this.random.nextInt(16);
         (new OreFeature(Tile.emeraldOre.id, 7)).place(this.level, this.random, var45, var57, var74);
      }

      for(int var34 = 0; var34 < 1; ++var34) {
         int var46 = var4 + this.random.nextInt(16);
         int var58 = this.random.nextInt(16) + this.random.nextInt(16);
         int var75 = var5 + this.random.nextInt(16);
         (new OreFeature(Tile.lapisOre.id, 6)).place(this.level, this.random, var46, var58, var75);
      }

      var11 = (double)0.5F;
      int var35 = (int)((this.forestNoise.getValue((double)var4 * var11, (double)var5 * var11) / (double)8.0F + this.random.nextDouble() * (double)4.0F + (double)4.0F) / (double)3.0F);
      int var47 = 0;
      if (this.random.nextInt(10) == 0) {
         ++var47;
      }

      if (var6 == Biome.forest) {
         var47 += var35 + 5;
      }

      if (var6 == Biome.rainForest) {
         var47 += var35 + 5;
      }

      if (var6 == Biome.seasonalForest) {
         var47 += var35 + 2;
      }

      if (var6 == Biome.taiga) {
         var47 += var35 + 5;
      }

      if (var6 == Biome.desert) {
         var47 -= 20;
      }

      if (var6 == Biome.tundra) {
         var47 -= 20;
      }

      if (var6 == Biome.plains) {
         var47 -= 20;
      }

      for(int var59 = 0; var59 < var47; ++var59) {
         int var76 = var4 + this.random.nextInt(16) + 8;
         int var17 = var5 + this.random.nextInt(16) + 8;
         Feature var18 = var6.getTreeFeature(this.random);
         var18.init((double)1.0F, (double)1.0F, (double)1.0F);
         var18.place(this.level, this.random, var76, this.level.getHeightmap(var76, var17), var17);
      }

      for(int var60 = 0; var60 < 2; ++var60) {
         int var77 = var4 + this.random.nextInt(16) + 8;
         int var87 = this.random.nextInt(128);
         int var97 = var5 + this.random.nextInt(16) + 8;
         (new FlowerFeature(Tile.flower.id)).place(this.level, this.random, var77, var87, var97);
      }

      if (this.random.nextInt(2) == 0) {
         int var61 = var4 + this.random.nextInt(16) + 8;
         int var78 = this.random.nextInt(128);
         int var88 = var5 + this.random.nextInt(16) + 8;
         (new FlowerFeature(Tile.rose.id)).place(this.level, this.random, var61, var78, var88);
      }

      if (this.random.nextInt(4) == 0) {
         int var62 = var4 + this.random.nextInt(16) + 8;
         int var79 = this.random.nextInt(128);
         int var89 = var5 + this.random.nextInt(16) + 8;
         (new FlowerFeature(Tile.mushroom1.id)).place(this.level, this.random, var62, var79, var89);
      }

      if (this.random.nextInt(8) == 0) {
         int var63 = var4 + this.random.nextInt(16) + 8;
         int var80 = this.random.nextInt(128);
         int var90 = var5 + this.random.nextInt(16) + 8;
         (new FlowerFeature(Tile.mushroom2.id)).place(this.level, this.random, var63, var80, var90);
      }

      for(int var64 = 0; var64 < 10; ++var64) {
         int var81 = var4 + this.random.nextInt(16) + 8;
         int var91 = this.random.nextInt(128);
         int var98 = var5 + this.random.nextInt(16) + 8;
         (new ReedsFeature()).place(this.level, this.random, var81, var91, var98);
      }

      if (this.random.nextInt(32) == 0) {
         int var65 = var4 + this.random.nextInt(16) + 8;
         int var82 = this.random.nextInt(128);
         int var92 = var5 + this.random.nextInt(16) + 8;
         (new PumpkinFeature()).place(this.level, this.random, var65, var82, var92);
      }

      int var66 = 0;
      if (var6 == Biome.desert) {
         var66 += 10;
      }

      for(int var83 = 0; var83 < var66; ++var83) {
         int var93 = var4 + this.random.nextInt(16) + 8;
         int var99 = this.random.nextInt(128);
         int var19 = var5 + this.random.nextInt(16) + 8;
         (new CactusFeature()).place(this.level, this.random, var93, var99, var19);
      }

      for(int var84 = 0; var84 < 50; ++var84) {
         int var94 = var4 + this.random.nextInt(16) + 8;
         int var100 = this.random.nextInt(this.random.nextInt(120) + 8);
         int var103 = var5 + this.random.nextInt(16) + 8;
         (new SpringFeature(Tile.water.id)).place(this.level, this.random, var94, var100, var103);
      }

      for(int var85 = 0; var85 < 20; ++var85) {
         int var95 = var4 + this.random.nextInt(16) + 8;
         int var101 = this.random.nextInt(this.random.nextInt(this.random.nextInt(112) + 8) + 8);
         int var104 = var5 + this.random.nextInt(16) + 8;
         (new SpringFeature(Tile.lava.id)).place(this.level, this.random, var95, var101, var104);
      }

      this.temperatures = this.level.getBiomeSource().getTemperatureBlock(this.temperatures, var4 + 8, var5 + 8, 16, 16);

      for(int var86 = var4 + 8; var86 < var4 + 8 + 16; ++var86) {
         for(int var96 = var5 + 8; var96 < var5 + 8 + 16; ++var96) {
            int var102 = var86 - (var4 + 8);
            int var105 = var96 - (var5 + 8);
            int var20 = this.level.getTopSolidBlock(var86, var96);
            double var21 = this.temperatures[var102 * 16 + var105] - (double)(var20 - 64) / (double)64.0F * 0.3;
            if (var21 < (double)0.5F && var20 > 0 && var20 < 128 && this.level.isEmptyTile(var86, var20, var96) && this.level.getMaterial(var86, var20 - 1, var96).blocksMotion() && this.level.getMaterial(var86, var20 - 1, var96) != Material.ice) {
               this.level.setTile(var86, var20, var96, Tile.topSnow.id);
            }
         }
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
      return "RandomLevelSource";
   }
}
