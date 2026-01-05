package net.minecraft.client.renderer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;

public class MobSkinTextureProcessor implements HttpTextureProcessor {
   private int[] pixels;
   private int width;
   private int height;

   public MobSkinTextureProcessor() {
   }

   public BufferedImage process(BufferedImage in) {
      if (in == null) {
         return null;
      } else {
         this.width = 64;
         this.height = 32;
         BufferedImage out = new BufferedImage(this.width, this.height, 2);
         Graphics g = out.getGraphics();
         g.drawImage(in, 0, 0, (ImageObserver)null);
         g.dispose();
         this.pixels = ((DataBufferInt)out.getRaster().getDataBuffer()).getData();
         this.setNoAlpha(0, 0, 32, 16);
         this.setForceAlpha(32, 0, 64, 32);
         this.setNoAlpha(0, 16, 64, 32);
         boolean hasAlpha = false;

         for(int x = 32; x < 64; ++x) {
            for(int y = 0; y < 16; ++y) {
               int pix = this.pixels[x + y * 64];
               if ((pix >> 24 & 255) < 128) {
                  hasAlpha = true;
               }
            }
         }

         if (!hasAlpha) {
            for(int x = 32; x < 64; ++x) {
               for(int y = 0; y < 16; ++y) {
                  int pix = this.pixels[x + y * 64];
                  if ((pix >> 24 & 255) < 128) {
                     hasAlpha = true;
                  }
               }
            }
         }

         return out;
      }
   }

   private void setForceAlpha(int x0, int y0, int x1, int y1) {
      if (!this.hasAlpha(x0, y0, x1, y1)) {
         for(int x = x0; x < x1; ++x) {
            for(int y = y0; y < y1; ++y) {
               int[] var10000 = this.pixels;
               int var10001 = x + y * this.width;
               var10000[var10001] &= 16777215;
            }
         }

      }
   }

   private void setNoAlpha(int x0, int y0, int x1, int y1) {
      for(int x = x0; x < x1; ++x) {
         for(int y = y0; y < y1; ++y) {
            int[] var10000 = this.pixels;
            int var10001 = x + y * this.width;
            var10000[var10001] |= -16777216;
         }
      }

   }

   private boolean hasAlpha(int x0, int y0, int x1, int y1) {
      for(int x = x0; x < x1; ++x) {
         for(int y = y0; y < y1; ++y) {
            int pix = this.pixels[x + y * this.width];
            if ((pix >> 24 & 255) < 128) {
               return true;
            }
         }
      }

      return false;
   }
}
