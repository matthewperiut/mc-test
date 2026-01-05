package net.minecraft.client.multiplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.particle.TakeAnimationParticle;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.packet.AddEntityPacket;
import net.minecraft.network.packet.AddItemEntityPacket;
import net.minecraft.network.packet.AddMobPacket;
import net.minecraft.network.packet.AddPaintingPacket;
import net.minecraft.network.packet.AddPlayerPacket;
import net.minecraft.network.packet.AnimatePacket;
import net.minecraft.network.packet.BlockRegionUpdatePacket;
import net.minecraft.network.packet.ChatPacket;
import net.minecraft.network.packet.ChunkTilesUpdatePacket;
import net.minecraft.network.packet.ChunkVisibilityPacket;
import net.minecraft.network.packet.ContainerAckPacket;
import net.minecraft.network.packet.ContainerClosePacket;
import net.minecraft.network.packet.ContainerOpenPacket;
import net.minecraft.network.packet.ContainerSetContentPacket;
import net.minecraft.network.packet.ContainerSetDataPacket;
import net.minecraft.network.packet.ContainerSetSlotPacket;
import net.minecraft.network.packet.DisconnectPacket;
import net.minecraft.network.packet.EntityEventPacket;
import net.minecraft.network.packet.ExplodePacket;
import net.minecraft.network.packet.LoginPacket;
import net.minecraft.network.packet.MoveEntityPacket;
import net.minecraft.network.packet.MovePlayerPacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketListener;
import net.minecraft.network.packet.PreLoginPacket;
import net.minecraft.network.packet.RemoveEntityPacket;
import net.minecraft.network.packet.RespawnPacket;
import net.minecraft.network.packet.SetEntityDataPacket;
import net.minecraft.network.packet.SetEntityMotionPacket;
import net.minecraft.network.packet.SetEquippedItemPacket;
import net.minecraft.network.packet.SetHealthPacket;
import net.minecraft.network.packet.SetRidingPacket;
import net.minecraft.network.packet.SetSpawnPositionPacket;
import net.minecraft.network.packet.SetTimePacket;
import net.minecraft.network.packet.SignUpdatePacket;
import net.minecraft.network.packet.TakeItemEntityPacket;
import net.minecraft.network.packet.TeleportEntityPacket;
import net.minecraft.network.packet.TileEventPacket;
import net.minecraft.network.packet.TileUpdatePacket;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityIO;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Painting;
import net.minecraft.world.entity.SynchedEntityData;
import net.minecraft.world.entity.item.Boat;
import net.minecraft.world.entity.item.FallingTile;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.Minecart;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.DispenserTileEntity;
import net.minecraft.world.level.tile.entity.FurnaceTileEntity;
import net.minecraft.world.level.tile.entity.SignTileEntity;
import net.minecraft.world.level.tile.entity.TileEntity;
import util.Mth;

public class ClientConnection extends PacketListener {
   private boolean done = false;
   private Connection connection;
   public String message;
   private Minecraft minecraft;
   private MultiPlayerLevel level;
   private boolean started = false;
   Random random = new Random();

   public ClientConnection(Minecraft minecraft, String ip, int port) throws IOException {
      this.minecraft = minecraft;
      Socket socket = new Socket(InetAddress.getByName(ip), port);
      this.connection = new Connection(socket, "Client", this);
   }

   public void tick() {
      if (!this.done) {
         this.connection.tick();
      }
   }

   public void handleLogin(LoginPacket packet) {
      this.minecraft.gameMode = new MultiPlayerGameMode(this.minecraft, this);
      this.level = new MultiPlayerLevel(this, packet.seed, packet.dimension);
      this.level.isOnline = true;
      this.minecraft.setLevel(this.level);
      this.minecraft.setScreen(new ReceivingLevelScreen(this));
      this.minecraft.player.entityId = packet.clientVersion;
   }

