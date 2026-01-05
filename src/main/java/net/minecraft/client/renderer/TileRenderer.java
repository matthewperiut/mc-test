package net.minecraft.client.renderer;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.DoorTile;
import net.minecraft.world.level.tile.LiquidTile;
import net.minecraft.world.level.tile.RedStoneDustTile;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import util.Mth;

public class TileRenderer {
   private LevelSource level;
   private int fixedTexture = -1;
   private boolean xFlipTexture = false;
   private boolean noCulling = false;

   public TileRenderer(LevelSource level) {
      this.level = level;
   }

   public TileRenderer() {
   }

   public void tesselateInWorld(Tile tile, int x, int y, int z, int fixedTexture) {
      this.fixedTexture = fixedTexture;
      this.tesselateInWorld(tile, x, y, z);
      this.fixedTexture = -1;
   }

   public void tesselateInWorldNoCulling(Tile tile, int x, int y, int z) {
      this.noCulling = true;
      this.tesselateInWorld(tile, x, y, z);
      this.noCulling = false;
   }

   public boolean tesselateInWorld(Tile tt, int x, int y, int z) {
      int shape = tt.getRenderShape();
      tt.updateShape(this.level, x, y, z);
      if (shape == 0) {
         return this.tesselateBlockInWorld(tt, x, y, z);
      } else if (shape == 4) {
         return this.tesselateWaterInWorld(tt, x, y, z);
      } else if (shape == 13) {
         return this.tesselateCactusInWorld(tt, x, y, z);
      } else if (shape == 1) {
         return this.tesselateCrossInWorld(tt, x, y, z);
      } else if (shape == 6) {
         return this.tesselateRowInWorld(tt, x, y, z);
      } else if (shape == 2) {
         return this.tesselateTorchInWorld(tt, x, y, z);
      } else if (shape == 3) {
         return this.tesselateFireInWorld(tt, x, y, z);
      } else if (shape == 5) {
         return this.tesselateDustInWorld(tt, x, y, z);
      } else if (shape == 8) {
         return this.tesselateLadderInWorld(tt, x, y, z);
      } else if (shape == 7) {
         return this.tesselateDoorInWorld(tt, x, y, z);
      } else if (shape == 9) {
         return this.tesselateRailInWorld(tt, x, y, z);
      } else if (shape == 10) {
         return this.tesselateStairsInWorld(tt, x, y, z);
      } else if (shape == 11) {
         return this.tesselateFenceInWorld(tt, x, y, z);
      } else {
         return shape == 12 ? this.tesselateLeverInWorld(tt, x, y, z) : false;
      }
   }

   public boolean tesselateTorchInWorld(Tile tt, int x, int y, int z) {
      int dir = this.level.getData(x, y, z);
      Tesselator t = Tesselator.instance;
      float br = tt.getBrightness(this.level, x, y, z);
      if (Tile.lightEmission[tt.id] > 0) {
         br = 1.0F;
      }

      t.color(br, br, br);
      double r = (double)0.4F;
      double r2 = (double)0.5F - r;
      double h = (double)0.2F;
      if (dir == 1) {
         this.tesselateTorch(tt, (double)x - r2, (double)y + h, (double)z, -r, (double)0.0F);
      } else if (dir == 2) {
         this.tesselateTorch(tt, (double)x + r2, (double)y + h, (double)z, r, (double)0.0F);
      } else if (dir == 3) {
         this.tesselateTorch(tt, (double)x, (double)y + h, (double)z - r2, (double)0.0F, -r);
      } else if (dir == 4) {
         this.tesselateTorch(tt, (double)x, (double)y + h, (double)z + r2, (double)0.0F, r);
      } else {
         this.tesselateTorch(tt, (double)x, (double)y, (double)z, (double)0.0F, (double)0.0F);
      }

      return true;
   }

   public boolean tesselateLeverInWorld(Tile tt, int x, int y, int z) {
      int data = this.level.getData(x, y, z);
      int dir = data & 7;
      boolean flipped = (data & 8) > 0;
      Tesselator t = Tesselator.instance;
      boolean hadFixed = this.fixedTexture >= 0;
      if (!hadFixed) {
         this.fixedTexture = Tile.stoneBrick.tex;
      }

      float w1 = 0.25F;
      float w2 = 0.1875F;
      float h = 0.1875F;
      if (dir == 5) {
         tt.setShape(0.5F - w2, 0.0F, 0.5F - w1, 0.5F + w2, h, 0.5F + w1);
      } else if (dir == 6) {
         tt.setShape(0.5F - w1, 0.0F, 0.5F - w2, 0.5F + w1, h, 0.5F + w2);
      } else if (dir == 4) {
         tt.setShape(0.5F - w2, 0.5F - w1, 1.0F - h, 0.5F + w2, 0.5F + w1, 1.0F);
      } else if (dir == 3) {
         tt.setShape(0.5F - w2, 0.5F - w1, 0.0F, 0.5F + w2, 0.5F + w1, h);
      } else if (dir == 2) {
         tt.setShape(1.0F - h, 0.5F - w1, 0.5F - w2, 1.0F, 0.5F + w1, 0.5F + w2);
      } else if (dir == 1) {
         tt.setShape(0.0F, 0.5F - w1, 0.5F - w2, h, 0.5F + w1, 0.5F + w2);
      }

      this.tesselateBlockInWorld(tt, x, y, z);
      if (!hadFixed) {
         this.fixedTexture = -1;
      }

      float br = tt.getBrightness(this.level, x, y, z);
      if (Tile.lightEmission[tt.id] > 0) {
         br = 1.0F;
      }

      t.color(br, br, br);
      int tex = tt.getTexture(0);
      if (this.fixedTexture >= 0) {
         tex = this.fixedTexture;
      }

      int xt = (tex & 15) << 4;
      int yt = tex & 240;
      float u0 = (float)xt / 256.0F;
      float u1 = ((float)xt + 15.99F) / 256.0F;
      float v0 = (float)yt / 256.0F;
      float v1 = ((float)yt + 15.99F) / 256.0F;
      Vec3[] corners = new Vec3[8];
      float xv = 0.0625F;
      float zv = 0.0625F;
      float yv = 0.625F;
      corners[0] = Vec3.newTemp((double)(-xv), (double)0.0F, (double)(-zv));
      corners[1] = Vec3.newTemp((double)xv, (double)0.0F, (double)(-zv));
      corners[2] = Vec3.newTemp((double)xv, (double)0.0F, (double)zv);
      corners[3] = Vec3.newTemp((double)(-xv), (double)0.0F, (double)zv);
      corners[4] = Vec3.newTemp((double)(-xv), (double)yv, (double)(-zv));
      corners[5] = Vec3.newTemp((double)xv, (double)yv, (double)(-zv));
      corners[6] = Vec3.newTemp((double)xv, (double)yv, (double)zv);
      corners[7] = Vec3.newTemp((double)(-xv), (double)yv, (double)zv);

      for(int i = 0; i < 8; ++i) {
         if (flipped) {
            corners[i].z -= (double)0.0625F;
            corners[i].xRot(0.69813174F);
         } else {
            corners[i].z += (double)0.0625F;
            corners[i].xRot(-0.69813174F);
         }

         if (dir == 6) {
            corners[i].yRot(((float)Math.PI / 2F));
         }

         if (dir < 5) {
            corners[i].y -= (double)0.375F;
            corners[i].xRot(((float)Math.PI / 2F));
            if (dir == 4) {
               corners[i].yRot(0.0F);
            }

            if (dir == 3) {
               corners[i].yRot((float)Math.PI);
            }

            if (dir == 2) {
               corners[i].yRot(((float)Math.PI / 2F));
            }

            if (dir == 1) {
               corners[i].yRot((-(float)Math.PI / 2F));
            }

            corners[i].x += (double)x + (double)0.5F;
            corners[i].y += (double)((float)y + 0.5F);
            corners[i].z += (double)z + (double)0.5F;
         } else {
            corners[i].x += (double)x + (double)0.5F;
            corners[i].y += (double)((float)y + 0.125F);
            corners[i].z += (double)z + (double)0.5F;
         }
      }

      Vec3 c0 = null;
      Vec3 c1 = null;
      Vec3 c2 = null;
      Vec3 c3 = null;

      for(int i = 0; i < 6; ++i) {
         if (i == 0) {
            u0 = (float)(xt + 7) / 256.0F;
            u1 = ((float)(xt + 9) - 0.01F) / 256.0F;
            v0 = (float)(yt + 6) / 256.0F;
            v1 = ((float)(yt + 8) - 0.01F) / 256.0F;
         } else if (i == 2) {
            u0 = (float)(xt + 7) / 256.0F;
            u1 = ((float)(xt + 9) - 0.01F) / 256.0F;
            v0 = (float)(yt + 6) / 256.0F;
            v1 = ((float)(yt + 16) - 0.01F) / 256.0F;
         }

         if (i == 0) {
            c0 = corners[0];
            c1 = corners[1];
            c2 = corners[2];
            c3 = corners[3];
         } else if (i == 1) {
            c0 = corners[7];
            c1 = corners[6];
            c2 = corners[5];
            c3 = corners[4];
         } else if (i == 2) {
            c0 = corners[1];
            c1 = corners[0];
            c2 = corners[4];
            c3 = corners[5];
         } else if (i == 3) {
            c0 = corners[2];
            c1 = corners[1];
            c2 = corners[5];
            c3 = corners[6];
         } else if (i == 4) {
            c0 = corners[3];
            c1 = corners[2];
            c2 = corners[6];
            c3 = corners[7];
         } else if (i == 5) {
            c0 = corners[0];
            c1 = corners[3];
            c2 = corners[7];
            c3 = corners[4];
         }

         t.vertexUV(c0.x, c0.y, c0.z, (double)u0, (double)v1);
         t.vertexUV(c1.x, c1.y, c1.z, (double)u1, (double)v1);
         t.vertexUV(c2.x, c2.y, c2.z, (double)u1, (double)v0);
         t.vertexUV(c3.x, c3.y, c3.z, (double)u0, (double)v0);
      }

      return true;
   }

