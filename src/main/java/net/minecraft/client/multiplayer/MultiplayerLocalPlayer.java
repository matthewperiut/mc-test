package net.minecraft.client.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.packet.AnimatePacket;
import net.minecraft.network.packet.ChatPacket;
import net.minecraft.network.packet.ContainerClosePacket;
import net.minecraft.network.packet.MovePlayerPacket;
import net.minecraft.network.packet.PlayerActionPacket;
import net.minecraft.network.packet.PlayerCommandPacket;
import net.minecraft.network.packet.RespawnPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import util.Mth;

public class MultiplayerLocalPlayer extends LocalPlayer {
   public ClientConnection connection;
   private int lastInventorySendTime = 0;
   private boolean flashOnSetHealth = false;
   private double xLast;
   private double yLast1;
   private double yLast2;
   private double zLast;
   private float yRotLast;
   private float xRotLast;
   private boolean lastOnGround = false;
   private boolean lastSneaked = false;
   private int noSendTime = 0;

   public MultiplayerLocalPlayer(Minecraft minecraft, Level level, User user, ClientConnection connection) {
      super(minecraft, level, user, 0);
      this.connection = connection;
   }

   public boolean hurt(Entity source, int dmg) {
      return false;
   }

   public void heal(int heal) {
   }

   public void tick() {
      if (this.level.hasChunkAt(Mth.floor(this.x), 64, Mth.floor(this.z))) {
         super.tick();
         this.sendPosition();
      }
   }

   public void prepareForTick() {
   }

   public void sendPosition() {
      if (this.lastInventorySendTime++ == 20) {
         this.ensureHasSentInventory();
         this.lastInventorySendTime = 0;
      }

      boolean sneaking = this.isSneaking();
      if (sneaking != this.lastSneaked) {
         if (sneaking) {
            this.connection.send(new PlayerCommandPacket(this, 1));
         } else {
            this.connection.send(new PlayerCommandPacket(this, 2));
         }

         this.lastSneaked = sneaking;
      }

      double xdd = this.x - this.xLast;
      double ydd1 = this.bb.y0 - this.yLast1;
      double ydd2 = this.y - this.yLast2;
      double zdd = this.z - this.zLast;
      double rydd = (double)(this.yRot - this.yRotLast);
      double rxdd = (double)(this.xRot - this.xRotLast);
      boolean move = ydd1 != (double)0.0F || ydd2 != (double)0.0F || xdd != (double)0.0F || zdd != (double)0.0F;
      boolean rot = rydd != (double)0.0F || rxdd != (double)0.0F;
      if (this.riding != null) {
         if (rot) {
            this.connection.send(new MovePlayerPacket.Pos(this.xd, (double)-999.0F, (double)-999.0F, this.zd, this.onGround));
         } else {
            this.connection.send(new MovePlayerPacket.PosRot(this.xd, (double)-999.0F, (double)-999.0F, this.zd, this.yRot, this.xRot, this.onGround));
         }

         move = false;
      } else if (move && rot) {
         this.connection.send(new MovePlayerPacket.PosRot(this.x, this.bb.y0, this.y, this.z, this.yRot, this.xRot, this.onGround));
         this.noSendTime = 0;
      } else if (move) {
         this.connection.send(new MovePlayerPacket.Pos(this.x, this.bb.y0, this.y, this.z, this.onGround));
         this.noSendTime = 0;
      } else if (rot) {
         this.connection.send(new MovePlayerPacket.Rot(this.yRot, this.xRot, this.onGround));
         this.noSendTime = 0;
      } else {
         this.connection.send(new MovePlayerPacket(this.onGround));
         if (this.lastOnGround == this.onGround && this.noSendTime <= 20) {
            ++this.noSendTime;
         } else {
            this.noSendTime = 0;
         }
      }

      this.lastOnGround = this.onGround;
      if (move) {
         this.xLast = this.x;
         this.yLast1 = this.bb.y0;
         this.yLast2 = this.y;
         this.zLast = this.z;
      }

      if (rot) {
         this.yRotLast = this.yRot;
         this.xRotLast = this.xRot;
      }

   }

   public void drop() {
      this.connection.send(new PlayerActionPacket(4, 0, 0, 0, 0));
   }

   private void ensureHasSentInventory() {
   }

   protected void reallyDrop(ItemEntity itemEntity) {
   }

   public void chat(String message) {
      this.connection.send(new ChatPacket(message));
   }

   public void swing() {
      super.swing();
      this.connection.send(new AnimatePacket(this, 1));
   }

   public void respawn() {
      this.ensureHasSentInventory();
      this.connection.send(new RespawnPacket());
   }

   protected void actuallyHurt(int dmg) {
      this.health -= dmg;
   }

   public void closeContainer() {
      this.connection.send(new ContainerClosePacket(this.containerMenu.containerId));
      this.inventory.setCarried((ItemInstance)null);
      super.closeContainer();
   }

   public void hurtTo(int newHealth) {
      if (this.flashOnSetHealth) {
         super.hurtTo(newHealth);
      } else {
         this.health = newHealth;
         this.flashOnSetHealth = true;
      }

   }
}
