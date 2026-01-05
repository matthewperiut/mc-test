package net.minecraft.client.gui;

import net.minecraft.client.title.TitleScreen;
import net.minecraft.world.level.Level;
import org.lwjgl.opengl.GL11;

public class DeathScreen extends Screen {
   public DeathScreen() {
   }

   public void init() {
      this.buttons.clear();
      this.buttons.add(new Button(1, this.width / 2 - 100, this.height / 4 + 72, "Respawn"));
      this.buttons.add(new Button(2, this.width / 2 - 100, this.height / 4 + 96, "Title menu"));
      if (this.minecraft.user == null) {
         ((Button)this.buttons.get(1)).active = false;
      }

   }

   protected void keyPressed(char eventCharacter, int eventKey) {
   }

   protected void buttonClicked(Button button) {
      int var10000 = button.id;
      if (button.id == 1) {
         this.minecraft.player.respawn();
         this.minecraft.setScreen((Screen)null);
      }

      if (button.id == 2) {
         this.minecraft.setLevel((Level)null);
         this.minecraft.setScreen(new TitleScreen());
      }

   }

   public void render(int xm, int ym, float a) {
      this.fillGradient(0, 0, this.width, this.height, 1615855616, -1602211792);
      GL11.glPushMatrix();
      GL11.glScalef(2.0F, 2.0F, 2.0F);
      this.drawCenteredString(this.font, "Game over!", this.width / 2 / 2, 30, 16777215);
      GL11.glPopMatrix();
      this.drawCenteredString(this.font, "Score: &e" + this.minecraft.player.getScore(), this.width / 2, 100, 16777215);
      super.render(xm, ym, a);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
