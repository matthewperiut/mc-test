package net.minecraft.isom;

import java.awt.image.BufferedImage;
import net.minecraft.world.level.Level;

public class Zone {
   public static final int CHUNK_SIZE = 16;
   public BufferedImage image;
   public Level level;
   public int x;
   public int y;
   public boolean rendered = false;
   public boolean noContent = false;
   public int lastVisible = 0;
   public boolean addedToRenderQueue = false;

   public Zone(Level level, int x, int y) {
      this.level = level;
      this.init(x, y);
   }

   public void init(int x, int y) {
      this.rendered = false;
      this.x = x;
      this.y = y;
      this.lastVisible = 0;
      this.addedToRenderQueue = false;
   }

   public void init(Level level, int x, int y) {
      this.level = level;
      this.init(x, y);
   }
}
