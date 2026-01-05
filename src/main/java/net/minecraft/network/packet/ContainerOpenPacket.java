package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ContainerOpenPacket extends Packet {
   public static final int CONTAINER = 0;
   public static final int WORKBENCH = 1;
   public static final int FURNACE = 2;
   public static final int TRAP = 3;
   public int containerId;
   public int type;
   public String title;
   public int size;

   public ContainerOpenPacket() {
   }

   public ContainerOpenPacket(int var1, int var2, String var3, int var4) {
      this.containerId = var1;
      this.type = var2;
      this.title = var3;
      this.size = var4;
   }

   public void handle(PacketListener var1) {
      var1.handleContainerOpen(this);
   }

   public void read(DataInputStream var1) throws IOException {
      this.containerId = var1.readByte();
      this.type = var1.readByte();
      this.title = var1.readUTF();
      this.size = var1.readByte();
   }

   public void write(DataOutputStream var1) throws IOException {
      var1.writeByte(this.containerId);
      var1.writeByte(this.type);
      var1.writeUTF(this.title);
      var1.writeByte(this.size);
   }

   public int getEstimatedSize() {
      return 3 + this.title.length();
   }
}
