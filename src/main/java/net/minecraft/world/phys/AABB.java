package net.minecraft.world.phys;

import java.util.ArrayList;
import java.util.List;

public class AABB {
   private static List<AABB> pool = new ArrayList();
   private static int poolPointer = 0;
   public double x0;
   public double y0;
   public double z0;
   public double x1;
   public double y1;
   public double z1;

   public static AABB newPermanent(double var0, double var2, double var4, double var6, double var8, double var10) {
      return new AABB(var0, var2, var4, var6, var8, var10);
   }

   public static void resetPool() {
      poolPointer = 0;
   }

   public static AABB newTemp(double var0, double var2, double var4, double var6, double var8, double var10) {
      if (poolPointer >= pool.size()) {
         pool.add(newPermanent((double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F));
      }

      return ((AABB)pool.get(poolPointer++)).set(var0, var2, var4, var6, var8, var10);
   }

   private AABB(double var1, double var3, double var5, double var7, double var9, double var11) {
      this.x0 = var1;
      this.y0 = var3;
      this.z0 = var5;
      this.x1 = var7;
      this.y1 = var9;
      this.z1 = var11;
   }

   public AABB set(double var1, double var3, double var5, double var7, double var9, double var11) {
      this.x0 = var1;
      this.y0 = var3;
      this.z0 = var5;
      this.x1 = var7;
      this.y1 = var9;
      this.z1 = var11;
      return this;
   }

   public AABB expand(double var1, double var3, double var5) {
      double var7 = this.x0;
      double var9 = this.y0;
      double var11 = this.z0;
      double var13 = this.x1;
      double var15 = this.y1;
      double var17 = this.z1;
      if (var1 < (double)0.0F) {
         var7 += var1;
      }

      if (var1 > (double)0.0F) {
         var13 += var1;
      }

      if (var3 < (double)0.0F) {
         var9 += var3;
      }

      if (var3 > (double)0.0F) {
         var15 += var3;
      }

      if (var5 < (double)0.0F) {
         var11 += var5;
      }

      if (var5 > (double)0.0F) {
         var17 += var5;
      }

      return newTemp(var7, var9, var11, var13, var15, var17);
   }

   public AABB grow(double var1, double var3, double var5) {
      double var7 = this.x0 - var1;
      double var9 = this.y0 - var3;
      double var11 = this.z0 - var5;
      double var13 = this.x1 + var1;
      double var15 = this.y1 + var3;
      double var17 = this.z1 + var5;
      return newTemp(var7, var9, var11, var13, var15, var17);
   }

   public AABB cloneMove(double var1, double var3, double var5) {
      return newTemp(this.x0 + var1, this.y0 + var3, this.z0 + var5, this.x1 + var1, this.y1 + var3, this.z1 + var5);
   }

   public double clipXCollide(AABB var1, double var2) {
      if (!(var1.y1 <= this.y0) && !(var1.y0 >= this.y1)) {
         if (!(var1.z1 <= this.z0) && !(var1.z0 >= this.z1)) {
            if (var2 > (double)0.0F && var1.x1 <= this.x0) {
               double var4 = this.x0 - var1.x1;
               if (var4 < var2) {
                  var2 = var4;
               }
            }

            if (var2 < (double)0.0F && var1.x0 >= this.x1) {
               double var6 = this.x1 - var1.x0;
               if (var6 > var2) {
                  var2 = var6;
               }
            }

            return var2;
         } else {
            return var2;
         }
      } else {
         return var2;
      }
   }

   public double clipYCollide(AABB var1, double var2) {
      if (!(var1.x1 <= this.x0) && !(var1.x0 >= this.x1)) {
         if (!(var1.z1 <= this.z0) && !(var1.z0 >= this.z1)) {
            if (var2 > (double)0.0F && var1.y1 <= this.y0) {
               double var4 = this.y0 - var1.y1;
               if (var4 < var2) {
                  var2 = var4;
               }
            }

            if (var2 < (double)0.0F && var1.y0 >= this.y1) {
               double var6 = this.y1 - var1.y0;
               if (var6 > var2) {
                  var2 = var6;
               }
            }

            return var2;
         } else {
            return var2;
         }
      } else {
         return var2;
      }
   }

   public double clipZCollide(AABB var1, double var2) {
      if (!(var1.x1 <= this.x0) && !(var1.x0 >= this.x1)) {
         if (!(var1.y1 <= this.y0) && !(var1.y0 >= this.y1)) {
            if (var2 > (double)0.0F && var1.z1 <= this.z0) {
               double var4 = this.z0 - var1.z1;
               if (var4 < var2) {
                  var2 = var4;
               }
            }

            if (var2 < (double)0.0F && var1.z0 >= this.z1) {
               double var6 = this.z1 - var1.z0;
               if (var6 > var2) {
                  var2 = var6;
               }
            }

            return var2;
         } else {
            return var2;
         }
      } else {
         return var2;
      }
   }

   public boolean intersects(AABB var1) {
      if (!(var1.x1 <= this.x0) && !(var1.x0 >= this.x1)) {
         if (!(var1.y1 <= this.y0) && !(var1.y0 >= this.y1)) {
            return !(var1.z1 <= this.z0) && !(var1.z0 >= this.z1);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean intersectsInner(AABB var1) {
      if (!(var1.x1 < this.x0) && !(var1.x0 > this.x1)) {
         if (!(var1.y1 < this.y0) && !(var1.y0 > this.y1)) {
            return !(var1.z1 < this.z0) && !(var1.z0 > this.z1);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public AABB move(double var1, double var3, double var5) {
      this.x0 += var1;
      this.y0 += var3;
      this.z0 += var5;
      this.x1 += var1;
      this.y1 += var3;
      this.z1 += var5;
      return this;
   }

   public boolean intersects(double var1, double var3, double var5, double var7, double var9, double var11) {
      if (!(var7 <= this.x0) && !(var1 >= this.x1)) {
         if (!(var9 <= this.y0) && !(var3 >= this.y1)) {
            return !(var11 <= this.z0) && !(var5 >= this.z1);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean contains(Vec3 var1) {
      if (!(var1.x <= this.x0) && !(var1.x >= this.x1)) {
         if (!(var1.y <= this.y0) && !(var1.y >= this.y1)) {
            return !(var1.z <= this.z0) && !(var1.z >= this.z1);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public double getSize() {
      double var1 = this.x1 - this.x0;
      double var3 = this.y1 - this.y0;
      double var5 = this.z1 - this.z0;
      return (var1 + var3 + var5) / (double)3.0F;
   }

   public AABB shrink(double var1, double var3, double var5) {
      double var7 = this.x0;
      double var9 = this.y0;
      double var11 = this.z0;
      double var13 = this.x1;
      double var15 = this.y1;
      double var17 = this.z1;
      if (var1 < (double)0.0F) {
         var7 -= var1;
      }

      if (var1 > (double)0.0F) {
         var13 -= var1;
      }

      if (var3 < (double)0.0F) {
         var9 -= var3;
      }

      if (var3 > (double)0.0F) {
         var15 -= var3;
      }

      if (var5 < (double)0.0F) {
         var11 -= var5;
      }

      if (var5 > (double)0.0F) {
         var17 -= var5;
      }

      return newTemp(var7, var9, var11, var13, var15, var17);
   }

   public AABB copy() {
      return newTemp(this.x0, this.y0, this.z0, this.x1, this.y1, this.z1);
   }

   public HitResult clip(Vec3 var1, Vec3 var2) {
      Vec3 var3 = var1.clipX(var2, this.x0);
      Vec3 var4 = var1.clipX(var2, this.x1);
      Vec3 var5 = var1.clipY(var2, this.y0);
      Vec3 var6 = var1.clipY(var2, this.y1);
      Vec3 var7 = var1.clipZ(var2, this.z0);
      Vec3 var8 = var1.clipZ(var2, this.z1);
      if (!this.containsX(var3)) {
         var3 = null;
      }

      if (!this.containsX(var4)) {
         var4 = null;
      }

      if (!this.containsY(var5)) {
         var5 = null;
      }

      if (!this.containsY(var6)) {
         var6 = null;
      }

      if (!this.containsZ(var7)) {
         var7 = null;
      }

      if (!this.containsZ(var8)) {
         var8 = null;
      }

      Vec3 var9 = null;
      if (var3 != null && (var9 == null || var1.distanceToSqr(var3) < var1.distanceToSqr(var9))) {
         var9 = var3;
      }

      if (var4 != null && (var9 == null || var1.distanceToSqr(var4) < var1.distanceToSqr(var9))) {
         var9 = var4;
      }

      if (var5 != null && (var9 == null || var1.distanceToSqr(var5) < var1.distanceToSqr(var9))) {
         var9 = var5;
      }

      if (var6 != null && (var9 == null || var1.distanceToSqr(var6) < var1.distanceToSqr(var9))) {
         var9 = var6;
      }

      if (var7 != null && (var9 == null || var1.distanceToSqr(var7) < var1.distanceToSqr(var9))) {
         var9 = var7;
      }

      if (var8 != null && (var9 == null || var1.distanceToSqr(var8) < var1.distanceToSqr(var9))) {
         var9 = var8;
      }

      if (var9 == null) {
         return null;
      } else {
         byte var10 = -1;
         if (var9 == var3) {
            var10 = 4;
         }

         if (var9 == var4) {
            var10 = 5;
         }

         if (var9 == var5) {
            var10 = 0;
         }

         if (var9 == var6) {
            var10 = 1;
         }

         if (var9 == var7) {
            var10 = 2;
         }

         if (var9 == var8) {
            var10 = 3;
         }

         return new HitResult(0, 0, 0, var10, var9);
      }
   }

   private boolean containsX(Vec3 var1) {
      if (var1 == null) {
         return false;
      } else {
         return var1.y >= this.y0 && var1.y <= this.y1 && var1.z >= this.z0 && var1.z <= this.z1;
      }
   }

   private boolean containsY(Vec3 var1) {
      if (var1 == null) {
         return false;
      } else {
         return var1.x >= this.x0 && var1.x <= this.x1 && var1.z >= this.z0 && var1.z <= this.z1;
      }
   }

   private boolean containsZ(Vec3 var1) {
      if (var1 == null) {
         return false;
      } else {
         return var1.x >= this.x0 && var1.x <= this.x1 && var1.y >= this.y0 && var1.y <= this.y1;
      }
   }

   public void set(AABB var1) {
      this.x0 = var1.x0;
      this.y0 = var1.y0;
      this.z0 = var1.z0;
      this.x1 = var1.x1;
      this.y1 = var1.y1;
      this.z1 = var1.z1;
   }

   public String toString() {
      return "box[" + this.x0 + ", " + this.y0 + ", " + this.z0 + " -> " + this.x1 + ", " + this.y1 + ", " + this.z1 + "]";
   }
}
