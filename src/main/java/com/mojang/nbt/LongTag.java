package com.mojang.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LongTag extends Tag {
   public long data;

   public LongTag() {
   }

   public LongTag(long var1) {
      this.data = var1;
   }

   void write(DataOutput var1) throws IOException {
      var1.writeLong(this.data);
   }

   void load(DataInput var1) throws IOException {
      this.data = var1.readLong();
   }

   public byte getId() {
      return 4;
   }

   public String toString() {
      return "" + this.data;
   }
}
