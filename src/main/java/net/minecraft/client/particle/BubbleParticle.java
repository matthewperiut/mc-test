package net.minecraft.client.particle;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import util.Mth;

public class BubbleParticle extends Particle {
   public BubbleParticle(Level level, double x, double y, double z, double xa, double ya, double za) {
      super(level, x, y, z, xa, ya, za);
      this.rCol = 1.0F;
      this.gCol = 1.0F;
      this.bCol = 1.0F;
      this.tex = 32;
      this.setSize(0.02F, 0.02F);
      this.size *= this.random.nextFloat() * 0.6F + 0.2F;
      this.xd = xa * (double)0.2F + (double)((float)(Math.random() * (double)2.0F - (double)1.0F) * 0.02F);
      this.yd = ya * (double)0.2F + (double)((float)(Math.random() * (double)2.0F - (double)1.0F) * 0.02F);
      this.zd = za * (double)0.2F + (double)((float)(Math.random() * (double)2.0F - (double)1.0F) * 0.02F);
      this.lifetime = (int)((double)8.0F / (Math.random() * 0.8 + 0.2));
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.yd += 0.002;
      this.move(this.xd, this.yd, this.zd);
      this.xd *= (double)0.85F;
      this.yd *= (double)0.85F;
      this.zd *= (double)0.85F;
      if (this.level.getMaterial(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z)) != Material.water) {
         this.remove();
      }

      if (this.lifetime-- <= 0) {
         this.remove();
      }

   }
}
