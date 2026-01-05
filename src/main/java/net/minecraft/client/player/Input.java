package net.minecraft.client.player;

import net.minecraft.world.entity.player.Player;

public class Input {
   public float xa = 0.0F;
   public float ya = 0.0F;
   public boolean wasJumping = false;
   public boolean jumping = false;
   public boolean sneaking = false;

   public Input() {
   }

   public void tick(Player player) {
   }

   public void releaseAllKeys() {
   }

   public void setKey(int eventKey, boolean eventKeyState) {
   }
}
