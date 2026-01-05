package net.minecraft.client.gui.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.level.tile.entity.FurnaceTileEntity;
import org.lwjgl.opengl.GL11;

public class FurnaceScreen extends AbstractContainerScreen {
   private FurnaceTileEntity furnace;

   public FurnaceScreen(Inventory inventory, FurnaceTileEntity furnace) {
      super(new FurnaceMenu(inventory, furnace));
      this.furnace = furnace;
   }

   protected void renderLabels() {
      this.font.draw("Furnace", 60, 6, 4210752);
      this.font.draw("Inventory", 8, this.imageHeight - 96 + 2, 4210752);
   }

   protected void renderBg(float a) {
      int tex = this.minecraft.textures.loadTexture("/gui/furnace.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.textures.bind(tex);
      int xo = (this.width - this.imageWidth) / 2;
      int yo = (this.height - this.imageHeight) / 2;
      this.blit(xo, yo, 0, 0, this.imageWidth, this.imageHeight);
      if (this.furnace.isLit()) {
         int p = this.furnace.getLitProgress(12);
         this.blit(xo + 56, yo + 36 + 12 - p, 176, 12 - p, 14, p + 2);
      }

      int p = this.furnace.getBurnProgress(24);
      this.blit(xo + 79, yo + 34, 176, 14, p + 1, 16);
   }
}
