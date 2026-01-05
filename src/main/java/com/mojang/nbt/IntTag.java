package com.mojang.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntTag extends Tag {
   public int data;

   public IntTag() {
   }

   public IntTag(int var1) {
      this.data = var1;
   }

   void write(DataOutput var1) throws IOException {
      var1.writeInt(this.data);
   }

   void load(DataInput var1) throws IOException {
      this.data = var1.readInt();
   }

   public byte getId() {
      return 3;
   }

   public String toString() {
      return "" + this.data;
   }
}
