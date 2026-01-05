package net.minecraft.world.level.chunk.storage;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.NbtIo;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class NbtSlotFile {
   private static final byte[] READ_BUFFER = new byte[1048576];
   private static final int FILE_HEADER_SIZE = 1024;
   private static final int MAGIC_NUMBER = 326332417;
   private static final int FILE_SLOT_HEADER_SIZE = 12;
   private static final int FILE_SLOT_SIZE = 500;
   private RandomAccessFile raf;
   private List<Integer>[] fileSlotMap = new List[1024];
   private List<Integer> freeFileSlots = new ArrayList();
   private int totalFileSlots = 0;
   private static long largest = 0L;
   private List<Integer> toReplace;

   public NbtSlotFile(File var1) throws IOException {
      if (var1.exists() && var1.length() != 0L) {
         this.raf = new RandomAccessFile(var1, "rw");
      } else {
         var1.createNewFile();
         this.raf = new RandomAccessFile(var1, "rw");
         this.writeHeader();
      }

      this.readHeader();

      for(int var2 = 0; var2 < this.fileSlotMap.length; ++var2) {
         this.fileSlotMap[var2] = new ArrayList();
      }

      for(int var4 = 0; var4 < this.totalFileSlots; ++var4) {
         this.seekSlotHeader(var4);
         short var3 = this.raf.readShort();
         if (var3 == 0) {
            this.freeFileSlots.add(var4);
         } else if (var3 < 0) {
            this.fileSlotMap[-var3 - 1].add(var4);
         } else {
            this.fileSlotMap[var3 - 1].add(var4);
         }
      }

   }

   private void readHeader() throws IOException {
      this.raf.seek(0L);
      int var1 = this.raf.readInt();
      if (var1 != 326332417) {
         throw new IOException("Bad magic number: " + var1);
      } else {
         short var2 = this.raf.readShort();
         if (var2 != 0) {
            throw new IOException("Bad version number: " + var2);
         } else {
            this.totalFileSlots = this.raf.readInt();
         }
      }
   }

   private void writeHeader() throws IOException {
      this.raf.seek(0L);
      this.raf.writeInt(326332417);
      this.raf.writeShort(0);
      this.raf.writeInt(this.totalFileSlots);
   }

   private void seekSlotHeader(int var1) throws IOException {
      int var2 = 1024 + var1 * 512;
      this.raf.seek((long)var2);
   }

   private void seekSlot(int var1) throws IOException {
      int var2 = 1024 + var1 * 512;
      this.raf.seek((long)(var2 + 12));
   }

   public List<CompoundTag> readAll(int var1) throws IOException {
      ArrayList var2 = new ArrayList();
      List var3 = this.fileSlotMap[var1];
      int var4 = 0;

      label34:
      for(int var5 = 0; var5 < var3.size(); ++var5) {
         int var6 = (Integer)var3.get(var5);
         int var7 = 0;
         int var8 = -1;
         int var9 = var1 + 1;

         do {
            this.seekSlotHeader(var6);
            short var10 = this.raf.readShort();
            short var11 = this.raf.readShort();
            var8 = this.raf.readInt();
            int var12 = this.raf.readInt();
            this.seekSlot(var6);
            if (var9 > 0 && var10 == -var9) {
               ++var4;
               continue label34;
            }

            if (var10 != var9) {
               throw new IOException("Wrong slot! Got " + var10 + ", expected " + var9);
            }

            this.raf.readFully(READ_BUFFER, var7, var11);
            if (var8 >= 0) {
               var7 += var11;
               var6 = var8;
               var9 = -var1 - 1;
            }
         } while(var8 >= 0);

         var2.add(NbtIo.decompress(READ_BUFFER));
      }

      return var2;
   }

   private int getFreeSlot() throws IOException {
      int var1;
      if (this.toReplace.size() > 0) {
         var1 = (Integer)this.toReplace.remove(this.toReplace.size() - 1);
      } else if (this.freeFileSlots.size() > 0) {
         var1 = (Integer)this.freeFileSlots.remove(this.freeFileSlots.size() - 1);
      } else {
         var1 = this.totalFileSlots++;
         this.writeHeader();
      }

      return var1;
   }

   public void replaceSlot(int var1, List<CompoundTag> var2) throws IOException {
      this.toReplace = this.fileSlotMap[var1];
      this.fileSlotMap[var1] = new ArrayList();

      for(int var3 = 0; var3 < var2.size(); ++var3) {
         CompoundTag var4 = (CompoundTag)var2.get(var3);
         byte[] var5 = NbtIo.compress(var4);
         if ((long)var5.length > largest) {
            largest = (long)var5.length;
            System.out.println("New largest: " + largest + " (" + var4.getString("id") + ")");
         }

         int var6 = 0;
         int var7 = var5.length;
         if (var7 != 0) {
            int var8 = this.getFreeSlot();
            int var9 = var1 + 1;
            int var10 = -1;

            while(var7 > 0) {
               int var11 = var8;
               this.fileSlotMap[var1].add(var8);
               int var12 = var7;
               if (var7 > 500) {
                  var12 = 500;
               }

               var7 -= var12;
               if (var7 > 0) {
                  var8 = this.getFreeSlot();
               } else {
                  var8 = -1;
               }

               this.seekSlotHeader(var11);
               this.raf.writeShort(var9);
               this.raf.writeShort(var12);
               this.raf.writeInt(var8);
               this.raf.writeInt(var10);
               this.seekSlot(var11);
               this.raf.write(var5, var6, var12);
               if (var7 > 0) {
                  var10 = var11;
                  var6 += var12;
                  var9 = -var1 - 1;
               }
            }
         }
      }

      for(int var13 = 0; var13 < this.toReplace.size(); ++var13) {
         Integer var14 = (Integer)this.toReplace.get(var13);
         this.freeFileSlots.add(var14);
         this.seekSlotHeader(var14);
         this.raf.writeShort(0);
      }

      this.toReplace.clear();
   }

   public void close() throws IOException {
      this.raf.getFD().sync();
      this.raf.close();
   }
}
