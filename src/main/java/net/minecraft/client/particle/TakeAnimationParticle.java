package net.minecraft.client.particle;

import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.lwjgl.opengl.GL11;
import util.Mth;

public class TakeAnimationParticle extends Particle {
   private Entity item;
   private Entity target;
   private int life = 0;
   private int lifeTime = 0;
   private float yOffs;

   public TakeAnimationParticle(Level level, Entity item, Entity target, float yOffs) {
      super(level, item.x, item.y, item.z, item.xd, item.yd, item.zd);
      this.item = item;
      this.target = target;
      this.lifeTime = 3;
      this.yOffs = yOffs;
   }

   public void render(Tesselator t, float a, float xa, float ya, float za, float xa2, float za2) {
      float time = ((float)this.life + a) / (float)this.lifeTime;
      time *= time;
      double xo = this.item.x;
      double yo = this.item.y;
      double zo = this.item.z;
      double xt = this.target.xOld + (this.target.x - this.target.xOld) * (double)a;
      double yt = this.target.yOld + (this.target.y - this.target.yOld) * (double)a + (double)this.yOffs;
      double zt = this.target.zOld + (this.target.z - this.target.zOld) * (double)a;
      double xx = xo + (xt - xo) * (double)time;
      double yy = yo + (yt - yo) * (double)time;
      double zz = zo + (zt - zo) * (double)time;
      int xTile = Mth.floor(xx);
      int yTile = Mth.floor(yy + (double)(this.heightOffset / 2.0F));
      int zTile = Mth.floor(zz);
      float br = this.level.getBrightness(xTile, yTile, zTile);
      xx -= xOff;
      yy -= yOff;
      zz -= zOff;
      GL11.glColor4f(br, br, br, 1.0F);
      EntityRenderDispatcher.instance.render(this.item, (double)((float)xx), (double)((float)yy), (double)((float)zz), this.item.yRot, a);
   }

   public void tick() {
      ++this.life;
      if (this.life == this.lifeTime) {
         this.remove();
      }

   }

   public int getParticleTexture() {
      return 3;
   }
}