   public boolean tesselateFireInWorld(Tile tt, int x, int y, int z) {
      Tesselator t = Tesselator.instance;
      int tex = tt.getTexture(0);
      if (this.fixedTexture >= 0) {
         tex = this.fixedTexture;
      }

      float br = tt.getBrightness(this.level, x, y, z);
      t.color(br, br, br);
      int xt = (tex & 15) << 4;
      int yt = tex & 240;
      double u0 = (double)((float)xt / 256.0F);
      double u1 = (double)(((float)xt + 15.99F) / 256.0F);
      double v0 = (double)((float)yt / 256.0F);
      double v1 = (double)(((float)yt + 15.99F) / 256.0F);
      float h = 1.4F;
      if (!this.level.isSolidTile(x, y - 1, z) && !Tile.fire.canBurn(this.level, x, y - 1, z)) {
         float r = 0.2F;
         float yo = 0.0625F;
         if ((x + y + z & 1) == 1) {
            u0 = (double)((float)xt / 256.0F);
            u1 = (double)(((float)xt + 15.99F) / 256.0F);
            v0 = (double)((float)(yt + 16) / 256.0F);
            v1 = (double)(((float)yt + 15.99F + 16.0F) / 256.0F);
         }

         if ((x / 2 + y / 2 + z / 2 & 1) == 1) {
            double tmp = u1;
            u1 = u0;
            u0 = tmp;
         }

         if (Tile.fire.canBurn(this.level, x - 1, y, z)) {
            t.vertexUV((double)((float)x + r), (double)((float)y + h + yo), (double)(z + 1), u1, v0);
            t.vertexUV((double)(x + 0), (double)((float)(y + 0) + yo), (double)(z + 1), u1, v1);
            t.vertexUV((double)(x + 0), (double)((float)(y + 0) + yo), (double)(z + 0), u0, v1);
            t.vertexUV((double)((float)x + r), (double)((float)y + h + yo), (double)(z + 0), u0, v0);
            t.vertexUV((double)((float)x + r), (double)((float)y + h + yo), (double)(z + 0), u0, v0);
            t.vertexUV((double)(x + 0), (double)((float)(y + 0) + yo), (double)(z + 0), u0, v1);
            t.vertexUV((double)(x + 0), (double)((float)(y + 0) + yo), (double)(z + 1), u1, v1);
            t.vertexUV((double)((float)x + r), (double)((float)y + h + yo), (double)(z + 1), u1, v0);
         }

         if (Tile.fire.canBurn(this.level, x + 1, y, z)) {
            t.vertexUV((double)((float)(x + 1) - r), (double)((float)y + h + yo), (double)(z + 0), u0, v0);
            t.vertexUV((double)(x + 1 - 0), (double)((float)(y + 0) + yo), (double)(z + 0), u0, v1);
            t.vertexUV((double)(x + 1 - 0), (double)((float)(y + 0) + yo), (double)(z + 1), u1, v1);
            t.vertexUV((double)((float)(x + 1) - r), (double)((float)y + h + yo), (double)(z + 1), u1, v0);
            t.vertexUV((double)((float)(x + 1) - r), (double)((float)y + h + yo), (double)(z + 1), u1, v0);
            t.vertexUV((double)(x + 1 - 0), (double)((float)(y + 0) + yo), (double)(z + 1), u1, v1);
            t.vertexUV((double)(x + 1 - 0), (double)((float)(y + 0) + yo), (double)(z + 0), u0, v1);
            t.vertexUV((double)((float)(x + 1) - r), (double)((float)y + h + yo), (double)(z + 0), u0, v0);
         }

         if (Tile.fire.canBurn(this.level, x, y, z - 1)) {
            t.vertexUV((double)(x + 0), (double)((float)y + h + yo), (double)((float)z + r), u1, v0);
            t.vertexUV((double)(x + 0), (double)((float)(y + 0) + yo), (double)(z + 0), u1, v1);
            t.vertexUV((double)(x + 1), (double)((float)(y + 0) + yo), (double)(z + 0), u0, v1);
            t.vertexUV((double)(x + 1), (double)((float)y + h + yo), (double)((float)z + r), u0, v0);
            t.vertexUV((double)(x + 1), (double)((float)y + h + yo), (double)((float)z + r), u0, v0);
            t.vertexUV((double)(x + 1), (double)((float)(y + 0) + yo), (double)(z + 0), u0, v1);
            t.vertexUV((double)(x + 0), (double)((float)(y + 0) + yo), (double)(z + 0), u1, v1);
            t.vertexUV((double)(x + 0), (double)((float)y + h + yo), (double)((float)z + r), u1, v0);
         }

         if (Tile.fire.canBurn(this.level, x, y, z + 1)) {
            t.vertexUV((double)(x + 1), (double)((float)y + h + yo), (double)((float)(z + 1) - r), u0, v0);
            t.vertexUV((double)(x + 1), (double)((float)(y + 0) + yo), (double)(z + 1 - 0), u0, v1);
            t.vertexUV((double)(x + 0), (double)((float)(y + 0) + yo), (double)(z + 1 - 0), u1, v1);
            t.vertexUV((double)(x + 0), (double)((float)y + h + yo), (double)((float)(z + 1) - r), u1, v0);
            t.vertexUV((double)(x + 0), (double)((float)y + h + yo), (double)((float)(z + 1) - r), u1, v0);
            t.vertexUV((double)(x + 0), (double)((float)(y + 0) + yo), (double)(z + 1 - 0), u1, v1);
            t.vertexUV((double)(x + 1), (double)((float)(y + 0) + yo), (double)(z + 1 - 0), u0, v1);
            t.vertexUV((double)(x + 1), (double)((float)y + h + yo), (double)((float)(z + 1) - r), u0, v0);
         }

         if (Tile.fire.canBurn(this.level, x, y + 1, z)) {
            double x0 = (double)x + (double)0.5F + (double)0.5F;
            double x1 = (double)x + (double)0.5F - (double)0.5F;
            double z0 = (double)z + (double)0.5F + (double)0.5F;
            double z1 = (double)z + (double)0.5F - (double)0.5F;
            double x0_ = (double)x + (double)0.5F - (double)0.5F;
            double x1_ = (double)x + (double)0.5F + (double)0.5F;
            double z0_ = (double)z + (double)0.5F - (double)0.5F;
            double z1_ = (double)z + (double)0.5F + (double)0.5F;
            u0 = (double)((float)xt / 256.0F);
            u1 = (double)(((float)xt + 15.99F) / 256.0F);
            v0 = (double)((float)yt / 256.0F);
            v1 = (double)(((float)yt + 15.99F) / 256.0F);
            ++y;
            h = -0.2F;
            if ((x + y + z & 1) == 0) {
               t.vertexUV(x0_, (double)((float)y + h), (double)(z + 0), u1, v0);
               t.vertexUV(x0, (double)(y + 0), (double)(z + 0), u1, v1);
               t.vertexUV(x0, (double)(y + 0), (double)(z + 1), u0, v1);
               t.vertexUV(x0_, (double)((float)y + h), (double)(z + 1), u0, v0);
               u0 = (double)((float)xt / 256.0F);
               u1 = (double)(((float)xt + 15.99F) / 256.0F);
               v0 = (double)((float)(yt + 16) / 256.0F);
               v1 = (double)(((float)yt + 15.99F + 16.0F) / 256.0F);
               t.vertexUV(x1_, (double)((float)y + h), (double)(z + 1), u1, v0);
               t.vertexUV(x1, (double)(y + 0), (double)(z + 1), u1, v1);
               t.vertexUV(x1, (double)(y + 0), (double)(z + 0), u0, v1);
               t.vertexUV(x1_, (double)((float)y + h), (double)(z + 0), u0, v0);
            } else {
               t.vertexUV((double)(x + 0), (double)((float)y + h), z1_, u1, v0);
               t.vertexUV((double)(x + 0), (double)(y + 0), z1, u1, v1);
               t.vertexUV((double)(x + 1), (double)(y + 0), z1, u0, v1);
               t.vertexUV((double)(x + 1), (double)((float)y + h), z1_, u0, v0);
               u0 = (double)((float)xt / 256.0F);
               u1 = (double)(((float)xt + 15.99F) / 256.0F);
               v0 = (double)((float)(yt + 16) / 256.0F);
               v1 = (double)(((float)yt + 15.99F + 16.0F) / 256.0F);
               t.vertexUV((double)(x + 1), (double)((float)y + h), z0_, u1, v0);
               t.vertexUV((double)(x + 1), (double)(y + 0), z0, u1, v1);
               t.vertexUV((double)(x + 0), (double)(y + 0), z0, u0, v1);
               t.vertexUV((double)(x + 0), (double)((float)y + h), z0_, u0, v0);
            }
         }
      } else {
         double x0 = (double)x + (double)0.5F + 0.2;
         double x1 = (double)x + (double)0.5F - 0.2;
         double z0 = (double)z + (double)0.5F + 0.2;
         double z1 = (double)z + (double)0.5F - 0.2;
         double x0_ = (double)x + (double)0.5F - 0.3;
         double x1_ = (double)x + (double)0.5F + 0.3;
         double z0_ = (double)z + (double)0.5F - 0.3;
         double z1_ = (double)z + (double)0.5F + 0.3;
         t.vertexUV(x0_, (double)((float)y + h), (double)(z + 1), u1, v0);
         t.vertexUV(x0, (double)(y + 0), (double)(z + 1), u1, v1);
         t.vertexUV(x0, (double)(y + 0), (double)(z + 0), u0, v1);
         t.vertexUV(x0_, (double)((float)y + h), (double)(z + 0), u0, v0);
         t.vertexUV(x1_, (double)((float)y + h), (double)(z + 0), u1, v0);
         t.vertexUV(x1, (double)(y + 0), (double)(z + 0), u1, v1);
         t.vertexUV(x1, (double)(y + 0), (double)(z + 1), u0, v1);
         t.vertexUV(x1_, (double)((float)y + h), (double)(z + 1), u0, v0);
         u0 = (double)((float)xt / 256.0F);
         u1 = (double)(((float)xt + 15.99F) / 256.0F);
         v0 = (double)((float)(yt + 16) / 256.0F);
         v1 = (double)(((float)yt + 15.99F + 16.0F) / 256.0F);
         t.vertexUV((double)(x + 1), (double)((float)y + h), z1_, u1, v0);
         t.vertexUV((double)(x + 1), (double)(y + 0), z1, u1, v1);
         t.vertexUV((double)(x + 0), (double)(y + 0), z1, u0, v1);
         t.vertexUV((double)(x + 0), (double)((float)y + h), z1_, u0, v0);
         t.vertexUV((double)(x + 0), (double)((float)y + h), z0_, u1, v0);
         t.vertexUV((double)(x + 0), (double)(y + 0), z0, u1, v1);
         t.vertexUV((double)(x + 1), (double)(y + 0), z0, u0, v1);
         t.vertexUV((double)(x + 1), (double)((float)y + h), z0_, u0, v0);
         x0 = (double)x + (double)0.5F - (double)0.5F;
         x1 = (double)x + (double)0.5F + (double)0.5F;
         z0 = (double)z + (double)0.5F - (double)0.5F;
         z1 = (double)z + (double)0.5F + (double)0.5F;
         x0_ = (double)x + (double)0.5F - 0.4;
         x1_ = (double)x + (double)0.5F + 0.4;
         z0_ = (double)z + (double)0.5F - 0.4;
         z1_ = (double)z + (double)0.5F + 0.4;
         t.vertexUV(x0_, (double)((float)y + h), (double)(z + 0), u0, v0);
         t.vertexUV(x0, (double)(y + 0), (double)(z + 0), u0, v1);
         t.vertexUV(x0, (double)(y + 0), (double)(z + 1), u1, v1);
         t.vertexUV(x0_, (double)((float)y + h), (double)(z + 1), u1, v0);
         t.vertexUV(x1_, (double)((float)y + h), (double)(z + 1), u0, v0);
         t.vertexUV(x1, (double)(y + 0), (double)(z + 1), u0, v1);
         t.vertexUV(x1, (double)(y + 0), (double)(z + 0), u1, v1);
         t.vertexUV(x1_, (double)((float)y + h), (double)(z + 0), u1, v0);
         u0 = (double)((float)xt / 256.0F);
         u1 = (double)(((float)xt + 15.99F) / 256.0F);
         v0 = (double)((float)yt / 256.0F);
         v1 = (double)(((float)yt + 15.99F) / 256.0F);
         t.vertexUV((double)(x + 0), (double)((float)y + h), z1_, u0, v0);
         t.vertexUV((double)(x + 0), (double)(y + 0), z1, u0, v1);
         t.vertexUV((double)(x + 1), (double)(y + 0), z1, u1, v1);
         t.vertexUV((double)(x + 1), (double)((float)y + h), z1_, u1, v0);
         t.vertexUV((double)(x + 1), (double)((float)y + h), z0_, u0, v0);
         t.vertexUV((double)(x + 1), (double)(y + 0), z0, u0, v1);
         t.vertexUV((double)(x + 0), (double)(y + 0), z0, u1, v1);
         t.vertexUV((double)(x + 0), (double)((float)y + h), z0_, u1, v0);
      }

      return true;
   }

