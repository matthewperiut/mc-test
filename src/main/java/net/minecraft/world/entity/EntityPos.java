package net.minecraft.world.entity;

public class EntityPos {
   public double x;
   public double y;
   public double z;
   public float yRot;
   public float xRot;
   public boolean rot = false;
   public boolean move = false;

   public EntityPos(double var1, double var3, double var5, float var7, float var8) {
      this.x = var1;
      this.y = var3;
      this.z = var5;
      this.yRot = var7;
      this.xRot = var8;
      this.rot = true;
      this.move = true;
   }

   public EntityPos(double var1, double var3, double var5) {
      this.x = var1;
      this.y = var3;
      this.z = var5;
      this.move = true;
      this.rot = false;
   }

   public EntityPos(float var1, float var2) {
      this.yRot = var1;
      this.xRot = var2;
      this.rot = true;
      this.move = false;
   }

   public EntityPos lerp(Entity var1, float var2) {
      double var3 = var1.x + (this.x - var1.x) * (double)var2;
      double var5 = var1.y + (this.y - var1.y) * (double)var2;
      double var7 = var1.z + (this.z - var1.z) * (double)var2;
      float var9 = this.yRot - var1.yRot;

      float var10;
      for(var10 = this.xRot - var1.xRot; var9 >= 180.0F; var9 -= 360.0F) {
      }

      while(var9 < -180.0F) {
         var9 += 360.0F;
      }

      while(var10 >= 180.0F) {
         var10 -= 360.0F;
      }

      while(var10 < -180.0F) {
         var10 += 360.0F;
      }

      float var11 = var1.yRot + var9 * var2;

      float var12;
      for(var12 = var1.xRot + var10 * var2; var11 >= 180.0F; var11 -= 360.0F) {
      }

      while(var11 < -180.0F) {
         var11 += 360.0F;
      }

      while(var12 >= 180.0F) {
         var12 -= 360.0F;
      }

      while(var12 < -180.0F) {
         var12 += 360.0F;
      }

      if (this.rot && this.move) {
         return new EntityPos(var3, var5, var7, var11, var12);
      } else if (this.move) {
         return new EntityPos(var3, var5, var7);
      } else {
         return this.rot ? new EntityPos(var11, var12) : null;
      }
   }
}