   public void handleAddItemEntity(AddItemEntityPacket packet) {
      double x = (double)packet.x / (double)32.0F;
      double y = (double)packet.y / (double)32.0F;
      double z = (double)packet.z / (double)32.0F;
      ItemEntity itemEntity = new ItemEntity(this.level, x, y, z, new ItemInstance(packet.itemId, packet.itemCount, packet.auxValue));
      itemEntity.xd = (double)packet.xa / (double)128.0F;
      itemEntity.yd = (double)packet.ya / (double)128.0F;
      itemEntity.zd = (double)packet.za / (double)128.0F;
      itemEntity.xp = packet.x;
      itemEntity.yp = packet.y;
      itemEntity.zp = packet.z;
      this.level.putEntity(packet.id, itemEntity);
   }

   public void handleAddEntity(AddEntityPacket packet) {
      double x = (double)packet.x / (double)32.0F;
      double y = (double)packet.y / (double)32.0F;
      double z = (double)packet.z / (double)32.0F;
      Entity e = null;
      if (packet.type == 10) {
         e = new Minecart(this.level, x, y, z, 0);
      }

      if (packet.type == 11) {
         e = new Minecart(this.level, x, y, z, 1);
      }

      if (packet.type == 12) {
         e = new Minecart(this.level, x, y, z, 2);
      }

      if (packet.type == 90) {
         e = new FishingHook(this.level, x, y, z);
      }

      if (packet.type == 60) {
         e = new Arrow(this.level, x, y, z);
      }

      if (packet.type == 61) {
         e = new Snowball(this.level, x, y, z);
      }

      if (packet.type == 62) {
         e = new ThrownEgg(this.level, x, y, z);
      }

      if (packet.type == 1) {
         e = new Boat(this.level, x, y, z);
      }

      if (packet.type == 50) {
         e = new PrimedTnt(this.level, x, y, z);
      }

      if (packet.type == 70) {
         e = new FallingTile(this.level, x, y, z, Tile.sand.id);
      }

      if (packet.type == 71) {
         e = new FallingTile(this.level, x, y, z, Tile.gravel.id);
      }

      if (e != null) {
         e.xp = packet.x;
         e.yp = packet.y;
         e.zp = packet.z;
         e.yRot = 0.0F;
         e.xRot = 0.0F;
         e.entityId = packet.id;
         this.level.putEntity(packet.id, e);
      }

   }

   public void handleAddPainting(AddPaintingPacket packet) {
      Painting painting = new Painting(this.level, packet.x, packet.y, packet.z, packet.dir, packet.motive);
      this.level.putEntity(packet.id, painting);
   }

   public void handleSetEntityMotion(SetEntityMotionPacket packet) {
      Entity e = this.getEntity(packet.id);
      if (e != null) {
         e.lerpMotion((double)packet.xa / (double)8000.0F, (double)packet.ya / (double)8000.0F, (double)packet.za / (double)8000.0F);
      }
   }

   public void handleSetEntityData(SetEntityDataPacket packet) {
      Entity e = this.getEntity(packet.id);
      if (e != null && packet.getUnpackedData() != null) {
         e.getEntityData().assignValues(packet.getUnpackedData());
      }

   }

   public void handleAddPlayer(AddPlayerPacket packet) {
      double x = (double)packet.x / (double)32.0F;
      double y = (double)packet.y / (double)32.0F;
      double z = (double)packet.z / (double)32.0F;
      float yRot = (float)(packet.yRot * 360) / 256.0F;
      float xRot = (float)(packet.xRot * 360) / 256.0F;
      RemotePlayer player = new RemotePlayer(this.minecraft.level, packet.name);
      player.xp = packet.x;
      player.yp = packet.y;
      player.zp = packet.z;
      int item = packet.carriedItem;
      if (item == 0) {
         player.inventory.items[player.inventory.selected] = null;
      } else {
         player.inventory.items[player.inventory.selected] = new ItemInstance(item, 1, 0);
      }

      player.absMoveTo(x, y, z, yRot, xRot);
      this.level.putEntity(packet.id, player);
   }

   public void handleTeleportEntity(TeleportEntityPacket packet) {
      Entity e = this.getEntity(packet.id);
      if (e != null) {
         e.xp = packet.x;
         e.yp = packet.y;
         e.zp = packet.z;
         double x = (double)e.xp / (double)32.0F;
         double y = (double)e.yp / (double)32.0F + (double)0.015625F;
         double z = (double)e.zp / (double)32.0F;
         float yRot = (float)(packet.yRot * 360) / 256.0F;
         float xRot = (float)(packet.xRot * 360) / 256.0F;
         e.lerpTo(x, y, z, yRot, xRot, 3);
      }
   }