   public boolean tesselateDustInWorld(Tile tt, int x, int y, int z) {
      Tesselator t = Tesselator.instance;
      int tex = tt.getTexture(1, this.level.getData(x, y, z));
      if (this.fixedTexture >= 0) {
         tex = this.fixedTexture;
      }

      float br = tt.getBrightness(this.level, x, y, z);
      t.color(br, br, br);
      int xt = (tex & 15) << 4;
      int yt = tex & 240;
      double u0 = (double)((float)xt / 256.0F);
      double u1 = (double)(((float)xt + 15.99F) / 256.0F);
      double v0 = (double)((float)yt / 256.0F);
      double v1 = (double)(((float)yt + 15.99F) / 256.0F);
      float o = 0.0F;
      float r = 0.03125F;
      boolean w = RedStoneDustTile.shouldConnectTo(this.level, x - 1, y, z) || !this.level.isSolidTile(x - 1, y, z) && RedStoneDustTile.shouldConnectTo(this.level, x - 1, y - 1, z);
      boolean e = RedStoneDustTile.shouldConnectTo(this.level, x + 1, y, z) || !this.level.isSolidTile(x + 1, y, z) && RedStoneDustTile.shouldConnectTo(this.level, x + 1, y - 1, z);
      boolean n = RedStoneDustTile.shouldConnectTo(this.level, x, y, z - 1) || !this.level.isSolidTile(x, y, z - 1) && RedStoneDustTile.shouldConnectTo(this.level, x, y - 1, z - 1);
      boolean s = RedStoneDustTile.shouldConnectTo(this.level, x, y, z + 1) || !this.level.isSolidTile(x, y, z + 1) && RedStoneDustTile.shouldConnectTo(this.level, x, y - 1, z + 1);
      if (!this.level.isSolidTile(x, y + 1, z)) {
         if (this.level.isSolidTile(x - 1, y, z) && RedStoneDustTile.shouldConnectTo(this.level, x - 1, y + 1, z)) {
            w = true;
         }

         if (this.level.isSolidTile(x + 1, y, z) && RedStoneDustTile.shouldConnectTo(this.level, x + 1, y + 1, z)) {
            e = true;
         }

         if (this.level.isSolidTile(x, y, z - 1) && RedStoneDustTile.shouldConnectTo(this.level, x, y + 1, z - 1)) {
            n = true;
         }

         if (this.level.isSolidTile(x, y, z + 1) && RedStoneDustTile.shouldConnectTo(this.level, x, y + 1, z + 1)) {
            s = true;
         }
      }

      float d = 0.3125F;
      float x0 = (float)(x + 0);
      float x1 = (float)(x + 1);
      float z0 = (float)(z + 0);
      float z1 = (float)(z + 1);
      int pic = 0;
      if ((w || e) && !n && !s) {
         pic = 1;
      }

      if ((n || s) && !e && !w) {
         pic = 2;
      }

      if (pic != 0) {
         u0 = (double)((float)(xt + 16) / 256.0F);
         u1 = (double)(((float)(xt + 16) + 15.99F) / 256.0F);
         v0 = (double)((float)yt / 256.0F);
         v1 = (double)(((float)yt + 15.99F) / 256.0F);
      }

      if (pic == 0) {
         if (e || n || s || w) {
            if (!w) {
               x0 += d;
            }

            if (!w) {
               u0 += (double)(d / 16.0F);
            }

            if (!e) {
               x1 -= d;
            }

            if (!e) {
               u1 -= (double)(d / 16.0F);
            }

            if (!n) {
               z0 += d;
            }

            if (!n) {
               v0 += (double)(d / 16.0F);
            }

            if (!s) {
               z1 -= d;
            }

            if (!s) {
               v1 -= (double)(d / 16.0F);
            }
         }

         t.vertexUV((double)(x1 + o), (double)((float)y + r), (double)(z1 + o), u1, v1);
         t.vertexUV((double)(x1 + o), (double)((float)y + r), (double)(z0 - o), u1, v0);
         t.vertexUV((double)(x0 - o), (double)((float)y + r), (double)(z0 - o), u0, v0);
         t.vertexUV((double)(x0 - o), (double)((float)y + r), (double)(z1 + o), u0, v1);
      }

      if (pic == 1) {
         t.vertexUV((double)(x1 + o), (double)((float)y + r), (double)(z1 + o), u1, v1);
         t.vertexUV((double)(x1 + o), (double)((float)y + r), (double)(z0 - o), u1, v0);
         t.vertexUV((double)(x0 - o), (double)((float)y + r), (double)(z0 - o), u0, v0);
         t.vertexUV((double)(x0 - o), (double)((float)y + r), (double)(z1 + o), u0, v1);
      }

      if (pic == 2) {
         t.vertexUV((double)(x1 + o), (double)((float)y + r), (double)(z1 + o), u1, v1);
         t.vertexUV((double)(x1 + o), (double)((float)y + r), (double)(z0 - o), u0, v1);
         t.vertexUV((double)(x0 - o), (double)((float)y + r), (double)(z0 - o), u0, v0);
         t.vertexUV((double)(x0 - o), (double)((float)y + r), (double)(z1 + o), u1, v0);
      }

      u0 = (double)((float)(xt + 16) / 256.0F);
      u1 = (double)(((float)(xt + 16) + 15.99F) / 256.0F);
      v0 = (double)((float)yt / 256.0F);
      v1 = (double)(((float)yt + 15.99F) / 256.0F);
      if (!this.level.isSolidTile(x, y + 1, z)) {
         if (this.level.isSolidTile(x - 1, y, z) && this.level.getTile(x - 1, y + 1, z) == Tile.redStoneDust.id) {
            t.vertexUV((double)((float)x + r), (double)((float)(y + 1) + o), (double)((float)(z + 1) + o), u1, v0);
            t.vertexUV((double)((float)x + r), (double)((float)(y + 0) - o), (double)((float)(z + 1) + o), u0, v0);
            t.vertexUV((double)((float)x + r), (double)((float)(y + 0) - o), (double)((float)(z + 0) - o), u0, v1);
            t.vertexUV((double)((float)x + r), (double)((float)(y + 1) + o), (double)((float)(z + 0) - o), u1, v1);
         }

         if (this.level.isSolidTile(x + 1, y, z) && this.level.getTile(x + 1, y + 1, z) == Tile.redStoneDust.id) {
            t.vertexUV((double)((float)(x + 1) - r), (double)((float)(y + 0) - o), (double)((float)(z + 1) + o), u0, v1);
            t.vertexUV((double)((float)(x + 1) - r), (double)((float)(y + 1) + o), (double)((float)(z + 1) + o), u1, v1);
            t.vertexUV((double)((float)(x + 1) - r), (double)((float)(y + 1) + o), (double)((float)(z + 0) - o), u1, v0);
            t.vertexUV((double)((float)(x + 1) - r), (double)((float)(y + 0) - o), (double)((float)(z + 0) - o), u0, v0);
         }

         if (this.level.isSolidTile(x, y, z - 1) && this.level.getTile(x, y + 1, z - 1) == Tile.redStoneDust.id) {
            t.vertexUV((double)((float)(x + 1) + o), (double)((float)(y + 0) - o), (double)((float)z + r), u0, v1);
            t.vertexUV((double)((float)(x + 1) + o), (double)((float)(y + 1) + o), (double)((float)z + r), u1, v1);
            t.vertexUV((double)((float)(x + 0) - o), (double)((float)(y + 1) + o), (double)((float)z + r), u1, v0);
            t.vertexUV((double)((float)(x + 0) - o), (double)((float)(y + 0) - o), (double)((float)z + r), u0, v0);
         }

         if (this.level.isSolidTile(x, y, z + 1) && this.level.getTile(x, y + 1, z + 1) == Tile.redStoneDust.id) {
            t.vertexUV((double)((float)(x + 1) + o), (double)((float)(y + 1) + o), (double)((float)(z + 1) - r), u1, v0);
            t.vertexUV((double)((float)(x + 1) + o), (double)((float)(y + 0) - o), (double)((float)(z + 1) - r), u0, v0);
            t.vertexUV((double)((float)(x + 0) - o), (double)((float)(y + 0) - o), (double)((float)(z + 1) - r), u0, v1);
            t.vertexUV((double)((float)(x + 0) - o), (double)((float)(y + 1) + o), (double)((float)(z + 1) - r), u1, v1);
         }
      }

      return true;
   }

   public boolean tesselateRailInWorld(Tile tt, int x, int y, int z) {
      Tesselator t = Tesselator.instance;
      int data = this.level.getData(x, y, z);
      int tex = tt.getTexture(0, data);
      if (this.fixedTexture >= 0) {
         tex = this.fixedTexture;
      }

      float br = tt.getBrightness(this.level, x, y, z);
      t.color(br, br, br);
      int xt = (tex & 15) << 4;
      int yt = tex & 240;
      double u0 = (double)((float)xt / 256.0F);
      double u1 = (double)(((float)xt + 15.99F) / 256.0F);
      double v0 = (double)((float)yt / 256.0F);
      double v1 = (double)(((float)yt + 15.99F) / 256.0F);
      float r = 0.0625F;
      float x0 = (float)(x + 1);
      float x1 = (float)(x + 1);
      float x2 = (float)(x + 0);
      float x3 = (float)(x + 0);
      float z0 = (float)(z + 0);
      float z1 = (float)(z + 1);
      float z2 = (float)(z + 1);
      float z3 = (float)(z + 0);
      float y0 = (float)y + r;
      float y1 = (float)y + r;
      float y2 = (float)y + r;
      float y3 = (float)y + r;
      if (data != 1 && data != 2 && data != 3 && data != 7) {
         if (data == 8) {
            x0 = x1 = (float)(x + 0);
            x2 = x3 = (float)(x + 1);
            z0 = z3 = (float)(z + 1);
            z1 = z2 = (float)(z + 0);
         } else if (data == 9) {
            x0 = x3 = (float)(x + 0);
            x1 = x2 = (float)(x + 1);
            z0 = z1 = (float)(z + 0);
            z2 = z3 = (float)(z + 1);
         }
      } else {
         x0 = x3 = (float)(x + 1);
         x1 = x2 = (float)(x + 0);
         z0 = z1 = (float)(z + 1);
         z2 = z3 = (float)(z + 0);
      }

      if (data != 2 && data != 4) {
         if (data == 3 || data == 5) {
            ++y1;
            ++y2;
         }
      } else {
         ++y0;
         ++y3;
      }

      t.vertexUV((double)x0, (double)y0, (double)z0, u1, v0);
      t.vertexUV((double)x1, (double)y1, (double)z1, u1, v1);
      t.vertexUV((double)x2, (double)y2, (double)z2, u0, v1);
      t.vertexUV((double)x3, (double)y3, (double)z3, u0, v0);
      t.vertexUV((double)x3, (double)y3, (double)z3, u0, v0);
      t.vertexUV((double)x2, (double)y2, (double)z2, u0, v1);
      t.vertexUV((double)x1, (double)y1, (double)z1, u1, v1);
      t.vertexUV((double)x0, (double)y0, (double)z0, u1, v0);
      return true;
   }

