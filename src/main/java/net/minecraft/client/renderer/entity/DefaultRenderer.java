package net.minecraft.client.renderer.entity;

import net.minecraft.world.entity.Entity;
import org.lwjgl.opengl.GL11;

public class DefaultRenderer extends EntityRenderer<Entity> {
   public DefaultRenderer() {
   }

   public void render(Entity entity, double x, double y, double z, float rot, float a) {
      GL11.glPushMatrix();
      render(entity.bb, x - entity.xOld, y - entity.yOld, z - entity.zOld);
      GL11.glPopMatrix();
   }
}
