package net.minecraft.client.gui;

import net.minecraft.SharedConstants;
import net.minecraft.client.locale.Language;
import net.minecraft.client.multiplayer.ConnectScreen;
import org.lwjgl.input.Keyboard;

public class JoinMultiplayerScreen extends Screen {
   private Screen lastScreen;
   private int frame = 0;
   private String ip = "";

   public JoinMultiplayerScreen(Screen lastScreen) {
      this.lastScreen = lastScreen;
   }

   public void tick() {
      ++this.frame;
   }

   public void init() {
      Language language = Language.getInstance();
      Keyboard.enableRepeatEvents(true);
      this.buttons.clear();
      this.buttons.add(new Button(0, this.width / 2 - 100, this.height / 4 + 96 + 12, language.getElement("multiplayer.connect")));
      this.buttons.add(new Button(1, this.width / 2 - 100, this.height / 4 + 120 + 12, language.getElement("gui.cancel")));
      this.ip = this.minecraft.options.lastMpIp.replaceAll("_", ":");
      ((Button)this.buttons.get(0)).active = this.ip.length() > 0;
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   protected void buttonClicked(Button button) {
      if (button.active) {
         if (button.id == 1) {
            this.minecraft.setScreen(this.lastScreen);
         } else if (button.id == 0) {
            this.minecraft.options.lastMpIp = this.ip.replaceAll(":", "_");
            this.minecraft.options.save();
            String[] parts = this.ip.split(":");
            this.minecraft.setScreen(new ConnectScreen(this.minecraft, parts[0], parts.length > 1 ? this.parseInt(parts[1], 25565) : 25565));
         }

      }
   }

   private int parseInt(String str, int def) {
      try {
         return Integer.parseInt(str.trim());
      } catch (Exception var4) {
         return def;
      }
   }

   protected void keyPressed(char ch, int eventKey) {
      if (ch == 22) {
         String msg = Screen.getClipboard();
         if (msg == null) {
            msg = "";
         }

         int toAdd = 32 - this.ip.length();
         if (toAdd > msg.length()) {
            toAdd = msg.length();
         }

         if (toAdd > 0) {
            this.ip = this.ip + msg.substring(0, toAdd);
         }
      }

      if (ch == '\r') {
         this.buttonClicked((Button)this.buttons.get(0));
      }

      if (eventKey == 14 && this.ip.length() > 0) {
         this.ip = this.ip.substring(0, this.ip.length() - 1);
      }

      if (SharedConstants.acceptableLetters.indexOf(ch) >= 0 && this.ip.length() < 32) {
         this.ip = this.ip + ch;
      }

      ((Button)this.buttons.get(0)).active = this.ip.length() > 0;
   }

   public void render(int xm, int ym, float a) {
      Language language = Language.getInstance();
      this.renderBackground();
      this.drawCenteredString(this.font, language.getElement("multiplayer.title"), this.width / 2, this.height / 4 - 60 + 20, 16777215);
      this.drawString(this.font, language.getElement("multiplayer.info1"), this.width / 2 - 140, this.height / 4 - 60 + 60 + 0, 10526880);
      this.drawString(this.font, language.getElement("multiplayer.info2"), this.width / 2 - 140, this.height / 4 - 60 + 60 + 9, 10526880);
      this.drawString(this.font, language.getElement("multiplayer.ipinfo"), this.width / 2 - 140, this.height / 4 - 60 + 60 + 36, 10526880);
      int bx = this.width / 2 - 100;
      int by = this.height / 4 - 10 + 50 + 18;
      int bw = 200;
      int bh = 20;
      this.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, -6250336);
      this.fill(bx, by, bx + bw, by + bh, -16777216);
      this.drawString(this.font, this.ip + (this.frame / 6 % 2 == 0 ? "_" : ""), bx + 4, by + (bh - 8) / 2, 14737632);
      super.render(xm, ym, a);
   }
}
