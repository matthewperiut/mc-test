package net.minecraft.client.renderer.ptexture;

import java.util.Random;
import net.minecraft.world.level.tile.Tile;
import util.Mth;

public class PortalTexture extends DynamicTexture {
   private int time = 0;
   private byte[][] frames = new byte[32][1024];

   public PortalTexture() {
      super(Tile.portalTile.tex);
      Random random = new Random(100L);

      for(int time = 0; time < 32; ++time) {
         for(int x = 0; x < 16; ++x) {
            for(int y = 0; y < 16; ++y) {
               float pow = 0.0F;

               for(int i = 0; i < 2; ++i) {
                  float xo = (float)(i * 8);
                  float yo = (float)(i * 8);
                  float xd = ((float)x - xo) / 16.0F * 2.0F;
                  float yd = ((float)y - yo) / 16.0F * 2.0F;
                  if (xd < -1.0F) {
                     xd += 2.0F;
                  }

                  if (xd >= 1.0F) {
                     xd -= 2.0F;
                  }

                  if (yd < -1.0F) {
                     yd += 2.0F;
                  }

                  if (yd >= 1.0F) {
                     yd -= 2.0F;
                  }

                  float dd = xd * xd + yd * yd;
                  float pp = (float)Math.atan2((double)yd, (double)xd) + ((float)time / 32.0F * (float)Math.PI * 2.0F - dd * 10.0F + (float)(i * 2)) * (float)(i * 2 - 1);
                  pp = (Mth.sin(pp) + 1.0F) / 2.0F;
                  pp /= dd + 1.0F;
                  pow += pp * 0.5F;
               }

               pow += random.nextFloat() * 0.1F;
               int b = (int)(pow * 100.0F + 155.0F);
               int r = (int)(pow * pow * 200.0F + 55.0F);
               int g = (int)(pow * pow * pow * pow * 255.0F);
               int a = (int)(pow * 100.0F + 155.0F);
               int i = y * 16 + x;
               this.frames[time][i * 4 + 0] = (byte)r;
               this.frames[time][i * 4 + 1] = (byte)g;
               this.frames[time][i * 4 + 2] = (byte)b;
               this.frames[time][i * 4 + 3] = (byte)a;
            }
         }
      }

   }

   public void tick() {
      ++this.time;
      byte[] source = this.frames[this.time & 31];

      for(int i = 0; i < 256; ++i) {
         int r = source[i * 4 + 0] & 255;
         int g = source[i * 4 + 1] & 255;
         int b = source[i * 4 + 2] & 255;
         int a = source[i * 4 + 3] & 255;
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
