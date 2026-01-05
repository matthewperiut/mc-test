package net.minecraft.world.level.dimension;

import java.io.File;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.OldChunkStorage;
import net.minecraft.world.level.levelgen.RandomLevelSource;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.Vec3;
import util.Mth;

public class Dimension {
   public Level level;
   public BiomeSource biomeSource;
   public boolean foggy = false;
   public boolean ultraWarm = false;
   public boolean hasCeiling = false;
   public float[] brightnessRamp = new float[16];
   public int id = 0;
   private static final long fogColor = 12638463L;
   private float[] sunriseCol = new float[4];

   public Dimension() {
   }

   public final void init(Level var1) {
      this.level = var1;
      this.init();
      this.updateLightRamp();
   }

   protected void updateLightRamp() {
      float var1 = 0.05F;

      for(int var2 = 0; var2 <= 15; ++var2) {
         float var3 = 1.0F - (float)var2 / 15.0F;
         this.brightnessRamp[var2] = (1.0F - var3) / (var3 * 3.0F + 1.0F) * (1.0F - var1) + var1;
      }

   }

   protected void init() {
      this.biomeSource = new BiomeSource(this.level);
   }

   public ChunkSource createRandomLevelSource() {
      return new RandomLevelSource(this.level, this.level.seed);
   }

   public ChunkStorage createStorage(File var1) {
      return new OldChunkStorage(var1, true);
   }

   public boolean isValidSpawn(int var1, int var2) {
      int var3 = this.level.getTopTile(var1, var2);
      return var3 == Tile.sand.id;
   }

   public float getTimeOfDay(long var1, float var3) {
      int var4 = (int)(var1 % 24000L);
      float var5 = ((float)var4 + var3) / 24000.0F - 0.25F;
      if (var5 < 0.0F) {
         ++var5;
      }

      if (var5 > 1.0F) {
         --var5;
      }

      float var7 = 1.0F - (float)((Math.cos((double)var5 * Math.PI) + (double)1.0F) / (double)2.0F);
      var5 += (var7 - var5) / 3.0F;
      return var5;
   }

   public float[] getSunriseColor(float var1, float var2) {
      float var3 = 0.4F;
      float var4 = Mth.cos(var1 * (float)Math.PI * 2.0F) - 0.0F;
      float var5 = -0.0F;
      if (var4 >= var5 - var3 && var4 <= var5 + var3) {
         float var6 = (var4 - var5) / var3 * 0.5F + 0.5F;
         float var7 = 1.0F - (1.0F - Mth.sin(var6 * (float)Math.PI)) * 0.99F;
         var7 *= var7;
         this.sunriseCol[0] = var6 * 0.3F + 0.7F;
         this.sunriseCol[1] = var6 * var6 * 0.7F + 0.2F;
         this.sunriseCol[2] = var6 * var6 * 0.0F + 0.2F;
         this.sunriseCol[3] = var7;
         return this.sunriseCol;
      } else {
         return null;
      }
   }

   public Vec3 getFogColor(float var1, float var2) {
      float var3 = Mth.cos(var1 * (float)Math.PI * 2.0F) * 2.0F + 0.5F;
      if (var3 < 0.0F) {
         var3 = 0.0F;
      }

      if (var3 > 1.0F) {
         var3 = 1.0F;
      }

      float var4 = 0.7529412F;
      float var5 = 0.84705883F;
      float var6 = 1.0F;
      var4 *= var3 * 0.94F + 0.06F;
      var5 *= var3 * 0.94F + 0.06F;
      var6 *= var3 * 0.91F + 0.09F;
      return Vec3.newTemp((double)var4, (double)var5, (double)var6);
   }

   public boolean mayRespawn() {
      return true;
   }

   public static Dimension getNew(int var0) {
      if (var0 == 0) {
         return new Dimension();
      } else {
         return var0 == -1 ? new HellDimension() : null;
      }
   }
}
