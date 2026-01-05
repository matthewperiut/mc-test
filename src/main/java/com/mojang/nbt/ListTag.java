package com.mojang.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ListTag<T extends Tag> extends Tag {
   private List<T> list = new ArrayList();
   private byte type;

   public ListTag() {
   }

   void write(DataOutput var1) throws IOException {
      if (this.list.size() > 0) {
         this.type = ((Tag)this.list.get(0)).getId();
      } else {
         this.type = 1;
      }

      var1.writeByte(this.type);
      var1.writeInt(this.list.size());

      for(int var2 = 0; var2 < this.list.size(); ++var2) {
         ((Tag)this.list.get(var2)).write(var1);
      }

   }

   void load(DataInput var1) throws IOException {
      this.type = var1.readByte();
      int var2 = var1.readInt();
      this.list = new ArrayList();

      for(int var3 = 0; var3 < var2; ++var3) {
         Tag var4 = Tag.newTag(this.type);
         var4.load(var1);
         this.list.add((T)var4);
      }

   }

   public byte getId() {
      return 9;
   }

   public String toString() {
      return "" + this.list.size() + " entries of type " + Tag.getTagName(this.type);
   }

   public void print(String var1, PrintStream var2) {
      super.print(var1, var2);
      var2.println(var1 + "{");
      var1 = var1 + "   ";

      for(int var4 = 0; var4 < this.list.size(); ++var4) {
         ((Tag)this.list.get(var4)).print(var1, var2);
      }

      var2.println(var1 + "}");
   }

   public void add(T var1) {
      this.type = var1.getId();
      this.list.add(var1);
   }

   public T get(int var1) {
      return (T)(this.list.get(var1));
   }

   public int size() {
      return this.list.size();
   }
}
