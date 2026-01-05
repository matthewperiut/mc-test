package net.minecraft.client.particle;

import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.level.Level;

public class LavaParticle extends Particle {
   private float oSize;

   public LavaParticle(Level level, double x, double y, double z) {
      super(level, x, y, z, (double)0.0F, (double)0.0F, (double)0.0F);
      this.xd *= (double)0.8F;
      this.yd *= (double)0.8F;
      this.zd *= (double)0.8F;
      this.yd = (double)(this.random.nextFloat() * 0.4F + 0.05F);
      this.rCol = this.gCol = this.bCol = 1.0F;
      this.size *= this.random.nextFloat() * 2.0F + 0.2F;
      this.oSize = this.size;
      this.lifetime = (int)((double)16.0F / (Math.random() * 0.8 + 0.2));
      this.noPhysics = false;
      this.tex = 49;
   }

   public float getBrightness(float a) {
      return 1.0F;
   }

   public void render(Tesselator t, float a, float xa, float ya, float za, float xa2, float za2) {
      float s = ((float)this.age + a) / (float)this.lifetime;
      this.size = this.oSize * (1.0F - s * s);
      super.render(t, a, xa, ya, za, xa2, za2);
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      }

      float odds = (float)this.age / (float)this.lifetime;
      if (this.random.nextFloat() > odds) {
         this.level.addParticle("smoke", this.x, this.y, this.z, this.xd, this.yd, this.zd);
      }

      this.yd -= 0.03;
      this.move(this.xd, this.yd, this.zd);
      this.xd *= (double)0.999F;
      this.yd *= (double)0.999F;
      this.zd *= (double)0.999F;
      if (this.onGround) {
         this.xd *= (double)0.7F;
         this.zd *= (double)0.7F;
      }

   }
}
