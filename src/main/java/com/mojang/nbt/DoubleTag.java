package com.mojang.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DoubleTag extends Tag {
   public double data;

   public DoubleTag() {
   }

   public DoubleTag(double var1) {
      this.data = var1;
   }

   void write(DataOutput var1) throws IOException {
      var1.writeDouble(this.data);
   }

   void load(DataInput var1) throws IOException {
      this.data = var1.readDouble();
   }

   public byte getId() {
      return 6;
   }

   public String toString() {
      return "" + this.data;
   }
}
