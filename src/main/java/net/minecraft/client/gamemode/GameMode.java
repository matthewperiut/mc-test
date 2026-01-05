package net.minecraft.client.gamemode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class GameMode {
   protected final Minecraft minecraft;
   public boolean instaBuild = false;

   public GameMode(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void initLevel(Level level) {
   }

   public void startDestroyBlock(int x, int y, int z, int face) {
      this.destroyBlock(x, y, z, face);
   }

   public boolean destroyBlock(int x, int y, int z, int face) {
      this.minecraft.particleEngine.destroy(x, y, z);
      Level level = this.minecraft.level;
      Tile oldTile = Tile.tiles[level.getTile(x, y, z)];
      int data = level.getData(x, y, z);
      boolean changed = level.setTile(x, y, z, 0);
      if (oldTile != null && changed) {
         this.minecraft.soundEngine.play(oldTile.soundType.getBreakSound(), (float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F, (oldTile.soundType.getVolume() + 1.0F) / 2.0F, oldTile.soundType.getPitch() * 0.8F);
         oldTile.destroy(level, x, y, z, data);
      }

      return changed;
   }

   public void continueDestroyBlock(int x, int y, int z, int face) {
   }

   public void stopDestroyBlock() {
   }

   public void render(float a) {
   }

   public float getPickRange() {
      return 5.0F;
   }

   public boolean useItem(Player player, Level level, ItemInstance item) {
      int oldCount = item.count;
      ItemInstance itemInstance = item.use(level, player);
      if (itemInstance != item || itemInstance != null && itemInstance.count != oldCount) {
         player.inventory.items[player.inventory.selected] = itemInstance;
         if (itemInstance.count == 0) {
            player.inventory.items[player.inventory.selected] = null;
         }

         return true;
      } else {
         return false;
      }
   }

   public void initPlayer(Player player) {
   }

   public void tick() {
   }

   public boolean canHurtPlayer() {
      return true;
   }

   public void adjustPlayer(Player player) {
   }

   public boolean useItemOn(Player player, Level level, ItemInstance item, int x, int y, int z, int face) {
      int t = level.getTile(x, y, z);
      if (t > 0 && Tile.tiles[t].use(level, x, y, z, player)) {
         return true;
      } else {
         return item == null ? false : item.useOn(player, level, x, y, z, face);
      }
   }

   public Player createPlayer(Level level) {
      return new LocalPlayer(this.minecraft, level, this.minecraft.user, level.dimension.id);
   }

   public void interact(Player player, Entity entity) {
      player.interact(entity);
   }

   public void attack(Player player, Entity entity) {
      player.attack(entity);
   }

   public ItemInstance handleInventoryMouseClick(int containerId, int slotNum, int buttonNum, Player player) {
      return player.containerMenu.clicked(slotNum, buttonNum, player);
   }

   public void handleCloseInventory(int containerId, Player player) {
      player.containerMenu.removed(player);
      player.containerMenu = player.inventoryMenu;
   }
}
