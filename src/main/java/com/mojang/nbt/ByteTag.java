package com.mojang.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ByteTag extends Tag {
   public byte data;

   public ByteTag() {
   }

   public ByteTag(byte var1) {
      this.data = var1;
   }

   void write(DataOutput var1) throws IOException {
      var1.writeByte(this.data);
   }

   void load(DataInput var1) throws IOException {
      this.data = var1.readByte();
   }

   public byte getId() {
      return 1;
   }

   public String toString() {
      return "" + this.data;
   }
}
