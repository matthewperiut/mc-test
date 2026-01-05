package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.entity.projectile.Arrow;
import org.lwjgl.opengl.GL11;
import util.Mth;

public class ArrowRenderer extends EntityRenderer<Arrow> {
   public ArrowRenderer() {
   }

   public void render(Arrow arrow, double x, double y, double z, float rot, float a) {
      this.bindTexture("/item/arrows.png");
      GL11.glPushMatrix();
      GL11.glTranslatef((float)x, (float)y, (float)z);
      GL11.glRotatef(arrow.yRotO + (arrow.yRot - arrow.yRotO) * a - 90.0F, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(arrow.xRotO + (arrow.xRot - arrow.xRotO) * a, 0.0F, 0.0F, 1.0F);
      Tesselator t = Tesselator.instance;
      int type = 0;
      float u0 = 0.0F;
      float u1 = 0.5F;
      float v0 = (float)(0 + type * 10) / 32.0F;
      float v1 = (float)(5 + type * 10) / 32.0F;
      float u02 = 0.0F;
      float u12 = 0.15625F;
      float v02 = (float)(5 + type * 10) / 32.0F;
      float v12 = (float)(10 + type * 10) / 32.0F;
      float ss = 0.05625F;
      GL11.glEnable(32826);
      float shake = (float)arrow.shakeTime - a;
      if (shake > 0.0F) {
         float pow = -Mth.sin(shake * 3.0F) * shake;
         GL11.glRotatef(pow, 0.0F, 0.0F, 1.0F);
      }

      GL11.glRotatef(45.0F, 1.0F, 0.0F, 0.0F);
      GL11.glScalef(ss, ss, ss);
      GL11.glTranslatef(-4.0F, 0.0F, 0.0F);
      GL11.glNormal3f(ss, 0.0F, 0.0F);
      t.begin();
      t.vertexUV((double)-7.0F, (double)-2.0F, (double)-2.0F, (double)u02, (double)v02);
      t.vertexUV((double)-7.0F, (double)-2.0F, (double)2.0F, (double)u12, (double)v02);
      t.vertexUV((double)-7.0F, (double)2.0F, (double)2.0F, (double)u12, (double)v12);
      t.vertexUV((double)-7.0F, (double)2.0F, (double)-2.0F, (double)u02, (double)v12);
      t.end();
      GL11.glNormal3f(-ss, 0.0F, 0.0F);
      t.begin();
      t.vertexUV((double)-7.0F, (double)2.0F, (double)-2.0F, (double)u02, (double)v02);
      t.vertexUV((double)-7.0F, (double)2.0F, (double)2.0F, (double)u12, (double)v02);
      t.vertexUV((double)-7.0F, (double)-2.0F, (double)2.0F, (double)u12, (double)v12);
      t.vertexUV((double)-7.0F, (double)-2.0F, (double)-2.0F, (double)u02, (double)v12);
      t.end();

      for(int i = 0; i < 4; ++i) {
         GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
         GL11.glNormal3f(0.0F, 0.0F, ss);
         t.begin();
         t.vertexUV((double)-8.0F, (double)-2.0F, (double)0.0F, (double)u0, (double)v0);
         t.vertexUV((double)8.0F, (double)-2.0F, (double)0.0F, (double)u1, (double)v0);
         t.vertexUV((double)8.0F, (double)2.0F, (double)0.0F, (double)u1, (double)v1);
         t.vertexUV((double)-8.0F, (double)2.0F, (double)0.0F, (double)u0, (double)v1);
         t.end();
      }

      GL11.glDisable(32826);
      GL11.glPopMatrix();
   }
}
