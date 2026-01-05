package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SpiderModel;
import net.minecraft.world.entity.monster.Spider;
import org.lwjgl.opengl.GL11;

public class SpiderRenderer extends MobRenderer<Spider> {
   public SpiderRenderer() {
      super(new SpiderModel(), 1.0F);
      this.setArmor(new SpiderModel());
   }

   protected float getFlipDegrees(Spider spider) {
      return 180.0F;
   }

   protected boolean prepareArmor(Spider spider, int layer, float a) {
      if (layer != 0) {
         return false;
      } else if (layer != 0) {
         return false;
      } else {
         this.bindTexture("/mob/spider_eyes.png");
         float br = (1.0F - spider.getBrightness(1.0F)) * 0.5F;
         GL11.glEnable(3042);
         GL11.glDisable(3008);
         GL11.glBlendFunc(770, 771);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, br);
         return true;
      }
   }
}
