package net.minecraft.client.gui;

import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.opengl.GL11;

public class GuiComponent {
   protected float blitOffset = 0.0F;

   public GuiComponent() {
   }

   protected void fill(int x0, int y0, int x1, int y1, int col) {
      float a = (float)(col >> 24 & 255) / 255.0F;
      float r = (float)(col >> 16 & 255) / 255.0F;
      float g = (float)(col >> 8 & 255) / 255.0F;
      float b = (float)(col & 255) / 255.0F;
      Tesselator t = Tesselator.instance;
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glBlendFunc(770, 771);
      GL11.glColor4f(r, g, b, a);
      t.begin();
      t.vertex((double)x0, (double)y1, (double)0.0F);
      t.vertex((double)x1, (double)y1, (double)0.0F);
      t.vertex((double)x1, (double)y0, (double)0.0F);
      t.vertex((double)x0, (double)y0, (double)0.0F);
      t.end();
      GL11.glEnable(3553);
      GL11.glDisable(3042);
   }

   protected void fillGradient(int x0, int y0, int x1, int y1, int col1, int col2) {
      float a1 = (float)(col1 >> 24 & 255) / 255.0F;
      float r1 = (float)(col1 >> 16 & 255) / 255.0F;
      float g1 = (float)(col1 >> 8 & 255) / 255.0F;
      float b1 = (float)(col1 & 255) / 255.0F;
      float a2 = (float)(col2 >> 24 & 255) / 255.0F;
      float r2 = (float)(col2 >> 16 & 255) / 255.0F;
      float g2 = (float)(col2 >> 8 & 255) / 255.0F;
      float b2 = (float)(col2 & 255) / 255.0F;
      GL11.glDisable(3553);
      GL11.glEnable(3042);
      GL11.glDisable(3008);
      GL11.glBlendFunc(770, 771);
      GL11.glShadeModel(7425);
      Tesselator t = Tesselator.instance;
      t.begin();
      t.color(r1, g1, b1, a1);
      t.vertex((double)x1, (double)y0, (double)0.0F);
      t.vertex((double)x0, (double)y0, (double)0.0F);
      t.color(r2, g2, b2, a2);
      t.vertex((double)x0, (double)y1, (double)0.0F);
      t.vertex((double)x1, (double)y1, (double)0.0F);
      t.end();
      GL11.glShadeModel(7424);
      GL11.glDisable(3042);
      GL11.glEnable(3008);
      GL11.glEnable(3553);
   }

   public void drawCenteredString(Font font, String str, int x, int y, int color) {
      font.drawShadow(str, x - font.width(str) / 2, y, color);
   }

   public void drawString(Font font, String str, int x, int y, int color) {
      font.drawShadow(str, x, y, color);
   }

   public void blit(int x, int y, int sx, int sy, int w, int h) {
      float us = 0.00390625F;
      float vs = 0.00390625F;
      Tesselator t = Tesselator.instance;
      t.begin();
      t.vertexUV((double)(x + 0), (double)(y + h), (double)this.blitOffset, (double)((float)(sx + 0) * us), (double)((float)(sy + h) * vs));
      t.vertexUV((double)(x + w), (double)(y + h), (double)this.blitOffset, (double)((float)(sx + w) * us), (double)((float)(sy + h) * vs));
      t.vertexUV((double)(x + w), (double)(y + 0), (double)this.blitOffset, (double)((float)(sx + w) * us), (double)((float)(sy + 0) * vs));
      t.vertexUV((double)(x + 0), (double)(y + 0), (double)this.blitOffset, (double)((float)(sx + 0) * us), (double)((float)(sy + 0) * vs));
      t.end();
   }
}
