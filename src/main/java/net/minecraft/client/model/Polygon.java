package net.minecraft.client.model;

import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.phys.Vec3;

public class Polygon {
   public Vertex[] vertices;
   public int vertexCount;
   private boolean flipNormal;

   public Polygon(Vertex[] vertices) {
      this.vertexCount = 0;
      this.flipNormal = false;
      this.vertices = vertices;
      this.vertexCount = vertices.length;
   }

   public Polygon(Vertex[] vertices, int u0, int v0, int u1, int v1) {
      this(vertices);
      float us = 0.0015625F;
      float vs = 0.003125F;
      vertices[0] = vertices[0].remap((float)u1 / 64.0F - us, (float)v0 / 32.0F + vs);
      vertices[1] = vertices[1].remap((float)u0 / 64.0F + us, (float)v0 / 32.0F + vs);
      vertices[2] = vertices[2].remap((float)u0 / 64.0F + us, (float)v1 / 32.0F - vs);
      vertices[3] = vertices[3].remap((float)u1 / 64.0F - us, (float)v1 / 32.0F - vs);
   }

   public Polygon(Vertex[] vertices, float u0, float v0, float u1, float v1) {
      this(vertices);
      vertices[0] = vertices[0].remap(u1, v0);
      vertices[1] = vertices[1].remap(u0, v0);
      vertices[2] = vertices[2].remap(u0, v1);
      vertices[3] = vertices[3].remap(u1, v1);
   }

   public void mirror() {
      Vertex[] newVertices = new Vertex[this.vertices.length];

      for(int i = 0; i < this.vertices.length; ++i) {
         newVertices[i] = this.vertices[this.vertices.length - i - 1];
      }

      this.vertices = newVertices;
   }

   public void render(Tesselator t, float scale) {
      Vec3 v0 = this.vertices[1].pos.vectorTo(this.vertices[0].pos);
      Vec3 v1 = this.vertices[1].pos.vectorTo(this.vertices[2].pos);
      Vec3 n = v1.cross(v0).normalize();
      t.begin();
      if (this.flipNormal) {
         t.normal(-((float)n.x), -((float)n.y), -((float)n.z));
      } else {
         t.normal((float)n.x, (float)n.y, (float)n.z);
      }

      for(int i = 0; i < 4; ++i) {
         Vertex v = this.vertices[i];
         t.vertexUV((double)((float)v.pos.x * scale), (double)((float)v.pos.y * scale), (double)((float)v.pos.z * scale), (double)v.u, (double)v.v);
      }

      t.end();
   }

   public Polygon flipNormal() {
      this.flipNormal = true;
      return this;
   }
}
