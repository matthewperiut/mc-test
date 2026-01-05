package net.minecraft.client.renderer.culling;

import net.minecraft.world.phys.AABB;

public class FrustumCuller implements Culler {
   private FrustumData frustum = Frustum.getFrustum();
   private double xOff;
   private double yOff;
   private double zOff;

   public FrustumCuller() {
   }

   public void prepare(double xOff, double yOff, double zOff) {
      this.xOff = xOff;
      this.yOff = yOff;
      this.zOff = zOff;
   }

   public boolean cubeFullyInFrustum(double x0, double y0, double z0, double x1, double y1, double z1) {
      return this.frustum.cubeFullyInFrustum(x0 - this.xOff, y0 - this.yOff, z0 - this.zOff, x1 - this.xOff, y1 - this.yOff, z1 - this.zOff);
   }

   public boolean cubeInFrustum(double x0, double y0, double z0, double x1, double y1, double z1) {
      return this.frustum.cubeInFrustum(x0 - this.xOff, y0 - this.yOff, z0 - this.zOff, x1 - this.xOff, y1 - this.yOff, z1 - this.zOff);
   }

   public boolean isVisible(AABB bb) {
      return this.cubeInFrustum(bb.x0, bb.y0, bb.z0, bb.x1, bb.y1, bb.z1);
   }
}
