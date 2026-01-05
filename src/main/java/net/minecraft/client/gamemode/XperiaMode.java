package net.minecraft.client.gamemode;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class XperiaMode extends SurvivalMode {
   public XperiaMode(Minecraft var1) {
      super(var1);
   }

   public void initPlayer(Player var1) {
      var1.yRot = -180.0F;
      var1.x = (double)-30.75F;
      var1.y = 75.62;
      var1.z = 0.9;
   }
}
