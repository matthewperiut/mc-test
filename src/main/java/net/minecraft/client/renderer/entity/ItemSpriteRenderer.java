package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.entity.Entity;
import org.lwjgl.opengl.GL11;

public class ItemSpriteRenderer extends EntityRenderer<Entity> {
   private int icon;

   public ItemSpriteRenderer(int icon) {
      this.icon = icon;
   }

   public void render(Entity e, double x, double y, double z, float rot, float a) {
      GL11.glPushMatrix();
      GL11.glTranslatef((float)x, (float)y, (float)z);
      GL11.glEnable(32826);
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      this.bindTexture("/gui/items.png");
      Tesselator t = Tesselator.instance;
      float u0 = (float)(this.icon % 16 * 16 + 0) / 256.0F;
      float u1 = (float)(this.icon % 16 * 16 + 16) / 256.0F;
      float v0 = (float)(this.icon / 16 * 16 + 0) / 256.0F;
      float v1 = (float)(this.icon / 16 * 16 + 16) / 256.0F;
      float r = 1.0F;
      float xo = 0.5F;
      float yo = 0.25F;
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
   }
}
