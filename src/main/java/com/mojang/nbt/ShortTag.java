package com.mojang.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ShortTag extends Tag {
   public short data;

   public ShortTag() {
   }

   public ShortTag(short var1) {
      this.data = var1;
   }

   void write(DataOutput var1) throws IOException {
      var1.writeShort(this.data);
   }

   void load(DataInput var1) throws IOException {
      this.data = var1.readShort();
   }

   public byte getId() {
      return 2;
   }

   public String toString() {
      return "" + this.data;
   }
}
