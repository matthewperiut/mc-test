package net.minecraft.client.gui;

import net.minecraft.client.title.TitleScreen;

public class LevelConflictScreen extends Screen {
   private int frame = 0;

   public LevelConflictScreen() {
   }

   public void tick() {
      ++this.frame;
   }

   public void init() {
      this.buttons.clear();
      this.buttons.add(new Button(0, this.width / 2 - 100, this.height / 4 + 120 + 12, "Back to title screen"));
   }

   protected void buttonClicked(Button button) {
      if (button.active) {
         if (button.id == 0) {
            this.minecraft.setScreen(new TitleScreen());
         }

      }
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(this.font, "Level save conflict", this.width / 2, this.height / 4 - 60 + 20, 16777215);
      this.drawString(this.font, "Minecraft detected a conflict in the level save data.", this.width / 2 - 140, this.height / 4 - 60 + 60 + 0, 10526880);
      this.drawString(this.font, "This could be caused by two copies of the game", this.width / 2 - 140, this.height / 4 - 60 + 60 + 18, 10526880);
      this.drawString(this.font, "accessing the same level.", this.width / 2 - 140, this.height / 4 - 60 + 60 + 27, 10526880);
      this.drawString(this.font, "To prevent level corruption, the current game has quit.", this.width / 2 - 140, this.height / 4 - 60 + 60 + 45, 10526880);
      super.render(xm, ym, a);
   }
}
