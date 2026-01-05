package net.minecraft.client.skins;

import java.io.File;
import java.util.List;
import net.minecraft.client.gui.Button;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.SmallButton;
import net.minecraft.client.locale.Language;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class TexturePackSelectScreen extends Screen {
   protected Screen lastScreen;
   private int yo = 0;
   private int y0 = 32;
   private int y1;
   private int x0;
   private int x1;
   private int yDrag;
   private int updateIn;
   private String instructions;

   public TexturePackSelectScreen(Screen lastScreen) {
      this.y1 = this.height - 55 + 4;
      this.x0 = 0;
      this.x1 = this.width;
      this.yDrag = -2;
      this.updateIn = -1;
      this.instructions = "";
      this.lastScreen = lastScreen;
   }

   public void init() {
      Language language = Language.getInstance();
      this.buttons.add(new SmallButton(5, this.width / 2 - 154, this.height - 48, language.getElement("texturePack.openFolder")));
      this.buttons.add(new SmallButton(6, this.width / 2 + 4, this.height - 48, language.getElement("gui.done")));
      this.minecraft.skins.updateList();
      this.instructions = (new File(this.minecraft.workingDirectory, "texturepacks")).getAbsolutePath();
      this.y0 = 32;
      this.y1 = this.height - 58 + 4;
      this.x0 = 0;
      this.x1 = this.width;
   }

   protected void buttonClicked(Button button) {
      if (button.active) {
         if (button.id == 5) {
            Sys.openURL("file://" + this.instructions);
         }

         if (button.id == 6) {
            this.minecraft.textures.reloadAll();
            this.minecraft.setScreen(this.lastScreen);
         }

      }
   }

   protected void mouseClicked(int x, int y, int buttonNum) {
      super.mouseClicked(x, y, buttonNum);
   }

   protected void mouseReleased(int x, int y, int buttonNum) {
      super.mouseReleased(x, y, buttonNum);
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      if (this.updateIn <= 0) {
         this.minecraft.skins.updateList();
         this.updateIn += 20;
      }

      List<TexturePack> skins = this.minecraft.skins.getAll();
      if (Mouse.isButtonDown(0)) {
         if (this.yDrag == -1) {
            if (ym >= this.y0 && ym <= this.y1) {
               int x0 = this.width / 2 - 110;
               int x1 = this.width / 2 + 110;
               int slot = (ym - this.y0 + this.yo - 2) / 36;
               if (xm >= x0 && xm <= x1 && slot >= 0 && slot < skins.size() && this.minecraft.skins.selectSkin((TexturePack)skins.get(slot))) {
                  this.minecraft.textures.reloadAll();
               }

               this.yDrag = ym;
            } else {
               this.yDrag = -2;
            }
         } else if (this.yDrag >= 0) {
            this.yo -= ym - this.yDrag;
            this.yDrag = ym;
         }
      } else {
         if (this.yDrag >= 0) {
         }

         this.yDrag = -1;
      }

      int max = skins.size() * 36 - (this.y1 - this.y0 - 4);
      if (max < 0) {
         max /= 2;
      }

      if (this.yo < 0) {
         this.yo = 0;
      }

      if (this.yo > max) {
         this.yo = max;
      }

      GL11.glDisable(2896);
      GL11.glDisable(2912);
      Tesselator t = Tesselator.instance;
      GL11.glBindTexture(3553, this.minecraft.textures.loadTexture("/gui/background.png"));
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      float s = 32.0F;
      t.begin();
      t.color(2105376);
      t.vertexUV((double)this.x0, (double)this.y1, (double)0.0F, (double)((float)this.x0 / s), (double)((float)(this.y1 + this.yo) / s));
      t.vertexUV((double)this.x1, (double)this.y1, (double)0.0F, (double)((float)this.x1 / s), (double)((float)(this.y1 + this.yo) / s));
      t.vertexUV((double)this.x1, (double)this.y0, (double)0.0F, (double)((float)this.x1 / s), (double)((float)(this.y0 + this.yo) / s));
      t.vertexUV((double)this.x0, (double)this.y0, (double)0.0F, (double)((float)this.x0 / s), (double)((float)(this.y0 + this.yo) / s));
      t.end();

      for(int i = 0; i < skins.size(); ++i) {
         TexturePack skin = (TexturePack)skins.get(i);
         int x = this.width / 2 - 92 - 16;
         int y = 36 + i * 36 - this.yo;
         int h = 32;
         int w = 32;
         if (skin == this.minecraft.skins.selected) {
            int x0 = this.width / 2 - 110;
            int x1 = this.width / 2 + 110;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(3553);
            t.begin();
            t.color(8421504);
            t.vertexUV((double)x0, (double)(y + h + 2), (double)0.0F, (double)0.0F, (double)1.0F);
            t.vertexUV((double)x1, (double)(y + h + 2), (double)0.0F, (double)1.0F, (double)1.0F);
            t.vertexUV((double)x1, (double)(y - 2), (double)0.0F, (double)1.0F, (double)0.0F);
            t.vertexUV((double)x0, (double)(y - 2), (double)0.0F, (double)0.0F, (double)0.0F);
            t.color(0);
            t.vertexUV((double)(x0 + 1), (double)(y + h + 1), (double)0.0F, (double)0.0F, (double)1.0F);
            t.vertexUV((double)(x1 - 1), (double)(y + h + 1), (double)0.0F, (double)1.0F, (double)1.0F);
            t.vertexUV((double)(x1 - 1), (double)(y - 1), (double)0.0F, (double)1.0F, (double)0.0F);
            t.vertexUV((double)(x0 + 1), (double)(y - 1), (double)0.0F, (double)0.0F, (double)0.0F);
            t.end();
            GL11.glEnable(3553);
         }

         skin.bindTexture(this.minecraft);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         t.begin();
         t.color(16777215);
         t.vertexUV((double)x, (double)(y + h), (double)0.0F, (double)0.0F, (double)1.0F);
         t.vertexUV((double)(x + w), (double)(y + h), (double)0.0F, (double)1.0F, (double)1.0F);
         t.vertexUV((double)(x + w), (double)y, (double)0.0F, (double)1.0F, (double)0.0F);
         t.vertexUV((double)x, (double)y, (double)0.0F, (double)0.0F, (double)0.0F);
         t.end();
         this.drawString(this.font, skin.name, x + w + 2, y + 1, 16777215);
         this.drawString(this.font, skin.desc1, x + w + 2, y + 12, 8421504);
         this.drawString(this.font, skin.desc2, x + w + 2, y + 12 + 10, 8421504);
      }

      int d = 4;
      this.renderHoleBackground(0, this.y0, 255, 255);
      this.renderHoleBackground(this.y1, this.height, 255, 255);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glDisable(3008);
      GL11.glShadeModel(7425);
      GL11.glDisable(3553);
      t.begin();
      t.color(0, 0);
      t.vertexUV((double)this.x0, (double)(this.y0 + d), (double)0.0F, (double)0.0F, (double)1.0F);
      t.vertexUV((double)this.x1, (double)(this.y0 + d), (double)0.0F, (double)1.0F, (double)1.0F);
      t.color(0, 255);
      t.vertexUV((double)this.x1, (double)this.y0, (double)0.0F, (double)1.0F, (double)0.0F);
      t.vertexUV((double)this.x0, (double)this.y0, (double)0.0F, (double)0.0F, (double)0.0F);
      t.end();
      t.begin();
      t.color(0, 255);
      t.vertexUV((double)this.x0, (double)this.y1, (double)0.0F, (double)0.0F, (double)1.0F);
      t.vertexUV((double)this.x1, (double)this.y1, (double)0.0F, (double)1.0F, (double)1.0F);
      t.color(0, 0);
      t.vertexUV((double)this.x1, (double)(this.y1 - d), (double)0.0F, (double)1.0F, (double)0.0F);
      t.vertexUV((double)this.x0, (double)(this.y1 - d), (double)0.0F, (double)0.0F, (double)0.0F);
      t.end();
      GL11.glEnable(3553);
      GL11.glShadeModel(7424);
      GL11.glEnable(3008);
      GL11.glDisable(3042);
      Language language = Language.getInstance();
      this.drawCenteredString(this.font, language.getElement("texturePack.title"), this.width / 2, 16, 16777215);
      this.drawCenteredString(this.font, language.getElement("texturePack.folderInfo"), this.width / 2 - 77, this.height - 26, 8421504);
      super.render(xm, ym, a);
   }

   public void tick() {
      super.tick();
      --this.updateIn;
   }

   public void renderHoleBackground(int y0, int y1, int a0, int a1) {
      Tesselator t = Tesselator.instance;
      GL11.glBindTexture(3553, this.minecraft.textures.loadTexture("/gui/background.png"));
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      float s = 32.0F;
      t.begin();
      t.color(4210752, a1);
      t.vertexUV((double)0.0F, (double)y1, (double)0.0F, (double)0.0F, (double)((float)y1 / s));
      t.vertexUV((double)this.width, (double)y1, (double)0.0F, (double)((float)this.width / s), (double)((float)y1 / s));
      t.color(4210752, a0);
      t.vertexUV((double)this.width, (double)y0, (double)0.0F, (double)((float)this.width / s), (double)((float)y0 / s));
      t.vertexUV((double)0.0F, (double)y0, (double)0.0F, (double)0.0F, (double)((float)y0 / s));
      t.end();
   }
}
