package net.minecraft.world.level.levelgen;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;

public class LargeFeature {
   protected int radius = 8;
   protected Random random = new Random();

   public LargeFeature() {
   }

   public void apply(ChunkSource var1, Level var2, int var3, int var4, byte[] var5) {
      int var6 = this.radius;
      this.random.setSeed(var2.seed);
      long var7 = this.random.nextLong() / 2L * 2L + 1L;
      long var9 = this.random.nextLong() / 2L * 2L + 1L;

      for(int var11 = var3 - var6; var11 <= var3 + var6; ++var11) {
         for(int var12 = var4 - var6; var12 <= var4 + var6; ++var12) {
            this.random.setSeed((long)var11 * var7 + (long)var12 * var9 ^ var2.seed);
            this.addFeature(var2, var11, var12, var3, var4, var5);
         }
      }

   }

   protected void addFeature(Level var1, int var2, int var3, int var4, int var5, byte[] var6) {
   }
}
