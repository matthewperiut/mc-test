package net.minecraft.client.model;

import net.minecraft.world.phys.Vec3;

public class Vertex {
   public Vec3 pos;
   public float u;
   public float v;

   public Vertex(float x, float y, float z, float u, float v) {
      this(Vec3.newPermanent((double)x, (double)y, (double)z), u, v);
   }

   public Vertex remap(float u, float v) {
      return new Vertex(this, u, v);
   }

   public Vertex(Vertex vertex, float u, float v) {
      this.pos = vertex.pos;
      this.u = u;
      this.v = v;
   }

   public Vertex(Vec3 pos, float u, float v) {
      this.pos = pos;
      this.u = u;
      this.v = v;
   }
}
