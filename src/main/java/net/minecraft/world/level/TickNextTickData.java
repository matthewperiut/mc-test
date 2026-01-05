package net.minecraft.world.level;

public class TickNextTickData implements Comparable<TickNextTickData> {
   private static long C = 0L;
   public int x;
   public int y;
   public int z;
   public int tileId;
   public long delay;
   private long c;

   public TickNextTickData(int var1, int var2, int var3, int var4) {
      this.c = (long)(C++);
      this.x = var1;
      this.y = var2;
      this.z = var3;
      this.tileId = var4;
   }

   public boolean equals(Object var1) {
      if (!(var1 instanceof TickNextTickData)) {
         return false;
      } else {
         TickNextTickData var2 = (TickNextTickData)var1;
         return this.x == var2.x && this.y == var2.y && this.z == var2.z && this.tileId == var2.tileId;
      }
   }

   public int hashCode() {
      return (this.x * 128 * 1024 + this.z * 128 + this.y) * 256 + this.tileId;
   }

   public TickNextTickData delay(long var1) {
      this.delay = var1;
      return this;
   }

   public int compareTo(TickNextTickData var1) {
      if (this.delay < var1.delay) {
         return -1;
      } else if (this.delay > var1.delay) {
         return 1;
      } else if (this.c < var1.c) {
         return -1;
      } else {
         return this.c > var1.c ? 1 : 0;
      }
   }
}
