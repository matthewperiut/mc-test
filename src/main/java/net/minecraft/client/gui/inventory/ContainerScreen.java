package net.minecraft.client.gui.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerMenu;
import org.lwjgl.opengl.GL11;

public class ContainerScreen extends AbstractContainerScreen {
   private Container inventory;
   private Container container;
   private int containerRows = 0;

   public ContainerScreen(Container inventory, Container container) {
      super(new ContainerMenu(inventory, container));
      this.inventory = inventory;
      this.container = container;
      this.passEvents = false;
      int defaultHeight = 222;
      int noRowHeight = defaultHeight - 108;
      this.containerRows = container.getContainerSize() / 9;
      this.imageHeight = noRowHeight + this.containerRows * 18;
   }

   protected void renderLabels() {
      this.font.draw(this.container.getName(), 8, 6, 4210752);
      this.font.draw(this.inventory.getName(), 8, this.imageHeight - 96 + 2, 4210752);
   }

   protected void renderBg(float a) {
      int tex = this.minecraft.textures.loadTexture("/gui/container.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.textures.bind(tex);
      int xo = (this.width - this.imageWidth) / 2;
      int yo = (this.height - this.imageHeight) / 2;
      this.blit(xo, yo, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
      this.blit(xo, yo + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
   }
}