   public boolean tesselateLadderInWorld(Tile tt, int x, int y, int z) {
      Tesselator t = Tesselator.instance;
      int tex = tt.getTexture(0);
      if (this.fixedTexture >= 0) {
         tex = this.fixedTexture;
      }

      float br = tt.getBrightness(this.level, x, y, z);
      t.color(br, br, br);
      int xt = (tex & 15) << 4;
      int yt = tex & 240;
      double u0 = (double)((float)xt / 256.0F);
      double u1 = (double)(((float)xt + 15.99F) / 256.0F);
      double v0 = (double)((float)yt / 256.0F);
      double v1 = (double)(((float)yt + 15.99F) / 256.0F);
      int face = this.level.getData(x, y, z);
      float o = 0.0F;
      float r = 0.05F;
      if (face == 5) {
         t.vertexUV((double)((float)x + r), (double)((float)(y + 1) + o), (double)((float)(z + 1) + o), u0, v0);
         t.vertexUV((double)((float)x + r), (double)((float)(y + 0) - o), (double)((float)(z + 1) + o), u0, v1);
         t.vertexUV((double)((float)x + r), (double)((float)(y + 0) - o), (double)((float)(z + 0) - o), u1, v1);
         t.vertexUV((double)((float)x + r), (double)((float)(y + 1) + o), (double)((float)(z + 0) - o), u1, v0);
      }

      if (face == 4) {
         t.vertexUV((double)((float)(x + 1) - r), (double)((float)(y + 0) - o), (double)((float)(z + 1) + o), u1, v1);
         t.vertexUV((double)((float)(x + 1) - r), (double)((float)(y + 1) + o), (double)((float)(z + 1) + o), u1, v0);
         t.vertexUV((double)((float)(x + 1) - r), (double)((float)(y + 1) + o), (double)((float)(z + 0) - o), u0, v0);
         t.vertexUV((double)((float)(x + 1) - r), (double)((float)(y + 0) - o), (double)((float)(z + 0) - o), u0, v1);
      }

      if (face == 3) {
         t.vertexUV((double)((float)(x + 1) + o), (double)((float)(y + 0) - o), (double)((float)z + r), u1, v1);
         t.vertexUV((double)((float)(x + 1) + o), (double)((float)(y + 1) + o), (double)((float)z + r), u1, v0);
         t.vertexUV((double)((float)(x + 0) - o), (double)((float)(y + 1) + o), (double)((float)z + r), u0, v0);
         t.vertexUV((double)((float)(x + 0) - o), (double)((float)(y + 0) - o), (double)((float)z + r), u0, v1);
      }

      if (face == 2) {
         t.vertexUV((double)((float)(x + 1) + o), (double)((float)(y + 1) + o), (double)((float)(z + 1) - r), u0, v0);
         t.vertexUV((double)((float)(x + 1) + o), (double)((float)(y + 0) - o), (double)((float)(z + 1) - r), u0, v1);
         t.vertexUV((double)((float)(x + 0) - o), (double)((float)(y + 0) - o), (double)((float)(z + 1) - r), u1, v1);
         t.vertexUV((double)((float)(x + 0) - o), (double)((float)(y + 1) + o), (double)((float)(z + 1) - r), u1, v0);
      }

      return true;
   }

   public boolean tesselateCrossInWorld(Tile tt, int x, int y, int z) {
      Tesselator t = Tesselator.instance;
      float br = tt.getBrightness(this.level, x, y, z);
      t.color(br, br, br);
      this.tesselateCrossTexture(tt, this.level.getData(x, y, z), (double)x, (double)y, (double)z);
      return true;
   }

   public boolean tesselateRowInWorld(Tile tt, int x, int y, int z) {
      Tesselator t = Tesselator.instance;
      float br = tt.getBrightness(this.level, x, y, z);
      t.color(br, br, br);
      this.tesselateRowTexture(tt, this.level.getData(x, y, z), (double)x, (double)((float)y - 0.0625F), (double)z);
      return true;
   }

   public void tesselateTorch(Tile tt, double x, double y, double z, double xxa, double zza) {
      Tesselator t = Tesselator.instance;
      int tex = tt.getTexture(0);
      if (this.fixedTexture >= 0) {
         tex = this.fixedTexture;
      }

      int xt = (tex & 15) << 4;
      int yt = tex & 240;
      float u0 = (float)xt / 256.0F;
      float u1 = ((float)xt + 15.99F) / 256.0F;
      float v0 = (float)yt / 256.0F;
      float v1 = ((float)yt + 15.99F) / 256.0F;
      double uc0 = (double)u0 + (double)0.02734375F;
      double vc0 = (double)v0 + (double)0.0234375F;
      double uc1 = (double)u0 + (double)0.03515625F;
      double vc1 = (double)v0 + (double)0.03125F;
      x += (double)0.5F;
      z += (double)0.5F;
      double x0 = x - (double)0.5F;
      double x1 = x + (double)0.5F;
      double z0 = z - (double)0.5F;
      double z1 = z + (double)0.5F;
      double r = (double)0.0625F;
      double h = (double)0.625F;
      t.vertexUV(x + xxa * ((double)1.0F - h) - r, y + h, z + zza * ((double)1.0F - h) - r, uc0, vc0);
      t.vertexUV(x + xxa * ((double)1.0F - h) - r, y + h, z + zza * ((double)1.0F - h) + r, uc0, vc1);
      t.vertexUV(x + xxa * ((double)1.0F - h) + r, y + h, z + zza * ((double)1.0F - h) + r, uc1, vc1);
      t.vertexUV(x + xxa * ((double)1.0F - h) + r, y + h, z + zza * ((double)1.0F - h) - r, uc1, vc0);
      t.vertexUV(x - r, y + (double)1.0F, z0, (double)u0, (double)v0);
      t.vertexUV(x - r + xxa, y + (double)0.0F, z0 + zza, (double)u0, (double)v1);
      t.vertexUV(x - r + xxa, y + (double)0.0F, z1 + zza, (double)u1, (double)v1);
      t.vertexUV(x - r, y + (double)1.0F, z1, (double)u1, (double)v0);
      t.vertexUV(x + r, y + (double)1.0F, z1, (double)u0, (double)v0);
      t.vertexUV(x + xxa + r, y + (double)0.0F, z1 + zza, (double)u0, (double)v1);
      t.vertexUV(x + xxa + r, y + (double)0.0F, z0 + zza, (double)u1, (double)v1);
      t.vertexUV(x + r, y + (double)1.0F, z0, (double)u1, (double)v0);
      t.vertexUV(x0, y + (double)1.0F, z + r, (double)u0, (double)v0);
      t.vertexUV(x0 + xxa, y + (double)0.0F, z + r + zza, (double)u0, (double)v1);
      t.vertexUV(x1 + xxa, y + (double)0.0F, z + r + zza, (double)u1, (double)v1);
      t.vertexUV(x1, y + (double)1.0F, z + r, (double)u1, (double)v0);
      t.vertexUV(x1, y + (double)1.0F, z - r, (double)u0, (double)v0);
      t.vertexUV(x1 + xxa, y + (double)0.0F, z - r + zza, (double)u0, (double)v1);
      t.vertexUV(x0 + xxa, y + (double)0.0F, z - r + zza, (double)u1, (double)v1);
      t.vertexUV(x0, y + (double)1.0F, z - r, (double)u1, (double)v0);
   }

   public void tesselateCrossTexture(Tile tt, int data, double x, double y, double z) {
      Tesselator t = Tesselator.instance;
      int tex = tt.getTexture(0, data);
      if (this.fixedTexture >= 0) {
         tex = this.fixedTexture;
      }

      int xt = (tex & 15) << 4;
      int yt = tex & 240;
      double u0 = (double)((float)xt / 256.0F);
      double u1 = (double)(((float)xt + 15.99F) / 256.0F);
      double v0 = (double)((float)yt / 256.0F);
      double v1 = (double)(((float)yt + 15.99F) / 256.0F);
      double x0 = x + (double)0.5F - (double)0.45F;
      double x1 = x + (double)0.5F + (double)0.45F;
      double z0 = z + (double)0.5F - (double)0.45F;
      double z1 = z + (double)0.5F + (double)0.45F;
      t.vertexUV(x0, y + (double)1.0F, z0, u0, v0);
      t.vertexUV(x0, y + (double)0.0F, z0, u0, v1);
      t.vertexUV(x1, y + (double)0.0F, z1, u1, v1);
      t.vertexUV(x1, y + (double)1.0F, z1, u1, v0);
      t.vertexUV(x1, y + (double)1.0F, z1, u0, v0);
      t.vertexUV(x1, y + (double)0.0F, z1, u0, v1);
      t.vertexUV(x0, y + (double)0.0F, z0, u1, v1);
      t.vertexUV(x0, y + (double)1.0F, z0, u1, v0);
      t.vertexUV(x0, y + (double)1.0F, z1, u0, v0);
      t.vertexUV(x0, y + (double)0.0F, z1, u0, v1);
      t.vertexUV(x1, y + (double)0.0F, z0, u1, v1);
      t.vertexUV(x1, y + (double)1.0F, z0, u1, v0);
      t.vertexUV(x1, y + (double)1.0F, z0, u0, v0);
      t.vertexUV(x1, y + (double)0.0F, z0, u0, v1);
      t.vertexUV(x0, y + (double)0.0F, z1, u1, v1);
      t.vertexUV(x0, y + (double)1.0F, z1, u1, v0);
   }

