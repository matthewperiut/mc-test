package net.minecraft.client.renderer.ptexture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;

public class CompassTexture extends DynamicTexture {
   private Minecraft mc;
   private int[] raw = new int[256];
   private double rot;
   private double rota;

   public CompassTexture(Minecraft mc) {
      super(Item.compass.getIcon((ItemInstance)null));
      this.mc = mc;
      this.textureId = 1;

      try {
         BufferedImage bi = ImageIO.read(Minecraft.class.getResource("/gui/items.png"));
         int xo = this.tex % 16 * 16;
         int yo = this.tex / 16 * 16;
         bi.getRGB(xo, yo, 16, 16, this.raw, 0, 16);
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   public void tick() {
      for(int i = 0; i < 256; ++i) {
         int a = this.raw[i] >> 24 & 255;
         int r = this.raw[i] >> 16 & 255;
         int g = this.raw[i] >> 8 & 255;
         int b = this.raw[i] >> 0 & 255;
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

      double rott = (double)0.0F;
      if (this.mc.level != null && this.mc.player != null) {
         double xa = (double)this.mc.level.xSpawn - this.mc.player.x;
         double za = (double)this.mc.level.zSpawn - this.mc.player.z;
         rott = (double)(this.mc.player.yRot - 90.0F) * Math.PI / (double)180.0F - Math.atan2(za, xa);
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

      for(int d = -4; d <= 4; ++d) {
         int x = (int)((double)8.5F + cos * (double)d * 0.3);
         int y = (int)((double)7.5F - sin * (double)d * 0.3 * (double)0.5F);
         int i = y * 16 + x;
         int r = 100;
         int g = 100;
         int b = 100;
         int a = 255;
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

      for(int d = -8; d <= 16; ++d) {
         int x = (int)((double)8.5F + sin * (double)d * 0.3);
         int y = (int)((double)7.5F + cos * (double)d * 0.3 * (double)0.5F);
         int i = y * 16 + x;
         int r = d >= 0 ? 255 : 100;
         int g = d >= 0 ? 20 : 100;
         int b = d >= 0 ? 20 : 100;
         int a = 255;
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
