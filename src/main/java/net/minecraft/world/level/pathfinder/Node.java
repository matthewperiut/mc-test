package net.minecraft.world.level.pathfinder;

import util.Mth;

public class Node {
   public final int x;
   public final int y;
   public final int z;
   public final int hash;
   int heapIdx = -1;
   float g;
   float h;
   float f;
   Node cameFrom;
   public boolean closed = false;

   public Node(int var1, int var2, int var3) {
      this.x = var1;
      this.y = var2;
      this.z = var3;
      this.hash = var1 | var2 << 10 | var3 << 20;
   }

   public float distanceTo(Node var1) {
      float var2 = (float)(var1.x - this.x);
      float var3 = (float)(var1.y - this.y);
      float var4 = (float)(var1.z - this.z);
      return Mth.sqrt(var2 * var2 + var3 * var3 + var4 * var4);
   }

   public boolean equals(Object var1) {
      return ((Node)var1).hash == this.hash;
   }

   public int hashCode() {
      return this.hash;
   }

   public boolean inOpenSet() {
      return this.heapIdx >= 0;
   }

   public String toString() {
      return this.x + ", " + this.y + ", " + this.z;
   }
}
