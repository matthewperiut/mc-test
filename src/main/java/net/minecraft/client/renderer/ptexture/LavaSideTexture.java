package net.minecraft.client.renderer.ptexture;

import net.minecraft.world.level.tile.Tile;
import util.Mth;

public class LavaSideTexture extends DynamicTexture {
   protected float[] current = new float[256];
   protected float[] next = new float[256];
   protected float[] heat = new float[256];
   protected float[] heata = new float[256];
   int tickCount = 0;

   public LavaSideTexture() {
      super(Tile.lava.tex + 1);
      this.replicate = 2;
   }

   public void tick() {
      ++this.tickCount;

      for(int x = 0; x < 16; ++x) {
         for(int y = 0; y < 16; ++y) {
            float pow = 0.0F;
            int xxo = (int)(Mth.sin((float)y * (float)Math.PI * 2.0F / 16.0F) * 1.2F);
            int yyo = (int)(Mth.sin((float)x * (float)Math.PI * 2.0F / 16.0F) * 1.2F);

            for(int xx = x - 1; xx <= x + 1; ++xx) {
               for(int yy = y - 1; yy <= y + 1; ++yy) {
                  int xi = xx + xxo & 15;
                  int yi = yy + yyo & 15;
                  pow += this.current[xi + yi * 16];
               }
            }

            this.next[x + y * 16] = pow / 10.0F + (this.heat[(x + 0 & 15) + (y + 0 & 15) * 16] + this.heat[(x + 1 & 15) + (y + 0 & 15) * 16] + this.heat[(x + 1 & 15) + (y + 1 & 15) * 16] + this.heat[(x + 0 & 15) + (y + 1 & 15) * 16]) / 4.0F * 0.8F;
            float[] var10000 = this.heat;
            var10000[x + y * 16] += this.heata[x + y * 16] * 0.01F;
            if (this.heat[x + y * 16] < 0.0F) {
               this.heat[x + y * 16] = 0.0F;
            }

            var10000 = this.heata;
            var10000[x + y * 16] -= 0.06F;
            if (Math.random() < 0.005) {
               this.heata[x + y * 16] = 1.5F;
            }
         }
      }

      float[] tmp = this.next;
      this.next = this.current;
      this.current = tmp;

      for(int i = 0; i < 256; ++i) {
         float pow = this.current[i - this.tickCount / 3 * 16 & 255] * 2.0F;
         if (pow > 1.0F) {
            pow = 1.0F;
         }

         if (pow < 0.0F) {
            pow = 0.0F;
         }

         int r = (int)(pow * 100.0F + 155.0F);
         int g = (int)(pow * pow * 255.0F);
         int b = (int)(pow * pow * pow * pow * 128.0F);
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
         this.pixels[i * 4 + 3] = -1;
      }

   }
}
