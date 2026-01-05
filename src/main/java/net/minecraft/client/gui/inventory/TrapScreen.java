package net.minecraft.client.gui.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.TrapMenu;
import net.minecraft.world.level.tile.entity.DispenserTileEntity;
import org.lwjgl.opengl.GL11;

public class TrapScreen extends AbstractContainerScreen {
   public TrapScreen(Inventory inventory, DispenserTileEntity trap) {
      super(new TrapMenu(inventory, trap));
   }

   protected void renderLabels() {
      this.font.draw("Dispenser", 60, 6, 4210752);
      this.font.draw("Inventory", 8, this.imageHeight - 96 + 2, 4210752);
   }

   protected void renderBg(float a) {
      int tex = this.minecraft.textures.loadTexture("/gui/trap.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.textures.bind(tex);
      int xo = (this.width - this.imageWidth) / 2;
      int yo = (this.height - this.imageHeight) / 2;
      this.blit(xo, yo, 0, 0, this.imageWidth, this.imageHeight);
   }
}
