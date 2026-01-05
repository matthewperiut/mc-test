package net.minecraft.client.model;

import net.minecraft.client.MemoryTracker;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.opengl.GL11;

public class Cube {
   private Vertex[] vertices;
   private Polygon[] polygons;
   private int xTexOffs;
   private int yTexOffs;
   public float x;
   public float y;
   public float z;
   public float xRot;
   public float yRot;
   public float zRot;
   private boolean compiled = false;
   private int list = 0;
   public boolean mirror = false;
   public boolean visible = true;
   public boolean neverRender = false;
   private static final float c = (180F / (float)Math.PI);

   public Cube(int xTexOffs, int yTexOffs) {
      this.xTexOffs = xTexOffs;
      this.yTexOffs = yTexOffs;
   }

   public void setTexOffs(int xTexOffs, int yTexOffs) {
      this.xTexOffs = xTexOffs;
      this.yTexOffs = yTexOffs;
   }

   public void addBox(float x0, float y0, float z0, int w, int h, int d) {
      this.addBox(x0, y0, z0, w, h, d, 0.0F);
   }

   public void addBox(float x0, float y0, float z0, int w, int h, int d, float g) {
      this.vertices = new Vertex[8];
      this.polygons = new Polygon[6];
      float x1 = x0 + (float)w;
      float y1 = y0 + (float)h;
      float z1 = z0 + (float)d;
      x0 -= g;
      y0 -= g;
      z0 -= g;
      x1 += g;
      y1 += g;
      z1 += g;
      if (this.mirror) {
         float tmp = x1;
         x1 = x0;
         x0 = tmp;
      }

      Vertex u0 = new Vertex(x0, y0, z0, 0.0F, 0.0F);
      Vertex u1 = new Vertex(x1, y0, z0, 0.0F, 8.0F);
      Vertex u2 = new Vertex(x1, y1, z0, 8.0F, 8.0F);
      Vertex u3 = new Vertex(x0, y1, z0, 8.0F, 0.0F);
      Vertex l0 = new Vertex(x0, y0, z1, 0.0F, 0.0F);
      Vertex l1 = new Vertex(x1, y0, z1, 0.0F, 8.0F);
      Vertex l2 = new Vertex(x1, y1, z1, 8.0F, 8.0F);
      Vertex l3 = new Vertex(x0, y1, z1, 8.0F, 0.0F);
      this.vertices[0] = u0;
      this.vertices[1] = u1;
      this.vertices[2] = u2;
      this.vertices[3] = u3;
      this.vertices[4] = l0;
      this.vertices[5] = l1;
      this.vertices[6] = l2;
      this.vertices[7] = l3;
      this.polygons[0] = new Polygon(new Vertex[]{l1, u1, u2, l2}, this.xTexOffs + d + w, this.yTexOffs + d, this.xTexOffs + d + w + d, this.yTexOffs + d + h);
      this.polygons[1] = new Polygon(new Vertex[]{u0, l0, l3, u3}, this.xTexOffs + 0, this.yTexOffs + d, this.xTexOffs + d, this.yTexOffs + d + h);
      this.polygons[2] = new Polygon(new Vertex[]{l1, l0, u0, u1}, this.xTexOffs + d, this.yTexOffs + 0, this.xTexOffs + d + w, this.yTexOffs + d);
      this.polygons[3] = new Polygon(new Vertex[]{u2, u3, l3, l2}, this.xTexOffs + d + w, this.yTexOffs + 0, this.xTexOffs + d + w + w, this.yTexOffs + d);
      this.polygons[4] = new Polygon(new Vertex[]{u1, u0, u3, u2}, this.xTexOffs + d, this.yTexOffs + d, this.xTexOffs + d + w, this.yTexOffs + d + h);
      this.polygons[5] = new Polygon(new Vertex[]{l0, l1, l2, l3}, this.xTexOffs + d + w + d, this.yTexOffs + d, this.xTexOffs + d + w + d + w, this.yTexOffs + d + h);
      if (this.mirror) {
         for(int i = 0; i < this.polygons.length; ++i) {
            this.polygons[i].mirror();
         }
      }

   }

