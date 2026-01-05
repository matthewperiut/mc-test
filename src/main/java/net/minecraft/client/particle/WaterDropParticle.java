package net.minecraft.client.particle;

import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.LiquidTile;
import util.Mth;

public class WaterDropParticle extends Particle {
   public WaterDropParticle(Level level, double x, double y, double z) {
      super(level, x, y, z, (double)0.0F, (double)0.0F, (double)0.0F);
      this.xd *= (double)0.3F;
      this.yd = (double)((float)Math.random() * 0.2F + 0.1F);
      this.zd *= (double)0.3F;
      this.rCol = 1.0F;
      this.gCol = 1.0F;
      this.bCol = 1.0F;
      this.tex = 19 + this.random.nextInt(4);
      this.setSize(0.01F, 0.01F);
      this.gravity = 0.06F;
      this.lifetime = (int)((double)8.0F / (Math.random() * 0.8 + 0.2));
   }

   public void render(Tesselator t, float a, float xa, float ya, float za, float xa2, float za2) {
      super.render(t, a, xa, ya, za, xa2, za2);
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.yd -= (double)this.gravity;
      this.move(this.xd, this.yd, this.zd);
      this.xd *= (double)0.98F;
      this.yd *= (double)0.98F;
      this.zd *= (double)0.98F;
      if (this.lifetime-- <= 0) {
         this.remove();
      }

      if (this.onGround) {
         if (Math.random() < (double)0.5F) {
            this.remove();
         }

         this.xd *= (double)0.7F;
         this.zd *= (double)0.7F;
      }

      Material m = this.level.getMaterial(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z));
      if (m.isLiquid() || m.isSolid()) {
         double y0 = (double)((float)(Mth.floor(this.y) + 1) - LiquidTile.getHeight(this.level.getData(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z))));
         if (this.y < y0) {
            this.remove();
         }
      }

   }
}
