package net.minecraft.client.player;

import net.minecraft.client.Options;
import net.minecraft.world.entity.player.Player;

public class KeyboardInput extends Input {
   public static final int KEY_UP = 0;
   public static final int KEY_DOWN = 1;
   public static final int KEY_LEFT = 2;
   public static final int KEY_RIGHT = 3;
   public static final int KEY_JUMP = 4;
   public static final int KEY_SNEAK = 5;
   private boolean[] keys = new boolean[10];
   private Options options;

   public KeyboardInput(Options options) {
      this.options = options;
   }

   public void setKey(int key, boolean state) {
      int id = -1;
      if (key == this.options.keyUp.key) {
         id = 0;
      }

      if (key == this.options.keyDown.key) {
         id = 1;
      }

      if (key == this.options.keyLeft.key) {
         id = 2;
      }

      if (key == this.options.keyRight.key) {
         id = 3;
      }

      if (key == this.options.keyJump.key) {
         id = 4;
      }

      if (key == this.options.keySneak.key) {
         id = 5;
      }

      if (id >= 0) {
         this.keys[id] = state;
      }

   }

   public void releaseAllKeys() {
      for(int i = 0; i < 10; ++i) {
         this.keys[i] = false;
      }

   }

   public void tick(Player player) {
      this.xa = 0.0F;
      this.ya = 0.0F;
      if (this.keys[0]) {
         ++this.ya;
      }

      if (this.keys[1]) {
         --this.ya;
      }

      if (this.keys[2]) {
         ++this.xa;
      }

      if (this.keys[3]) {
         --this.xa;
      }

      this.jumping = this.keys[4];
      this.sneaking = this.keys[5];
      if (this.sneaking) {
         this.xa = (float)((double)this.xa * 0.3);
         this.ya = (float)((double)this.ya * 0.3);
      }

   }
}
