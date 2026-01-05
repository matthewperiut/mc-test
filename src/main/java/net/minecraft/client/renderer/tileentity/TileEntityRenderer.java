package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Textures;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.entity.TileEntity;

public abstract class TileEntityRenderer<T extends TileEntity> {
   protected TileEntityRenderDispatcher tileEntityRenderDispatcher;

   public TileEntityRenderer() {
   }

   public abstract void render(T var1, double var2, double var4, double var6, float var8);

   protected void bindTexture(String resourceName) {
      Textures t = this.tileEntityRenderDispatcher.textures;
      t.bind(t.loadTexture(resourceName));
   }

   protected void bindTexture(String urlTexture, String backupTexture) {
      Textures t = this.tileEntityRenderDispatcher.textures;
      t.bind(t.loadHttpTexture(urlTexture, backupTexture));
   }

   private Level getLevel() {
      return this.tileEntityRenderDispatcher.level;
   }

   public void init(TileEntityRenderDispatcher tileEntityRenderDispatcher) {
      this.tileEntityRenderDispatcher = tileEntityRenderDispatcher;
   }

   public Font getFont() {
      return this.tileEntityRenderDispatcher.getFont();
   }
}
