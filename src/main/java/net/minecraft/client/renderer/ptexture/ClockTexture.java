package net.minecraft.client.renderer.ptexture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;

public class ClockTexture extends DynamicTexture {
   private Minecraft mc;
   private int[] raw = new int[256];
   private int[] dialRaw = new int[256];
   private double rot;
   private double rota;

   public ClockTexture(Minecraft mc) {
      super(Item.clock.getIcon((ItemInstance)null));
      this.mc = mc;
      this.textureId = 1;

      try {
         BufferedImage bi = ImageIO.read(Minecraft.class.getResource("/gui/items.png"));
         int xo = this.tex % 16 * 16;
         int yo = this.tex / 16 * 16;
         bi.getRGB(xo, yo, 16, 16, this.raw, 0, 16);
         bi = ImageIO.read(Minecraft.class.getResource("/misc/dial.png"));
         bi.getRGB(0, 0, 16, 16, this.dialRaw, 0, 16);
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   public void tick() {
      double rott = (double)0.0F;
      if (this.mc.level != null && this.mc.player != null) {
         float time = this.mc.level.getTimeOfDay(1.0F);
         rott = (double)(-time * (float)Math.PI * 2.0F);
         if (this.mc.level.dimension.foggy) {
            rott = Math.random() * (double)(float)Math.PI * (double)2.0F;
         }
      }

      double rotd;
      for(rotd = rott - this.rot; rotd < -Math.PI; rotd += (Math.PI * 2D)) {
      }

      while(rotd >= Math.PI) {
         rotd -= (Math.PI * 2D);
      }

      if (rotd < (double)-1.0F) {
         rotd = (double)-1.0F;
      }

      if (rotd > (double)1.0F) {
         rotd = (double)1.0F;
      }

      this.rota += rotd * 0.1;
      this.rota *= 0.8;
      this.rot += this.rota;
      double sin = Math.sin(this.rot);
      double cos = Math.cos(this.rot);

      for(int i = 0; i < 256; ++i) {
         int a = this.raw[i] >> 24 & 255;
         int r = this.raw[i] >> 16 & 255;
         int g = this.raw[i] >> 8 & 255;
         int b = this.raw[i] >> 0 & 255;
         if (r == b && g == 0 && b > 0) {
            double xo = -((double)(i % 16) / (double)15.0F - (double)0.5F);
            double yo = (double)(i / 16) / (double)15.0F - (double)0.5F;
            int br = r;
            int x = (int)((xo * cos + yo * sin + (double)0.5F) * (double)16.0F);
            int y = (int)((yo * cos - xo * sin + (double)0.5F) * (double)16.0F);
            int j = (x & 15) + (y & 15) * 16;
            a = this.dialRaw[j] >> 24 & 255;
            r = (this.dialRaw[j] >> 16 & 255) * r / 255;
            g = (this.dialRaw[j] >> 8 & 255) * br / 255;
            b = (this.dialRaw[j] >> 0 & 255) * br / 255;
         }

         if (this.anaglyph3d) {
            int rr = (r * 30 + g * 59 + b * 11) / 100;
            int gg = (r * 30 + g * 70) / 100;
            int bb = (r * 30 + b * 70) / 100;
            r = rr;
            g = gg;
            b = bb;
         }

         this.pixels[i * 4 + 0] = (byte)r;
         this.pixels[i * 4 + 1] = (byte)g;
         this.pixels[i * 4 + 2] = (byte)b;
         this.pixels[i * 4 + 3] = (byte)a;
      }

   }
}