   public void tesselateRowTexture(Tile tt, int data, double x, double y, double z) {
      Tesselator t = Tesselator.instance;
      int tex = tt.getTexture(0, data);
      if (this.fixedTexture >= 0) {
         tex = this.fixedTexture;
      }

      int xt = (tex & 15) << 4;
      int yt = tex & 240;
      double u0 = (double)((float)xt / 256.0F);
      double u1 = (double)(((float)xt + 15.99F) / 256.0F);
      double v0 = (double)((float)yt / 256.0F);
      double v1 = (double)(((float)yt + 15.99F) / 256.0F);
      double x0 = x + (double)0.5F - (double)0.25F;
      double x1 = x + (double)0.5F + (double)0.25F;
      double z0 = z + (double)0.5F - (double)0.5F;
      double z1 = z + (double)0.5F + (double)0.5F;
      t.vertexUV(x0, y + (double)1.0F, z0, u0, v0);
      t.vertexUV(x0, y + (double)0.0F, z0, u0, v1);
      t.vertexUV(x0, y + (double)0.0F, z1, u1, v1);
      t.vertexUV(x0, y + (double)1.0F, z1, u1, v0);
      t.vertexUV(x0, y + (double)1.0F, z1, u0, v0);
      t.vertexUV(x0, y + (double)0.0F, z1, u0, v1);
      t.vertexUV(x0, y + (double)0.0F, z0, u1, v1);
      t.vertexUV(x0, y + (double)1.0F, z0, u1, v0);
      t.vertexUV(x1, y + (double)1.0F, z1, u0, v0);
      t.vertexUV(x1, y + (double)0.0F, z1, u0, v1);
      t.vertexUV(x1, y + (double)0.0F, z0, u1, v1);
      t.vertexUV(x1, y + (double)1.0F, z0, u1, v0);
      t.vertexUV(x1, y + (double)1.0F, z0, u0, v0);
      t.vertexUV(x1, y + (double)0.0F, z0, u0, v1);
      t.vertexUV(x1, y + (double)0.0F, z1, u1, v1);
      t.vertexUV(x1, y + (double)1.0F, z1, u1, v0);
      x0 = x + (double)0.5F - (double)0.5F;
      x1 = x + (double)0.5F + (double)0.5F;
      z0 = z + (double)0.5F - (double)0.25F;
      z1 = z + (double)0.5F + (double)0.25F;
      t.vertexUV(x0, y + (double)1.0F, z0, u0, v0);
      t.vertexUV(x0, y + (double)0.0F, z0, u0, v1);
      t.vertexUV(x1, y + (double)0.0F, z0, u1, v1);
      t.vertexUV(x1, y + (double)1.0F, z0, u1, v0);
      t.vertexUV(x1, y + (double)1.0F, z0, u0, v0);
      t.vertexUV(x1, y + (double)0.0F, z0, u0, v1);
      t.vertexUV(x0, y + (double)0.0F, z0, u1, v1);
      t.vertexUV(x0, y + (double)1.0F, z0, u1, v0);
      t.vertexUV(x1, y + (double)1.0F, z1, u0, v0);
      t.vertexUV(x1, y + (double)0.0F, z1, u0, v1);
      t.vertexUV(x0, y + (double)0.0F, z1, u1, v1);
      t.vertexUV(x0, y + (double)1.0F, z1, u1, v0);
      t.vertexUV(x0, y + (double)1.0F, z1, u0, v0);
      t.vertexUV(x0, y + (double)0.0F, z1, u0, v1);
      t.vertexUV(x1, y + (double)0.0F, z1, u1, v1);
      t.vertexUV(x1, y + (double)1.0F, z1, u1, v0);
   }

   public boolean tesselateWaterInWorld(Tile tt, int x, int y, int z) {
      Tesselator t = Tesselator.instance;
      boolean up = tt.shouldRenderFace(this.level, x, y + 1, z, 1);
      boolean down = tt.shouldRenderFace(this.level, x, y - 1, z, 0);
      boolean[] dirs = new boolean[]{tt.shouldRenderFace(this.level, x, y, z - 1, 2), tt.shouldRenderFace(this.level, x, y, z + 1, 3), tt.shouldRenderFace(this.level, x - 1, y, z, 4), tt.shouldRenderFace(this.level, x + 1, y, z, 5)};
      if (!up && !down && !dirs[0] && !dirs[1] && !dirs[2] && !dirs[3]) {
         return false;
      } else {
         boolean changed = false;
         float c10 = 0.5F;
         float c11 = 1.0F;
         float c2 = 0.8F;
         float c3 = 0.6F;
         double yo0 = (double)0.0F;
         double yo1 = (double)1.0F;
         Material m = tt.material;
         int data = this.level.getData(x, y, z);
         float h0 = this.getWaterHeight(x, y, z, m);
         float h1 = this.getWaterHeight(x, y, z + 1, m);
         float h2 = this.getWaterHeight(x + 1, y, z + 1, m);
         float h3 = this.getWaterHeight(x + 1, y, z, m);
         if (this.noCulling || up) {
            changed = true;
            int tex = tt.getTexture(1, data);
            float angle = (float)LiquidTile.getSlopeAngle(this.level, x, y, z, m);
            if (angle > -999.0F) {
               tex = tt.getTexture(2, data);
            }

            int xt = (tex & 15) << 4;
            int yt = tex & 240;
            double uc = ((double)xt + (double)8.0F) / (double)256.0F;
            double vc = ((double)yt + (double)8.0F) / (double)256.0F;
            if (angle < -999.0F) {
               angle = 0.0F;
            } else {
               uc = (double)((float)(xt + 16) / 256.0F);
               vc = (double)((float)(yt + 16) / 256.0F);
            }

            float s = Mth.sin(angle) * 8.0F / 256.0F;
            float c = Mth.cos(angle) * 8.0F / 256.0F;
            float br = tt.getBrightness(this.level, x, y, z);
            t.color(c11 * br, c11 * br, c11 * br);
            t.vertexUV((double)(x + 0), (double)((float)y + h0), (double)(z + 0), uc - (double)c - (double)s, vc - (double)c + (double)s);
            t.vertexUV((double)(x + 0), (double)((float)y + h1), (double)(z + 1), uc - (double)c + (double)s, vc + (double)c + (double)s);
            t.vertexUV((double)(x + 1), (double)((float)y + h2), (double)(z + 1), uc + (double)c + (double)s, vc + (double)c - (double)s);
            t.vertexUV((double)(x + 1), (double)((float)y + h3), (double)(z + 0), uc + (double)c - (double)s, vc - (double)c - (double)s);
         }

         if (this.noCulling || down) {
            float br = tt.getBrightness(this.level, x, y - 1, z);
            t.color(c10 * br, c10 * br, c10 * br);
            this.renderFaceUp(tt, (double)x, (double)y, (double)z, tt.getTexture(0));
            changed = true;
         }

         for(int face = 0; face < 4; ++face) {
            int xt = x;
            int zt = z;
            if (face == 0) {
               zt = z - 1;
            }

            if (face == 1) {
               ++zt;
            }

            if (face == 2) {
               xt = x - 1;
            }

            if (face == 3) {
               ++xt;
            }

            int tex = tt.getTexture(face + 2, data);
            int xTex = (tex & 15) << 4;
            int yTex = tex & 240;
            if (this.noCulling || dirs[face]) {
               float hh0;
               float x1;
               float z1;
               float hh1;
               float x0;
               float z0;
               if (face == 0) {
                  hh0 = h0;
                  hh1 = h3;
                  x0 = (float)x;
                  x1 = (float)(x + 1);
                  z0 = (float)z;
                  z1 = (float)z;
               } else if (face == 1) {
                  hh0 = h2;
                  hh1 = h1;
                  x0 = (float)(x + 1);
                  x1 = (float)x;
                  z0 = (float)(z + 1);
                  z1 = (float)(z + 1);
               } else if (face == 2) {
                  hh0 = h1;
                  hh1 = h0;
                  x0 = (float)x;
                  x1 = (float)x;
                  z0 = (float)(z + 1);
                  z1 = (float)z;
               } else {
                  hh0 = h3;
                  hh1 = h2;
                  x0 = (float)(x + 1);
                  x1 = (float)(x + 1);
                  z0 = (float)z;
                  z1 = (float)(z + 1);
               }

               changed = true;
               double u0 = (double)((float)(xTex + 0) / 256.0F);
               double u1 = ((double)(xTex + 16) - 0.01) / (double)256.0F;
               double v01 = (double)(((float)yTex + (1.0F - hh0) * 16.0F) / 256.0F);
               double v02 = (double)(((float)yTex + (1.0F - hh1) * 16.0F) / 256.0F);
               double v1 = ((double)(yTex + 16) - 0.01) / (double)256.0F;
               float br = tt.getBrightness(this.level, xt, y, zt);
               if (face < 2) {
                  br *= c2;
               } else {
                  br *= c3;
               }

               t.color(c11 * br, c11 * br, c11 * br);
               t.vertexUV((double)x0, (double)((float)y + hh0), (double)z0, u0, v01);
               t.vertexUV((double)x1, (double)((float)y + hh1), (double)z1, u1, v02);
               t.vertexUV((double)x1, (double)(y + 0), (double)z1, u1, v1);
               t.vertexUV((double)x0, (double)(y + 0), (double)z0, u0, v1);
            }
         }

         tt.yy0 = yo0;
         tt.yy1 = yo1;
         return changed;
      }
   }

   private float getWaterHeight(int x, int y, int z, Material m) {
      int count = 0;
      float h = 0.0F;

      for(int i = 0; i < 4; ++i) {
         int xx = x - (i & 1);
         int zz = z - (i >> 1 & 1);
         if (this.level.getMaterial(xx, y + 1, zz) == m) {
            return 1.0F;
         }

         Material tm = this.level.getMaterial(xx, y, zz);
         if (tm == m) {
            int d = this.level.getData(xx, y, zz);
            if (d >= 8 || d == 0) {
               h += LiquidTile.getHeight(d) * 10.0F;
               count += 10;
            }

            h += LiquidTile.getHeight(d);
            ++count;
         } else if (!tm.isSolid()) {
            ++h;
            ++count;
         }
      }

      return 1.0F - h / (float)count;
   }

   public void renderBlock(Tile tt, Level level, int x, int y, int z) {
      float c10 = 0.5F;
      float c11 = 1.0F;
      float c2 = 0.8F;
      float c3 = 0.6F;
      Tesselator t = Tesselator.instance;
      t.begin();
      float center = tt.getBrightness(level, x, y, z);
      float br = tt.getBrightness(level, x, y - 1, z);
      if (br < center) {
         br = center;
      }

      t.color(c10 * br, c10 * br, c10 * br);
      this.renderFaceUp(tt, (double)-0.5F, (double)-0.5F, (double)-0.5F, tt.getTexture(0));
      br = tt.getBrightness(level, x, y + 1, z);
      if (br < center) {
         br = center;
      }

      t.color(c11 * br, c11 * br, c11 * br);
      this.renderFaceDown(tt, (double)-0.5F, (double)-0.5F, (double)-0.5F, tt.getTexture(1));
      br = tt.getBrightness(level, x, y, z - 1);
      if (br < center) {
         br = center;
      }

      t.color(c2 * br, c2 * br, c2 * br);
      this.renderNorth(tt, (double)-0.5F, (double)-0.5F, (double)-0.5F, tt.getTexture(2));
      br = tt.getBrightness(level, x, y, z + 1);
      if (br < center) {
         br = center;
      }

      t.color(c2 * br, c2 * br, c2 * br);
      this.renderSouth(tt, (double)-0.5F, (double)-0.5F, (double)-0.5F, tt.getTexture(3));
      br = tt.getBrightness(level, x - 1, y, z);
      if (br < center) {
         br = center;
      }

      t.color(c3 * br, c3 * br, c3 * br);
      this.renderWest(tt, (double)-0.5F, (double)-0.5F, (double)-0.5F, tt.getTexture(4));
      br = tt.getBrightness(level, x + 1, y, z);
      if (br < center) {
         br = center;
      }

      t.color(c3 * br, c3 * br, c3 * br);
      this.renderEast(tt, (double)-0.5F, (double)-0.5F, (double)-0.5F, tt.getTexture(5));
      t.end();
   }

