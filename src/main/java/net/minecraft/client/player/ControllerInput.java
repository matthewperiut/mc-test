package net.minecraft.client.player;

import net.minecraft.world.entity.player.Player;
import org.lwjgl.input.Controller;

public class ControllerInput extends Input {
   private Controller controller;
   private boolean lReset = false;
   private boolean rReset = false;

   public ControllerInput(Controller controller) {
      this.controller = controller;
      controller.setXAxisDeadZone(0.15F);
      controller.setYAxisDeadZone(0.15F);
      controller.setRXAxisDeadZone(0.2F);
      controller.setRYAxisDeadZone(0.2F);
   }

   public void releaseAllKeys() {
   }

   public void tick(Player player) {
      this.controller.poll();
      this.xa = this.controller.getXAxisValue();
      this.ya = this.controller.getYAxisValue();
      if (!this.lReset) {
         if (this.xa * this.xa + this.ya * this.ya == 0.0F) {
            this.lReset = true;
         }

         this.xa = this.ya = 0.0F;
      }

      float turnSpeed = 50.0F;
      float tx = this.controller.getRXAxisValue();
      float ty = this.controller.getRYAxisValue();
      if (!this.rReset) {
         if (tx * tx + ty * ty == 0.0F) {
            this.rReset = true;
         }

         ty = 0.0F;
         tx = 0.0F;
      }

      player.interpolateTurn(tx * Math.abs(tx) * turnSpeed, ty * Math.abs(ty) * turnSpeed);
      this.jumping = this.controller.isButtonPressed(0);
   }
}
