package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MoveEntityPacket extends Packet {
   public int id;
   public byte xa;
   public byte ya;
   public byte za;
   public byte yRot;
   public byte xRot;
   public boolean hasRot = false;

   public MoveEntityPacket() {
   }

   public MoveEntityPacket(int var1) {
      this.id = var1;
   }

   public void read(DataInputStream var1) throws IOException {
      this.id = var1.readInt();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeInt(this.id);
   }

   public void handle(PacketListener var1) {
      var1.handleMoveEntity(this);
   }

   public int getEstimatedSize() {
      return 4;
   }

   public static class Pos extends MoveEntityPacket {
      public Pos() {
      }

      public Pos(int var1, byte var2, byte var3, byte var4) {
         super(var1);
         this.xa = var2;
         this.ya = var3;
         this.za = var4;
      }

      public void read(DataInputStream var1) throws IOException {
         super.read(var1);
         this.xa = var1.readByte();
         this.ya = var1.readByte();
         this.za = var1.readByte();
      }

      public void write(DataOutputStream var1) throws IOException {
         super.write(var1);
         var1.writeByte(this.xa);
         var1.writeByte(this.ya);
         var1.writeByte(this.za);
      }

      public int getEstimatedSize() {
         return 7;
      }
   }

   public static class PosRot extends MoveEntityPacket {
      public PosRot() {
         this.hasRot = true;
      }

      public PosRot(int var1, byte var2, byte var3, byte var4, byte var5, byte var6) {
         super(var1);
         this.xa = var2;
         this.ya = var3;
         this.za = var4;
         this.yRot = var5;
         this.xRot = var6;
         this.hasRot = true;
      }

      public void read(DataInputStream var1) throws IOException {
         super.read(var1);
         this.xa = var1.readByte();
         this.ya = var1.readByte();
         this.za = var1.readByte();
         this.yRot = var1.readByte();
         this.xRot = var1.readByte();
      }

      public void write(DataOutputStream var1) throws IOException {
         super.write(var1);
         var1.writeByte(this.xa);
         var1.writeByte(this.ya);
         var1.writeByte(this.za);
         var1.writeByte(this.yRot);
         var1.writeByte(this.xRot);
      }

      public int getEstimatedSize() {
         return 9;
      }
   }

   public static class Rot extends MoveEntityPacket {
      public Rot() {
         this.hasRot = true;
      }

      public Rot(int var1, byte var2, byte var3) {
         super(var1);
         this.yRot = var2;
         this.xRot = var3;
         this.hasRot = true;
      }

      public void read(DataInputStream var1) throws IOException {
         super.read(var1);
         this.yRot = var1.readByte();
         this.xRot = var1.readByte();
      }

      public void write(DataOutputStream var1) throws IOException {
         super.write(var1);
         var1.writeByte(this.yRot);
         var1.writeByte(this.xRot);
      }

      public int getEstimatedSize() {
         return 6;
      }
   }
}
