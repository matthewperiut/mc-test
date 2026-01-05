package com.mojang.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class FloatTag extends Tag {
   public float data;

   public FloatTag() {
   }

   public FloatTag(float var1) {
      this.data = var1;
   }

   void write(DataOutput var1) throws IOException {
      var1.writeFloat(this.data);
   }

   void load(DataInput var1) throws IOException {
      this.data = var1.readFloat();
   }

   public byte getId() {
      return 5;
   }

   public String toString() {
      return "" + this.data;
   }
}
