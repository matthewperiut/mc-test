package net.minecraft.client.gui;

public class ScreenSizeCalculator {
   private int w;
   private int h;
   public int scale;

   public ScreenSizeCalculator(int width, int height) {
      this.w = width;
      this.h = height;

      for(this.scale = 1; this.w / (this.scale + 1) >= 320 && this.h / (this.scale + 1) >= 240; ++this.scale) {
      }

      this.w /= this.scale;
      this.h /= this.scale;
   }

   public int getWidth() {
      return this.w;
   }

   public int getHeight() {
      return this.h;
   }
}
