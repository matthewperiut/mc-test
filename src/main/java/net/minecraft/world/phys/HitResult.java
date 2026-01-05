package net.minecraft.world.phys;

import net.minecraft.world.entity.Entity;

public class HitResult {
   public Type type;
   public int x;
   public int y;
   public int z;
   public int f;
   public Vec3 pos;
   public Entity entity;

   public HitResult(int var1, int var2, int var3, int var4, Vec3 var5) {
      this.type = HitResult.Type.TILE;
      this.x = var1;
      this.y = var2;
      this.z = var3;
      this.f = var4;
      this.pos = Vec3.newTemp(var5.x, var5.y, var5.z);
   }

   public HitResult(Entity var1) {
      this.type = HitResult.Type.ENTITY;
      this.entity = var1;
      this.pos = Vec3.newTemp(var1.x, var1.y, var1.z);
   }

   public double distanceTo(Entity var1) {
      double var2 = this.pos.x - var1.x;
      double var4 = this.pos.y - var1.y;
      double var6 = this.pos.z - var1.z;
      return var2 * var2 + var4 * var4 + var6 * var6;
   }

   public static enum Type {
      TILE,
      ENTITY;

      private Type() {
      }
   }
}
