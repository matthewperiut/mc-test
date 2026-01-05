package net.minecraft.client.gamemode;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class SurvivalMode extends GameMode {
   private int xDestroyBlock = -1;
   private int yDestroyBlock = -1;
   private int zDestroyBlock = -1;
   private float destroyProgress = 0.0F;
   private float oDestroyProgress = 0.0F;
   private float destroyTicks = 0.0F;
   private int destroyDelay = 0;

   public SurvivalMode(Minecraft minecraft) {
      super(minecraft);
   }

   public void initPlayer(Player player) {
      player.yRot = -180.0F;
   }

   public void init() {
   }

   public boolean destroyBlock(int x, int y, int z, int face) {
      int t = this.minecraft.level.getTile(x, y, z);
      int data = this.minecraft.level.getData(x, y, z);
      boolean changed = super.destroyBlock(x, y, z, face);
      ItemInstance item = this.minecraft.player.getSelectedItem();
      boolean couldDestroy = this.minecraft.player.canDestroy(Tile.tiles[t]);
      if (item != null) {
         item.mineBlock(t, x, y, z);
         if (item.count == 0) {
            item.snap(this.minecraft.player);
            this.minecraft.player.removeSelectedItem();
         }
      }

      if (changed && couldDestroy) {
         Tile.tiles[t].playerDestroy(this.minecraft.level, x, y, z, data);
      }

      return changed;
   }

   public void startDestroyBlock(int x, int y, int z, int face) {
      int t = this.minecraft.level.getTile(x, y, z);
      if (t > 0 && this.destroyProgress == 0.0F) {
         Tile.tiles[t].attack(this.minecraft.level, x, y, z, this.minecraft.player);
      }

      if (t > 0 && Tile.tiles[t].getDestroyProgress(this.minecraft.player) >= 1.0F) {
         this.destroyBlock(x, y, z, face);
      }

   }

   public void stopDestroyBlock() {
      this.destroyProgress = 0.0F;
      this.destroyDelay = 0;
   }

   public void continueDestroyBlock(int x, int y, int z, int face) {
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
      this.oDestroyProgress = this.destroyProgress;
      this.minecraft.soundEngine.playMusicTick();
   }
}
