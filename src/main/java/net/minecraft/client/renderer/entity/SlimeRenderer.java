package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.Model;
import net.minecraft.world.entity.monster.Slime;
import org.lwjgl.opengl.GL11;

public class SlimeRenderer extends MobRenderer<Slime> {
   private Model armor;

   public SlimeRenderer(Model model, Model armor, float shadow) {
      super(model, shadow);
      this.armor = armor;
   }

   protected boolean prepareArmor(Slime slime, int layer, float a) {
      if (layer == 0) {
         this.setArmor(this.armor);
         GL11.glEnable(2977);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         return true;
      } else {
         if (layer == 1) {
            GL11.glDisable(3042);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         }

         return false;
      }
   }

   protected void scale(Slime slime, float a) {
      float ss = (slime.oSquish + (slime.squish - slime.oSquish) * a) / ((float)slime.size * 0.5F + 1.0F);
      float w = 1.0F / (ss + 1.0F);
      float s = (float)slime.size;
      GL11.glScalef(w * s, 1.0F / w * s, w * s);
   }
}
