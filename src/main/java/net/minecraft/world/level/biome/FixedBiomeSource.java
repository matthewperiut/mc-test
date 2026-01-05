package net.minecraft.world.level.biome;

import java.util.Arrays;
import net.minecraft.world.level.ChunkPos;

public class FixedBiomeSource extends BiomeSource {
   private Biome biome;
   private double temperature;
   private double downfall;

   public FixedBiomeSource(Biome var1, double var2, double var4) {
      this.biome = var1;
      this.temperature = var2;
      this.downfall = var4;
   }

   public Biome getBiome(ChunkPos var1) {
      return this.biome;
   }

   public Biome getBiome(int var1, int var2) {
      return this.biome;
   }

   public double getTemperature(int var1, int var2) {
      return this.temperature;
   }

   public Biome[] getBiomeBlock(int var1, int var2, int var3, int var4) {
      this.biomes = this.getBiomeBlock(this.biomes, var1, var2, var3, var4);
      return this.biomes;
   }

   public double[] getTemperatureBlock(double[] var1, int var2, int var3, int var4, int var5) {
      if (var1 == null || var1.length < var4 * var5) {
         var1 = new double[var4 * var5];
      }

      Arrays.fill(var1, 0, var4 * var5, this.temperature);
      return var1;
   }

   public double[] getDownfallBlock(double[] var1, int var2, int var3, int var4, int var5) {
      if (var1 == null || var1.length < var4 * var5) {
         var1 = new double[var4 * var5];
      }

      Arrays.fill(var1, 0, var4 * var5, this.downfall);
      return var1;
   }

   public Biome[] getBiomeBlock(Biome[] var1, int var2, int var3, int var4, int var5) {
      if (var1 == null || var1.length < var4 * var5) {
         var1 = new Biome[var4 * var5];
         this.temperatures = new double[var4 * var5];
         this.downfalls = new double[var4 * var5];
      }

      Arrays.fill(var1, 0, var4 * var5, this.biome);
      Arrays.fill(this.downfalls, 0, var4 * var5, this.downfall);
      Arrays.fill(this.temperatures, 0, var4 * var5, this.temperature);
      return var1;
   }
}
