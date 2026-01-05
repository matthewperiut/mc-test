package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import net.minecraft.world.level.Level;

public class BlockRegionUpdatePacket extends Packet {
   public int x;
   public int y;
   public int z;
   public int xs;
   public int ys;
   public int zs;
   public byte[] buffer;
   private int size;

   public BlockRegionUpdatePacket() {
      this.shouldDelay = true;
   }

   public BlockRegionUpdatePacket(int var1, int var2, int var3, int var4, int var5, int var6, Level var7) {
      this.shouldDelay = true;
      this.x = var1;
      this.y = var2;
      this.z = var3;
      this.xs = var4;
      this.ys = var5;
      this.zs = var6;
      byte[] var8 = var7.getBlocksAndData(var1, var2, var3, var4, var5, var6);
      Deflater var9 = new Deflater(1);

      try {
         var9.setInput(var8);
         var9.finish();
         this.buffer = new byte[var4 * var5 * var6 * 5 / 2];
         this.size = var9.deflate(this.buffer);
      } finally {
         var9.end();
      }

   }

   public void read(DataInputStream var1) throws IOException {
      this.x = var1.readInt();
      this.y = var1.readShort();
      this.z = var1.readInt();
      this.xs = var1.read() + 1;
      this.ys = var1.read() + 1;
      this.zs = var1.read() + 1;
      int var2 = var1.readInt();
      byte[] var3 = new byte[var2];
      var1.readFully(var3);
      this.buffer = new byte[this.xs * this.ys * this.zs * 5 / 2];
      Inflater var4 = new Inflater();
      var4.setInput(var3);

      try {
         var4.inflate(this.buffer);
      } catch (DataFormatException var9) {
         throw new IOException("Bad compressed data format");
      } finally {
         var4.end();
      }

   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.x);
      var1.writeShort(this.y);
      var1.writeInt(this.z);
      var1.write(this.xs - 1);
      var1.write(this.ys - 1);
      var1.write(this.zs - 1);
      var1.writeInt(this.size);
      var1.write(this.buffer, 0, this.size);
   }

   public void handle(PacketListener var1) {
      var1.handleBlockRegionUpdate(this);
   }

   public int getEstimatedSize() {
      return 17 + this.size;
   }
}