   public boolean tesselateBlockInWorld(Tile tt, int x, int y, int z) {
      int col = tt.getColor(this.level, x, y, z);
      float r = (float)(col >> 16 & 255) / 255.0F;
      float g = (float)(col >> 8 & 255) / 255.0F;
      float b = (float)(col & 255) / 255.0F;
      return this.tesselateBlockInWorld(tt, x, y, z, r, g, b);
   }

   public boolean tesselateBlockInWorld(Tile tt, int x, int y, int z, float r, float g, float b) {
      Tesselator t = Tesselator.instance;
      boolean changed = false;
      float c10 = 0.5F;
      float c11 = 1.0F;
      float c2 = 0.8F;
      float c3 = 0.6F;
      float r11 = c11 * r;
      float g11 = c11 * g;
      float b11 = c11 * b;
      if (tt == Tile.grass) {
         b = 1.0F;
         g = 1.0F;
         r = 1.0F;
      }

      float r10 = c10 * r;
      float r2 = c2 * r;
      float r3 = c3 * r;
      float g10 = c10 * g;
      float g2 = c2 * g;
      float g3 = c3 * g;
      float b10 = c10 * b;
      float b2 = c2 * b;
      float b3 = c3 * b;
      float centerBrightness = tt.getBrightness(this.level, x, y, z);
      if (this.noCulling || tt.shouldRenderFace(this.level, x, y - 1, z, 0)) {
         float br = tt.getBrightness(this.level, x, y - 1, z);
         t.color(r10 * br, g10 * br, b10 * br);
         this.renderFaceUp(tt, (double)x, (double)y, (double)z, tt.getTexture(this.level, x, y, z, 0));
         changed = true;
      }

      if (this.noCulling || tt.shouldRenderFace(this.level, x, y + 1, z, 1)) {
         float br = tt.getBrightness(this.level, x, y + 1, z);
         if (tt.yy1 != (double)1.0F && !tt.material.isLiquid()) {
            br = centerBrightness;
         }

         t.color(r11 * br, g11 * br, b11 * br);
         this.renderFaceDown(tt, (double)x, (double)y, (double)z, tt.getTexture(this.level, x, y, z, 1));
         changed = true;
      }

      if (this.noCulling || tt.shouldRenderFace(this.level, x, y, z - 1, 2)) {
         float br = tt.getBrightness(this.level, x, y, z - 1);
         if (tt.zz0 > (double)0.0F) {
            br = centerBrightness;
         }

         t.color(r2 * br, g2 * br, b2 * br);
         this.renderNorth(tt, (double)x, (double)y, (double)z, tt.getTexture(this.level, x, y, z, 2));
         changed = true;
      }

      if (this.noCulling || tt.shouldRenderFace(this.level, x, y, z + 1, 3)) {
         float br = tt.getBrightness(this.level, x, y, z + 1);
         if (tt.zz1 < (double)1.0F) {
            br = centerBrightness;
         }

         t.color(r2 * br, g2 * br, b2 * br);
         this.renderSouth(tt, (double)x, (double)y, (double)z, tt.getTexture(this.level, x, y, z, 3));
         changed = true;
      }

      if (this.noCulling || tt.shouldRenderFace(this.level, x - 1, y, z, 4)) {
         float br = tt.getBrightness(this.level, x - 1, y, z);
         if (tt.xx0 > (double)0.0F) {
            br = centerBrightness;
         }

         t.color(r3 * br, g3 * br, b3 * br);
         this.renderWest(tt, (double)x, (double)y, (double)z, tt.getTexture(this.level, x, y, z, 4));
         changed = true;
      }

      if (this.noCulling || tt.shouldRenderFace(this.level, x + 1, y, z, 5)) {
         float br = tt.getBrightness(this.level, x + 1, y, z);
         if (tt.xx1 < (double)1.0F) {
            br = centerBrightness;
         }

         t.color(r3 * br, g3 * br, b3 * br);
         this.renderEast(tt, (double)x, (double)y, (double)z, tt.getTexture(this.level, x, y, z, 5));
         changed = true;
      }

      return changed;
   }

   public boolean tesselateCactusInWorld(Tile tt, int x, int y, int z) {
      int col = tt.getColor(this.level, x, y, z);
      float r = (float)(col >> 16 & 255) / 255.0F;
      float g = (float)(col >> 8 & 255) / 255.0F;
      float b = (float)(col & 255) / 255.0F;
      return this.tesselateCactusInWorld(tt, x, y, z, r, g, b);
   }

   public boolean tesselateCactusInWorld(Tile tt, int x, int y, int z, float r, float g, float b) {
      Tesselator t = Tesselator.instance;
      boolean changed = false;
      float c10 = 0.5F;
      float c11 = 1.0F;
      float c2 = 0.8F;
      float c3 = 0.6F;
      float r10 = c10 * r;
      float r11 = c11 * r;
      float r2 = c2 * r;
      float r3 = c3 * r;
      float g10 = c10 * g;
      float g11 = c11 * g;
      float g2 = c2 * g;
      float g3 = c3 * g;
      float b10 = c10 * b;
      float b11 = c11 * b;
      float b2 = c2 * b;
      float b3 = c3 * b;
      float s = 0.0625F;
      float centerBrightness = tt.getBrightness(this.level, x, y, z);
      if (this.noCulling || tt.shouldRenderFace(this.level, x, y - 1, z, 0)) {
         float br = tt.getBrightness(this.level, x, y - 1, z);
         t.color(r10 * br, g10 * br, b10 * br);
         this.renderFaceUp(tt, (double)x, (double)y, (double)z, tt.getTexture(this.level, x, y, z, 0));
         changed = true;
      }

      if (this.noCulling || tt.shouldRenderFace(this.level, x, y + 1, z, 1)) {
         float br = tt.getBrightness(this.level, x, y + 1, z);
         if (tt.yy1 != (double)1.0F && !tt.material.isLiquid()) {
            br = centerBrightness;
         }

         t.color(r11 * br, g11 * br, b11 * br);
         this.renderFaceDown(tt, (double)x, (double)y, (double)z, tt.getTexture(this.level, x, y, z, 1));
         changed = true;
      }

      if (this.noCulling || tt.shouldRenderFace(this.level, x, y, z - 1, 2)) {
         float br = tt.getBrightness(this.level, x, y, z - 1);
         if (tt.zz0 > (double)0.0F) {
            br = centerBrightness;
         }

         t.color(r2 * br, g2 * br, b2 * br);
         t.addOffset(0.0F, 0.0F, s);
         this.renderNorth(tt, (double)x, (double)y, (double)z, tt.getTexture(this.level, x, y, z, 2));
         t.addOffset(0.0F, 0.0F, -s);
         changed = true;
      }

      if (this.noCulling || tt.shouldRenderFace(this.level, x, y, z + 1, 3)) {
         float br = tt.getBrightness(this.level, x, y, z + 1);
         if (tt.zz1 < (double)1.0F) {
            br = centerBrightness;
         }

         t.color(r2 * br, g2 * br, b2 * br);
         t.addOffset(0.0F, 0.0F, -s);
         this.renderSouth(tt, (double)x, (double)y, (double)z, tt.getTexture(this.level, x, y, z, 3));
         t.addOffset(0.0F, 0.0F, s);
         changed = true;
      }

      if (this.noCulling || tt.shouldRenderFace(this.level, x - 1, y, z, 4)) {
         float br = tt.getBrightness(this.level, x - 1, y, z);
         if (tt.xx0 > (double)0.0F) {
            br = centerBrightness;
         }

         t.color(r3 * br, g3 * br, b3 * br);
         t.addOffset(s, 0.0F, 0.0F);
         this.renderWest(tt, (double)x, (double)y, (double)z, tt.getTexture(this.level, x, y, z, 4));
         t.addOffset(-s, 0.0F, 0.0F);
         changed = true;
      }

      if (this.noCulling || tt.shouldRenderFace(this.level, x + 1, y, z, 5)) {
         float br = tt.getBrightness(this.level, x + 1, y, z);
         if (tt.xx1 < (double)1.0F) {
            br = centerBrightness;
         }

         t.color(r3 * br, g3 * br, b3 * br);
         t.addOffset(-s, 0.0F, 0.0F);
         this.renderEast(tt, (double)x, (double)y, (double)z, tt.getTexture(this.level, x, y, z, 5));
         t.addOffset(s, 0.0F, 0.0F);
         changed = true;
      }

      return changed;
   }

   public boolean tesselateFenceInWorld(Tile tt, int x, int y, int z) {
      boolean changed = false;
      float a = 0.375F;
      float b = 0.625F;
      tt.setShape(a, 0.0F, a, b, 1.0F, b);
      this.tesselateBlockInWorld(tt, x, y, z);
      boolean vertical = false;
      boolean horizontal = false;
      if (this.level.getTile(x - 1, y, z) == tt.id || this.level.getTile(x + 1, y, z) == tt.id) {
         vertical = true;
      }

      if (this.level.getTile(x, y, z - 1) == tt.id || this.level.getTile(x, y, z + 1) == tt.id) {
         horizontal = true;
      }

      boolean l = this.level.getTile(x - 1, y, z) == tt.id;
      boolean r = this.level.getTile(x + 1, y, z) == tt.id;
      boolean u = this.level.getTile(x, y, z - 1) == tt.id;
      boolean d = this.level.getTile(x, y, z + 1) == tt.id;
      if (!vertical && !horizontal) {
         vertical = true;
      }

      a = 0.4375F;
      b = 0.5625F;
      float h0 = 0.75F;
      float h1 = 0.9375F;
      float x0 = l ? 0.0F : a;
      float x1 = r ? 1.0F : b;
      float z0 = u ? 0.0F : a;
      float z1 = d ? 1.0F : b;
      if (vertical) {
         tt.setShape(x0, h0, a, x1, h1, b);
         this.tesselateBlockInWorld(tt, x, y, z);
      }

      if (horizontal) {
         tt.setShape(a, h0, z0, b, h1, z1);
         this.tesselateBlockInWorld(tt, x, y, z);
      }

      h0 = 0.375F;
      h1 = 0.5625F;
      if (vertical) {
         tt.setShape(x0, h0, a, x1, h1, b);
         this.tesselateBlockInWorld(tt, x, y, z);
      }

      if (horizontal) {
         tt.setShape(a, h0, z0, b, h1, z1);
         this.tesselateBlockInWorld(tt, x, y, z);
      }

      tt.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      return changed;
   }

