package util;

public class IntHashMap<V> {
   private static final int DEFAULT_INITIAL_CAPACITY = 16;
   private static final int MAXIMUM_CAPACITY = 1073741824;
   private static final float DEFAULT_LOAD_FACTOR = 0.75F;
   private transient Entry<V>[] table = new Entry[16];
   private transient int size;
   private int threshold = 12;
   private final float loadFactor = 0.75F;
   private transient volatile int modCount;

   public IntHashMap() {
   }

   private static int hash(int var0) {
      var0 ^= var0 >>> 20 ^ var0 >>> 12;
      return var0 ^ var0 >>> 7 ^ var0 >>> 4;
   }

   private static int indexFor(int var0, int var1) {
      return var0 & var1 - 1;
   }

   public int size() {
      return this.size;
   }

   public boolean isEmpty() {
      return this.size == 0;
   }

   @SuppressWarnings("unchecked")
   public V get(int var1) {
      int var2 = hash(var1);

      for(Entry<V> var3 = this.table[indexFor(var2, this.table.length)]; var3 != null; var3 = var3.next) {
         if (var3.key == var1) {
            return var3.value;
         }
      }

      return null;
   }

   public boolean containsKey(int var1) {
      return this.getEntry(var1) != null;
   }

   final Entry<V> getEntry(int var1) {
      int var2 = hash(var1);

      for(Entry var3 = this.table[indexFor(var2, this.table.length)]; var3 != null; var3 = var3.next) {
         if (var3.key == var1) {
            return var3;
         }
      }

      return null;
   }

   public void put(int var1, V var2) {
      int var3 = hash(var1);
      int var4 = indexFor(var3, this.table.length);

      for(Entry var5 = this.table[var4]; var5 != null; var5 = var5.next) {
         if (var5.key == var1) {
            var5.value = var2;
         }
      }

      ++this.modCount;
      this.addEntry(var3, var1, var2, var4);
   }

   private void resize(int var1) {
      Entry[] var2 = this.table;
      int var3 = var2.length;
      if (var3 == 1073741824) {
         this.threshold = Integer.MAX_VALUE;
      } else {
         Entry[] var4 = new Entry[var1];
         this.transfer(var4);
         this.table = var4;
         this.threshold = (int)((float)var1 * this.loadFactor);
      }
   }

   private void transfer(Entry<V>[] var1) {
      Entry[] var2 = this.table;
      int var3 = var1.length;

      for(int var4 = 0; var4 < var2.length; ++var4) {
         Entry var5 = var2[var4];
         if (var5 != null) {
            var2[var4] = null;

            while(true) {
               Entry var6 = var5.next;
               int var7 = indexFor(var5.hash, var3);
               var5.next = var1[var7];
               var1[var7] = var5;
               var5 = var6;
               if (var6 == null) {
                  break;
               }
            }
         }
      }

   }

   public V remove(int var1) {
      Entry var2 = this.removeEntryForKey(var1);
      return (V)(var2 == null ? null : var2.value);
   }

   final Entry<V> removeEntryForKey(int var1) {
      int var2 = hash(var1);
      int var3 = indexFor(var2, this.table.length);
      Entry var4 = this.table[var3];

      Entry var5;
      Entry var6;
      for(var5 = var4; var5 != null; var5 = var6) {
         var6 = var5.next;
         if (var5.key == var1) {
            ++this.modCount;
            --this.size;
            if (var4 == var5) {
               this.table[var3] = var6;
            } else {
               var4.next = var6;
            }

            return var5;
         }

         var4 = var5;
      }

      return var5;
   }

   public void clear() {
      ++this.modCount;
      Entry[] var1 = this.table;

      for(int var2 = 0; var2 < var1.length; ++var2) {
         var1[var2] = null;
      }

      this.size = 0;
   }

   public boolean containsValue(Object var1) {
      if (var1 == null) {
         return this.containsNullValue();
      } else {
         Entry[] var2 = this.table;

         for(int var3 = 0; var3 < var2.length; ++var3) {
            for(Entry var4 = var2[var3]; var4 != null; var4 = var4.next) {
               if (var1.equals(var4.value)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private boolean containsNullValue() {
      Entry[] var1 = this.table;

      for(int var2 = 0; var2 < var1.length; ++var2) {
         for(Entry var3 = var1[var2]; var3 != null; var3 = var3.next) {
            if (var3.value == null) {
               return true;
            }
         }
      }

      return false;
   }

   private void addEntry(int var1, int var2, V var3, int var4) {
      Entry var5 = this.table[var4];
      this.table[var4] = new Entry(var1, var2, var3, var5);
      if (this.size++ >= this.threshold) {
         this.resize(2 * this.table.length);
      }

   }

   private static class Entry<V> {
      final int key;
      V value;
      Entry<V> next;
      final int hash;

      Entry(int var1, int var2, V var3, Entry<V> var4) {
         this.value = var3;
         this.next = var4;
         this.key = var2;
         this.hash = var1;
      }

      public final int getKey() {
         return this.key;
      }

      public final V getValue() {
         return this.value;
      }

      public final boolean equals(Object var1) {
         if (!(var1 instanceof Entry)) {
            return false;
         } else {
            Entry var2 = (Entry)var1;
            Integer var3 = this.getKey();
            Integer var4 = var2.getKey();
            if (var3 == var4 || var3 != null && var3.equals(var4)) {
               Object var5 = this.getValue();
               Object var6 = var2.getValue();
               if (var5 == var6 || var5 != null && var5.equals(var6)) {
                  return true;
               }
            }

            return false;
         }
      }

      public final int hashCode() {
         return IntHashMap.hash(this.key);
      }

      public final String toString() {
         return this.getKey() + "=" + this.getValue();
      }
   }
}
