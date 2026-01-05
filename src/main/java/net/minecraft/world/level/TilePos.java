package net.minecraft.world.level;

public class TilePos {
   public final int x;
   public final int y;
   public final int z;

   public TilePos(int var1, int var2, int var3) {
      this.x = var1;
      this.y = var2;
      this.z = var3;
   }

   public boolean equals(Object var1) {
      if (!(var1 instanceof TilePos)) {
         return false;
      } else {
         TilePos var2 = (TilePos)var1;
         return var2.x == this.x && var2.y == this.y && var2.z == this.z;
      }
   }

   public int hashCode() {
      return this.x * 8976890 + this.y * 981131 + this.z;
   }
}
