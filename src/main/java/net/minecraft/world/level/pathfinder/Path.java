package net.minecraft.world.level.pathfinder;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class Path {
   private final Node[] nodes;
   public final int length;
   private int pos;

   public Path(Node[] var1) {
      this.nodes = var1;
      this.length = var1.length;
   }

   public Node current() {
      return this.nodes[this.pos];
   }

   public void next() {
      ++this.pos;
   }

   public boolean isDone() {
      return this.pos >= this.nodes.length;
   }

   public Node get(int var1) {
      return this.nodes[var1];
   }

   public Vec3 current(Entity var1) {
      double var2 = (double)this.nodes[this.pos].x + (double)((int)(var1.bbWidth + 1.0F)) * (double)0.5F;
      double var4 = (double)this.nodes[this.pos].y;
      double var6 = (double)this.nodes[this.pos].z + (double)((int)(var1.bbWidth + 1.0F)) * (double)0.5F;
      return Vec3.newTemp(var2, var4, var6);
   }
}
