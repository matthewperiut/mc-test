package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MovePlayerPacket extends Packet {
   public double x;
   public double y;
   public double z;
   public double yView;
   public float yRot;
   public float xRot;
   public boolean onGround;
   public boolean hasPos;
   public boolean hasRot;

   public MovePlayerPacket() {
   }

   public MovePlayerPacket(boolean var1) {
      this.onGround = var1;
   }

   public void handle(PacketListener var1) {
      var1.handleMovePlayer(this);
   }

   public void read(DataInputStream var1) throws IOException {
      this.onGround = var1.read() != 0;
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.write(this.onGround ? 1 : 0);
   }

   public int getEstimatedSize() {
      return 1;
   }

   public static class Pos extends MovePlayerPacket {
      public Pos() {
         this.hasPos = true;
      }

      public Pos(double var1, double var3, double var5, double var7, boolean var9) {
         this.x = var1;
         this.y = var3;
         this.yView = var5;
         this.z = var7;
         this.onGround = var9;
         this.hasPos = true;
      }

      public void read(DataInputStream var1) throws IOException {
         this.x = var1.readDouble();
         this.y = var1.readDouble();
         this.yView = var1.readDouble();
         this.z = var1.readDouble();
         super.read(var1);
      }

      public void write(DataOutputStream var1) throws IOException {
         var1.writeDouble(this.x);
         var1.writeDouble(this.y);
         var1.writeDouble(this.yView);
         var1.writeDouble(this.z);
         super.write(var1);
      }

      public int getEstimatedSize() {
         return 33;
      }
   }

   public static class PosRot extends MovePlayerPacket {
      public PosRot() {
         this.hasRot = true;
         this.hasPos = true;
      }

      public PosRot(double var1, double var3, double var5, double var7, float var9, float var10, boolean var11) {
         this.x = var1;
         this.y = var3;
         this.yView = var5;
         this.z = var7;
         this.yRot = var9;
         this.xRot = var10;
         this.onGround = var11;
         this.hasRot = true;
         this.hasPos = true;
      }

      public void read(DataInputStream var1) throws IOException {
         this.x = var1.readDouble();
         this.y = var1.readDouble();
         this.yView = var1.readDouble();
         this.z = var1.readDouble();
         this.yRot = var1.readFloat();
         this.xRot = var1.readFloat();
         super.read(var1);
      }

      public void write(DataOutputStream var1) throws IOException {
         var1.writeDouble(this.x);
         var1.writeDouble(this.y);
         var1.writeDouble(this.yView);
         var1.writeDouble(this.z);
         var1.writeFloat(this.yRot);
         var1.writeFloat(this.xRot);
         super.write(var1);
      }

      public int getEstimatedSize() {
         return 41;
      }
   }

   public static class Rot extends MovePlayerPacket {
      public Rot() {
         this.hasRot = true;
      }

      public Rot(float var1, float var2, boolean var3) {
         this.yRot = var1;
         this.xRot = var2;
         this.onGround = var3;
         this.hasRot = true;
      }

      public void read(DataInputStream var1) throws IOException {
         this.yRot = var1.readFloat();
         this.xRot = var1.readFloat();
         super.read(var1);
      }

      public void write(DataOutputStream var1) throws IOException {
         var1.writeFloat(this.yRot);
         var1.writeFloat(this.xRot);
         super.write(var1);
      }

      public int getEstimatedSize() {
         return 9;
      }
   }
}
