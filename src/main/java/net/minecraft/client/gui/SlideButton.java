package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.lwjgl.opengl.GL11;

public class SlideButton extends Button {
   public float value = 1.0F;
   public boolean sliding = false;
   private Options.Option option = null;

   public SlideButton(int id, int x, int y, Options.Option option, String msg, float value) {
      super(id, x, y, 150, 20, msg);
      this.option = option;
      this.value = value;
   }

   protected int getYImage(boolean hovered) {
      return 0;
   }

   protected void renderBg(Minecraft minecraft, int xm, int ym) {
      if (this.visible) {
         if (this.sliding) {
            this.value = (float)(xm - (this.x + 4)) / (float)(this.w - 8);
            if (this.value < 0.0F) {
               this.value = 0.0F;
            }

            if (this.value > 1.0F) {
               this.value = 1.0F;
            }

            minecraft.options.set(this.option, this.value);
            this.msg = minecraft.options.getMessage(this.option);
         }

         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.blit(this.x + (int)(this.value * (float)(this.w - 8)), this.y, 0, 66, 4, 20);
         this.blit(this.x + (int)(this.value * (float)(this.w - 8)) + 4, this.y, 196, 66, 4, 20);
      }
   }

   public boolean clicked(Minecraft minecraft, int mx, int my) {
      if (super.clicked(minecraft, mx, my)) {
         this.value = (float)(mx - (this.x + 4)) / (float)(this.w - 8);
         if (this.value < 0.0F) {
            this.value = 0.0F;
         }

         if (this.value > 1.0F) {
            this.value = 1.0F;
         }

         minecraft.options.set(this.option, this.value);
         this.msg = minecraft.options.getMessage(this.option);
         this.sliding = true;
         return true;
      } else {
         return false;
      }
   }

   public void released(int mx, int my) {
      this.sliding = false;
   }
}