   public void handleMoveEntity(MoveEntityPacket packet) {
      Entity e = this.getEntity(packet.id);
      if (e != null) {
         e.xp += packet.xa;
         e.yp += packet.ya;
         e.zp += packet.za;
         double x = (double)e.xp / (double)32.0F;
         double y = (double)e.yp / (double)32.0F + (double)0.015625F;
         double z = (double)e.zp / (double)32.0F;
         float yRot = packet.hasRot ? (float)(packet.yRot * 360) / 256.0F : e.yRot;
         float xRot = packet.hasRot ? (float)(packet.xRot * 360) / 256.0F : e.xRot;
         e.lerpTo(x, y, z, yRot, xRot, 3);
      }
   }

   public void handleRemoveEntity(RemoveEntityPacket packet) {
      this.level.removeEntity(packet.id);
   }

   public void handleMovePlayer(MovePlayerPacket packet) {
      Player player = this.minecraft.player;
      double x = player.x;
      double y = player.y;
      double z = player.z;
      float yRot = player.yRot;
      float xRot = player.xRot;
      if (packet.hasPos) {
         x = packet.x;
         y = packet.y;
         z = packet.z;
      }

      if (packet.hasRot) {
         yRot = packet.yRot;
         xRot = packet.xRot;
      }

      player.ySlideOffset = 0.0F;
      player.xd = player.yd = player.zd = (double)0.0F;
      player.absMoveTo(x, y, z, yRot, xRot);
      packet.x = player.x;
      packet.y = player.bb.y0;
      packet.z = player.z;
      packet.yView = player.y;
      this.connection.send(packet);
      if (!this.started) {
         this.minecraft.player.xo = this.minecraft.player.x;
         this.minecraft.player.yo = this.minecraft.player.y;
         this.minecraft.player.zo = this.minecraft.player.z;
         this.started = true;
         this.minecraft.setScreen((Screen)null);
      }

   }

   public void handleChunkVisibility(ChunkVisibilityPacket packet) {
      this.level.setChunkVisible(packet.x, packet.z, packet.visible);
   }

   public void handleChunkTilesUpdate(ChunkTilesUpdatePacket packet) {
      LevelChunk lc = this.level.getChunk(packet.xc, packet.zc);
      int xo = packet.xc * 16;
      int zo = packet.zc * 16;

      for(int i = 0; i < packet.count; ++i) {
         int pos = packet.positions[i];
         int tile = packet.blocks[i] & 255;
         int data = packet.data[i];
         int x = pos >> 12 & 15;
         int z = pos >> 8 & 15;
         int y = pos & 255;
         lc.setTileAndData(x, y, z, tile, data);
         this.level.clearResetRegion(x + xo, y, z + zo, x + xo, y, z + zo);
         this.level.setTilesDirty(x + xo, y, z + zo, x + xo, y, z + zo);
      }

   }

   public void handleBlockRegionUpdate(BlockRegionUpdatePacket packet) {
      this.level.clearResetRegion(packet.x, packet.y, packet.z, packet.x + packet.xs - 1, packet.y + packet.ys - 1, packet.z + packet.zs - 1);
      this.level.setBlocksAndData(packet.x, packet.y, packet.z, packet.xs, packet.ys, packet.zs, packet.buffer);
   }

   public void handleTileUpdate(TileUpdatePacket packet) {
      this.level.doSetTileAndData(packet.x, packet.y, packet.z, packet.block, packet.data);
   }

   public void handleDisconnect(DisconnectPacket packet) {
      this.connection.close("disconnect.kicked");
      this.done = true;
      this.minecraft.setLevel((Level)null);
      this.minecraft.setScreen(new DisconnectedScreen("disconnect.disconnected", "disconnect.genericReason", new Object[]{packet.reason}));
   }

   public void onDisconnect(String reason, Object[] reasonObjects) {
      if (!this.done) {
         this.done = true;
         this.minecraft.setLevel((Level)null);
         this.minecraft.setScreen(new DisconnectedScreen("disconnect.lost", reason, reasonObjects));
      }
   }

