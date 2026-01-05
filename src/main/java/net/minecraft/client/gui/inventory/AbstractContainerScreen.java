package net.minecraft.client.gui.inventory;

import net.minecraft.client.Lighting;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.locale.Language;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemInstance;
import org.lwjgl.opengl.GL11;

public abstract class AbstractContainerScreen extends Screen {
   private static ItemRenderer itemRenderer = new ItemRenderer();
   protected int imageWidth = 176;
   protected int imageHeight = 166;
   public AbstractContainerMenu menu;

   public AbstractContainerScreen(AbstractContainerMenu var1) {
      this.menu = var1;
   }

   public void init() {
      super.init();
      this.minecraft.player.containerMenu = this.menu;
   }

   public void render(int var1, int var2, float var3) {
      this.renderBackground();
      int var4 = (this.width - this.imageWidth) / 2;
      int var5 = (this.height - this.imageHeight) / 2;
      this.renderBg(var3);
      GL11.glPushMatrix();
      GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
      Lighting.turnOn();
      GL11.glPopMatrix();
      GL11.glPushMatrix();
      GL11.glTranslatef((float)var4, (float)var5, 0.0F);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glEnable(32826);
      Slot var6 = null;

      for(int var7 = 0; var7 < this.menu.slots.size(); ++var7) {
         Slot var8 = (Slot)this.menu.slots.get(var7);
         this.renderSlot(var8);
         if (this.isHovering(var8, var1, var2)) {
            var6 = var8;
            GL11.glDisable(2896);
            GL11.glDisable(2929);
            int var9 = var8.x;
            int var10 = var8.y;
            this.fillGradient(var9, var10, var9 + 16, var10 + 16, -2130706433, -2130706433);
            GL11.glEnable(2896);
            GL11.glEnable(2929);
         }
      }

      Inventory var12 = this.minecraft.player.inventory;
      if (var12.getCarried() != null) {
         GL11.glTranslatef(0.0F, 0.0F, 32.0F);
         itemRenderer.renderGuiItem(this.font, this.minecraft.textures, var12.getCarried(), var1 - var4 - 8, var2 - var5 - 8);
         itemRenderer.renderGuiItemDecorations(this.font, this.minecraft.textures, var12.getCarried(), var1 - var4 - 8, var2 - var5 - 8);
      }

      GL11.glDisable(32826);
      Lighting.turnOff();
      GL11.glDisable(2896);
      GL11.glDisable(2929);
      this.renderLabels();
      if (var12.getCarried() == null && var6 != null && var6.hasItem()) {
         String var13 = ("" + Language.getInstance().getElementName(var6.getItem().getDescriptionId())).trim();
         if (var13.length() > 0) {
            int var14 = var1 - var4 + 12;
            int var15 = var2 - var5 - 12;
            int var11 = this.font.width(var13);
            this.fillGradient(var14 - 3, var15 - 3, var14 + var11 + 3, var15 + 8 + 3, -1073741824, -1073741824);
            this.font.drawShadow(var13, var14, var15, -1);
         }
      }

      GL11.glEnable(2896);
      GL11.glEnable(2929);
      GL11.glPopMatrix();
   }

   protected void renderLabels() {
   }

   protected abstract void renderBg(float var1);

   private void renderSlot(Slot var1) {
      int var2 = var1.x;
      int var3 = var1.y;
      ItemInstance var4 = var1.getItem();
      if (var4 == null) {
         int var5 = var1.getNoItemIcon();
         if (var5 >= 0) {
            GL11.glDisable(2896);
            this.minecraft.textures.bind(this.minecraft.textures.loadTexture("/gui/items.png"));
            this.blit(var2, var3, var5 % 16 * 16, var5 / 16 * 16, 16, 16);
            GL11.glEnable(2896);
            return;
         }
      }

      itemRenderer.renderGuiItem(this.font, this.minecraft.textures, var4, var2, var3);
      itemRenderer.renderGuiItemDecorations(this.font, this.minecraft.textures, var4, var2, var3);
   }

   private Slot findSlot(int var1, int var2) {
      for(int var3 = 0; var3 < this.menu.slots.size(); ++var3) {
         Slot var4 = (Slot)this.menu.slots.get(var3);
         if (this.isHovering(var4, var1, var2)) {
            return var4;
         }
      }

      return null;
   }

   private boolean isHovering(Slot var1, int var2, int var3) {
      int var4 = (this.width - this.imageWidth) / 2;
      int var5 = (this.height - this.imageHeight) / 2;
      var2 -= var4;
      var3 -= var5;
      return var2 >= var1.x - 1 && var2 < var1.x + 16 + 1 && var3 >= var1.y - 1 && var3 < var1.y + 16 + 1;
   }

   protected void mouseClicked(int var1, int var2, int var3) {
      if (var3 == 0 || var3 == 1) {
         Slot var4 = this.findSlot(var1, var2);
         int var5 = (this.width - this.imageWidth) / 2;
         int var6 = (this.height - this.imageHeight) / 2;
         boolean var7 = var1 < var5 || var2 < var6 || var1 >= var5 + this.imageWidth || var2 >= var6 + this.imageHeight;
         int var8 = -1;
         if (var4 != null) {
            var8 = var4.index;
         }

         if (var7) {
            var8 = -999;
         }

         if (var8 != -1) {
            this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, var8, var3, this.minecraft.player);
         }
      }

   }

   protected void mouseReleased(int var1, int var2, int var3) {
      if (var3 == 0) {
      }

   }

   protected void keyPressed(char var1, int var2) {
   }

   public void removed() {
      if (this.minecraft.player != null) {
         this.minecraft.gameMode.handleCloseInventory(this.menu.containerId, this.minecraft.player);
      }
   }

   public void slotsChanged(Container var1) {
   }

   public boolean isPauseScreen() {
      return false;
   }
}
