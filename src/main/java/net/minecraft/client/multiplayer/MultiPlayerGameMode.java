package net.minecraft.client.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gamemode.GameMode;
import net.minecraft.network.packet.ContainerClickPacket;
import net.minecraft.network.packet.InteractPacket;
import net.minecraft.network.packet.PlayerActionPacket;
import net.minecraft.network.packet.SetCarriedItemPacket;
import net.minecraft.network.packet.UseItemPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class MultiPlayerGameMode extends GameMode {
   private int xDestroyBlock = -1;
   private int yDestroyBlock = -1;
   private int zDestroyBlock = -1;
   private float destroyProgress = 0.0F;
   private float oDestroyProgress = 0.0F;
   private float destroyTicks = 0.0F;
   private int destroyDelay = 0;
   private boolean isDestroying = false;
   private ClientConnection connection;
   private int carriedItem = 0;

   public MultiPlayerGameMode(Minecraft minecraft, ClientConnection connection) {
      super(minecraft);
      this.connection = connection;
   }

   public void initPlayer(Player player) {
      player.yRot = -180.0F;
   }

   public void init() {
   }

   public boolean destroyBlock(int x, int y, int z, int face) {
      this.connection.send(new PlayerActionPacket(3, x, y, z, face));
      int t = this.minecraft.level.getTile(x, y, z);
      boolean changed = super.destroyBlock(x, y, z, face);
      ItemInstance item = this.minecraft.player.getSelectedItem();
      if (item != null) {
         item.mineBlock(t, x, y, z);
         if (item.count == 0) {
            item.snap(this.minecraft.player);
            this.minecraft.player.removeSelectedItem();
         }
      }

      return changed;
   }

   public void startDestroyBlock(int x, int y, int z, int face) {
      this.isDestroying = true;
      this.connection.send(new PlayerActionPacket(0, x, y, z, face));
      int t = this.minecraft.level.getTile(x, y, z);
      if (t > 0 && this.destroyProgress == 0.0F) {
         Tile.tiles[t].attack(this.minecraft.level, x, y, z, this.minecraft.player);
      }

      if (t > 0 && Tile.tiles[t].getDestroyProgress(this.minecraft.player) >= 1.0F) {
         this.destroyBlock(x, y, z, face);
      }

   }

   public void stopDestroyBlock() {
      if (this.isDestroying) {
         this.isDestroying = false;
         this.connection.send(new PlayerActionPacket(2, 0, 0, 0, 0));
         this.destroyProgress = 0.0F;
         this.destroyDelay = 0;
      }
   }

   public void continueDestroyBlock(int x, int y, int z, int face) {
      this.isDestroying = true;
      this.ensureHasSentCarriedItem();
      this.connection.send(new PlayerActionPacket(1, x, y, z, face));
      if (this.destroyDelay > 0) {
         --this.destroyDelay;
      } else {
         if (x == this.xDestroyBlock && y == this.yDestroyBlock && z == this.zDestroyBlock) {
            int t = this.minecraft.level.getTile(x, y, z);
            if (t == 0) {
               return;
            }

            Tile tile = Tile.tiles[t];
            this.destroyProgress += tile.getDestroyProgress(this.minecraft.player);
            if (this.destroyTicks % 4.0F == 0.0F) {
               this.minecraft.soundEngine.play(tile.soundType.getStepSound(), (float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F, (tile.soundType.getVolume() + 1.0F) / 8.0F, tile.soundType.getPitch() * 0.5F);
            }

            ++this.destroyTicks;
            if (this.destroyProgress >= 1.0F) {
               this.destroyBlock(x, y, z, face);
               this.destroyProgress = 0.0F;
               this.oDestroyProgress = 0.0F;
               this.destroyTicks = 0.0F;
               this.destroyDelay = 5;
            }
         } else {
            this.destroyProgress = 0.0F;
            this.oDestroyProgress = 0.0F;
            this.destroyTicks = 0.0F;
            this.xDestroyBlock = x;
            this.yDestroyBlock = y;
            this.zDestroyBlock = z;
         }

      }
   }

   public void render(float a) {
      if (this.destroyProgress <= 0.0F) {
         this.minecraft.gui.progress = 0.0F;
         this.minecraft.levelRenderer.destroyProgress = 0.0F;
      } else {
         float dp = this.oDestroyProgress + (this.destroyProgress - this.oDestroyProgress) * a;
         this.minecraft.gui.progress = dp;
         this.minecraft.levelRenderer.destroyProgress = dp;
      }

   }

   public float getPickRange() {
      return 4.0F;
   }

   public void initLevel(Level level) {
      super.initLevel(level);
   }

   public void tick() {
      this.ensureHasSentCarriedItem();
      this.oDestroyProgress = this.destroyProgress;
      this.minecraft.soundEngine.playMusicTick();
   }

   private void ensureHasSentCarriedItem() {
      int newItem = this.minecraft.player.inventory.selected;
      if (newItem != this.carriedItem) {
         this.carriedItem = newItem;
         this.connection.send(new SetCarriedItemPacket(this.carriedItem));
      }

   }

   public boolean useItemOn(Player player, Level level, ItemInstance item, int x, int y, int z, int face) {
      this.ensureHasSentCarriedItem();
      this.connection.send(new UseItemPacket(x, y, z, face, player.inventory.getSelected()));
      boolean result = super.useItemOn(player, level, item, x, y, z, face);
      return result;
   }

   public boolean useItem(Player player, Level level, ItemInstance item) {
      this.ensureHasSentCarriedItem();
      this.connection.send(new UseItemPacket(-1, -1, -1, 255, player.inventory.getSelected()));
      boolean result = super.useItem(player, level, item);
      return result;
   }

   public Player createPlayer(Level level) {
      return new MultiplayerLocalPlayer(this.minecraft, level, this.minecraft.user, this.connection);
   }

   public void attack(Player player, Entity entity) {
      this.ensureHasSentCarriedItem();
      this.connection.send(new InteractPacket(player.entityId, entity.entityId, 1));
      player.attack(entity);
   }

   public void interact(Player player, Entity entity) {
      this.ensureHasSentCarriedItem();
      this.connection.send(new InteractPacket(player.entityId, entity.entityId, 0));
      player.interact(entity);
   }

   public ItemInstance handleInventoryMouseClick(int containerId, int slotNum, int buttonNum, Player player) {
      short changeUid = player.containerMenu.backup(player.inventory);
      ItemInstance clicked = super.handleInventoryMouseClick(containerId, slotNum, buttonNum, player);
      this.connection.send(new ContainerClickPacket(containerId, slotNum, buttonNum, clicked, changeUid));
      return clicked;
   }

   public void handleCloseInventory(int containerId, Player player) {
      if (containerId != -9999) {
         ;
      }
   }
}
