package net.minecraft.world.level.chunk.storage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ZoneFile {
   public static final int FILE_HEADER_SIZE = 4096;
   private static final int MAGIC_NUMBER = 326332416;
   private short[] slots = new short[1024];
   private short slotCount;
   public long lastUse = 0L;
   private final FileChannel channel;
   public final Long key;
   public final File file;
   public NbtSlotFile entityFile;

   public ZoneFile(Long var1, File var2, File var3) throws IOException {
      this.key = var1;
      this.file = var2;

      try {
         this.entityFile = new NbtSlotFile(var3);
      } catch (Exception var6) {
         System.out.println("Broken entity file: " + var3 + " (" + var6.toString() + "), replacing..");
         var3.delete();
         var3.createNewFile();
         this.entityFile = new NbtSlotFile(var3);
      }

      this.channel = (new RandomAccessFile(var2, "rw")).getChannel();

      try {
         this.readHeader();
      } catch (Exception var5) {
         var5.printStackTrace();
         throw new IOException("Broken zone file: " + var2 + ": " + var5);
      }
   }

   private void readHeader() throws IOException {
      ZoneIo var1 = new ZoneIo(this.channel, 0L);
      ByteBuffer var2 = var1.read(4096);
      var2.flip();
      if (var2.remaining() >= 5) {
         int var3 = var2.getInt();
         if (var3 != 326332416) {
            throw new IOException("Bad magic number: " + var3);
         } else {
            short var4 = var2.getShort();
            if (var4 != 0) {
               throw new IOException("Bad version number: " + var4);
            } else {
               this.slotCount = var2.getShort();
               var2.asShortBuffer().get(this.slots);
               var2.position(var2.position() + this.slots.length * 2);
            }
         }
      }
   }

   private void writeHeader() throws IOException {
      ZoneIo var1 = new ZoneIo(this.channel, 0L);
      ByteBuffer var2 = ByteBuffer.allocate(4096);
      var2.order(ZonedChunkStorage.BYTE_ORDER);
      var2.putInt(326332416);
      var2.putShort((short)0);
      var2.putShort(this.slotCount);
      var2.asShortBuffer().put(this.slots);
      var2.position(var2.position() + this.slots.length * 2);
      var2.flip();
      var1.write((ByteBuffer)var2, 4096);
   }

   public void close() throws IOException {
      this.channel.force(true);
      this.channel.close();
      this.entityFile.close();
   }

   public ZoneIo getZoneIo(int var1) throws IOException {
      if (this.slots[var1] == 0) {
         this.slots[var1] = ++this.slotCount;
         this.writeHeader();
      }

      int var2 = (this.slots[var1] - 1) * 98560 + 4096;
      return new ZoneIo(this.channel, (long)var2);
   }

   public boolean containsSlot(int var1) {
      return this.slots[var1] > 0;
   }
}
