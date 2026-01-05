package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.entity.projectile.FishingHook;
import org.lwjgl.opengl.GL11;
import util.Mth;

public class FishingHookRenderer extends EntityRenderer<FishingHook> {
   public FishingHookRenderer() {
   }

   public void render(FishingHook hook, double x, double y, double z, float rot, float a) {
      GL11.glPushMatrix();
      GL11.glTranslatef((float)x, (float)y, (float)z);
      GL11.glEnable(32826);
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      int xi = 1;
      int yi = 2;
      this.bindTexture("/particles.png");
      Tesselator t = Tesselator.instance;
      float u0 = (float)(xi * 8 + 0) / 128.0F;
      float u1 = (float)(xi * 8 + 8) / 128.0F;
      float v0 = (float)(yi * 8 + 0) / 128.0F;
      float v1 = (float)(yi * 8 + 8) / 128.0F;
      float r = 1.0F;
      float xo = 0.5F;
      float yo = 0.5F;
      GL11.glRotatef(180.0F - this.entityRenderDispatcher.playerRotY, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(-this.entityRenderDispatcher.playerRotX, 1.0F, 0.0F, 0.0F);
      t.begin();
      t.normal(0.0F, 1.0F, 0.0F);
      t.vertexUV((double)(0.0F - xo), (double)(0.0F - yo), (double)0.0F, (double)u0, (double)v1);
      t.vertexUV((double)(r - xo), (double)(0.0F - yo), (double)0.0F, (double)u1, (double)v1);
      t.vertexUV((double)(r - xo), (double)(1.0F - yo), (double)0.0F, (double)u1, (double)v0);
      t.vertexUV((double)(0.0F - xo), (double)(1.0F - yo), (double)0.0F, (double)u0, (double)v0);
      t.end();
      GL11.glDisable(32826);
      GL11.glPopMatrix();
      if (hook.owner != null) {
         float rr = (hook.owner.yRotO + (hook.owner.yRot - hook.owner.yRotO) * a) * (float)Math.PI / 180.0F;
         float rr2 = (hook.owner.xRotO + (hook.owner.xRot - hook.owner.xRotO) * a) * (float)Math.PI / 180.0F;
         double ss = (double)Mth.sin(rr);
         double cc = (double)Mth.cos(rr);
         double ss2 = (double)Mth.sin(rr2);
         double cc2 = (double)Mth.cos(rr2);
         double xp = hook.owner.xo + (hook.owner.x - hook.owner.xo) * (double)a - cc * 0.7 - ss * (double)0.5F * cc2;
         double yp = hook.owner.yo + (hook.owner.y - hook.owner.yo) * (double)a - ss2 * (double)0.5F;
         double zp = hook.owner.zo + (hook.owner.z - hook.owner.zo) * (double)a - ss * 0.7 + cc * (double)0.5F * cc2;
         if (this.entityRenderDispatcher.options.thirdPersonView) {
            rr = (hook.owner.yBodyRotO + (hook.owner.yBodyRot - hook.owner.yBodyRotO) * a) * (float)Math.PI / 180.0F;
            ss = (double)Mth.sin(rr);
            cc = (double)Mth.cos(rr);
            xp = hook.owner.xo + (hook.owner.x - hook.owner.xo) * (double)a - cc * 0.35 - ss * 0.85;
            yp = hook.owner.yo + (hook.owner.y - hook.owner.yo) * (double)a - 0.45;
            zp = hook.owner.zo + (hook.owner.z - hook.owner.zo) * (double)a - ss * 0.35 + cc * 0.85;
         }

         double xh = hook.xo + (hook.x - hook.xo) * (double)a;
         double yh = hook.yo + (hook.y - hook.yo) * (double)a + (double)0.25F;
         double zh = hook.zo + (hook.z - hook.zo) * (double)a;
         double xa = (double)((float)(xp - xh));
         double ya = (double)((float)(yp - yh));
         double za = (double)((float)(zp - zh));
         GL11.glDisable(3553);
         GL11.glDisable(2896);
         t.begin(3);
         t.color(0);
         int steps = 16;

         for(int i = 0; i <= steps; ++i) {
            float aa = (float)i / (float)steps;
            t.vertex(x + xa * (double)aa, y + ya * (double)(aa * aa + aa) * (double)0.5F + (double)0.25F, z + za * (double)aa);
         }

         t.end();
         GL11.glEnable(2896);
         GL11.glEnable(3553);
      }

   }
}
