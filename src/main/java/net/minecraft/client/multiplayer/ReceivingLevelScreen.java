package net.minecraft.client.multiplayer;

import net.minecraft.client.gui.Button;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.locale.Language;
import net.minecraft.network.packet.KeepAlivePacket;

public class ReceivingLevelScreen extends Screen {
   private ClientConnection connection;
   private int tickCount = 0;

   public ReceivingLevelScreen(ClientConnection connection) {
      this.connection = connection;
   }

   protected void keyPressed(char eventCharacter, int eventKey) {
   }

   public void init() {
      this.buttons.clear();
   }

   public void tick() {
      ++this.tickCount;
      if (this.tickCount % 20 == 0) {
         this.connection.send(new KeepAlivePacket());
      }

      if (this.connection != null) {
         this.connection.tick();
      }

   }

   protected void buttonClicked(Button button) {
   }

   public void render(int xm, int ym, float a) {
      this.renderDirtBackground(0);
      Language language = Language.getInstance();
      this.drawCenteredString(this.font, language.getElement("multiplayer.downloadingTerrain"), this.width / 2, this.height / 2 - 50, 16777215);
      super.render(xm, ym, a);
   }
}
