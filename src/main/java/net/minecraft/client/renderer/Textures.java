package net.minecraft.client.renderer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.client.MemoryTracker;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.ptexture.DynamicTexture;
import net.minecraft.client.skins.TexturePack;
import net.minecraft.client.skins.TexturePackRepository;
import org.lwjgl.opengl.GL11;

public class Textures {
   public static boolean MIPMAP = false;
   private HashMap<String, Integer> idMap = new HashMap();
   private HashMap<Integer, BufferedImage> loadedImages = new HashMap();
   private IntBuffer ib = MemoryTracker.createIntBuffer(1);
   private ByteBuffer pixels = MemoryTracker.createByteBuffer(1048576);
   private List<DynamicTexture> dynamicTextures = new ArrayList();
   private Map<String, HttpTexture> httpTextures = new HashMap();
   private Options options;
   private boolean clamp = false;
   private boolean blur = false;
   private TexturePackRepository skins;

   public Textures(TexturePackRepository skins, Options options) {
      this.skins = skins;
      this.options = options;
   }

   public int loadTexture(String resourceName) {
      TexturePack skin = this.skins.selected;
      Integer id = (Integer)this.idMap.get(resourceName);
      if (id != null) {
         return id;
      } else {
         try {
            this.ib.clear();
            MemoryTracker.genTextures(this.ib);
            int texId = this.ib.get(0);
            if (resourceName.startsWith("##")) {
               this.loadTexture(this.makeStrip(this.readImage(skin.getResource(resourceName.substring(2)))), texId);
            } else if (resourceName.startsWith("%clamp%")) {
               this.clamp = true;
               this.loadTexture(this.readImage(skin.getResource(resourceName.substring(7))), texId);
               this.clamp = false;
            } else if (resourceName.startsWith("%blur%")) {
               this.blur = true;
               this.loadTexture(this.readImage(skin.getResource(resourceName.substring(6))), texId);
               this.blur = false;
            } else {
               this.loadTexture(this.readImage(skin.getResource(resourceName)), texId);
            }

            this.idMap.put(resourceName, texId);
            return texId;
         } catch (IOException var4) {
            throw new RuntimeException("!!");
         }
      }
   }

   private BufferedImage makeStrip(BufferedImage source) {
      int cols = source.getWidth() / 16;
      BufferedImage out = new BufferedImage(16, source.getHeight() * cols, 2);
      Graphics g = out.getGraphics();

      for(int i = 0; i < cols; ++i) {
         g.drawImage(source, -i * 16, i * source.getHeight(), (ImageObserver)null);
      }

      g.dispose();
      return out;
   }

   public int getTexture(BufferedImage img) {
      this.ib.clear();
      MemoryTracker.genTextures(this.ib);
      int id = this.ib.get(0);
      this.loadTexture(img, id);
      this.loadedImages.put(id, img);
      return id;
   }

   public void loadTexture(BufferedImage img, int id) {
      GL11.glBindTexture(3553, id);
      if (MIPMAP) {
         GL11.glTexParameteri(3553, 10241, 9987);
         GL11.glTexParameteri(3553, 10240, 9729);
      } else {
         GL11.glTexParameteri(3553, 10241, 9728);
         GL11.glTexParameteri(3553, 10240, 9728);
      }

      if (this.blur) {
         GL11.glTexParameteri(3553, 10241, 9729);
         GL11.glTexParameteri(3553, 10240, 9729);
      }

      if (this.clamp) {
         GL11.glTexParameteri(3553, 10242, 10496);
         GL11.glTexParameteri(3553, 10243, 10496);
      } else {
         GL11.glTexParameteri(3553, 10242, 10497);
         GL11.glTexParameteri(3553, 10243, 10497);
      }

      int w = img.getWidth();
      int h = img.getHeight();
      int[] rawPixels = new int[w * h];
      byte[] newPixels = new byte[w * h * 4];
      img.getRGB(0, 0, w, h, rawPixels, 0, w);

      for(int i = 0; i < rawPixels.length; ++i) {
         int a = rawPixels[i] >> 24 & 255;
         int r = rawPixels[i] >> 16 & 255;
         int g = rawPixels[i] >> 8 & 255;
         int b = rawPixels[i] & 255;
         if (this.options != null && this.options.anaglyph3d) {
            int rr = (r * 30 + g * 59 + b * 11) / 100;
            int gg = (r * 30 + g * 70) / 100;
            int bb = (r * 30 + b * 70) / 100;
            r = rr;
            g = gg;
            b = bb;
         }

         newPixels[i * 4 + 0] = (byte)r;
         newPixels[i * 4 + 1] = (byte)g;
         newPixels[i * 4 + 2] = (byte)b;
         newPixels[i * 4 + 3] = (byte)a;
      }

      this.pixels.clear();
      this.pixels.put(newPixels);
      this.pixels.position(0).limit(newPixels.length);
      GL11.glTexImage2D(3553, 0, 6408, w, h, 0, 6408, 5121, this.pixels);
      if (MIPMAP) {
         for(int level = 1; level <= 4; ++level) {
            int ow = w >> level - 1;
            int ww = w >> level;
            int hh = h >> level;

            for(int x = 0; x < ww; ++x) {
               for(int y = 0; y < hh; ++y) {
                  int c0 = this.pixels.getInt((x * 2 + 0 + (y * 2 + 0) * ow) * 4);
                  int c1 = this.pixels.getInt((x * 2 + 1 + (y * 2 + 0) * ow) * 4);
                  int c2 = this.pixels.getInt((x * 2 + 1 + (y * 2 + 1) * ow) * 4);
                  int c3 = this.pixels.getInt((x * 2 + 0 + (y * 2 + 1) * ow) * 4);
                  int col = this.crispBlend(this.crispBlend(c0, c1), this.crispBlend(c2, c3));
                  this.pixels.putInt((x + y * ww) * 4, col);
               }
            }

            GL11.glTexImage2D(3553, level, 6408, ww, hh, 0, 6408, 5121, this.pixels);
         }
      }

   }

