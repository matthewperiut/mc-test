package net.minecraft.client.gui;

import net.minecraft.client.title.TitleScreen;
import net.minecraft.world.level.Level;
import util.Mth;

public class PauseScreen extends Screen {
   private int saveStep = 0;
   private int visibleTime = 0;

   public PauseScreen() {
   }

   public void init() {
      this.saveStep = 0;
      this.buttons.clear();
      this.buttons.add(new Button(1, this.width / 2 - 100, this.height / 4 + 48, "Save and quit to title"));
      if (this.minecraft.isOnline()) {
         ((Button)this.buttons.get(0)).msg = "Disconnect";
      }

      this.buttons.add(new Button(4, this.width / 2 - 100, this.height / 4 + 24, "Back to game"));
      this.buttons.add(new Button(0, this.width / 2 - 100, this.height / 4 + 96, "Options..."));
   }

   protected void buttonClicked(Button button) {
      if (button.id == 0) {
         this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
      }

      if (button.id == 1) {
         if (this.minecraft.isOnline()) {
            this.minecraft.level.disconnect();
         }

         this.minecraft.setLevel((Level)null);
         this.minecraft.setScreen(new TitleScreen());
      }

      if (button.id == 4) {
         this.minecraft.setScreen((Screen)null);
         this.minecraft.grabMouse();
      }

   }

   public void tick() {
      super.tick();
      ++this.visibleTime;
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      boolean isSaving = !this.minecraft.level.pauseSave(this.saveStep++);
      if (isSaving || this.visibleTime < 20) {
         float col = ((float)(this.visibleTime % 10) + a) / 10.0F;
         col = Mth.sin(col * (float)Math.PI * 2.0F) * 0.2F + 0.8F;
         int br = (int)(255.0F * col);
         this.drawString(this.font, "Saving level..", 8, this.height - 16, br << 16 | br << 8 | br);
      }

      this.drawCenteredString(this.font, "Game menu", this.width / 2, 40, 16777215);
      super.render(xm, ym, a);
   }
}
