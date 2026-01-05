package net.minecraft.client.gamemode.secret;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelListener;
import net.minecraft.world.level.tile.entity.TileEntity;

class SecretModeLevelListener implements LevelListener {
   private SecretMode bar;

   public SecretModeLevelListener(SecretMode var1) {
      this.bar = var1;
   }

   public void tileChanged(int var1, int var2, int var3) {
   }

   public void setTilesDirty(int var1, int var2, int var3, int var4, int var5, int var6) {
   }

   public void allChanged() {
   }

   public void playSound(String var1, double var2, double var4, double var6, float var8, float var9) {
   }

   public void addParticle(String var1, double var2, double var4, double var6, double var8, double var10, double var12) {
   }

   public void playMusic(String var1, double var2, double var4, double var6, float var8) {
   }

   public void entityAdded(Entity var1) {
      if (var1 instanceof Player) {
         Player var2 = (Player)var1;
         var2.moveTo((double)119.0F, (double)76.0F, (double)155.0F, 1263.59F, 4.0F);
         this.bar.foo();
      }

   }

   public void entityRemoved(Entity var1) {
   }

   public void skyColorChanged() {
   }

   public void playStreamingMusic(String var1, int var2, int var3, int var4) {
   }

   public void tileEntityChanged(int var1, int var2, int var3, TileEntity var4) {
   }
}