   public void releaseTexture(int id) {
      this.loadedImages.remove(id);
      this.ib.clear();
      this.ib.put(id);
      this.ib.flip();
      GL11.glDeleteTextures(this.ib);
   }

   public int loadHttpTexture(String url, String backup) {
      HttpTexture texture = (HttpTexture)this.httpTextures.get(url);
      if (texture != null && texture.loadedImage != null && !texture.isLoaded) {
         if (texture.id < 0) {
            texture.id = this.getTexture(texture.loadedImage);
         } else {
            this.loadTexture(texture.loadedImage, texture.id);
         }

         texture.isLoaded = true;
      }

      if (texture != null && texture.id >= 0) {
         return texture.id;
      } else {
         return backup == null ? -1 : this.loadTexture(backup);
      }
   }

   public HttpTexture addHttpTexture(String url, HttpTextureProcessor processor) {
      HttpTexture texture = (HttpTexture)this.httpTextures.get(url);
      if (texture == null) {
         this.httpTextures.put(url, new HttpTexture(url, processor));
      } else {
         ++texture.count;
      }

      return texture;
   }

   public void removeHttpTexture(String url) {
      HttpTexture texture = (HttpTexture)this.httpTextures.get(url);
      if (texture != null) {
         --texture.count;
         if (texture.count == 0) {
            if (texture.id >= 0) {
               this.releaseTexture(texture.id);
            }

            this.httpTextures.remove(url);
         }
      }

   }

   public void addDynamicTexture(DynamicTexture dynamicTexture) {
      this.dynamicTextures.add(dynamicTexture);
      dynamicTexture.tick();
   }

