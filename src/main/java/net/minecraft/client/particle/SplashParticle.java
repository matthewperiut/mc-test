package net.minecraft.client.particle;

import net.minecraft.world.level.Level;

public class SplashParticle extends WaterDropParticle {
   public SplashParticle(Level level, double x, double y, double z, double xa, double ya, double za) {
      super(level, x, y, z);
      this.gravity = 0.04F;
      ++this.tex;
      if (ya == (double)0.0F && (xa != (double)0.0F || za != (double)0.0F)) {
         this.xd = xa;
         this.yd = ya + 0.1;
         this.zd = za;
      }

   }
}
