package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.Model;
import net.minecraft.world.entity.monster.Giant;
import org.lwjgl.opengl.GL11;

public class GiantMobRenderer extends MobRenderer<Giant> {
   private float scale;

   public GiantMobRenderer(Model model, float shadow, float scale) {
      super(model, shadow * scale);
      this.scale = scale;
   }

   protected void scale(Giant mob, float a) {
      GL11.glScalef(this.scale, this.scale, this.scale);
   }
}
