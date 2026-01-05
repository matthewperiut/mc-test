package net.minecraft.world.level;

import net.minecraft.world.entity.Entity;

public class ChunkPos {
   public int x;
   public int z;

   public ChunkPos(int var1, int var2) {
      this.x = var1;
      this.z = var2;
   }

   public int hashCode() {
      return this.x << 8 | this.z;
   }

   public boolean equals(Object var1) {
      ChunkPos var2 = (ChunkPos)var1;
      return var2.x == this.x && var2.z == this.z;
   }

   public double distanceToSqr(Entity var1) {
      double var2 = (double)(this.x * 16 + 8);
      double var4 = (double)(this.z * 16 + 8);
      double var6 = var2 - var1.x;
      double var8 = var4 - var1.z;
      return var6 * var6 + var8 * var8;
   }
}
