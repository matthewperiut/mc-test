package net.minecraft.client.gui.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.Level;
import org.lwjgl.opengl.GL11;

public class CraftingScreen extends AbstractContainerScreen {
   public CraftingScreen(Inventory inventory, Level level, int x, int y, int z) {
      super(new CraftingMenu(inventory, level, x, y, z));
   }

   public void removed() {
      super.removed();
      this.menu.removed(this.minecraft.player);
   }

   protected void renderLabels() {
      this.font.draw("Crafting", 28, 6, 4210752);
      this.font.draw("Inventory", 8, this.imageHeight - 96 + 2, 4210752);
   }

   protected void renderBg(float a) {
      int tex = this.minecraft.textures.loadTexture("/gui/crafting.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.textures.bind(tex);
      int xo = (this.width - this.imageWidth) / 2;
      int yo = (this.height - this.imageHeight) / 2;
      this.blit(xo, yo, 0, 0, this.imageWidth, this.imageHeight);
   }
}
