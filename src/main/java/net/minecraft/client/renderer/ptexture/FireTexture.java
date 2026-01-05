package net.minecraft.client.renderer.ptexture;

import net.minecraft.world.level.tile.Tile;

public class FireTexture extends DynamicTexture {
   protected float[] current = new float[320];
   protected float[] next = new float[320];

   public FireTexture(int id) {
      super(Tile.fire.tex + id * 16);
   }

   public void tick() {
      for(int x = 0; x < 16; ++x) {
         for(int y = 0; y < 20; ++y) {
            int count = 18;
            float pow = this.current[x + (y + 1) % 20 * 16] * (float)count;

            for(int xx = x - 1; xx <= x + 1; ++xx) {
               for(int yy = y; yy <= y + 1; ++yy) {
                  if (xx >= 0 && yy >= 0 && xx < 16 && yy < 20) {
                     pow += this.current[xx + yy * 16];
                  }

                  ++count;
               }
            }

            this.next[x + y * 16] = pow / ((float)count * 1.06F);
            if (y >= 19) {
               this.next[x + y * 16] = (float)(Math.random() * Math.random() * Math.random() * (double)4.0F + Math.random() * (double)0.1F + (double)0.2F);
            }
         }
      }

      float[] tmp = this.next;
      this.next = this.current;
      this.current = tmp;

      for(int i = 0; i < 256; ++i) {
         float pow = this.current[i] * 1.8F;
         if (pow > 1.0F) {
            pow = 1.0F;
         }

         if (pow < 0.0F) {
            pow = 0.0F;
         }

         int r = (int)(pow * 155.0F + 100.0F);
         int g = (int)(pow * pow * 255.0F);
         int b = (int)(pow * pow * pow * pow * pow * pow * pow * pow * pow * pow * 255.0F);
         int a = 255;
         if (pow < 0.5F) {
            a = 0;
         }

         float var15 = (pow - 0.5F) * 2.0F;
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
