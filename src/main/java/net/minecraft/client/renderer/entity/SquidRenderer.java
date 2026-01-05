package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.Model;
import net.minecraft.world.entity.animal.Squid;
import org.lwjgl.opengl.GL11;

public class SquidRenderer extends MobRenderer<Squid> {
   public SquidRenderer(Model model, float shadow) {
      super(model, shadow);
   }

   public void render(Squid mob, double x, double y, double z, float rot, float a) {
      super.render(mob, x, y, z, rot, a);
   }

   protected void setupRotations(Squid mob, float bob, float bodyRot, float a) {
      float bodyXRot = mob.xBodyRotO + (mob.xBodyRot - mob.xBodyRotO) * a;
      float bodyZRot = mob.zBodyRotO + (mob.zBodyRot - mob.zBodyRotO) * a;
      GL11.glTranslatef(0.0F, 0.5F, 0.0F);
      GL11.glRotatef(180.0F - bodyRot, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(bodyXRot, 1.0F, 0.0F, 0.0F);
      GL11.glRotatef(bodyZRot, 0.0F, 1.0F, 0.0F);
      GL11.glTranslatef(0.0F, -1.2F, 0.0F);
   }

   protected void scale(Squid mob, float a) {
   }

   protected float getBob(Squid mob, float a) {
      float flap = mob.oldTentacleAngle + (mob.tentacleAngle - mob.oldTentacleAngle) * a;
      return flap;
   }
}
