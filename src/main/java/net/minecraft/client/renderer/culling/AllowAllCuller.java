package net.minecraft.client.renderer.culling;

import net.minecraft.world.phys.AABB;

public class AllowAllCuller implements Culler {
   public AllowAllCuller() {
   }

   public boolean isVisible(AABB bb) {
      return true;
   }

   public boolean cubeFullyInFrustum(double x1, double y1, double z1, double x2, double y2, double z2) {
      return true;
   }

   public boolean cubeInFrustum(double x1, double y1, double z1, double x2, double y2, double z2) {
      return true;
   }

   public void prepare(double xOff, double yOff, double zOff) {
   }
}
