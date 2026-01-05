package net.minecraft.isom;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;

public class ZoneRenderer {
   private static final int IMG_WIDTH = 32;
   private static final int IMG_HEIGHT = 160;
   private float[] texCols = new float[768];
   private int[] pixels = new int[5120];
   private int[] zBuf = new int[5120];
   private int[] waterBuf = new int[5120];
   private int[] waterBr = new int[5120];
   private int[] yBuf = new int[34];
   private int[] textures = new int[768];

   public ZoneRenderer() {
      try {
         BufferedImage img = ImageIO.read(ZoneRenderer.class.getResource("/terrain.png"));
         int[] cols = new int[65536];
         img.getRGB(0, 0, 256, 256, cols, 0, 256);

         for(int i = 0; i < 256; ++i) {
            int r = 0;
            int g = 0;
            int b = 0;
            int xo = i % 16 * 16;
            int yo = i / 16 * 16;
            int count = 0;

            for(int y = 0; y < 16; ++y) {
               for(int x = 0; x < 16; ++x) {
                  int col = cols[x + xo + (y + yo) * 256];
                  int a = col >> 24 & 255;
                  if (a > 128) {
                     r += col >> 16 & 255;
                     g += col >> 8 & 255;
                     b += col & 255;
                     ++count;
                  }
               }

               if (count == 0) {
                  ++count;
               }

               this.texCols[i * 3 + 0] = (float)(r / count);
               this.texCols[i * 3 + 1] = (float)(g / count);
               this.texCols[i * 3 + 2] = (float)(b / count);
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }

      for(int i = 0; i < 256; ++i) {
         if (Tile.tiles[i] != null) {
            this.textures[i * 3 + 0] = Tile.tiles[i].getTexture(1);
            this.textures[i * 3 + 1] = Tile.tiles[i].getTexture(2);
            this.textures[i * 3 + 2] = Tile.tiles[i].getTexture(3);
         }
      }

   }

   public void render(Zone zone) {
      Level level = zone.level;
      if (level == null) {
         zone.noContent = true;
         zone.rendered = true;
      } else {
         int x0 = zone.x * 16;
         int z0 = zone.y * 16;
         int x1 = x0 + 16;
         int z1 = z0 + 16;
         LevelChunk chunk = level.getChunk(zone.x, zone.y);
         if (chunk.isEmpty()) {
            zone.noContent = true;
            zone.rendered = true;
         } else {
            zone.noContent = false;
            Arrays.fill(this.zBuf, 0);
            Arrays.fill(this.waterBuf, 0);
            Arrays.fill(this.yBuf, 160);

            for(int z = z1 - 1; z >= z0; --z) {
               for(int x = x1 - 1; x >= x0; --x) {
                  int xx = x - x0;
                  int zz = z - z0;
                  int xp = xx + zz;
                  boolean solid = true;

                  for(int y = 0; y < 128; ++y) {
                     int yp = zz - xx - y + 160 - 16;
                     if (yp < this.yBuf[xp] || yp < this.yBuf[xp + 1]) {
                        Tile t = Tile.tiles[level.getTile(x, y, z)];
                        if (t == null) {
                           solid = false;
                        } else if (t.material == Material.water) {
                           int ta = level.getTile(x, y + 1, z);
                           if (ta == 0 || Tile.tiles[ta].material != Material.water) {
                              float hh = (float)y / 127.0F * 0.6F + 0.4F;
                              float br = level.getBrightness(x, y + 1, z) * hh;
                              if (yp >= 0 && yp < 160) {
                                 int p = xp + yp * 32;
                                 if (xp >= 0 && xp <= 32 && this.waterBuf[p] <= y) {
                                    this.waterBuf[p] = y;
                                    this.waterBr[p] = (int)(br * 127.0F);
                                 }

                                 if (xp >= -1 && xp <= 31 && this.waterBuf[p + 1] <= y) {
                                    this.waterBuf[p + 1] = y;
                                    this.waterBr[p + 1] = (int)(br * 127.0F);
                                 }

                                 solid = false;
                              }
                           }
                        } else {
                           if (solid) {
                              if (yp < this.yBuf[xp]) {
                                 this.yBuf[xp] = yp;
                              }

                              if (yp < this.yBuf[xp + 1]) {
                                 this.yBuf[xp + 1] = yp;
                              }
                           }

                           float hh = (float)y / 127.0F * 0.6F + 0.4F;
                           if (yp >= 0 && yp < 160) {
                              int p = xp + yp * 32;
                              int upTex = this.textures[t.id * 3 + 0];
                              float upBr = (level.getBrightness(x, y + 1, z) * 0.8F + 0.2F) * hh;
                              if (xp >= 0 && this.zBuf[p] <= y) {
                                 this.zBuf[p] = y;
                                 this.pixels[p] = -16777216 | (int)(this.texCols[upTex * 3 + 0] * upBr) << 16 | (int)(this.texCols[upTex * 3 + 1] * upBr) << 8 | (int)(this.texCols[upTex * 3 + 2] * upBr);
                              }

                              if (xp < 31) {
                                 float br = upBr * 0.9F;
                                 if (this.zBuf[p + 1] <= y) {
                                    this.zBuf[p + 1] = y;
                                    this.pixels[p + 1] = -16777216 | (int)(this.texCols[upTex * 3 + 0] * br) << 16 | (int)(this.texCols[upTex * 3 + 1] * br) << 8 | (int)(this.texCols[upTex * 3 + 2] * br);
                                 }
                              }
                           }

                           if (yp >= -1 && yp < 159) {
                              int p = xp + (yp + 1) * 32;
                              int lTex = this.textures[t.id * 3 + 1];
                              float lBr = level.getBrightness(x - 1, y, z) * 0.8F + 0.2F;
                              int rTex = this.textures[t.id * 3 + 2];
                              float rBr = level.getBrightness(x, y, z + 1) * 0.8F + 0.2F;
                              if (xp >= 0) {
                                 float br = lBr * hh * 0.6F;
                                 if (this.zBuf[p] <= y - 1) {
                                    this.zBuf[p] = y - 1;
                                    this.pixels[p] = -16777216 | (int)(this.texCols[lTex * 3 + 0] * br) << 16 | (int)(this.texCols[lTex * 3 + 1] * br) << 8 | (int)(this.texCols[lTex * 3 + 2] * br);
                                 }
                              }

                              if (xp < 31) {
                                 float br = rBr * 0.9F * hh * 0.4F;
                                 if (this.zBuf[p + 1] <= y - 1) {
                                    this.zBuf[p + 1] = y - 1;
                                    this.pixels[p + 1] = -16777216 | (int)(this.texCols[rTex * 3 + 0] * br) << 16 | (int)(this.texCols[rTex * 3 + 1] * br) << 8 | (int)(this.texCols[rTex * 3 + 2] * br);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            this.postProcess();
            if (zone.image == null) {
               zone.image = new BufferedImage(32, 160, 2);
            }

            zone.image.setRGB(0, 0, 32, 160, this.pixels, 0, 32);
            zone.rendered = true;
         }
      }
   }

   private void postProcess() {
      for(int x = 0; x < 32; ++x) {
         for(int y = 0; y < 160; ++y) {
            int p = x + y * 32;
            if (this.zBuf[p] == 0) {
               this.pixels[p] = 0;
            }

            if (this.waterBuf[p] > this.zBuf[p]) {
               int a = this.pixels[p] >> 24 & 255;
               this.pixels[p] = ((this.pixels[p] & 16711422) >> 1) + this.waterBr[p];
               if (a < 128) {
                  this.pixels[p] = Integer.MIN_VALUE + this.waterBr[p] * 2;
               } else {
                  int[] var10000 = this.pixels;
                  var10000[p] |= -16777216;
               }
            }
         }
      }

   }
}
