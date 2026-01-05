package net.minecraft.world.level.chunk.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ZoneIo {
   private FileChannel channel;
   private long pos;

   public ZoneIo(FileChannel var1, long var2) {
      this.channel = var1;
      this.pos = var2;
   }

   public void write(byte[] var1, int var2) throws IOException {
      ByteBuffer var3 = ByteBuffer.wrap(var1);
      if (var1.length != var2) {
         throw new IllegalArgumentException("Expected " + var2 + " bytes, got " + var1.length);
      } else {
         var3.order(ZonedChunkStorage.BYTE_ORDER);
         var3.position(var1.length);
         var3.flip();
         this.write(var3, var2);
      }
   }

   public void write(ByteBuffer var1, int var2) throws IOException {
      this.channel.position(this.pos);
      this.channel.write(var1);
      this.pos += (long)var2;
   }

   public ByteBuffer read(int var1) throws IOException {
      byte[] var2 = new byte[var1];
      this.channel.position(this.pos);
      ByteBuffer var3 = ByteBuffer.wrap(var2);
      var3.order(ZonedChunkStorage.BYTE_ORDER);
      var3.position(var1);
      var3.flip();
      this.channel.read(var3);
      this.pos += (long)var1;
      return var3;
   }

   public void flush() throws IOException {
      this.channel.force(false);
   }
}
