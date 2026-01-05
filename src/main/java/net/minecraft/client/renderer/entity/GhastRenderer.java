package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.GhastModel;
import net.minecraft.world.entity.monster.Ghast;
import org.lwjgl.opengl.GL11;

public class GhastRenderer extends MobRenderer<Ghast> {
   public GhastRenderer() {
      super(new GhastModel(), 0.5F);
   }

   protected void scale(Ghast mob, float a) {
      float ss = ((float)mob.oCharge + (float)(mob.charge - mob.oCharge) * a) / 20.0F;
      if (ss < 0.0F) {
         ss = 0.0F;
      }

      ss = 1.0F / (ss * ss * ss * ss * ss * 2.0F + 1.0F);
      float s = (8.0F + ss) / 2.0F;
      float hs = (8.0F + 1.0F / ss) / 2.0F;
      GL11.glScalef(hs, s, hs);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
   }
}
