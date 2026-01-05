package net.minecraft.client.particle;

import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.level.Level;

public class RedDustParticle extends Particle {
   float oSize;

   public RedDustParticle(Level level, double x, double y, double z) {
      this(level, x, y, z, 1.0F);
   }

   public RedDustParticle(Level level, double x, double y, double z, float scale) {
      super(level, x, y, z, (double)0.0F, (double)0.0F, (double)0.0F);
      this.xd *= (double)0.1F;
      this.yd *= (double)0.1F;
      this.zd *= (double)0.1F;
      this.rCol = (float)(Math.random() * (double)0.3F) + 0.7F;
      this.gCol = this.bCol = (float)(Math.random() * (double)0.1F);
      this.size *= 0.75F;
      this.size *= scale;
      this.oSize = this.size;
      this.lifetime = (int)((double)8.0F / (Math.random() * 0.8 + 0.2));
      this.lifetime = (int)((float)this.lifetime * scale);
      this.noPhysics = false;
   }

   public void render(Tesselator t, float a, float xa, float ya, float za, float xa2, float za2) {
      float l = ((float)this.age + a) / (float)this.lifetime * 32.0F;
      if (l < 0.0F) {
         l = 0.0F;
      }

      if (l > 1.0F) {
         l = 1.0F;
      }

      this.size = this.oSize * l;
      super.render(t, a, xa, ya, za, xa2, za2);
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      }

      this.tex = 7 - this.age * 8 / this.lifetime;
      this.move(this.xd, this.yd, this.zd);
      if (this.y == this.yo) {
         this.xd *= 1.1;
         this.zd *= 1.1;
      }

      this.xd *= (double)0.96F;
      this.yd *= (double)0.96F;
      this.zd *= (double)0.96F;
      if (this.onGround) {
         this.xd *= (double)0.7F;
         this.zd *= (double)0.7F;
      }

   }
}