   public void addTexBox(float x0, float y0, float z0, int w, int h, int d, int tex) {
      this.vertices = new Vertex[8];
      this.polygons = new Polygon[6];
      float x1 = x0 + (float)w;
      float y1 = y0 + (float)h;
      float z1 = z0 + (float)d;
      Vertex u0 = new Vertex(x0, y0, z0, 0.0F, 0.0F);
      Vertex u1 = new Vertex(x1, y0, z0, 0.0F, 8.0F);
      Vertex u2 = new Vertex(x1, y1, z0, 8.0F, 8.0F);
      Vertex u3 = new Vertex(x0, y1, z0, 8.0F, 0.0F);
      Vertex l0 = new Vertex(x0, y0, z1, 0.0F, 0.0F);
      Vertex l1 = new Vertex(x1, y0, z1, 0.0F, 8.0F);
      Vertex l2 = new Vertex(x1, y1, z1, 8.0F, 8.0F);
      Vertex l3 = new Vertex(x0, y1, z1, 8.0F, 0.0F);
      this.vertices[0] = u0;
      this.vertices[1] = u1;
      this.vertices[2] = u2;
      this.vertices[3] = u3;
      this.vertices[4] = l0;
      this.vertices[5] = l1;
      this.vertices[6] = l2;
      this.vertices[7] = l3;
      float us = 0.25F;
      float vs = 0.25F;
      float _u0 = ((float)(tex % 16) + (1.0F - us)) / 16.0F;
      float v0 = ((float)(tex / 16) + (1.0F - vs)) / 16.0F;
      float _u1 = ((float)(tex % 16) + us) / 16.0F;
      float v1 = ((float)(tex / 16) + vs) / 16.0F;
      this.polygons[0] = new Polygon(new Vertex[]{l1, u1, u2, l2}, _u0, v0, _u1, v1);
      this.polygons[1] = new Polygon(new Vertex[]{u0, l0, l3, u3}, _u0, v0, _u1, v1);
      this.polygons[2] = new Polygon(new Vertex[]{l1, l0, u0, u1}, _u0, v0, _u1, v1);
      this.polygons[3] = new Polygon(new Vertex[]{u2, u3, l3, l2}, _u0, v0, _u1, v1);
      this.polygons[4] = new Polygon(new Vertex[]{u1, u0, u3, u2}, _u0, v0, _u1, v1);
      this.polygons[5] = new Polygon(new Vertex[]{l0, l1, l2, l3}, _u0, v0, _u1, v1);
   }

   public void setPos(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public void render(float scale) {
      if (!this.neverRender) {
         if (this.visible) {
            if (!this.compiled) {
               this.compile(scale);
            }

            if (this.xRot == 0.0F && this.yRot == 0.0F && this.zRot == 0.0F) {
               if (this.x == 0.0F && this.y == 0.0F && this.z == 0.0F) {
                  GL11.glCallList(this.list);
               } else {
                  GL11.glTranslatef(this.x * scale, this.y * scale, this.z * scale);
                  GL11.glCallList(this.list);
                  GL11.glTranslatef(-this.x * scale, -this.y * scale, -this.z * scale);
               }
            } else {
               GL11.glPushMatrix();
               GL11.glTranslatef(this.x * scale, this.y * scale, this.z * scale);
               if (this.zRot != 0.0F) {
                  GL11.glRotatef(this.zRot * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
               }

               if (this.yRot != 0.0F) {
                  GL11.glRotatef(this.yRot * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
               }

               if (this.xRot != 0.0F) {
                  GL11.glRotatef(this.xRot * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
               }

               GL11.glCallList(this.list);
               GL11.glPopMatrix();
            }

         }
      }
   }

   public void translateTo(float scale) {
      if (!this.neverRender) {
         if (this.visible) {
            if (!this.compiled) {
               this.compile(scale);
            }

            if (this.xRot == 0.0F && this.yRot == 0.0F && this.zRot == 0.0F) {
               if (this.x != 0.0F || this.y != 0.0F || this.z != 0.0F) {
                  GL11.glTranslatef(this.x * scale, this.y * scale, this.z * scale);
               }
            } else {
               GL11.glTranslatef(this.x * scale, this.y * scale, this.z * scale);
               if (this.zRot != 0.0F) {
                  GL11.glRotatef(this.zRot * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
               }

               if (this.yRot != 0.0F) {
                  GL11.glRotatef(this.yRot * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
               }

               if (this.xRot != 0.0F) {
                  GL11.glRotatef(this.xRot * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
               }
            }

         }
      }
   }

   private void compile(float scale) {
      this.list = MemoryTracker.genLists(1);
      GL11.glNewList(this.list, 4864);
      Tesselator t = Tesselator.instance;

      for(int i = 0; i < this.polygons.length; ++i) {
         this.polygons[i].render(t, scale);
      }

      GL11.glEndList();
      this.compiled = true;
   }
}
