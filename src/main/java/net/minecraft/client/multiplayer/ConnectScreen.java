package net.minecraft.client.multiplayer;

import java.net.ConnectException;
import java.net.UnknownHostException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Button;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.locale.Language;
import net.minecraft.client.title.TitleScreen;
import net.minecraft.network.packet.PreLoginPacket;
import net.minecraft.world.level.Level;

public class ConnectScreen extends Screen {
   private ClientConnection connection;
   private boolean aborted = false;

   public ConnectScreen(final Minecraft minecraft, final String ip, final int port) {
      minecraft.setLevel((Level)null);
      (new Thread() {
         public void run() {
            try {
               ConnectScreen.this.connection = new ClientConnection(minecraft, ip, port);
               if (ConnectScreen.this.aborted) {
                  return;
               }

               ConnectScreen.this.connection.send(new PreLoginPacket(minecraft.user.name));
            } catch (UnknownHostException var2) {
               if (ConnectScreen.this.aborted) {
                  return;
               }

               minecraft.setScreen(new DisconnectedScreen("connect.failed", "disconnect.genericReason", new Object[]{"Unknown host '" + ip + "'"}));
            } catch (ConnectException e) {
               if (ConnectScreen.this.aborted) {
                  return;
               }

               minecraft.setScreen(new DisconnectedScreen("connect.failed", "disconnect.genericReason", new Object[]{e.getMessage()}));
            } catch (Exception e) {
               if (ConnectScreen.this.aborted) {
                  return;
               }

               e.printStackTrace();
               minecraft.setScreen(new DisconnectedScreen("connect.failed", "disconnect.genericReason", new Object[]{e.toString()}));
            }

         }
      }).start();
   }

   public void tick() {
      if (this.connection != null) {
         this.connection.tick();
      }

   }

   protected void keyPressed(char eventCharacter, int eventKey) {
   }

   public void init() {
      Language language = Language.getInstance();
      this.buttons.clear();
      this.buttons.add(new Button(0, this.width / 2 - 100, this.height / 4 + 120 + 12, language.getElement("gui.cancel")));
   }

   protected void buttonClicked(Button button) {
      if (button.id == 0) {
         this.aborted = true;
         if (this.connection != null) {
            this.connection.close();
         }

         this.minecraft.setScreen(new TitleScreen());
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      Language language = Language.getInstance();
      if (this.connection == null) {
         this.drawCenteredString(this.font, language.getElement("connect.connecting"), this.width / 2, this.height / 2 - 50, 16777215);
         this.drawCenteredString(this.font, "", this.width / 2, this.height / 2 - 10, 16777215);
      } else {
         this.drawCenteredString(this.font, language.getElement("connect.authorizing"), this.width / 2, this.height / 2 - 50, 16777215);
         this.drawCenteredString(this.font, this.connection.message, this.width / 2, this.height / 2 - 10, 16777215);
      }

      super.render(xm, ym, a);
   }
}
