package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.Model;
import net.minecraft.world.entity.animal.Chicken;
import util.Mth;

public class ChickenRenderer extends MobRenderer<Chicken> {
   public ChickenRenderer(Model model, float shadow) {
      super(model, shadow);
   }

   public void render(Chicken mob, double x, double y, double z, float rot, float a) {
      super.render(mob, x, y, z, rot, a);
   }

   protected float getBob(Chicken mob, float a) {
      float flap = mob.oFlap + (mob.flap - mob.oFlap) * a;
      float flapSpeed = mob.oFlapSpeed + (mob.flapSpeed - mob.oFlapSpeed) * a;
      return (Mth.sin(flap) + 1.0F) * flapSpeed;
   }
}