   public void tick() {
      for(int i = 0; i < this.dynamicTextures.size(); ++i) {
         DynamicTexture dynamicTexture = (DynamicTexture)this.dynamicTextures.get(i);
         dynamicTexture.anaglyph3d = this.options.anaglyph3d;
         dynamicTexture.tick();
         this.pixels.clear();
         this.pixels.put(dynamicTexture.pixels);
         this.pixels.position(0).limit(dynamicTexture.pixels.length);
         dynamicTexture.bindTexture(this);

         for(int xx = 0; xx < dynamicTexture.replicate; ++xx) {
            for(int yy = 0; yy < dynamicTexture.replicate; ++yy) {
               GL11.glTexSubImage2D(3553, 0, dynamicTexture.tex % 16 * 16 + xx * 16, dynamicTexture.tex / 16 * 16 + yy * 16, 16, 16, 6408, 5121, this.pixels);
               if (MIPMAP) {
                  for(int level = 1; level <= 4; ++level) {
                     int os = 16 >> level - 1;
                     int s = 16 >> level;

                     for(int x = 0; x < s; ++x) {
                        for(int y = 0; y < s; ++y) {
                           int c0 = this.pixels.getInt((x * 2 + 0 + (y * 2 + 0) * os) * 4);
                           int c1 = this.pixels.getInt((x * 2 + 1 + (y * 2 + 0) * os) * 4);
                           int c2 = this.pixels.getInt((x * 2 + 1 + (y * 2 + 1) * os) * 4);
                           int c3 = this.pixels.getInt((x * 2 + 0 + (y * 2 + 1) * os) * 4);
                           int col = this.smoothBlend(this.smoothBlend(c0, c1), this.smoothBlend(c2, c3));
                           this.pixels.putInt((x + y * s) * 4, col);
                        }
                     }

                     GL11.glTexSubImage2D(3553, level, dynamicTexture.tex % 16 * s, dynamicTexture.tex / 16 * s, s, s, 6408, 5121, this.pixels);
                  }
               }
            }
         }
      }

      for(int i = 0; i < this.dynamicTextures.size(); ++i) {
         DynamicTexture dynamicTexture = (DynamicTexture)this.dynamicTextures.get(i);
         if (dynamicTexture.copyTo > 0) {
            this.pixels.clear();
            this.pixels.put(dynamicTexture.pixels);
            this.pixels.position(0).limit(dynamicTexture.pixels.length);
            GL11.glBindTexture(3553, dynamicTexture.copyTo);
            GL11.glTexSubImage2D(3553, 0, 0, 0, 16, 16, 6408, 5121, this.pixels);
            if (MIPMAP) {
               for(int level = 1; level <= 4; ++level) {
                  int os = 16 >> level - 1;
                  int s = 16 >> level;

                  for(int x = 0; x < s; ++x) {
                     for(int y = 0; y < s; ++y) {
                        int c0 = this.pixels.getInt((x * 2 + 0 + (y * 2 + 0) * os) * 4);
                        int c1 = this.pixels.getInt((x * 2 + 1 + (y * 2 + 0) * os) * 4);
                        int c2 = this.pixels.getInt((x * 2 + 1 + (y * 2 + 1) * os) * 4);
                        int c3 = this.pixels.getInt((x * 2 + 0 + (y * 2 + 1) * os) * 4);
                        int col = this.smoothBlend(this.smoothBlend(c0, c1), this.smoothBlend(c2, c3));
                        this.pixels.putInt((x + y * s) * 4, col);
                     }
                  }

                  GL11.glTexSubImage2D(3553, level, 0, 0, s, s, 6408, 5121, this.pixels);
               }
            }
         }
      }

   }

   private int smoothBlend(int c0, int c1) {
      int a0 = (c0 & -16777216) >> 24 & 255;
      int a1 = (c1 & -16777216) >> 24 & 255;
      return (a0 + a1 >> 1 << 24) + ((c0 & 16711422) + (c1 & 16711422) >> 1);
   }

   private int crispBlend(int c0, int c1) {
      int a0 = (c0 & -16777216) >> 24 & 255;
      int a1 = (c1 & -16777216) >> 24 & 255;
      int a = 255;
      if (a0 + a1 == 0) {
         a0 = 1;
         a1 = 1;
         a = 0;
      }

      int r0 = (c0 >> 16 & 255) * a0;
      int g0 = (c0 >> 8 & 255) * a0;
      int b0 = (c0 & 255) * a0;
      int r1 = (c1 >> 16 & 255) * a1;
      int g1 = (c1 >> 8 & 255) * a1;
      int b1 = (c1 & 255) * a1;
      int r = (r0 + r1) / (a0 + a1);
      int g = (g0 + g1) / (a0 + a1);
      int b = (b0 + b1) / (a0 + a1);
      return a << 24 | r << 16 | g << 8 | b;
   }

   public void reloadAll() {
      TexturePack skin = this.skins.selected;

      for(int id : this.loadedImages.keySet()) {
         BufferedImage image = (BufferedImage)this.loadedImages.get(id);
         this.loadTexture(image, id);
      }

      for(HttpTexture httpTexture : this.httpTextures.values()) {
         httpTexture.isLoaded = false;
      }

      for(String name : this.idMap.keySet()) {
         try {
            BufferedImage image;
            if (name.startsWith("##")) {
               image = this.makeStrip(this.readImage(skin.getResource(name.substring(2))));
            } else if (name.startsWith("%clamp%")) {
               this.clamp = true;
               image = this.readImage(skin.getResource(name.substring(7)));
            } else if (name.startsWith("%blur%")) {
               this.blur = true;
               image = this.readImage(skin.getResource(name.substring(6)));
            } else {
               image = this.readImage(skin.getResource(name));
            }

            int id = (Integer)this.idMap.get(name);
            this.loadTexture(image, id);
            this.blur = false;
            this.clamp = false;
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

   }

   private BufferedImage readImage(InputStream in) throws IOException {
      BufferedImage img = ImageIO.read(in);
      in.close();
      return img;
   }

   public void bind(int id) {
      if (id >= 0) {
         GL11.glBindTexture(3553, id);
      }
   }
}
