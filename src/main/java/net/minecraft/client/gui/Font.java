package net.minecraft.client.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import net.minecraft.SharedConstants;
import net.minecraft.client.MemoryTracker;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.Textures;
import org.lwjgl.opengl.GL11;

public class Font {
   private int[] charWidths = new int[256];
   public int fontTexture = 0;
   private int listPos;
   private IntBuffer ib = MemoryTracker.createIntBuffer(1024);

   public Font(Options options, String name, Textures textures) {
      BufferedImage img;
      try {
         img = ImageIO.read(Textures.class.getResourceAsStream(name));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }

      int w = img.getWidth();
      int h = img.getHeight();
      int[] rawPixels = new int[w * h];
      img.getRGB(0, 0, w, h, rawPixels, 0, w);

      for(int i = 0; i < 256; ++i) {
         int xt = i % 16;
         int yt = i / 16;

         int x;
         for(x = 7; x >= 0; --x) {
            int xPixel = xt * 8 + x;
            boolean emptyColumn = true;

            for(int y = 0; y < 8 && emptyColumn; ++y) {
               int yPixel = (yt * 8 + y) * w;
               int pixel = rawPixels[xPixel + yPixel] & 255;
               if (pixel > 0) {
                  emptyColumn = false;
               }
            }

            if (!emptyColumn) {
               break;
            }
         }

         if (i == 32) {
            x = 2;
         }

         this.charWidths[i] = x + 2;
      }

      this.fontTexture = textures.getTexture(img);
      this.listPos = MemoryTracker.genLists(288);
      Tesselator t = Tesselator.instance;

      for(int i = 0; i < 256; ++i) {
         GL11.glNewList(this.listPos + i, 4864);
         t.begin();
         int ix = i % 16 * 8;
         int iy = i / 16 * 8;
         float s = 7.99F;
         float uo = 0.0F;
         float vo = 0.0F;
         t.vertexUV((double)0.0F, (double)(0.0F + s), (double)0.0F, (double)((float)ix / 128.0F + uo), (double)(((float)iy + s) / 128.0F + vo));
         t.vertexUV((double)(0.0F + s), (double)(0.0F + s), (double)0.0F, (double)(((float)ix + s) / 128.0F + uo), (double)(((float)iy + s) / 128.0F + vo));
         t.vertexUV((double)(0.0F + s), (double)0.0F, (double)0.0F, (double)(((float)ix + s) / 128.0F + uo), (double)((float)iy / 128.0F + vo));
         t.vertexUV((double)0.0F, (double)0.0F, (double)0.0F, (double)((float)ix / 128.0F + uo), (double)((float)iy / 128.0F + vo));
         t.end();
         GL11.glTranslatef((float)this.charWidths[i], 0.0F, 0.0F);
         GL11.glEndList();
      }

      for(int i = 0; i < 32; ++i) {
         int br = (i >> 3 & 1) * 85;
         int r = (i >> 2 & 1) * 170 + br;
         int g = (i >> 1 & 1) * 170 + br;
         int b = (i >> 0 & 1) * 170 + br;
         if (i == 6) {
            r += 85;
         }

         boolean darken = i >= 16;
         if (options.anaglyph3d) {
            int cr = (r * 30 + g * 59 + b * 11) / 100;
            int cg = (r * 30 + g * 70) / 100;
            int cb = (r * 30 + b * 70) / 100;
            r = cr;
            g = cg;
            b = cb;
         }

         if (darken) {
            r /= 4;
            g /= 4;
            b /= 4;
         }

         GL11.glNewList(this.listPos + 256 + i, 4864);
         GL11.glColor3f((float)r / 255.0F, (float)g / 255.0F, (float)b / 255.0F);
         GL11.glEndList();
      }

   }

   public void drawShadow(String str, int x, int y, int color) {
      this.draw(str, x + 1, y + 1, color, true);
      this.draw(str, x, y, color);
   }

   public void draw(String str, int x, int y, int color) {
      this.draw(str, x, y, color, false);
   }

   public void draw(String str, int x, int y, int color, boolean darken) {
      if (str != null) {
         if (darken) {
            int oldAlpha = color & -16777216;
            color = (color & 16579836) >> 2;
            color += oldAlpha;
         }

         GL11.glBindTexture(3553, this.fontTexture);
         float r = (float)(color >> 16 & 255) / 255.0F;
         float g = (float)(color >> 8 & 255) / 255.0F;
         float b = (float)(color & 255) / 255.0F;
         float a = (float)(color >> 24 & 255) / 255.0F;
         if (a == 0.0F) {
            a = 1.0F;
         }

         GL11.glColor4f(r, g, b, a);
         this.ib.clear();
         GL11.glPushMatrix();
         GL11.glTranslatef((float)x, (float)y, 0.0F);

         for(int i = 0; i < str.length(); ++i) {
            for(; str.charAt(i) == 223 && str.length() > i + 1; i += 2) {
               int cc = "0123456789abcdef".indexOf(str.toLowerCase().charAt(i + 1));
               if (cc < 0 || cc > 15) {
                  cc = 15;
               }

               this.ib.put(this.listPos + 256 + cc + (darken ? 16 : 0));
               if (this.ib.remaining() == 0) {
                  this.ib.flip();
                  GL11.glCallLists(this.ib);
                  this.ib.clear();
               }
            }

            int ch = SharedConstants.acceptableLetters.indexOf(str.charAt(i));
            if (ch >= 0) {
               this.ib.put(this.listPos + ch + 32);
            }

            if (this.ib.remaining() == 0) {
               this.ib.flip();
               GL11.glCallLists(this.ib);
               this.ib.clear();
            }
         }

         this.ib.flip();
         GL11.glCallLists(this.ib);
         GL11.glPopMatrix();
      }
   }

   public int width(String str) {
      if (str == null) {
         return 0;
      } else {
         int len = 0;

         for(int i = 0; i < str.length(); ++i) {
            if (str.charAt(i) == 223) {
               ++i;
            } else {
               int ch = SharedConstants.acceptableLetters.indexOf(str.charAt(i));
               if (ch >= 0) {
                  len += this.charWidths[ch + 32];
               }
            }
         }

         return len;
      }
   }

   public static String sanitize(String str) {
      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < str.length(); ++i) {
         if (str.charAt(i) == 223) {
            ++i;
         } else if (SharedConstants.acceptableLetters.indexOf(str.charAt(i)) >= 0) {
            sb.append(str.charAt(i));
         }
      }

      return sb.toString();
   }
}
