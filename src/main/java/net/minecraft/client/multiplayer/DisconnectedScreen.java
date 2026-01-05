package net.minecraft.client.multiplayer;

import net.minecraft.client.gui.Button;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.locale.Language;
import net.minecraft.client.title.TitleScreen;

public class DisconnectedScreen extends Screen {
   private String title;
   private String reason;

   public DisconnectedScreen(String title, String reason, Object... reasonObjects) {
      Language language = Language.getInstance();
      this.title = language.getElement(title);
      if (reasonObjects != null) {
         this.reason = language.getElement(reason, reasonObjects);
      } else {
         this.reason = language.getElement(reason);
      }

   }

   public void tick() {
   }

   protected void keyPressed(char eventCharacter, int eventKey) {
   }

   public void init() {
      Language language = Language.getInstance();
      this.buttons.clear();
      this.buttons.add(new Button(0, this.width / 2 - 100, this.height / 4 + 120 + 12, language.getElement("gui.toMenu")));
   }

   protected void buttonClicked(Button button) {
      if (button.id == 0) {
         this.minecraft.setScreen(new TitleScreen());
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 50, 16777215);
      this.drawCenteredString(this.font, this.reason, this.width / 2, this.height / 2 - 10, 16777215);
      super.render(xm, ym, a);
   }
}
