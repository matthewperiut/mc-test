package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import org.lwjgl.opengl.GL11;

public class FireballRenderer extends EntityRenderer<Fireball> {
   public FireballRenderer() {
   }

   public void render(Fireball snowball, double x, double y, double z, float rot, float a) {
      GL11.glPushMatrix();
      GL11.glTranslatef((float)x, (float)y, (float)z);
      GL11.glEnable(32826);
      float s = 2.0F;
      GL11.glScalef(s / 1.0F, s / 1.0F, s / 1.0F);
      int icon = Item.snowBall.getIcon((ItemInstance)null);
      this.bindTexture("/gui/items.png");
      Tesselator t = Tesselator.instance;
      float u0 = (float)(icon % 16 * 16 + 0) / 256.0F;
      float u1 = (float)(icon % 16 * 16 + 16) / 256.0F;
      float v0 = (float)(icon / 16 * 16 + 0) / 256.0F;
      float v1 = (float)(icon / 16 * 16 + 16) / 256.0F;
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
