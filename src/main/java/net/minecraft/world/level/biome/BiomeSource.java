package net.minecraft.world.level.biome;

import java.util.Random;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

public class BiomeSource {
   private PerlinSimplexNoise temperatureMap;
   private PerlinSimplexNoise downfallMap;
   private PerlinSimplexNoise noiseMap;
   public double[] temperatures;
   public double[] downfalls;
   public double[] noises;
   public Biome[] biomes;
   private static final float zoom = 2.0F;
   private static final float tempScale = 0.025F;
   private static final float downfallScale = 0.05F;
   private static final float noiseScale = 0.25F;

   protected BiomeSource() {
   }

   public BiomeSource(Level var1) {
      this.temperatureMap = new PerlinSimplexNoise(new Random(var1.seed * 9871L), 4);
      this.downfallMap = new PerlinSimplexNoise(new Random(var1.seed * 39811L), 4);
      this.noiseMap = new PerlinSimplexNoise(new Random(var1.seed * 543321L), 2);
   }

   public Biome getBiome(ChunkPos var1) {
      return this.getBiome(var1.x, var1.z);
   }

   public Biome getBiome(int var1, int var2) {
      return this.getBiomeBlock(var1, var2, 1, 1)[0];
   }

   public double getTemperature(int var1, int var2) {
      this.temperatures = this.temperatureMap.getRegion(this.temperatures, (double)var1, (double)var2, 1, 1, (double)0.025F, (double)0.025F, (double)0.5F);
      return this.temperatures[0];
   }

   public Biome[] getBiomeBlock(int var1, int var2, int var3, int var4) {
      this.biomes = this.getBiomeBlock(this.biomes, var1, var2, var3, var4);
      return this.biomes;
   }

   public double[] getTemperatureBlock(double[] var1, int var2, int var3, int var4, int var5) {
      if (var1 == null || var1.length < var4 * var5) {
         var1 = new double[var4 * var5];
      }

      var1 = this.temperatureMap.getRegion(var1, (double)var2, (double)var3, var4, var4, (double)0.025F, (double)0.025F, (double)0.25F);
      this.noises = this.noiseMap.getRegion(this.noises, (double)var2, (double)var3, var4, var4, (double)0.25F, (double)0.25F, 0.5882352941176471);
      int var6 = 0;

      for(int var7 = 0; var7 < var4; ++var7) {
         for(int var8 = 0; var8 < var5; ++var8) {
            double var9 = this.noises[var6] * 1.1 + (double)0.5F;
            double var11 = 0.01;
            double var13 = (double)1.0F - var11;
            double var15 = (var1[var6] * 0.15 + 0.7) * var13 + var9 * var11;
            var15 = (double)1.0F - ((double)1.0F - var15) * ((double)1.0F - var15);
            if (var15 < (double)0.0F) {
               var15 = (double)0.0F;
            }

            if (var15 > (double)1.0F) {
               var15 = (double)1.0F;
            }

            var1[var6] = var15;
            ++var6;
         }
      }

      return var1;
   }

   public double[] getDownfallBlock(double[] var1, int var2, int var3, int var4, int var5) {
      if (var1 == null || var1.length < var4 * var5) {
         var1 = new double[var4 * var5];
      }

      var1 = this.downfallMap.getRegion(var1, (double)var2, (double)var3, var4, var4, (double)0.05F, (double)0.05F, (double)0.5F);
      return var1;
   }

   public Biome[] getBiomeBlock(Biome[] var1, int var2, int var3, int var4, int var5) {
      if (var1 == null || var1.length < var4 * var5) {
         var1 = new Biome[var4 * var5];
      }

      this.temperatures = this.temperatureMap.getRegion(this.temperatures, (double)var2, (double)var3, var4, var4, (double)0.025F, (double)0.025F, (double)0.25F);
      this.downfalls = this.downfallMap.getRegion(this.downfalls, (double)var2, (double)var3, var4, var4, (double)0.05F, (double)0.05F, 0.3333333333333333);
      this.noises = this.noiseMap.getRegion(this.noises, (double)var2, (double)var3, var4, var4, (double)0.25F, (double)0.25F, 0.5882352941176471);
      int var6 = 0;

      for(int var7 = 0; var7 < var4; ++var7) {
         for(int var8 = 0; var8 < var5; ++var8) {
            double var9 = this.noises[var6] * 1.1 + (double)0.5F;
            double var11 = 0.01;
            double var13 = (double)1.0F - var11;
            double var15 = (this.temperatures[var6] * 0.15 + 0.7) * var13 + var9 * var11;
            var11 = 0.002;
            var13 = (double)1.0F - var11;
            double var17 = (this.downfalls[var6] * 0.15 + (double)0.5F) * var13 + var9 * var11;
            var15 = (double)1.0F - ((double)1.0F - var15) * ((double)1.0F - var15);
            if (var15 < (double)0.0F) {
               var15 = (double)0.0F;
            }

            if (var17 < (double)0.0F) {
               var17 = (double)0.0F;
            }

            if (var15 > (double)1.0F) {
               var15 = (double)1.0F;
            }

            if (var17 > (double)1.0F) {
               var17 = (double)1.0F;
            }

            this.temperatures[var6] = var15;
            this.downfalls[var6] = var17;
            var1[var6++] = Biome.getBiome(var15, var17);
         }
      }

      return var1;
   }
}
