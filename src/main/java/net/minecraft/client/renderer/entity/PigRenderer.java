package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.Model;
import net.minecraft.world.entity.animal.Pig;

public class PigRenderer extends MobRenderer<Pig> {
   public PigRenderer(Model model, Model armor, float shadow) {
      super(model, shadow);
      this.setArmor(armor);
   }

   protected boolean prepareArmor(Pig pig, int layer, float a) {
      this.bindTexture("/mob/saddle.png");
      return layer == 0 && pig.hasSaddle();
   }
}
