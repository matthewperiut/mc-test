package com.mojang.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintStream;

public abstract class Tag {
   public static final byte TAG_End = 0;
   public static final byte TAG_Byte = 1;
   public static final byte TAG_Short = 2;
   public static final byte TAG_Int = 3;
   public static final byte TAG_Long = 4;
   public static final byte TAG_Float = 5;
   public static final byte TAG_Double = 6;
   public static final byte TAG_Byte_Array = 7;
   public static final byte TAG_String = 8;
   public static final byte TAG_List = 9;
   public static final byte TAG_Compound = 10;
   private String name = null;

   public Tag() {
   }

   abstract void write(DataOutput var1) throws IOException;

   abstract void load(DataInput var1) throws IOException;

   public abstract String toString();

   public abstract byte getId();

   public void print(PrintStream var1) {
      this.print("", var1);
   }

   public void print(String var1, PrintStream var2) {
      String var3 = this.getName();
      var2.print(var1);
      var2.print(getTagName(this.getId()));
      if (var3.length() > 0) {
         var2.print("(\"" + var3 + "\")");
      }

      var2.print(": ");
      var2.println(this.toString());
   }

   public String getName() {
      return this.name == null ? "" : this.name;
   }

   public Tag setName(String var1) {
      this.name = var1;
      return this;
   }

   public static Tag readNamedTag(DataInput var0) throws IOException {
      byte var1 = var0.readByte();
      if (var1 == 0) {
         return new EndTag();
      } else {
         Tag var2 = newTag(var1);
         var2.name = var0.readUTF();
         var2.load(var0);
         return var2;
      }
   }

   public static void writeNamedTag(Tag var0, DataOutput var1) throws IOException {
      var1.writeByte(var0.getId());
      if (var0.getId() != 0) {
         var1.writeUTF(var0.getName());
         var0.write(var1);
      }
   }

   public static Tag newTag(byte var0) {
      switch (var0) {
         case 0:
            return new EndTag();
         case 1:
            return new ByteTag();
         case 2:
            return new ShortTag();
         case 3:
            return new IntTag();
         case 4:
            return new LongTag();
         case 5:
            return new FloatTag();
         case 6:
            return new DoubleTag();
         case 7:
            return new ByteArrayTag();
         case 8:
            return new StringTag();
         case 9:
            return new ListTag();
         case 10:
            return new CompoundTag();
         default:
            return null;
      }
   }

   public static String getTagName(byte var0) {
      switch (var0) {
         case 0:
            return "TAG_End";
         case 1:
            return "TAG_Byte";
         case 2:
            return "TAG_Short";
         case 3:
            return "TAG_Int";
         case 4:
            return "TAG_Long";
         case 5:
            return "TAG_Float";
         case 6:
            return "TAG_Double";
         case 7:
            return "TAG_Byte_Array";
         case 8:
            return "TAG_String";
         case 9:
            return "TAG_List";
         case 10:
            return "TAG_Compound";
         default:
            return "UNKNOWN";
      }
   }
}