   public void send(Packet packet) {
      if (!this.done) {
         this.connection.send(packet);
      }
   }

   public void handleTakeItemEntity(TakeItemEntityPacket packet) {
      Entity from = this.getEntity(packet.itemId);
      Mob to = (Mob)this.getEntity(packet.playerId);
      if (to == null) {
         to = this.minecraft.player;
      }

      if (from != null) {
         this.level.playSound(from, "random.pop", 0.2F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
         this.minecraft.particleEngine.add(new TakeAnimationParticle(this.minecraft.level, from, to, -0.5F));
         this.level.removeEntity(packet.itemId);
      }

   }

   public void handleChat(ChatPacket packet) {
      this.minecraft.gui.addMessage(packet.message);
   }

   public void handleAnimate(AnimatePacket packet) {
      Entity e = this.getEntity(packet.id);
      if (e != null) {
         if (packet.action == 1) {
            Player player = (Player)e;
            player.swing();
         } else if (packet.action == 2) {
            e.animateHurt();
         }

      }
   }

   public void handlePreLogin(PreLoginPacket packet) {
      if (packet.userName.equals("-")) {
         this.send(new LoginPacket(this.minecraft.user.name, "Password", 8));
      } else {
         try {
            URL url = new URL("http://www.minecraft.net/game/joinserver.jsp?user=" + this.minecraft.user.name + "&sessionId=" + this.minecraft.user.sessionId + "&serverId=" + packet.userName);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String msg = br.readLine();
            br.close();
            if (msg.equalsIgnoreCase("ok")) {
               this.send(new LoginPacket(this.minecraft.user.name, "Password", 8));
            } else {
               this.connection.close("disconnect.loginFailedInfo", msg);
            }
         } catch (Exception e) {
            e.printStackTrace();
            this.connection.close("disconnect.genericReason", "Internal client error: " + e.toString());
         }
      }

   }

   public void close() {
      this.done = true;
      this.connection.close("disconnect.closed");
   }

   public void handleAddMob(AddMobPacket packet) {
      double x = (double)packet.x / (double)32.0F;
      double y = (double)packet.y / (double)32.0F;
      double z = (double)packet.z / (double)32.0F;
      float yRot = (float)(packet.yRot * 360) / 256.0F;
      float xRot = (float)(packet.xRot * 360) / 256.0F;
      Mob mob = (Mob)EntityIO.newById(packet.type, this.minecraft.level);
      mob.xp = packet.x;
      mob.yp = packet.y;
      mob.zp = packet.z;
      mob.entityId = packet.id;
      mob.absMoveTo(x, y, z, yRot, xRot);
      mob.interpolateOnly = true;
      this.level.putEntity(packet.id, mob);
      List<SynchedEntityData.DataItem> unpackedData = packet.getUnpackedData();
      if (unpackedData != null) {
         mob.getEntityData().assignValues(unpackedData);
      }

   }

   public void handleSetTime(SetTimePacket packet) {
      this.minecraft.level.setTime(packet.time);
   }

   public void handleSetSpawn(SetSpawnPositionPacket packet) {
      this.level.xSpawn = packet.x;
      this.level.ySpawn = packet.y;
      this.level.zSpawn = packet.z;
   }

   public void handleRidePacket(SetRidingPacket packet) {
      Entity rider = this.getEntity(packet.riderId);
      Entity ridden = this.getEntity(packet.riddenId);
      if (packet.riderId == this.minecraft.player.entityId) {
         rider = this.minecraft.player;
      }

      if (rider != null) {
         rider.ride(ridden);
      }
   }

   public void handleEntityEvent(EntityEventPacket packet) {
      Entity e = this.getEntity(packet.entityId);
      if (e != null) {
         e.handleEntityEvent(packet.eventId);
      }

   }

   private Entity getEntity(int entityId) {
      return (Entity)(entityId == this.minecraft.player.entityId ? this.minecraft.player : this.level.getEntity(entityId));
   }

   public void handleSetHealth(SetHealthPacket packet) {
      this.minecraft.player.hurtTo(packet.health);
   }

   public void handleRespawn(RespawnPacket packet) {
      this.minecraft.respawnPlayer();
   }

   public void handleExplosion(ExplodePacket packet) {
      Explosion e = new Explosion(this.minecraft.level, (Entity)null, packet.x, packet.y, packet.z, packet.r);
      e.toBlow = packet.toBlow;
      e.addParticles();
   }

   public void handleContainerOpen(ContainerOpenPacket packet) {
      if (packet.type == 0) {
         Container container = new SimpleContainer(packet.title, packet.size);
         this.minecraft.player.openContainer(container);
         this.minecraft.player.containerMenu.containerId = packet.containerId;
      } else if (packet.type == 2) {
         FurnaceTileEntity fte = new FurnaceTileEntity();
         this.minecraft.player.openFurnace(fte);
         this.minecraft.player.containerMenu.containerId = packet.containerId;
      } else if (packet.type == 3) {
         DispenserTileEntity atte = new DispenserTileEntity();
         this.minecraft.player.openTrap(atte);
         this.minecraft.player.containerMenu.containerId = packet.containerId;
      } else if (packet.type == 1) {
         Player p = this.minecraft.player;
         this.minecraft.player.startCrafting(Mth.floor(p.x), Mth.floor(p.y), Mth.floor(p.z));
         this.minecraft.player.containerMenu.containerId = packet.containerId;
      }

   }

   public void handleContainerSetSlot(ContainerSetSlotPacket packet) {
      if (packet.containerId == -1) {
         this.minecraft.player.inventory.setCarried(packet.item);
      } else if (packet.containerId == 0) {
         this.minecraft.player.inventoryMenu.setItem(packet.slot, packet.item);
      } else if (packet.containerId == this.minecraft.player.containerMenu.containerId) {
         this.minecraft.player.containerMenu.setItem(packet.slot, packet.item);
      }

   }

   public void handleContainerAck(ContainerAckPacket packet) {
      AbstractContainerMenu menu = null;
      if (packet.containerId == 0) {
         menu = this.minecraft.player.inventoryMenu;
      } else if (packet.containerId == this.minecraft.player.containerMenu.containerId) {
         menu = this.minecraft.player.containerMenu;
      }

      if (menu != null) {
         if (packet.accepted) {
            menu.deleteBackup(packet.uid);
         } else {
            menu.rollbackToBackup(packet.uid);
            this.send(new ContainerAckPacket(packet.containerId, packet.uid, true));
         }
      }

   }

   public void handleContainerContent(ContainerSetContentPacket packet) {
      if (packet.containerId == 0) {
         this.minecraft.player.inventoryMenu.setAll(packet.items);
      } else if (packet.containerId == this.minecraft.player.containerMenu.containerId) {
         this.minecraft.player.containerMenu.setAll(packet.items);
      }

   }

   public void handleSignUpdate(SignUpdatePacket packet) {
      if (this.minecraft.level.hasChunkAt(packet.x, packet.y, packet.z)) {
         TileEntity te = this.minecraft.level.getTileEntity(packet.x, packet.y, packet.z);
         if (te instanceof SignTileEntity) {
            SignTileEntity ste = (SignTileEntity)te;

            for(int i = 0; i < 4; ++i) {
               ste.messages[i] = packet.lines[i];
            }

            ste.setChanged();
         }
      }

   }

   public void handleContainerSetData(ContainerSetDataPacket packet) {
      this.onUnhandledPacket(packet);
      if (this.minecraft.player.containerMenu != null && this.minecraft.player.containerMenu.containerId == packet.containerId) {
         this.minecraft.player.containerMenu.setData(packet.id, packet.value);
      }

   }

   public void handleSetEquippedItem(SetEquippedItemPacket packet) {
      Entity entity = this.getEntity(packet.entity);
      if (entity != null) {
         entity.setEquippedSlot(packet.slot, packet.item, packet.auxValue);
      }

   }

   public void handleContainerClose(ContainerClosePacket packet) {
      this.minecraft.player.closeContainer();
   }

   public void handleTileEvent(TileEventPacket packet) {
      this.minecraft.level.tileEvent(packet.x, packet.y, packet.z, packet.b0, packet.b1);
   }
}
