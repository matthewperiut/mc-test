package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.CreeperModel;
import net.minecraft.world.entity.monster.Creeper;
import org.lwjgl.opengl.GL11;
import util.Mth;

public class CreeperRenderer extends MobRenderer<Creeper> {
   public CreeperRenderer() {
      super(new CreeperModel(), 0.5F);
   }

   protected void scale(Creeper mob, float a) {
      float g = mob.getSwelling(a);
      float wobble = 1.0F + Mth.sin(g * 100.0F) * g * 0.01F;
      if (g < 0.0F) {
         g = 0.0F;
      }

      if (g > 1.0F) {
         g = 1.0F;
      }

      g *= g;
      g *= g;
      float s = (1.0F + g * 0.4F) * wobble;
      float hs = (1.0F + g * 0.1F) / wobble;
      GL11.glScalef(s, hs, s);
   }

   protected int getOverlayColor(Creeper mob, float br, float a) {
      float step = mob.getSwelling(a);
      if ((int)(step * 10.0F) % 2 == 0) {
         return 0;
      } else {
         int _a = (int)(step * 0.2F * 255.0F);
         if (_a < 0) {
            _a = 0;
         }

         if (_a > 255) {
            _a = 255;
         }

         int r = 255;
         int g = 255;
         int b = 255;
         return _a << 24 | r << 16 | g << 8 | b;
      }
   }
}
