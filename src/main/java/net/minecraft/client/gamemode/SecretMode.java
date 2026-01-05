package net.minecraft.client.gamemode;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SecretMode extends SurvivalMode {
   public boolean initialized = false;

   public SecretMode(Minecraft var1) {
      super(var1);
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
         SecretBuilder var1 = new SecretBuilder(this.minecraft.level);
         var1.minecraft = this.minecraft;
         double var2 = (double)-35.75F;
         double var4 = 74.62;
         double var6 = (double)0.0F;
         var1.moveTo(var2, var4, var6, 1163.59F, 5.25F);
         if (var1.canSpawn()) {
            this.minecraft.level.addEntity(var1);
         }

         this.minecraft.level.time = 1L;
      }

   }
}
