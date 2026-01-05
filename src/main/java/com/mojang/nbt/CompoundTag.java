package com.mojang.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class CompoundTag extends Tag {
   private Map<String, Tag> tags = new HashMap();

   public CompoundTag() {
   }

   void write(DataOutput var1) throws IOException {
      for(Tag var3 : this.tags.values()) {
         Tag.writeNamedTag(var3, var1);
      }

      var1.writeByte(0);
   }

   void load(DataInput var1) throws IOException {
      this.tags.clear();

      Tag var2;
      while((var2 = Tag.readNamedTag(var1)).getId() != 0) {
         this.tags.put(var2.getName(), var2);
      }

   }

   public byte getId() {
      return 10;
   }

   public void put(String var1, Tag var2) {
      this.tags.put(var1, var2.setName(var1));
   }

   public void putByte(String var1, byte var2) {
      this.tags.put(var1, (new ByteTag(var2)).setName(var1));
   }

   public void putShort(String var1, short var2) {
      this.tags.put(var1, (new ShortTag(var2)).setName(var1));
   }

   public void putInt(String var1, int var2) {
      this.tags.put(var1, (new IntTag(var2)).setName(var1));
   }

   public void putLong(String var1, long var2) {
      this.tags.put(var1, (new LongTag(var2)).setName(var1));
   }

   public void putFloat(String var1, float var2) {
      this.tags.put(var1, (new FloatTag(var2)).setName(var1));
   }

   public void putDouble(String var1, double var2) {
      this.tags.put(var1, (new DoubleTag(var2)).setName(var1));
   }

   public void putString(String var1, String var2) {
      this.tags.put(var1, (new StringTag(var2)).setName(var1));
   }

   public void putByteArray(String var1, byte[] var2) {
      this.tags.put(var1, (new ByteArrayTag(var2)).setName(var1));
   }

   public void putCompound(String var1, CompoundTag var2) {
      this.tags.put(var1, var2.setName(var1));
   }

   public void putBoolean(String var1, boolean var2) {
      this.putByte(var1, (byte)(var2 ? 1 : 0));
   }

   public Tag get(String var1) {
      return (Tag)this.tags.get(var1);
   }

   public boolean contains(String var1) {
      return this.tags.containsKey(var1);
   }

   public byte getByte(String var1) {
      return !this.tags.containsKey(var1) ? 0 : ((ByteTag)this.tags.get(var1)).data;
   }

   public short getShort(String var1) {
      return !this.tags.containsKey(var1) ? 0 : ((ShortTag)this.tags.get(var1)).data;
   }

   public int getInt(String var1) {
      return !this.tags.containsKey(var1) ? 0 : ((IntTag)this.tags.get(var1)).data;
   }

   public long getLong(String var1) {
      return !this.tags.containsKey(var1) ? 0L : ((LongTag)this.tags.get(var1)).data;
   }

   public float getFloat(String var1) {
      return !this.tags.containsKey(var1) ? 0.0F : ((FloatTag)this.tags.get(var1)).data;
   }

   public double getDouble(String var1) {
      return !this.tags.containsKey(var1) ? (double)0.0F : ((DoubleTag)this.tags.get(var1)).data;
   }

   public String getString(String var1) {
      return !this.tags.containsKey(var1) ? "" : ((StringTag)this.tags.get(var1)).data;
   }

   public byte[] getByteArray(String var1) {
      return !this.tags.containsKey(var1) ? new byte[0] : ((ByteArrayTag)this.tags.get(var1)).data;
   }

   public CompoundTag getCompound(String var1) {
      return !this.tags.containsKey(var1) ? new CompoundTag() : (CompoundTag)this.tags.get(var1);
   }

   public ListTag<? extends Tag> getList(String var1) {
      return !this.tags.containsKey(var1) ? new ListTag() : (ListTag)this.tags.get(var1);
   }

   public boolean getBoolean(String var1) {
      return this.getByte(var1) != 0;
   }

   public String toString() {
      return "" + this.tags.size() + " entries";
   }

   public void print(String var1, PrintStream var2) {
      super.print(var1, var2);
      var2.println(var1 + "{");
      String var3 = var1;
      var1 = var1 + "   ";

      for(Tag var5 : this.tags.values()) {
         var5.print(var1, var2);
      }

      var2.println(var3 + "}");
   }

   public boolean isEmpty() {
      return this.tags.isEmpty();
   }
}
