package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.Model;
import net.minecraft.world.entity.animal.Sheep;
import org.lwjgl.opengl.GL11;

public class SheepRenderer extends MobRenderer<Sheep> {
   public SheepRenderer(Model model, Model armor, float shadow) {
      super(model, shadow);
      this.setArmor(armor);
   }

   protected boolean prepareArmor(Sheep sheep, int layer, float a) {
      if (layer == 0 && !sheep.isSheared()) {
         this.bindTexture("/mob/sheep_fur.png");
         float brightness = sheep.getBrightness(a);
         int color = sheep.getColor();
         GL11.glColor3f(brightness * Sheep.COLOR[color][0], brightness * Sheep.COLOR[color][1], brightness * Sheep.COLOR[color][2]);
         return true;
      } else {
         return false;
      }
   }
}
