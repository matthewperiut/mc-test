package net.minecraft.client.gui;

import net.minecraft.client.Options;
import net.minecraft.client.locale.Language;

public class OptionsScreen extends Screen {
   private Screen lastScreen;
   protected String title = "Options";
   private Options options;

   public OptionsScreen(Screen lastScreen, Options options) {
      this.lastScreen = lastScreen;
      this.options = options;
   }

   public void init() {
      Language language = Language.getInstance();
      this.title = language.getElement("options.title");

      Options.Option[] var5;
      for(Options.Option item : var5 = Options.Option.values()) {
         int position = item.getId();
         if (!item.isProgress()) {
            this.buttons.add(new SmallButton(item.getId(), this.width / 2 - 155 + position % 2 * 160, this.height / 6 + 24 * (position >> 1), item, this.options.getMessage(item)));
         } else {
            this.buttons.add(new SlideButton(item.getId(), this.width / 2 - 155 + position % 2 * 160, this.height / 6 + 24 * (position >> 1), item, this.options.getMessage(item), this.options.getProgressValue(item)));
         }
      }

      this.buttons.add(new Button(100, this.width / 2 - 100, this.height / 6 + 120 + 12, language.getElement("options.controls")));
      this.buttons.add(new Button(200, this.width / 2 - 100, this.height / 6 + 168, language.getElement("gui.done")));
   }

   protected void buttonClicked(Button button) {
      if (button.active) {
         if (button.id < 100 && button instanceof SmallButton) {
            this.options.toggle(((SmallButton)button).getOption(), 1);
            button.msg = this.options.getMessage(Options.Option.getItem(button.id));
         }

         if (button.id == 100) {
            this.minecraft.options.save();
            this.minecraft.setScreen(new ControlsScreen(this, this.options));
         }

         if (button.id == 200) {
            this.minecraft.options.save();
            this.minecraft.setScreen(this.lastScreen);
         }

      }
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
      super.render(xm, ym, a);
   }
}
