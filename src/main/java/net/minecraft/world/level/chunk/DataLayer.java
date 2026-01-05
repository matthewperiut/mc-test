package net.minecraft.world.level.chunk;

public class DataLayer {
   public final byte[] data;

   public DataLayer(int var1) {
      this.data = new byte[var1 >> 1];
   }

   public DataLayer(byte[] var1) {
      this.data = var1;
   }

   public int get(int var1, int var2, int var3) {
      int var4 = var1 << 11 | var3 << 7 | var2;
      int var5 = var4 >> 1;
      int var6 = var4 & 1;
      return var6 == 0 ? this.data[var5] & 15 : this.data[var5] >> 4 & 15;
   }

   public void set(int var1, int var2, int var3, int var4) {
      int var5 = var1 << 11 | var3 << 7 | var2;
      int var6 = var5 >> 1;
      int var7 = var5 & 1;
      if (var7 == 0) {
         this.data[var6] = (byte)(this.data[var6] & 240 | var4 & 15);
      } else {
         this.data[var6] = (byte)(this.data[var6] & 15 | (var4 & 15) << 4);
      }

   }

   public boolean isValid() {
      return this.data != null;
   }

   public void setAll(int var1) {
      byte var2 = (byte)(var1 & var1 << 4);

      for(int var3 = 0; var3 < this.data.length; ++var3) {
         this.data[var3] = var2;
      }

   }
}
