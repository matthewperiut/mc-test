package net.minecraft.world.entity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemInstance;

public class SynchedEntityData {
   public static final int EOF_MARKER = 127;
   private static final int TYPE_BYTE = 0;
   private static final int TYPE_SHORT = 1;
   private static final int TYPE_INT = 2;
   private static final int TYPE_FLOAT = 3;
   private static final int TYPE_STRING = 4;
   private static final int TYPE_ITEMINSTANCE = 5;
   private static final HashMap<Class<?>, Integer> typeToConstant = new HashMap();
   private static final int TYPE_MASK = 224;
   private static final int TYPE_SHIFT = 5;
   private static final int MAX_ID_VALUE = 31;
   private final Map<Integer, DataItem> itemsById = new HashMap();
   private boolean isDirty;

   public SynchedEntityData() {
   }

   public <T> void define(int var1, T var2) {
      Integer var3 = (Integer)typeToConstant.get(var2.getClass());
      if (var3 == null) {
         throw new IllegalArgumentException("Unknown data type: " + var2.getClass());
      } else if (var1 > 31) {
         throw new IllegalArgumentException("Data value id is too big with " + var1 + "! (Max is " + 31 + ")");
      } else if (this.itemsById.containsKey(var1)) {
         throw new IllegalArgumentException("Duplicate id value for " + var1 + "!");
      } else {
         DataItem var4 = new DataItem(var3, var1, var2);
         this.itemsById.put(var1, var4);
      }
   }

   public byte getByte(int var1) {
      return (Byte)((DataItem)this.itemsById.get(var1)).getValue();
   }

   public short getShort(int var1) {
      return (Short)((DataItem)this.itemsById.get(var1)).getValue();
   }

   public int getInteger(int var1) {
      return (Integer)((DataItem)this.itemsById.get(var1)).getValue();
   }

   public float getFloat(int var1) {
      return (Float)((DataItem)this.itemsById.get(var1)).getValue();
   }

   public String getString(int var1) {
      return (String)((DataItem)this.itemsById.get(var1)).getValue();
   }

   public ItemInstance getItemInstance(int var1) {
      return (ItemInstance)((DataItem)this.itemsById.get(var1)).getValue();
   }

   public <T> void set(int var1, T var2) {
      DataItem var3 = (DataItem)this.itemsById.get(var1);
      if (!var2.equals(var3.getValue())) {
         var3.setValue(var2);
         var3.setDirty(true);
         this.isDirty = true;
      }

   }

   public boolean isDirty() {
      return this.isDirty;
   }

   public static void pack(List<DataItem> var0, DataOutputStream var1) throws IOException {
      if (var0 != null) {
         for(DataItem var3 : var0) {
            writeDataItem(var1, var3);
         }
      }

      var1.writeByte(127);
   }

   public ArrayList<DataItem> packDirty() {
      ArrayList var1 = null;
      if (this.isDirty) {
         for(DataItem var3 : this.itemsById.values()) {
            if (var3.isDirty()) {
               var3.setDirty(false);
               if (var1 == null) {
                  var1 = new ArrayList();
               }

               var1.add(var3);
            }
         }
      }

      this.isDirty = false;
      return var1;
   }

   public void packAll(DataOutputStream var1) throws IOException {
      for(DataItem var3 : this.itemsById.values()) {
         writeDataItem(var1, var3);
      }

      var1.writeByte(127);
   }

   private static void writeDataItem(DataOutputStream var0, DataItem var1) throws IOException {
      int var2 = (var1.getType() << 5 | var1.getId() & 31) & 255;
      var0.writeByte(var2);
      switch (var1.getType()) {
         case 0:
            var0.writeByte((Byte)var1.getValue());
            break;
         case 1:
            var0.writeShort((Short)var1.getValue());
            break;
         case 2:
            var0.writeInt((Integer)var1.getValue());
            break;
         case 3:
            var0.writeFloat((Float)var1.getValue());
            break;
         case 4:
            var0.writeUTF((String)var1.getValue());
            break;
         case 5:
            ItemInstance var3 = (ItemInstance)var1.getValue();
            var0.writeShort(var3.getItem().id);
            var0.writeByte(var3.count);
            var0.writeShort(var3.getAuxValue());
      }

   }

   public static List<DataItem> unpack(DataInputStream var0) throws IOException {
      ArrayList var1 = null;

      for(byte var2 = var0.readByte(); var2 != 127; var2 = var0.readByte()) {
         if (var1 == null) {
            var1 = new ArrayList();
         }

         int var3 = (var2 & 224) >> 5;
         int var4 = var2 & 31;
         DataItem var5 = null;
         switch (var3) {
            case 0:
               var5 = new DataItem(var3, var4, var0.readByte());
               break;
            case 1:
               var5 = new DataItem(var3, var4, var0.readShort());
               break;
            case 2:
               var5 = new DataItem(var3, var4, var0.readInt());
               break;
            case 3:
               var5 = new DataItem(var3, var4, var0.readFloat());
               break;
            case 4:
               var5 = new DataItem(var3, var4, var0.readUTF());
               break;
            case 5:
               short var6 = var0.readShort();
               byte var7 = var0.readByte();
               short var8 = var0.readShort();
               var5 = new DataItem(var3, var4, new ItemInstance(var6, var7, var8));
         }

         var1.add(var5);
      }

      return var1;
   }

   public void assignValues(List<DataItem> var1) {
      for(DataItem var3 : var1) {
         DataItem var4 = (DataItem)this.itemsById.get(var3.getId());
         if (var4 != null) {
            var4.setValue(var3.getValue());
         }
      }

   }

   static {
      typeToConstant.put(Byte.class, 0);
      typeToConstant.put(Short.class, 1);
      typeToConstant.put(Integer.class, 2);
      typeToConstant.put(Float.class, 3);
      typeToConstant.put(String.class, 4);
      typeToConstant.put(ItemInstance.class, 5);
   }

   public static class DataItem {
      private final int type;
      private final int id;
      private Object value;
      private boolean dirty;

      public DataItem(int var1, int var2, Object var3) {
         this.id = var2;
         this.value = var3;
         this.type = var1;
         this.dirty = true;
      }

      public int getId() {
         return this.id;
      }

      public void setValue(Object var1) {
         this.value = var1;
      }

      public Object getValue() {
         return this.value;
      }

      public int getType() {
         return this.type;
      }

      public boolean isDirty() {
         return this.dirty;
      }

      public void setDirty(boolean var1) {
         this.dirty = var1;
      }
   }
}
