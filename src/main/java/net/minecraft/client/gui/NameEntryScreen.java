package net.minecraft.client.gui;

import org.lwjgl.input.Keyboard;

public class NameEntryScreen extends Screen {
   private Screen lastScreen;
   protected String title = "Enter level name:";
   private int slot;
   private String name;
   private int frame = 0;
   private static final String allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ,.:-_'*!\"#%/()=+?[]{}<>";

   public NameEntryScreen(Screen lastScreen, String oldName, int slot) {
      this.lastScreen = lastScreen;
      this.slot = slot;
      this.name = oldName;
      if (this.name.equals("-")) {
         this.name = "";
      }

   }

   public void init() {
      this.buttons.clear();
      Keyboard.enableRepeatEvents(true);
      this.buttons.add(new Button(0, this.width / 2 - 100, this.height / 4 + 120, "Save"));
      this.buttons.add(new Button(1, this.width / 2 - 100, this.height / 4 + 144, "Cancel"));
      ((Button)this.buttons.get(0)).active = this.name.trim().length() > 1;
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void tick() {
      ++this.frame;
   }

   protected void buttonClicked(Button button) {
      if (button.active) {
         if (button.id == 0 && this.name.trim().length() > 1) {
            this.minecraft.saveSlot(this.slot, this.name.trim());
            this.minecraft.setScreen((Screen)null);
            this.minecraft.grabMouse();
         }

         if (button.id == 1) {
            this.minecraft.setScreen(this.lastScreen);
         }

      }
   }

   protected void keyPressed(char ch, int eventKey) {
      if (eventKey == 14 && this.name.length() > 0) {
         this.name = this.name.substring(0, this.name.length() - 1);
      }

      if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ,.:-_'*!\"#%/()=+?[]{}<>".indexOf(ch) >= 0 && this.name.length() < 64) {
         this.name = this.name + ch;
      }

      ((Button)this.buttons.get(0)).active = this.name.trim().length() > 1;
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(this.font, this.title, this.width / 2, 40, 16777215);
      int bx = this.width / 2 - 100;
      int by = this.height / 2 - 10;
      int bw = 200;
      int bh = 20;
      this.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, -6250336);
      this.fill(bx, by, bx + bw, by + bh, -16777216);
      this.drawString(this.font, this.name + (this.frame / 6 % 2 == 0 ? "_" : ""), bx + 4, by + (bh - 8) / 2, 14737632);
      super.render(xm, ym, a);
   }
}
