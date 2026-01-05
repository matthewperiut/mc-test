package net.minecraft.client.particle;

import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.level.Level;

public class ExplodeParticle extends Particle {
   public ExplodeParticle(Level level, double x, double y, double z, double xa, double ya, double za) {
      super(level, x, y, z, xa, ya, za);
      this.xd = xa + (double)((float)(Math.random() * (double)2.0F - (double)1.0F) * 0.05F);
      this.yd = ya + (double)((float)(Math.random() * (double)2.0F - (double)1.0F) * 0.05F);
      this.zd = za + (double)((float)(Math.random() * (double)2.0F - (double)1.0F) * 0.05F);
      this.rCol = this.gCol = this.bCol = this.random.nextFloat() * 0.3F + 0.7F;
      this.size = this.random.nextFloat() * this.random.nextFloat() * 6.0F + 1.0F;
      this.lifetime = (int)((double)16.0F / ((double)this.random.nextFloat() * 0.8 + 0.2)) + 2;
   }

   public void render(Tesselator t, float a, float xa, float ya, float za, float xa2, float za2) {
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
      this.yd += 0.004;
      this.move(this.xd, this.yd, this.zd);
      this.xd *= (double)0.9F;
      this.yd *= (double)0.9F;
      this.zd *= (double)0.9F;
      if (this.onGround) {
         this.xd *= (double)0.7F;
         this.zd *= (double)0.7F;
      }

   }
}
