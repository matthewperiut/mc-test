package net.minecraft.client.skins;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class FileTexturePack extends TexturePack {
   private ZipFile zf;
   private int texture = -1;
   private BufferedImage icon;
   private File file;

   public FileTexturePack(File file) throws IOException {
      this.name = file.getName();
      this.file = file;
   }

   private String trim(String line) {
      if (line != null && line.length() > 34) {
         line = line.substring(0, 34);
      }

      return line;
   }

   public void load(Minecraft minecraft) {
      ZipFile zf = null;
      InputStream in = null;

      try {
         zf = new ZipFile(this.file);

         try {
            in = zf.getInputStream(zf.getEntry("pack.txt"));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            this.desc1 = this.trim(br.readLine());
            this.desc2 = this.trim(br.readLine());
            br.close();
            in.close();
         } catch (Exception var20) {
         }

         try {
            in = zf.getInputStream(zf.getEntry("pack.png"));
            this.icon = ImageIO.read(in);
            in.close();
         } catch (Exception var19) {
         }

         zf.close();
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         try {
            in.close();
         } catch (Exception var18) {
         }

         try {
            zf.close();
         } catch (Exception var17) {
         }

      }

   }

   public void unload(Minecraft minecraft) {
      if (this.icon != null) {
         minecraft.textures.releaseTexture(this.texture);
      }

      this.deselect();
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

   public void select() {
      try {
         this.zf = new ZipFile(this.file);
      } catch (Exception var2) {
      }

   }

   public void deselect() {
      try {
         this.zf.close();
      } catch (Exception var2) {
      }

      this.zf = null;
   }

   public InputStream getResource(String name) {
      try {
         ZipEntry entry = this.zf.getEntry(name.substring(1));
         if (entry != null) {
            return this.zf.getInputStream(entry);
         }
      } catch (Exception var3) {
      }

      return TexturePack.class.getResourceAsStream(name);
   }
}
