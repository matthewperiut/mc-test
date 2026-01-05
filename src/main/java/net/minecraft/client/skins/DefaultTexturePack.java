package net.minecraft.client.skins;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class DefaultTexturePack extends TexturePack {
   private int texture = -1;
   private BufferedImage icon;

   public DefaultTexturePack() {
      this.name = "Default";
      this.desc1 = "The default look of Minecraft";

      try {
         this.icon = ImageIO.read(DefaultTexturePack.class.getResource("/pack.png"));
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   public void unload(Minecraft minecraft) {
      if (this.icon != null) {
         minecraft.textures.releaseTexture(this.texture);
      }

   }

   public void bindTexture(Minecraft minecraft) {
      if (this.icon != null && this.texture < 0) {
         this.texture = minecraft.textures.getTexture(this.icon);
      }

      if (this.icon != null) {
         minecraft.textures.bind(this.texture);
      } else {
         GL11.glBindTexture(3553, minecraft.textures.loadTexture("/gui/unknown_pack.png"));
      }

   }
}
