package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.Model;
import net.minecraft.world.entity.animal.Cow;

public class CowRenderer extends MobRenderer<Cow> {
   public CowRenderer(Model model, float shadow) {
      super(model, shadow);
   }

   public void render(Cow mob, double x, double y, double z, float rot, float a) {
      super.render(mob, x, y, z, rot, a);
   }
}
