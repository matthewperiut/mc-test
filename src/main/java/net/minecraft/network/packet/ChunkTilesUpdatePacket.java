package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkTilesUpdatePacket extends Packet {
   public int xc;
   public int zc;
   public short[] positions;
   public byte[] blocks;
   public byte[] data;
   public int count;

   public ChunkTilesUpdatePacket() {
      this.shouldDelay = true;
   }

   public ChunkTilesUpdatePacket(int var1, int var2, short[] var3, int var4, Level var5) {
      this.shouldDelay = true;
      this.xc = var1;
      this.zc = var2;
      this.count = var4;
      this.positions = new short[var4];
      this.blocks = new byte[var4];
      this.data = new byte[var4];
      LevelChunk var6 = var5.getChunk(var1, var2);

      for(int var7 = 0; var7 < var4; ++var7) {
         int var8 = var3[var7] >> 12 & 15;
         int var9 = var3[var7] >> 8 & 15;
         int var10 = var3[var7] & 255;
         this.positions[var7] = var3[var7];
         this.blocks[var7] = (byte)var6.getTile(var8, var10, var9);
         this.data[var7] = (byte)var6.getData(var8, var10, var9);
      }

   }

   public void read(DataInputStream var1) throws IOException {
      this.xc = var1.readInt();
      this.zc = var1.readInt();
      this.count = var1.readShort() & '\uffff';
      this.positions = new short[this.count];
      this.blocks = new byte[this.count];
      this.data = new byte[this.count];

      for(int var2 = 0; var2 < this.count; ++var2) {
         this.positions[var2] = var1.readShort();
      }

      var1.readFully(this.blocks);
      var1.readFully(this.data);
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.xc);
      var1.writeInt(this.zc);
      var1.writeShort((short)this.count);

      for(int var2 = 0; var2 < this.count; ++var2) {
         var1.writeShort(this.positions[var2]);
      }

      var1.write(this.blocks);
      var1.write(this.data);
   }

   public void handle(PacketListener var1) {
      var1.handleChunkTilesUpdate(this);
   }

   public int getEstimatedSize() {
      return 10 + this.count * 4;
   }
}
