package net.minecraft.client.gui;

import com.mojang.nbt.CompoundTag;
import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gamemode.SurvivalMode;
import net.minecraft.client.locale.Language;
import net.minecraft.world.level.Level;

public class SelectWorldScreen extends Screen {
   protected Screen lastScreen;
   protected String title = "Select world";
   private boolean done = false;

   public SelectWorldScreen(Screen var1) {
      this.lastScreen = var1;
   }

   public void init() {
      Language var1 = Language.getInstance();
      this.title = var1.getElement("selectWorld.title");
      String var2 = var1.getElement("selectWorld.empty");
      String var3 = var1.getElement("selectWorld.world");
      File var4 = Minecraft.getWorkingDirectory();

      for(int var5 = 0; var5 < 5; ++var5) {
         CompoundTag var6 = Level.getDataTagFor(var4, "World" + (var5 + 1));
         if (var6 == null) {
            this.buttons.add(new Button(var5, this.width / 2 - 100, this.height / 6 + 24 * var5, "- " + var2 + " -"));
         } else {
            String var7 = var3 + " " + (var5 + 1);
            long var8 = var6.getLong("SizeOnDisk");
            var7 = var7 + " (" + (float)(var8 / 1024L * 100L / 1024L) / 100.0F + " MB)";
            this.buttons.add(new Button(var5, this.width / 2 - 100, this.height / 6 + 24 * var5, var7));
         }
      }

      this.postInit();
   }

   protected String getWorldName(int var1) {
      File var2 = Minecraft.getWorkingDirectory();
      return Level.getDataTagFor(var2, "World" + var1) != null ? "World" + var1 : null;
   }

   public void postInit() {
      Language var1 = Language.getInstance();
      this.buttons.add(new Button(5, this.width / 2 - 100, this.height / 6 + 120 + 12, var1.getElement("selectWorld.delete")));
      this.buttons.add(new Button(6, this.width / 2 - 100, this.height / 6 + 168, var1.getElement("gui.cancel")));
   }

   protected void buttonClicked(Button var1) {
      if (var1.active) {
         if (var1.id < 5) {
            this.worldSelected(var1.id + 1);
         } else if (var1.id == 5) {
            this.minecraft.setScreen(new DeleteWorldScreen(this));
         } else if (var1.id == 6) {
            this.minecraft.setScreen(this.lastScreen);
         }

      }
   }

   public void worldSelected(int var1) {
      this.minecraft.setScreen((Screen)null);
      if (!this.done) {
         this.done = true;
         this.minecraft.gameMode = new SurvivalMode(this.minecraft);
         this.minecraft.selectLevel("World" + var1);
         this.minecraft.setScreen((Screen)null);
      }
   }

   public void render(int var1, int var2, float var3) {
      this.renderBackground();
      this.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
      super.render(var1, var2, var3);
   }
}
