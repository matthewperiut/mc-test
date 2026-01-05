package com.mojang.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ByteArrayTag extends Tag {
   public byte[] data;

   public ByteArrayTag() {
   }

   public ByteArrayTag(byte[] var1) {
      this.data = var1;
   }

   void write(DataOutput var1) throws IOException {
      var1.writeInt(this.data.length);
      var1.write(this.data);
   }

   void load(DataInput var1) throws IOException {
      int var2 = var1.readInt();
      this.data = new byte[var2];
      var1.readFully(this.data);
   }

   public byte getId() {
      return 7;
   }

   public String toString() {
      return "[" + this.data.length + " bytes]";
   }
}
