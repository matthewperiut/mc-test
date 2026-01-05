package net.minecraft.client.renderer.ptexture;

import net.minecraft.client.renderer.Textures;
import org.lwjgl.opengl.GL11;

public class DynamicTexture {
   public static final int TEXTURE_TERRAIN = 0;
   public static final int TEXTURE_ITEMS = 1;
   public byte[] pixels = new byte[1024];
   public int tex;
   public boolean anaglyph3d = false;
   public int copyTo = 0;
   public int replicate = 1;
   public int textureId = 0;

   public DynamicTexture(int tex) {
      this.tex = tex;
   }

   public void tick() {
   }

   public void bindTexture(Textures textures) {
      if (this.textureId == 0) {
         GL11.glBindTexture(3553, textures.loadTexture("/terrain.png"));
      } else if (this.textureId == 1) {
         GL11.glBindTexture(3553, textures.loadTexture("/gui/items.png"));
      }

   }
}
