package net.minecraft.client.renderer.culling;

import net.minecraft.world.phys.AABB;

public class FrustumData {
   public static final int RIGHT = 0;
   public static final int LEFT = 1;
   public static final int BOTTOM = 2;
   public static final int TOP = 3;
   public static final int BACK = 4;
   public static final int FRONT = 5;
   public static final int A = 0;
   public static final int B = 1;
   public static final int C = 2;
   public static final int D = 3;
   public float[][] m_Frustum = new float[16][16];
   public float[] proj = new float[16];
   public float[] modl = new float[16];
   public float[] clip = new float[16];

   public FrustumData() {
   }

   public boolean pointInFrustum(float x, float y, float z) {
      for(int i = 0; i < 6; ++i) {
         if (this.m_Frustum[i][0] * x + this.m_Frustum[i][1] * y + this.m_Frustum[i][2] * z + this.m_Frustum[i][3] <= 0.0F) {
            return false;
         }
      }

      return true;
   }

   public boolean sphereInFrustum(float x, float y, float z, float radius) {
      for(int i = 0; i < 6; ++i) {
         if (this.m_Frustum[i][0] * x + this.m_Frustum[i][1] * y + this.m_Frustum[i][2] * z + this.m_Frustum[i][3] <= -radius) {
            return false;
         }
      }

      return true;
   }

   public boolean cubeFullyInFrustum(double x1, double y1, double z1, double x2, double y2, double z2) {
      for(int i = 0; i < 6; ++i) {
         if (!((double)this.m_Frustum[i][0] * x1 + (double)this.m_Frustum[i][1] * y1 + (double)this.m_Frustum[i][2] * z1 + (double)this.m_Frustum[i][3] > (double)0.0F)) {
            return false;
         }

         if (!((double)this.m_Frustum[i][0] * x2 + (double)this.m_Frustum[i][1] * y1 + (double)this.m_Frustum[i][2] * z1 + (double)this.m_Frustum[i][3] > (double)0.0F)) {
            return false;
         }

         if (!((double)this.m_Frustum[i][0] * x1 + (double)this.m_Frustum[i][1] * y2 + (double)this.m_Frustum[i][2] * z1 + (double)this.m_Frustum[i][3] > (double)0.0F)) {
            return false;
         }

         if (!((double)this.m_Frustum[i][0] * x2 + (double)this.m_Frustum[i][1] * y2 + (double)this.m_Frustum[i][2] * z1 + (double)this.m_Frustum[i][3] > (double)0.0F)) {
            return false;
         }

         if (!((double)this.m_Frustum[i][0] * x1 + (double)this.m_Frustum[i][1] * y1 + (double)this.m_Frustum[i][2] * z2 + (double)this.m_Frustum[i][3] > (double)0.0F)) {
            return false;
         }

         if (!((double)this.m_Frustum[i][0] * x2 + (double)this.m_Frustum[i][1] * y1 + (double)this.m_Frustum[i][2] * z2 + (double)this.m_Frustum[i][3] > (double)0.0F)) {
            return false;
         }

         if (!((double)this.m_Frustum[i][0] * x1 + (double)this.m_Frustum[i][1] * y2 + (double)this.m_Frustum[i][2] * z2 + (double)this.m_Frustum[i][3] > (double)0.0F)) {
            return false;
         }

         if (!((double)this.m_Frustum[i][0] * x2 + (double)this.m_Frustum[i][1] * y2 + (double)this.m_Frustum[i][2] * z2 + (double)this.m_Frustum[i][3] > (double)0.0F)) {
            return false;
         }
      }

      return true;
   }

   public boolean cubeInFrustum(double x1, double y1, double z1, double x2, double y2, double z2) {
      for(int i = 0; i < 6; ++i) {
         if (!((double)this.m_Frustum[i][0] * x1 + (double)this.m_Frustum[i][1] * y1 + (double)this.m_Frustum[i][2] * z1 + (double)this.m_Frustum[i][3] > (double)0.0F) && !((double)this.m_Frustum[i][0] * x2 + (double)this.m_Frustum[i][1] * y1 + (double)this.m_Frustum[i][2] * z1 + (double)this.m_Frustum[i][3] > (double)0.0F) && !((double)this.m_Frustum[i][0] * x1 + (double)this.m_Frustum[i][1] * y2 + (double)this.m_Frustum[i][2] * z1 + (double)this.m_Frustum[i][3] > (double)0.0F) && !((double)this.m_Frustum[i][0] * x2 + (double)this.m_Frustum[i][1] * y2 + (double)this.m_Frustum[i][2] * z1 + (double)this.m_Frustum[i][3] > (double)0.0F) && !((double)this.m_Frustum[i][0] * x1 + (double)this.m_Frustum[i][1] * y1 + (double)this.m_Frustum[i][2] * z2 + (double)this.m_Frustum[i][3] > (double)0.0F) && !((double)this.m_Frustum[i][0] * x2 + (double)this.m_Frustum[i][1] * y1 + (double)this.m_Frustum[i][2] * z2 + (double)this.m_Frustum[i][3] > (double)0.0F) && !((double)this.m_Frustum[i][0] * x1 + (double)this.m_Frustum[i][1] * y2 + (double)this.m_Frustum[i][2] * z2 + (double)this.m_Frustum[i][3] > (double)0.0F) && !((double)this.m_Frustum[i][0] * x2 + (double)this.m_Frustum[i][1] * y2 + (double)this.m_Frustum[i][2] * z2 + (double)this.m_Frustum[i][3] > (double)0.0F)) {
            return false;
         }
      }

      return true;
   }

   public boolean isVisible(AABB aabb) {
      return this.cubeInFrustum(aabb.x0, aabb.y0, aabb.z0, aabb.x1, aabb.y1, aabb.z1);
   }
}
