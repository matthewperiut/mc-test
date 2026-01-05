package com.mojang.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class StringTag extends Tag {
   public String data;

   public StringTag() {
   }

   public StringTag(String var1) {
      this.data = var1;
      if (var1 == null) {
         throw new IllegalArgumentException("Empty string not allowed");
      }
   }

   void write(DataOutput var1) throws IOException {
      var1.writeUTF(this.data);
   }

   void load(DataInput var1) throws IOException {
      this.data = var1.readUTF();
   }

   public byte getId() {
      return 8;
   }

   public String toString() {
      return "" + this.data;
   }
}
