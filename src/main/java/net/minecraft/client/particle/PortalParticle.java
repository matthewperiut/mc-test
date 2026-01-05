package net.minecraft.client.particle;

import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.level.Level;

public class PortalParticle extends Particle {
   private float oSize;
   private double xStart;
   private double yStart;
   private double zStart;

   public PortalParticle(Level level, double x, double y, double z, double xd, double yd, double zd) {
      super(level, x, y, z, xd, yd, zd);
      this.xd = xd;
      this.yd = yd;
      this.zd = zd;
      this.xStart = this.x = x;
      this.yStart = this.y = y;
      this.zStart = this.z = z;
      float br = this.random.nextFloat() * 0.6F + 0.4F;
      this.oSize = this.size = this.random.nextFloat() * 0.2F + 0.5F;
      this.rCol = this.gCol = this.bCol = 1.0F * br;
      this.gCol *= 0.3F;
      this.rCol *= 0.9F;
      this.lifetime = (int)(Math.random() * (double)10.0F) + 40;
      this.noPhysics = true;
      this.tex = (int)(Math.random() * (double)8.0F);
   }

   public void render(Tesselator t, float a, float xa, float ya, float za, float xa2, float za2) {
      float s = ((float)this.age + a) / (float)this.lifetime;
      s = 1.0F - s;
      s *= s;
      s = 1.0F - s;
      this.size = this.oSize * s;
      super.render(t, a, xa, ya, za, xa2, za2);
   }

   public float getBrightness(float a) {
      float br = super.getBrightness(a);
      float pos = (float)this.age / (float)this.lifetime;
      pos *= pos;
      pos *= pos;
      return br * (1.0F - pos) + pos;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      float pos = (float)this.age / (float)this.lifetime;
      float a = pos;
      pos = -pos + pos * pos * 2.0F;
      pos = 1.0F - pos;
      this.x = this.xStart + this.xd * (double)pos;
      this.y = this.yStart + this.yd * (double)pos + (double)(1.0F - a);
      this.z = this.zStart + this.zd * (double)pos;
      if (this.age++ >= this.lifetime) {
         this.remove();
      }

   }
}