   public boolean tesselateStairsInWorld(Tile tt, int x, int y, int z) {
      boolean changed = false;
      int dir = this.level.getData(x, y, z);
      if (dir == 0) {
         tt.setShape(0.0F, 0.0F, 0.0F, 0.5F, 0.5F, 1.0F);
         this.tesselateBlockInWorld(tt, x, y, z);
         tt.setShape(0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         this.tesselateBlockInWorld(tt, x, y, z);
      } else if (dir == 1) {
         tt.setShape(0.0F, 0.0F, 0.0F, 0.5F, 1.0F, 1.0F);
         this.tesselateBlockInWorld(tt, x, y, z);
         tt.setShape(0.5F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
         this.tesselateBlockInWorld(tt, x, y, z);
      } else if (dir == 2) {
         tt.setShape(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 0.5F);
         this.tesselateBlockInWorld(tt, x, y, z);
         tt.setShape(0.0F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F);
         this.tesselateBlockInWorld(tt, x, y, z);
      } else if (dir == 3) {
         tt.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.5F);
         this.tesselateBlockInWorld(tt, x, y, z);
         tt.setShape(0.0F, 0.0F, 0.5F, 1.0F, 0.5F, 1.0F);
         this.tesselateBlockInWorld(tt, x, y, z);
      }

      tt.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      return changed;
   }

   public boolean tesselateDoorInWorld(Tile tt, int x, int y, int z) {
      Tesselator t = Tesselator.instance;
      DoorTile dt = (DoorTile)tt;
      boolean changed = false;
      float c10 = 0.5F;
      float c11 = 1.0F;
      float c2 = 0.8F;
      float c3 = 0.6F;
      float centerBrightness = tt.getBrightness(this.level, x, y, z);
      float br = tt.getBrightness(this.level, x, y - 1, z);
      if (dt.yy0 > (double)0.0F) {
         br = centerBrightness;
      }

      if (Tile.lightEmission[tt.id] > 0) {
         br = 1.0F;
      }

      t.color(c10 * br, c10 * br, c10 * br);
      this.renderFaceUp(tt, (double)x, (double)y, (double)z, tt.getTexture(this.level, x, y, z, 0));
      changed = true;
      br = tt.getBrightness(this.level, x, y + 1, z);
      if (dt.yy1 < (double)1.0F) {
         br = centerBrightness;
      }

      if (Tile.lightEmission[tt.id] > 0) {
         br = 1.0F;
      }

      t.color(c11 * br, c11 * br, c11 * br);
      this.renderFaceDown(tt, (double)x, (double)y, (double)z, tt.getTexture(this.level, x, y, z, 1));
      changed = true;
      br = tt.getBrightness(this.level, x, y, z - 1);
      if (dt.zz0 > (double)0.0F) {
         br = centerBrightness;
      }

      if (Tile.lightEmission[tt.id] > 0) {
         br = 1.0F;
      }

      t.color(c2 * br, c2 * br, c2 * br);
      int tex = tt.getTexture(this.level, x, y, z, 2);
      if (tex < 0) {
         this.xFlipTexture = true;
         tex = -tex;
      }

      this.renderNorth(tt, (double)x, (double)y, (double)z, tex);
      changed = true;
      this.xFlipTexture = false;
      br = tt.getBrightness(this.level, x, y, z + 1);
      if (dt.zz1 < (double)1.0F) {
         br = centerBrightness;
      }

      if (Tile.lightEmission[tt.id] > 0) {
         br = 1.0F;
      }

      t.color(c2 * br, c2 * br, c2 * br);
      tex = tt.getTexture(this.level, x, y, z, 3);
      if (tex < 0) {
         this.xFlipTexture = true;
         tex = -tex;
      }

      this.renderSouth(tt, (double)x, (double)y, (double)z, tex);
      changed = true;
      this.xFlipTexture = false;
      br = tt.getBrightness(this.level, x - 1, y, z);
      if (dt.xx0 > (double)0.0F) {
         br = centerBrightness;
      }

      if (Tile.lightEmission[tt.id] > 0) {
         br = 1.0F;
      }

      t.color(c3 * br, c3 * br, c3 * br);
      tex = tt.getTexture(this.level, x, y, z, 4);
      if (tex < 0) {
         this.xFlipTexture = true;
         tex = -tex;
      }

      this.renderWest(tt, (double)x, (double)y, (double)z, tex);
      changed = true;
      this.xFlipTexture = false;
      br = tt.getBrightness(this.level, x + 1, y, z);
      if (dt.xx1 < (double)1.0F) {
         br = centerBrightness;
      }

      if (Tile.lightEmission[tt.id] > 0) {
         br = 1.0F;
      }

      t.color(c3 * br, c3 * br, c3 * br);
      tex = tt.getTexture(this.level, x, y, z, 5);
      if (tex < 0) {
         this.xFlipTexture = true;
         tex = -tex;
      }

      this.renderEast(tt, (double)x, (double)y, (double)z, tex);
      changed = true;
      this.xFlipTexture = false;
      return changed;
   }

   public void renderFaceUp(Tile tt, double x, double y, double z, int tex) {
      Tesselator t = Tesselator.instance;
      if (this.fixedTexture >= 0) {
         tex = this.fixedTexture;
      }

      int xt = (tex & 15) << 4;
      int yt = tex & 240;
      double u0 = ((double)xt + tt.xx0 * (double)16.0F) / (double)256.0F;
      double u1 = ((double)xt + tt.xx1 * (double)16.0F - 0.01) / (double)256.0F;
      double v0 = ((double)yt + tt.zz0 * (double)16.0F) / (double)256.0F;
      double v1 = ((double)yt + tt.zz1 * (double)16.0F - 0.01) / (double)256.0F;
      if (tt.xx0 < (double)0.0F || tt.xx1 > (double)1.0F) {
         u0 = (double)(((float)xt + 0.0F) / 256.0F);
         u1 = (double)(((float)xt + 15.99F) / 256.0F);
      }

      if (tt.zz0 < (double)0.0F || tt.zz1 > (double)1.0F) {
         v0 = (double)(((float)yt + 0.0F) / 256.0F);
         v1 = (double)(((float)yt + 15.99F) / 256.0F);
      }

      double x0 = x + tt.xx0;
      double x1 = x + tt.xx1;
      double y0 = y + tt.yy0;
      double z0 = z + tt.zz0;
      double z1 = z + tt.zz1;
      t.vertexUV(x0, y0, z1, u0, v1);
      t.vertexUV(x0, y0, z0, u0, v0);
      t.vertexUV(x1, y0, z0, u1, v0);
      t.vertexUV(x1, y0, z1, u1, v1);
   }

   public void renderFaceDown(Tile tt, double x, double y, double z, int tex) {
      Tesselator t = Tesselator.instance;
      if (this.fixedTexture >= 0) {
         tex = this.fixedTexture;
      }

      int xt = (tex & 15) << 4;
      int yt = tex & 240;
      double u0 = ((double)xt + tt.xx0 * (double)16.0F) / (double)256.0F;
      double u1 = ((double)xt + tt.xx1 * (double)16.0F - 0.01) / (double)256.0F;
      double v0 = ((double)yt + tt.zz0 * (double)16.0F) / (double)256.0F;
      double v1 = ((double)yt + tt.zz1 * (double)16.0F - 0.01) / (double)256.0F;
      if (tt.xx0 < (double)0.0F || tt.xx1 > (double)1.0F) {
         u0 = (double)(((float)xt + 0.0F) / 256.0F);
         u1 = (double)(((float)xt + 15.99F) / 256.0F);
      }

      if (tt.zz0 < (double)0.0F || tt.zz1 > (double)1.0F) {
         v0 = (double)(((float)yt + 0.0F) / 256.0F);
         v1 = (double)(((float)yt + 15.99F) / 256.0F);
      }

      double x0 = x + tt.xx0;
      double x1 = x + tt.xx1;
      double y1 = y + tt.yy1;
      double z0 = z + tt.zz0;
      double z1 = z + tt.zz1;
      t.vertexUV(x1, y1, z1, u1, v1);
      t.vertexUV(x1, y1, z0, u1, v0);
      t.vertexUV(x0, y1, z0, u0, v0);
      t.vertexUV(x0, y1, z1, u0, v1);
   }

   public void renderNorth(Tile tt, double x, double y, double z, int tex) {
      Tesselator t = Tesselator.instance;
      if (this.fixedTexture >= 0) {
         tex = this.fixedTexture;
      }

      int xt = (tex & 15) << 4;
      int yt = tex & 240;
      double u0 = ((double)xt + tt.xx0 * (double)16.0F) / (double)256.0F;
      double u1 = ((double)xt + tt.xx1 * (double)16.0F - 0.01) / (double)256.0F;
      double v0 = ((double)yt + tt.yy0 * (double)16.0F) / (double)256.0F;
      double v1 = ((double)yt + tt.yy1 * (double)16.0F - 0.01) / (double)256.0F;
      if (this.xFlipTexture) {
         double tmp = u0;
         u0 = u1;
         u1 = tmp;
      }

      if (tt.xx0 < (double)0.0F || tt.xx1 > (double)1.0F) {
         u0 = (double)(((float)xt + 0.0F) / 256.0F);
         u1 = (double)(((float)xt + 15.99F) / 256.0F);
      }

      if (tt.yy0 < (double)0.0F || tt.yy1 > (double)1.0F) {
         v0 = (double)(((float)yt + 0.0F) / 256.0F);
         v1 = (double)(((float)yt + 15.99F) / 256.0F);
      }

      double x0 = x + tt.xx0;
      double x1 = x + tt.xx1;
      double y0 = y + tt.yy0;
      double y1 = y + tt.yy1;
      double z0 = z + tt.zz0;
      t.vertexUV(x0, y1, z0, u1, v0);
      t.vertexUV(x1, y1, z0, u0, v0);
      t.vertexUV(x1, y0, z0, u0, v1);
      t.vertexUV(x0, y0, z0, u1, v1);
   }

   public void renderSouth(Tile tt, double x, double y, double z, int tex) {
      Tesselator t = Tesselator.instance;
      if (this.fixedTexture >= 0) {
         tex = this.fixedTexture;
      }

      int xt = (tex & 15) << 4;
      int yt = tex & 240;
      double u0 = ((double)xt + tt.xx0 * (double)16.0F) / (double)256.0F;
      double u1 = ((double)xt + tt.xx1 * (double)16.0F - 0.01) / (double)256.0F;
      double v0 = ((double)yt + tt.yy0 * (double)16.0F) / (double)256.0F;
      double v1 = ((double)yt + tt.yy1 * (double)16.0F - 0.01) / (double)256.0F;
      if (this.xFlipTexture) {
         double tmp = u0;
         u0 = u1;
         u1 = tmp;
      }

      if (tt.xx0 < (double)0.0F || tt.xx1 > (double)1.0F) {
         u0 = (double)(((float)xt + 0.0F) / 256.0F);
         u1 = (double)(((float)xt + 15.99F) / 256.0F);
      }

      if (tt.yy0 < (double)0.0F || tt.yy1 > (double)1.0F) {
         v0 = (double)(((float)yt + 0.0F) / 256.0F);
         v1 = (double)(((float)yt + 15.99F) / 256.0F);
      }

      double x0 = x + tt.xx0;
      double x1 = x + tt.xx1;
      double y0 = y + tt.yy0;
      double y1 = y + tt.yy1;
      double z1 = z + tt.zz1;
      t.vertexUV(x0, y1, z1, u0, v0);
      t.vertexUV(x0, y0, z1, u0, v1);
      t.vertexUV(x1, y0, z1, u1, v1);
      t.vertexUV(x1, y1, z1, u1, v0);
   }

   public void renderWest(Tile tt, double x, double y, double z, int tex) {
      Tesselator t = Tesselator.instance;
      if (this.fixedTexture >= 0) {
         tex = this.fixedTexture;
      }

      int xt = (tex & 15) << 4;
      int yt = tex & 240;
      double u0 = ((double)xt + tt.zz0 * (double)16.0F) / (double)256.0F;
      double u1 = ((double)xt + tt.zz1 * (double)16.0F - 0.01) / (double)256.0F;
      double v0 = ((double)yt + tt.yy0 * (double)16.0F) / (double)256.0F;
      double v1 = ((double)yt + tt.yy1 * (double)16.0F - 0.01) / (double)256.0F;
      if (this.xFlipTexture) {
         double tmp = u0;
         u0 = u1;
         u1 = tmp;
      }

      if (tt.zz0 < (double)0.0F || tt.zz1 > (double)1.0F) {
         u0 = (double)(((float)xt + 0.0F) / 256.0F);
         u1 = (double)(((float)xt + 15.99F) / 256.0F);
      }

      if (tt.yy0 < (double)0.0F || tt.yy1 > (double)1.0F) {
         v0 = (double)(((float)yt + 0.0F) / 256.0F);
         v1 = (double)(((float)yt + 15.99F) / 256.0F);
      }

      double x0 = x + tt.xx0;
      double y0 = y + tt.yy0;
      double y1 = y + tt.yy1;
      double z0 = z + tt.zz0;
      double z1 = z + tt.zz1;
      t.vertexUV(x0, y1, z1, u1, v0);
      t.vertexUV(x0, y1, z0, u0, v0);
      t.vertexUV(x0, y0, z0, u0, v1);
      t.vertexUV(x0, y0, z1, u1, v1);
   }

   public void renderEast(Tile tt, double x, double y, double z, int tex) {
      Tesselator t = Tesselator.instance;
      if (this.fixedTexture >= 0) {
         tex = this.fixedTexture;
      }

      int xt = (tex & 15) << 4;
      int yt = tex & 240;
      double u0 = ((double)xt + tt.zz0 * (double)16.0F) / (double)256.0F;
      double u1 = ((double)xt + tt.zz1 * (double)16.0F - 0.01) / (double)256.0F;
      double v0 = ((double)yt + tt.yy0 * (double)16.0F) / (double)256.0F;
      double v1 = ((double)yt + tt.yy1 * (double)16.0F - 0.01) / (double)256.0F;
      if (this.xFlipTexture) {
         double tmp = u0;
         u0 = u1;
         u1 = tmp;
      }

      if (tt.zz0 < (double)0.0F || tt.zz1 > (double)1.0F) {
         u0 = (double)(((float)xt + 0.0F) / 256.0F);
         u1 = (double)(((float)xt + 15.99F) / 256.0F);
      }

      if (tt.yy0 < (double)0.0F || tt.yy1 > (double)1.0F) {
         v0 = (double)(((float)yt + 0.0F) / 256.0F);
         v1 = (double)(((float)yt + 15.99F) / 256.0F);
      }

      double x1 = x + tt.xx1;
      double y0 = y + tt.yy0;
      double y1 = y + tt.yy1;
      double z0 = z + tt.zz0;
      double z1 = z + tt.zz1;
      t.vertexUV(x1, y0, z1, u0, v1);
      t.vertexUV(x1, y0, z0, u1, v1);
      t.vertexUV(x1, y1, z0, u1, v0);
      t.vertexUV(x1, y1, z1, u0, v0);
   }

   public void renderCube(Tile tile, float alpha) {
      int shape = tile.getRenderShape();
      Tesselator t = Tesselator.instance;
      if (shape == 0) {
         tile.updateDefaultShape();
         GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
         float c10 = 0.5F;
         float c11 = 1.0F;
         float c2 = 0.8F;
         float c3 = 0.6F;
         t.begin();
         t.color(c11, c11, c11, alpha);
         this.renderFaceUp(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(0));
         t.color(c10, c10, c10, alpha);
         this.renderFaceDown(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(1));
         t.color(c2, c2, c2, alpha);
         this.renderNorth(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(2));
         this.renderSouth(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(3));
         t.color(c3, c3, c3, alpha);
         this.renderWest(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(4));
         this.renderEast(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(5));
         t.end();
         GL11.glTranslatef(0.5F, 0.5F, 0.5F);
      }

   }

   public void renderTile(Tile tile, int data) {
      Tesselator t = Tesselator.instance;
      int shape = tile.getRenderShape();
      if (shape == 0) {
         tile.updateDefaultShape();
         GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
         t.begin();
         t.normal(0.0F, -1.0F, 0.0F);
         this.renderFaceUp(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(0, data));
         t.end();
         t.begin();
         t.normal(0.0F, 1.0F, 0.0F);
         this.renderFaceDown(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(1, data));
         t.end();
         t.begin();
         t.normal(0.0F, 0.0F, -1.0F);
         this.renderNorth(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(2, data));
         t.end();
         t.begin();
         t.normal(0.0F, 0.0F, 1.0F);
         this.renderSouth(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(3, data));
         t.end();
         t.begin();
         t.normal(-1.0F, 0.0F, 0.0F);
         this.renderWest(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(4, data));
         t.end();
         t.begin();
         t.normal(1.0F, 0.0F, 0.0F);
         this.renderEast(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(5, data));
         t.end();
         GL11.glTranslatef(0.5F, 0.5F, 0.5F);
      } else if (shape == 1) {
         t.begin();
         t.normal(0.0F, -1.0F, 0.0F);
         this.tesselateCrossTexture(tile, data, (double)-0.5F, (double)-0.5F, (double)-0.5F);
         t.end();
      } else if (shape == 13) {
         tile.updateDefaultShape();
         GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
         float s = 0.0625F;
         t.begin();
         t.normal(0.0F, -1.0F, 0.0F);
         this.renderFaceUp(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(0));
         t.end();
         t.begin();
         t.normal(0.0F, 1.0F, 0.0F);
         this.renderFaceDown(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(1));
         t.end();
         t.begin();
         t.normal(0.0F, 0.0F, -1.0F);
         t.addOffset(0.0F, 0.0F, s);
         this.renderNorth(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(2));
         t.addOffset(0.0F, 0.0F, -s);
         t.end();
         t.begin();
         t.normal(0.0F, 0.0F, 1.0F);
         t.addOffset(0.0F, 0.0F, -s);
         this.renderSouth(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(3));
         t.addOffset(0.0F, 0.0F, s);
         t.end();
         t.begin();
         t.normal(-1.0F, 0.0F, 0.0F);
         t.addOffset(s, 0.0F, 0.0F);
         this.renderWest(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(4));
         t.addOffset(-s, 0.0F, 0.0F);
         t.end();
         t.begin();
         t.normal(1.0F, 0.0F, 0.0F);
         t.addOffset(-s, 0.0F, 0.0F);
         this.renderEast(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(5));
         t.addOffset(s, 0.0F, 0.0F);
         t.end();
         GL11.glTranslatef(0.5F, 0.5F, 0.5F);
      } else if (shape == 6) {
         t.begin();
         t.normal(0.0F, -1.0F, 0.0F);
         this.tesselateRowTexture(tile, data, (double)-0.5F, (double)-0.5F, (double)-0.5F);
         t.end();
      } else if (shape == 2) {
         t.begin();
         t.normal(0.0F, -1.0F, 0.0F);
         this.tesselateTorch(tile, (double)-0.5F, (double)-0.5F, (double)-0.5F, (double)0.0F, (double)0.0F);
         t.end();
      } else if (shape == 10) {
         for(int i = 0; i < 2; ++i) {
            if (i == 0) {
               tile.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.5F);
            }

            if (i == 1) {
               tile.setShape(0.0F, 0.0F, 0.5F, 1.0F, 0.5F, 1.0F);
            }

            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            t.begin();
            t.normal(0.0F, -1.0F, 0.0F);
            this.renderFaceUp(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(0));
            t.end();
            t.begin();
            t.normal(0.0F, 1.0F, 0.0F);
            this.renderFaceDown(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(1));
            t.end();
            t.begin();
            t.normal(0.0F, 0.0F, -1.0F);
            this.renderNorth(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(2));
            t.end();
            t.begin();
            t.normal(0.0F, 0.0F, 1.0F);
            this.renderSouth(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(3));
            t.end();
            t.begin();
            t.normal(-1.0F, 0.0F, 0.0F);
            this.renderWest(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(4));
            t.end();
            t.begin();
            t.normal(1.0F, 0.0F, 0.0F);
            this.renderEast(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(5));
            t.end();
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
         }
      } else if (shape == 11) {
         for(int i = 0; i < 4; ++i) {
            float w = 0.125F;
            if (i == 0) {
               tile.setShape(0.5F - w, 0.0F, 0.0F, 0.5F + w, 1.0F, w * 2.0F);
            }

            if (i == 1) {
               tile.setShape(0.5F - w, 0.0F, 1.0F - w * 2.0F, 0.5F + w, 1.0F, 1.0F);
            }

            w = 0.0625F;
            if (i == 2) {
               tile.setShape(0.5F - w, 1.0F - w * 3.0F, -w * 2.0F, 0.5F + w, 1.0F - w, 1.0F + w * 2.0F);
            }

            if (i == 3) {
               tile.setShape(0.5F - w, 0.5F - w * 3.0F, -w * 2.0F, 0.5F + w, 0.5F - w, 1.0F + w * 2.0F);
            }

            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            t.begin();
            t.normal(0.0F, -1.0F, 0.0F);
            this.renderFaceUp(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(0));
            t.end();
            t.begin();
            t.normal(0.0F, 1.0F, 0.0F);
            this.renderFaceDown(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(1));
            t.end();
            t.begin();
            t.normal(0.0F, 0.0F, -1.0F);
            this.renderNorth(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(2));
            t.end();
            t.begin();
            t.normal(0.0F, 0.0F, 1.0F);
            this.renderSouth(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(3));
            t.end();
            t.begin();
            t.normal(-1.0F, 0.0F, 0.0F);
            this.renderWest(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(4));
            t.end();
            t.begin();
            t.normal(1.0F, 0.0F, 0.0F);
            this.renderEast(tile, (double)0.0F, (double)0.0F, (double)0.0F, tile.getTexture(5));
            t.end();
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
         }

         tile.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      }

   }

   public static boolean canRender(int renderShape) {
      if (renderShape == 0) {
         return true;
      } else if (renderShape == 13) {
         return true;
      } else if (renderShape == 10) {
         return true;
      } else {
         return renderShape == 11;
      }
   }
}
