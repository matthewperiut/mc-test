package net.minecraft.client.gamemode.secret;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gamemode.SurvivalMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SecretMode extends SurvivalMode {
   public boolean initialized = false;

   public SecretMode(Minecraft var1) {
      super(var1);
   }

   public void startDestroyBlock(int var1, int var2, int var3, int var4) {
      super.startDestroyBlock(var1, var2, var3, var4);
   }

   public void initPlayer(Player var1) {
      System.out.println("SecretMode initPlayer");
      var1.yRot = -180.0F;
      this.minecraft.level.addListener(new SecretModeLevelListener(this));
   }

   public void initLevel(Level var1) {
      super.initLevel(var1);
   }

   public void foo() {
      if (!this.initialized) {
         this.initialized = true;
         this.spawnBuilder();
         this.spawnBuilder();
         this.spawnBuilder();
         this.spawnBuilder();
         this.minecraft.level.time = 1L;
      }

   }

   public void spawnBuilder() {
      Builder var1 = new Builder(this.minecraft.level);
      var1.minecraft = this.minecraft;
      double var2 = (double)119.0F + Math.random() * (double)10.0F;
      double var4 = (double)70.0F + Math.random() * (double)10.0F;
      double var6 = (double)170.0F + Math.random() * (double)10.0F;
      var1.moveTo(var2, var4, var6, 1263.59F, 4.0F);
      if (var1.canSpawn()) {
         this.minecraft.level.addEntity(var1);
      } else {
         this.spawnBuilder();
      }

   }

   public void attack(Player var1, Entity var2) {
   }
}
