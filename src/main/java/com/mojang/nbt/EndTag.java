package com.mojang.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class EndTag extends Tag {
   public EndTag() {
   }

   void load(DataInput var1) throws IOException {
   }

   void write(DataOutput var1) throws IOException {
   }

   public byte getId() {
      return 0;
   }

   public String toString() {
      return "END";
   }
}
