package net.minecraft.client.particle;

import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.level.Level;
import util.Mth;

public class NoteParticle extends Particle {
   float oSize;

   public NoteParticle(Level level, double x, double y, double z, double xa, double ya, double za) {
      this(level, x, y, z, xa, ya, za, 2.0F);
   }

   public NoteParticle(Level level, double x, double y, double z, double xa, double ya, double za, float scale) {
      super(level, x, y, z, (double)0.0F, (double)0.0F, (double)0.0F);
      this.xd *= (double)0.01F;
      this.yd *= (double)0.01F;
      this.zd *= (double)0.01F;
      this.yd += 0.2;
      this.rCol = Mth.sin(((float)xa + 0.0F) * (float)Math.PI * 2.0F) * 0.65F + 0.35F;
      this.gCol = Mth.sin(((float)xa + 0.33333334F) * (float)Math.PI * 2.0F) * 0.65F + 0.35F;
      this.bCol = Mth.sin(((float)xa + 0.6666667F) * (float)Math.PI * 2.0F) * 0.65F + 0.35F;
      this.size *= 0.75F;
      this.size *= scale;
      this.oSize = this.size;
      this.lifetime = 6;
      this.noPhysics = false;
      this.tex = 64;
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

      this.move(this.xd, this.yd, this.zd);
      if (this.y == this.yo) {
         this.xd *= 1.1;
         this.zd *= 1.1;
      }

      this.xd *= (double)0.66F;
      this.yd *= (double)0.66F;
      this.zd *= (double)0.66F;
      if (this.onGround) {
         this.xd *= (double)0.7F;
         this.zd *= (double)0.7F;
      }

   }
}
