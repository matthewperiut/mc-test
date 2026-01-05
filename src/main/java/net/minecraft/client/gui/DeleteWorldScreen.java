package net.minecraft.client.gui;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

public class DeleteWorldScreen extends SelectWorldScreen {
   public DeleteWorldScreen(Screen lastScreen) {
      super(lastScreen);
      this.title = "Delete world";
   }

   public void postInit() {
      this.buttons.add(new Button(6, this.width / 2 - 100, this.height / 6 + 168, "Cancel"));
   }

   public void worldSelected(int id) {
      String worldName = this.getWorldName(id);
      if (worldName != null) {
         this.minecraft.setScreen(new ConfirmScreen(this, "Are you sure you want to delete this world?", "'" + worldName + "' will be lost forever!", id));
      }

   }

   public void confirmResult(boolean result, int id) {
      if (result) {
         File dir = Minecraft.getWorkingDirectory();
         Level.deleteLevel(dir, this.getWorldName(id));
      }

      this.minecraft.setScreen(this.lastScreen);
   }
}
