package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class Button extends GuiComponent {
   protected int w;
   protected int h;
   public int x;
   public int y;
   public String msg;
   public int id;
   public boolean active;
   public boolean visible;

   public Button(int id, int x, int y, String msg) {
      this(id, x, y, 200, 20, msg);
   }

   public Button(int id, int x, int y, int w, int h, String msg) {
      this.w = 200;
      this.h = 20;
      this.active = true;
      this.visible = true;
      this.id = id;
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      this.msg = msg;
   }

   protected int getYImage(boolean hovered) {
      int res = 1;
      if (!this.active) {
         res = 0;
      } else if (hovered) {
         res = 2;
      }

      return res;
   }

   public void render(Minecraft minecraft, int xm, int ym) {
      if (this.visible) {
         Font font = minecraft.font;
         GL11.glBindTexture(3553, minecraft.textures.loadTexture("/gui/gui.png"));
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         boolean hovered = xm >= this.x && ym >= this.y && xm < this.x + this.w && ym < this.y + this.h;
         int yImage = this.getYImage(hovered);
         this.blit(this.x, this.y, 0, 46 + yImage * 20, this.w / 2, this.h);
         this.blit(this.x + this.w / 2, this.y, 200 - this.w / 2, 46 + yImage * 20, this.w / 2, this.h);
         this.renderBg(minecraft, xm, ym);
         if (!this.active) {
            this.drawCenteredString(font, this.msg, this.x + this.w / 2, this.y + (this.h - 8) / 2, -6250336);
         } else if (hovered) {
            this.drawCenteredString(font, this.msg, this.x + this.w / 2, this.y + (this.h - 8) / 2, 16777120);
         } else {
            this.drawCenteredString(font, this.msg, this.x + this.w / 2, this.y + (this.h - 8) / 2, 14737632);
         }

      }
   }

   protected void renderBg(Minecraft minecraft, int xm, int ym) {
   }

   public void released(int mx, int my) {
   }

   public boolean clicked(Minecraft minecraft, int mx, int my) {
      return this.active && mx >= this.x && my >= this.y && mx < this.x + this.w && my < this.y + this.h;
   }
}
