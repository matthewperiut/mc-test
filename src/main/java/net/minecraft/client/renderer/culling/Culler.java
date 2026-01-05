package net.minecraft.client.renderer.culling;

import net.minecraft.world.phys.AABB;

public interface Culler {
   boolean isVisible(AABB var1);

   boolean cubeInFrustum(double var1, double var3, double var5, double var7, double var9, double var11);

   boolean cubeFullyInFrustum(double var1, double var3, double var5, double var7, double var9, double var11);

   void prepare(double var1, double var3, double var5);
}
