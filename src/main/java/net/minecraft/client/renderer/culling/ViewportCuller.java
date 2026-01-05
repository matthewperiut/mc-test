package net.minecraft.client.renderer.culling;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import util.Mth;

public class ViewportCuller implements Culler {
   private Face[] faces = new Face[6];
   private double xOff;
   private double yOff;
   private double zOff;

   public ViewportCuller(Mob mob, double fogDistance, float a) {
      float yRot = mob.yRotO + (mob.yRot - mob.yRotO) * a;
      float xRot = mob.xRotO + (mob.xRot - mob.xRotO) * a;
      double x = mob.xOld + (mob.x - mob.xOld) * (double)a;
      double y = mob.yOld + (mob.y - mob.yOld) * (double)a;
      double z = mob.zOld + (mob.z - mob.zOld) * (double)a;
      double xd = (double)(Mth.sin(yRot / 180.0F * (float)Math.PI) * Mth.cos(xRot / 180.0F * (float)Math.PI));
      double zd = (double)(-Mth.cos(yRot / 180.0F * (float)Math.PI) * Mth.cos(xRot / 180.0F * (float)Math.PI));
      double yd = (double)(-Mth.sin(xRot / 180.0F * (float)Math.PI));
      float xFov = 30.0F;
      float yFov = 45.0F;
      this.faces[0] = new Face(x, y, z, yRot, xRot);
      this.faces[1] = new Face(x, y, z, yRot + xFov, xRot);
      this.faces[2] = new Face(x, y, z, yRot - xFov, xRot);
      this.faces[3] = new Face(x, y, z, yRot, xRot + yFov);
      this.faces[4] = new Face(x, y, z, yRot, xRot - yFov);
      this.faces[5] = new Face(x + xd * fogDistance, y + yd * fogDistance, z + zd * fogDistance, yRot + 180.0F, -xRot);
   }

   public boolean isVisible(AABB bb) {
      return this.cubeInFrustum(bb.x0, bb.y0, bb.z0, bb.x1, bb.y1, bb.z1);
   }

   public boolean cubeInFrustum(double x0, double y0, double z0, double x1, double y1, double z1) {
      x0 -= this.xOff;
      y0 -= this.yOff;
      z0 -= this.zOff;
      x1 -= this.xOff;
      y1 -= this.yOff;
      z1 -= this.zOff;
      double xd = (x1 - x0) / (double)2.0F;
      double yd = (y1 - y0) / (double)2.0F;
      double zd = (z1 - z0) / (double)2.0F;
      double xc = x0 + xd;
      double yc = y0 + yd;
      double zc = z0 + zd;
      double r = this.max(xd, yd, zd) * (double)1.5F;
      if (!this.faces[0].inFront(xc, yc, zc, r)) {
         return false;
      } else if (!this.faces[1].inFront(xc, yc, zc, r)) {
         return false;
      } else if (!this.faces[2].inFront(xc, yc, zc, r)) {
         return false;
      } else if (!this.faces[3].inFront(xc, yc, zc, r)) {
         return false;
      } else if (!this.faces[4].inFront(xc, yc, zc, r)) {
         return false;
      } else if (!this.faces[5].inFront(xc, yc, zc, r)) {
         return false;
      } else if (!this.faces[0].inFront(x0, y0, z0, x1, y1, z1)) {
         return false;
      } else if (!this.faces[1].inFront(x0, y0, z0, x1, y1, z1)) {
         return false;
      } else if (!this.faces[2].inFront(x0, y0, z0, x1, y1, z1)) {
         return false;
      } else if (!this.faces[3].inFront(x0, y0, z0, x1, y1, z1)) {
         return false;
      } else if (!this.faces[4].inFront(x0, y0, z0, x1, y1, z1)) {
         return false;
      } else {
         return this.faces[5].inFront(x0, y0, z0, x1, y1, z1);
      }
   }

