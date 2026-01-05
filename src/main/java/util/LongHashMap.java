package util;

public class LongHashMap<V> {
   private static final int DEFAULT_INITIAL_CAPACITY = 16;
   private static final int MAXIMUM_CAPACITY = 1073741824;
   private static final float DEFAULT_LOAD_FACTOR = 0.75F;
   private transient Entry<V>[] table = new Entry[16];
   private transient int size;
   private int threshold = 12;
   private final float loadFactor = 0.75F;
   private transient volatile int modCount;

   public LongHashMap() {
   }

   private static int hash(long var0) {
      return hash((int)(var0 ^ var0 >>> 32));
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
   public V get(long var1) {
      int var3 = hash(var1);

      for(Entry<V> var4 = this.table[indexFor(var3, this.table.length)]; var4 != null; var4 = var4.next) {
         if (var4.key == var1) {
            return var4.value;
         }
      }

      return null;
   }

   public boolean containsKey(long var1) {
      return this.getEntry(var1) != null;
   }

   final Entry<V> getEntry(long var1) {
      int var3 = hash(var1);

      for(Entry var4 = this.table[indexFor(var3, this.table.length)]; var4 != null; var4 = var4.next) {
         if (var4.key == var1) {
            return var4;
         }
      }

      return null;
   }

   public void put(long var1, V var3) {
      int var4 = hash(var1);
      int var5 = indexFor(var4, this.table.length);

      for(Entry var6 = this.table[var5]; var6 != null; var6 = var6.next) {
         if (var6.key == var1) {
            var6.value = var3;
         }
      }

      ++this.modCount;
      this.addEntry(var4, var1, var3, var5);
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

   public V remove(long var1) {
      Entry var3 = this.removeEntryForKey(var1);
      return (V)(var3 == null ? null : var3.value);
   }

   final Entry<V> removeEntryForKey(long var1) {
      int var3 = hash(var1);
      int var4 = indexFor(var3, this.table.length);
      Entry var5 = this.table[var4];

      Entry var6;
      Entry var7;
      for(var6 = var5; var6 != null; var6 = var7) {
         var7 = var6.next;
         if (var6.key == var1) {
            ++this.modCount;
            --this.size;
            if (var5 == var6) {
               this.table[var4] = var7;
            } else {
               var5.next = var7;
            }

            return var6;
         }

         var5 = var6;
      }

      return var6;
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

   private void addEntry(int var1, long var2, V var4, int var5) {
      Entry var6 = this.table[var5];
      this.table[var5] = new Entry(var1, var2, var4, var6);
      if (this.size++ >= this.threshold) {
         this.resize(2 * this.table.length);
      }

   }

   private static class Entry<V> {
      final long key;
      V value;
      Entry<V> next;
      final int hash;

      Entry(int var1, long var2, V var4, Entry<V> var5) {
         this.value = var4;
         this.next = var5;
         this.key = var2;
         this.hash = var1;
      }

      public final long getKey() {
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
            Long var3 = this.getKey();
            Long var4 = var2.getKey();
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
         return LongHashMap.hash(this.key);
      }

      public final String toString() {
         return this.getKey() + "=" + this.getValue();
      }
   }
}
