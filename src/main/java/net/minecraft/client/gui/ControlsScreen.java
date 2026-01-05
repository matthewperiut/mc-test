package net.minecraft.client.gui;

import net.minecraft.client.Options;
import net.minecraft.client.locale.Language;

public class ControlsScreen extends Screen {
   private Screen lastScreen;
   protected String title = "Controls";
   private Options options;
   private int selectedKey = -1;
   private static final int BUTTON_WIDTH = 70;
   private static final int ROW_WIDTH = 160;

   public ControlsScreen(Screen lastScreen, Options options) {
      this.lastScreen = lastScreen;
      this.options = options;
   }

   private int getLeftScreenPosition() {
      return this.width / 2 - 155;
   }

   public void init() {
      Language language = Language.getInstance();
      int leftPos = this.getLeftScreenPosition();

      for(int i = 0; i < this.options.keyMappings.length; ++i) {
         this.buttons.add(new SmallButton(i, leftPos + i % 2 * 160, this.height / 6 + 24 * (i >> 1), 70, 20, this.options.getKeyMessage(i)));
      }

      this.buttons.add(new Button(200, this.width / 2 - 100, this.height / 6 + 168, language.getElement("gui.done")));
      this.title = language.getElement("controls.title");
   }

   protected void buttonClicked(Button button) {
      for(int i = 0; i < this.options.keyMappings.length; ++i) {
         ((Button)this.buttons.get(i)).msg = this.options.getKeyMessage(i);
      }

      if (button.id == 200) {
         this.minecraft.setScreen(this.lastScreen);
      } else {
         this.selectedKey = button.id;
         button.msg = "> " + this.options.getKeyMessage(button.id) + " <";
      }

   }

   protected void keyPressed(char eventCharacter, int eventKey) {
      if (this.selectedKey >= 0) {
         this.options.setKey(this.selectedKey, eventKey);
         ((Button)this.buttons.get(this.selectedKey)).msg = this.options.getKeyMessage(this.selectedKey);
         this.selectedKey = -1;
      } else {
         super.keyPressed(eventCharacter, eventKey);
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
      int leftPos = this.getLeftScreenPosition();

      for(int i = 0; i < this.options.keyMappings.length; ++i) {
         this.drawString(this.font, this.options.getKeyDescription(i), leftPos + i % 2 * 160 + 70 + 6, this.height / 6 + 24 * (i >> 1) + 7, -1);
      }

      super.render(xm, ym, a);
   }
}
