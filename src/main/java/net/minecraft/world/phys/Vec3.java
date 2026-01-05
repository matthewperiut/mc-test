package net.minecraft.world.phys;

import java.util.ArrayList;
import java.util.List;
import util.Mth;

public class Vec3 {
   private static List<Vec3> pool = new ArrayList();
   private static int poolPointer = 0;
   public double x;
   public double y;
   public double z;

   public static Vec3 newPermanent(double var0, double var2, double var4) {
      return new Vec3(var0, var2, var4);
   }

   public static void resetPool() {
      poolPointer = 0;
   }

   public static Vec3 newTemp(double var0, double var2, double var4) {
      if (poolPointer >= pool.size()) {
         pool.add(newPermanent((double)0.0F, (double)0.0F, (double)0.0F));
      }

      return ((Vec3)pool.get(poolPointer++)).set(var0, var2, var4);
   }

   private Vec3(double var1, double var3, double var5) {
      if (var1 == (double)-0.0F) {
         var1 = (double)0.0F;
      }

      if (var3 == (double)-0.0F) {
         var3 = (double)0.0F;
      }

      if (var5 == (double)-0.0F) {
         var5 = (double)0.0F;
      }

      this.x = var1;
      this.y = var3;
      this.z = var5;
   }

   private Vec3 set(double var1, double var3, double var5) {
      this.x = var1;
      this.y = var3;
      this.z = var5;
      return this;
   }

   public Vec3 interpolateTo(Vec3 var1, double var2) {
      double var4 = this.x + (var1.x - this.x) * var2;
      double var6 = this.y + (var1.y - this.y) * var2;
      double var8 = this.z + (var1.z - this.z) * var2;
      return newTemp(var4, var6, var8);
   }

   public Vec3 vectorTo(Vec3 var1) {
      return newTemp(var1.x - this.x, var1.y - this.y, var1.z - this.z);
   }

   public Vec3 normalize() {
      double var1 = (double)Mth.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
      return var1 < 1.0E-4 ? newTemp((double)0.0F, (double)0.0F, (double)0.0F) : newTemp(this.x / var1, this.y / var1, this.z / var1);
   }

   public double dot(Vec3 var1) {
      return this.x * var1.x + this.y * var1.y + this.z * var1.z;
   }

   public Vec3 cross(Vec3 var1) {
      return newTemp(this.y * var1.z - this.z * var1.y, this.z * var1.x - this.x * var1.z, this.x * var1.y - this.y * var1.x);
   }

   public Vec3 add(double var1, double var3, double var5) {
      return newTemp(this.x + var1, this.y + var3, this.z + var5);
   }

   public double distanceTo(Vec3 var1) {
      double var2 = var1.x - this.x;
      double var4 = var1.y - this.y;
      double var6 = var1.z - this.z;
      return (double)Mth.sqrt(var2 * var2 + var4 * var4 + var6 * var6);
   }

   public double distanceToSqr(Vec3 var1) {
      double var2 = var1.x - this.x;
      double var4 = var1.y - this.y;
      double var6 = var1.z - this.z;
      return var2 * var2 + var4 * var4 + var6 * var6;
   }

   public double distanceToSqr(double var1, double var3, double var5) {
      double var7 = var1 - this.x;
      double var9 = var3 - this.y;
      double var11 = var5 - this.z;
      return var7 * var7 + var9 * var9 + var11 * var11;
   }

   public Vec3 scale(double var1) {
      return newTemp(this.x * var1, this.y * var1, this.z * var1);
   }

   public double length() {
      return (double)Mth.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
   }

   public Vec3 clipX(Vec3 var1, double var2) {
      double var4 = var1.x - this.x;
      double var6 = var1.y - this.y;
      double var8 = var1.z - this.z;
      if (var4 * var4 < (double)1.0E-7F) {
         return null;
      } else {
         double var10 = (var2 - this.x) / var4;
         return !(var10 < (double)0.0F) && !(var10 > (double)1.0F) ? newTemp(this.x + var4 * var10, this.y + var6 * var10, this.z + var8 * var10) : null;
      }
   }

   public Vec3 clipY(Vec3 var1, double var2) {
      double var4 = var1.x - this.x;
      double var6 = var1.y - this.y;
      double var8 = var1.z - this.z;
      if (var6 * var6 < (double)1.0E-7F) {
         return null;
      } else {
         double var10 = (var2 - this.y) / var6;
         return !(var10 < (double)0.0F) && !(var10 > (double)1.0F) ? newTemp(this.x + var4 * var10, this.y + var6 * var10, this.z + var8 * var10) : null;
      }
   }

   public Vec3 clipZ(Vec3 var1, double var2) {
      double var4 = var1.x - this.x;
      double var6 = var1.y - this.y;
      double var8 = var1.z - this.z;
      if (var8 * var8 < (double)1.0E-7F) {
         return null;
      } else {
         double var10 = (var2 - this.z) / var8;
         return !(var10 < (double)0.0F) && !(var10 > (double)1.0F) ? newTemp(this.x + var4 * var10, this.y + var6 * var10, this.z + var8 * var10) : null;
      }
   }

   public String toString() {
      return "(" + this.x + ", " + this.y + ", " + this.z + ")";
   }

   public Vec3 lerp(Vec3 var1, double var2) {
      return newTemp(this.x + (var1.x - this.x) * var2, this.y + (var1.y - this.y) * var2, this.z + (var1.z - this.z) * var2);
   }

   public void xRot(float var1) {
      float var2 = Mth.cos(var1);
      float var3 = Mth.sin(var1);
      double var4 = this.x;
      double var6 = this.y * (double)var2 + this.z * (double)var3;
      double var8 = this.z * (double)var2 - this.y * (double)var3;
      this.x = var4;
      this.y = var6;
      this.z = var8;
   }

   public void yRot(float var1) {
      float var2 = Mth.cos(var1);
      float var3 = Mth.sin(var1);
      double var4 = this.x * (double)var2 + this.z * (double)var3;
      double var6 = this.y;
      double var8 = this.z * (double)var2 - this.x * (double)var3;
      this.x = var4;
      this.y = var6;
      this.z = var8;
   }

   public void zRot(float var1) {
      float var2 = Mth.cos(var1);
      float var3 = Mth.sin(var1);
      double var4 = this.x * (double)var2 + this.y * (double)var3;
      double var6 = this.y * (double)var2 - this.x * (double)var3;
      double var8 = this.z;
      this.x = var4;
      this.y = var6;
      this.z = var8;
   }
}
