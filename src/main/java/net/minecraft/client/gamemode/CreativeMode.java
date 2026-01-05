package net.minecraft.client.gamemode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class CreativeMode extends GameMode {
   public CreativeMode(Minecraft minecraft) {
      super(minecraft);
      this.instaBuild = true;
   }

   public void init() {
   }

   public void initPlayer() {
      for(int i = 0; i < 9; ++i) {
         this.minecraft.player.inventory.items[i] = new ItemInstance((Tile)User.allowedTiles.get(i));
      }

   }

   public void adjustPlayer(Player player) {
      for(int i = 0; i < 9; ++i) {
         if (player.inventory.items[i] == null) {
            this.minecraft.player.inventory.items[i] = new ItemInstance((Tile)User.allowedTiles.get(i));
         } else {
            this.minecraft.player.inventory.items[i].count = 1;
         }
      }

   }

   public boolean canHurtPlayer() {
      return false;
   }

   public void initLevel(Level level) {
      super.initLevel(level);
   }

   public void tick() {
   }
}
