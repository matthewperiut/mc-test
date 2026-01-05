package net.minecraft.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketListener;

public class Connection {
   public static final Object threadCounterLock = new Object();
   public static int readThreads;
   public static int writeThreads;
   private Object writeLock = new Object();
   private static final int MAX_TICKS_WITHOUT_INPUT = 1200;
   public static final int IPTOS_LOWCOST = 2;
   public static final int IPTOS_RELIABILITY = 4;
   public static final int IPTOS_THROUGHPUT = 8;
   public static final int IPTOS_LOWDELAY = 16;
   private Socket socket;
   private final SocketAddress address;
   private DataInputStream dis;
   private DataOutputStream dos;
   private boolean running = true;
   private List<Packet> incoming = Collections.synchronizedList(new ArrayList());
   private List<Packet> outgoing = Collections.synchronizedList(new ArrayList());
   private List<Packet> outgoing_slow = Collections.synchronizedList(new ArrayList());
   private PacketListener packetListener;
   private boolean quitting = false;
   private Thread writeThread;
   private Thread readThread;
   private boolean disconnected = false;
   private String disconnectReason = "";
   private Object[] disconnectReasonObjects;
   private int noInputTicks = 0;
   private int estimatedRemaining = 0;
   public int fakeLag = 0;
   private int slowWriteDelay = 0;

   public Connection(Socket var1, String var2, PacketListener var3) throws IOException {
      this.socket = var1;
      this.address = var1.getRemoteSocketAddress();
      this.packetListener = var3;
      var1.setTrafficClass(24);
      this.dis = new DataInputStream(var1.getInputStream());
      this.dos = new DataOutputStream(var1.getOutputStream());
      this.readThread = new Thread(var2 + " read thread") {
         public void run() {
            synchronized(Connection.threadCounterLock) {
               ++Connection.readThreads;
            }

            try {
               while(Connection.this.running && !Connection.this.quitting) {
                  Connection.this.readTick();
               }
            } finally {
               synchronized(Connection.threadCounterLock) {
                  --Connection.readThreads;
               }
            }

         }
      };
      this.writeThread = new Thread(var2 + " write thread") {
         public void run() {
            synchronized(Connection.threadCounterLock) {
               ++Connection.writeThreads;
            }

            try {
               while(Connection.this.running) {
                  Connection.this.writeTick();
               }
            } finally {
               synchronized(Connection.threadCounterLock) {
                  --Connection.writeThreads;
               }
            }

         }
      };
      this.readThread.start();
      this.writeThread.start();
   }

   public void setListener(PacketListener var1) {
      this.packetListener = var1;
   }

   public void send(Packet var1) {
      if (!this.quitting) {
         synchronized(this.writeLock) {
            this.estimatedRemaining += var1.getEstimatedSize() + 1;
            if (var1.shouldDelay) {
               this.outgoing_slow.add(var1);
            } else {
               this.outgoing.add(var1);
            }

         }
      }
   }

   public void queueSend(Packet var1) {
      if (!this.quitting) {
         this.outgoing_slow.add(var1);
      }
   }

   private void writeTick() {
      try {
         boolean var1 = true;
         if (!this.outgoing.isEmpty() && (this.fakeLag == 0 || System.currentTimeMillis() - ((Packet)this.outgoing.get(0)).createTime >= (long)this.fakeLag)) {
            var1 = false;
            Packet var2;
            synchronized(this.writeLock) {
               var2 = (Packet)this.outgoing.remove(0);
               this.estimatedRemaining -= var2.getEstimatedSize() + 1;
            }

            Packet.writePacket(var2, this.dos);
         }

         if ((var1 || this.slowWriteDelay-- <= 0) && !this.outgoing_slow.isEmpty() && (this.fakeLag == 0 || System.currentTimeMillis() - ((Packet)this.outgoing_slow.get(0)).createTime >= (long)this.fakeLag)) {
            var1 = false;
            Packet var10;
            synchronized(this.writeLock) {
               var10 = (Packet)this.outgoing_slow.remove(0);
               this.estimatedRemaining -= var10.getEstimatedSize() + 1;
            }

            Packet.writePacket(var10, this.dos);
            this.slowWriteDelay = 50;
         }

         if (var1) {
            Thread.sleep(10L);
         }
      } catch (InterruptedException var8) {
      } catch (Exception var9) {
         if (!this.disconnected) {
            this.handleException(var9);
         }
      }

   }

   private void readTick() {
      try {
         Packet var1 = Packet.readPacket(this.dis);
         if (var1 != null) {
            this.incoming.add(var1);
         } else {
            this.close("disconnect.endOfStream");
         }
      } catch (Exception var2) {
         if (!this.disconnected) {
            this.handleException(var2);
         }
      }

   }

   private void handleException(Exception var1) {
      var1.printStackTrace();
      this.close("disconnect.genericReason", "Internal exception: " + var1.toString());
   }

   public void close(String var1, Object... var2) {
      if (this.running) {
         this.disconnected = true;
         this.disconnectReason = var1;
         this.disconnectReasonObjects = var2;
         (new Thread() {
            public void run() {
               try {
                  Thread.sleep(5000L);
                  if (Connection.this.readThread.isAlive()) {
                     try {
                        Connection.this.readThread.stop();
                     } catch (Throwable var3) {
                     }
                  }

                  if (Connection.this.writeThread.isAlive()) {
                     try {
                        Connection.this.writeThread.stop();
                     } catch (Throwable var2) {
                     }
                  }
               } catch (InterruptedException var4) {
                  var4.printStackTrace();
               }

            }
         }).start();
         this.running = false;

         try {
            this.dis.close();
            this.dis = null;
         } catch (Throwable var6) {
         }

         try {
            this.dos.close();
            this.dos = null;
         } catch (Throwable var5) {
         }

         try {
            this.socket.close();
            this.socket = null;
         } catch (Throwable var4) {
         }

      }
   }

   public void tick() {
      if (this.estimatedRemaining > 1048576) {
         this.close("disconnect.overflow");
      }

      if (this.incoming.isEmpty()) {
         if (this.noInputTicks++ == 1200) {
            this.close("disconnect.timeout");
         }
      } else {
         this.noInputTicks = 0;
      }

      int var1 = 100;

      while(!this.incoming.isEmpty() && var1-- >= 0) {
         Packet var2 = (Packet)this.incoming.remove(0);
         var2.handle(this.packetListener);
      }

      if (this.disconnected && this.incoming.isEmpty()) {
         this.packetListener.onDisconnect(this.disconnectReason, this.disconnectReasonObjects);
      }

   }

   public SocketAddress getRemoteAddress() {
      return this.address;
   }

   public void sendAndQuit() {
      this.quitting = true;
      this.readThread.interrupt();
      (new Thread() {
         public void run() {
            try {
               Thread.sleep(2000L);
               if (Connection.this.running) {
                  Connection.this.writeThread.interrupt();
                  Connection.this.close("disconnect.closed");
               }
            } catch (Exception var2) {
               var2.printStackTrace();
            }

         }
      }).start();
   }

   public int countDelayedPackets() {
      return this.outgoing_slow.size();
   }
}