   public boolean cubeFullyInFrustum(double x0, double y0, double z0, double x1, double y1, double z1) {
      x0 -= this.xOff;
      y0 -= this.yOff;
      z0 -= this.zOff;
      x1 -= this.xOff;
      y1 -= this.yOff;
      z1 -= this.zOff;
      double xd = (x1 - x0) / (double)2.0F;
      double yd = (y1 - y0) / (double)2.0F;
      double zd = (z1 - z0) / (double)2.0F;
      double xc = x0 + xd;
      double yc = y0 + yd;
      double zc = z0 + zd;
      double r = this.max(xd, yd, zd) * (double)1.5F;
      if (!this.faces[0].inFront(xc, yc, zc, r)) {
         return false;
      } else if (!this.faces[1].inFront(xc, yc, zc, r)) {
         return false;
      } else if (!this.faces[2].inFront(xc, yc, zc, r)) {
         return false;
      } else if (!this.faces[3].inFront(xc, yc, zc, r)) {
         return false;
      } else if (!this.faces[4].inFront(xc, yc, zc, r)) {
         return false;
      } else if (!this.faces[5].inFront(xc, yc, zc, r)) {
         return false;
      } else if (!this.faces[0].fullyInFront(x0, y0, z0, x1, y1, z1)) {
         return false;
      } else if (!this.faces[1].fullyInFront(x0, y0, z0, x1, y1, z1)) {
         return false;
      } else if (!this.faces[2].fullyInFront(x0, y0, z0, x1, y1, z1)) {
         return false;
      } else if (!this.faces[3].fullyInFront(x0, y0, z0, x1, y1, z1)) {
         return false;
      } else if (!this.faces[4].fullyInFront(x0, y0, z0, x1, y1, z1)) {
         return false;
      } else {
         return this.faces[5].fullyInFront(x0, y0, z0, x1, y1, z1);
      }
   }

   private double max(double a, double b, double c) {
      return a > b ? (a > c ? c : a) : (b > c ? b : c);
   }

   public void prepare(double xOff, double yOff, double zOff) {
      this.xOff = xOff;
      this.yOff = yOff;
      this.zOff = zOff;
   }

   private static class Face {
      private double xc;
      private double yc;
      private double zc;
      private double xd;
      private double yd;
      private double zd;
      private double cullOffs;

      public Face(double x, double y, double z, float yRot, float xRot) {
         this.xc = x;
         this.yc = y;
         this.zc = z;
         this.xd = (double)(Mth.sin(yRot / 180.0F * (float)Math.PI) * Mth.cos(xRot / 180.0F * (float)Math.PI));
         this.zd = (double)(-Mth.cos(yRot / 180.0F * (float)Math.PI) * Mth.cos(xRot / 180.0F * (float)Math.PI));
         this.yd = (double)(-Mth.sin(xRot / 180.0F * (float)Math.PI));
         this.cullOffs = this.xc * this.xd + this.yc * this.yd + this.zc * this.zd;
      }

      public boolean inFront(double x, double y, double z, double r) {
         return x * this.xd + y * this.yd + z * this.zd > this.cullOffs - r;
      }

      public boolean inFront(double x0, double y0, double z0, double x1, double y1, double z1) {
         return x0 * this.xd + y0 * this.yd + z0 * this.zd > this.cullOffs || x1 * this.xd + y0 * this.yd + z0 * this.zd > this.cullOffs || x0 * this.xd + y1 * this.yd + z0 * this.zd > this.cullOffs || x1 * this.xd + y1 * this.yd + z0 * this.zd > this.cullOffs || x0 * this.xd + y0 * this.yd + z1 * this.zd > this.cullOffs || x1 * this.xd + y0 * this.yd + z1 * this.zd > this.cullOffs || x0 * this.xd + y1 * this.yd + z1 * this.zd > this.cullOffs || x1 * this.xd + y1 * this.yd + z1 * this.zd > this.cullOffs;
      }

      public boolean fullyInFront(double x0, double y0, double z0, double x1, double y1, double z1) {
         return !(x0 * this.xd + y0 * this.yd + z0 * this.zd < this.cullOffs) && !(x1 * this.xd + y0 * this.yd + z0 * this.zd < this.cullOffs) && !(x0 * this.xd + y1 * this.yd + z0 * this.zd < this.cullOffs) && !(x1 * this.xd + y1 * this.yd + z0 * this.zd < this.cullOffs) && !(x0 * this.xd + y0 * this.yd + z1 * this.zd < this.cullOffs) && !(x1 * this.xd + y0 * this.yd + z1 * this.zd < this.cullOffs) && !(x0 * this.xd + y1 * this.yd + z1 * this.zd < this.cullOffs) && !(x1 * this.xd + y1 * this.yd + z1 * this.zd < this.cullOffs);
      }
   }
}
