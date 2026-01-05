package net.minecraft.isom;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.ImageObserver;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ReadOnlyChunkCache;
import net.minecraft.world.level.chunk.storage.OldChunkStorage;

public class IsomPreview extends Canvas implements Runnable, MouseListener, MouseMotionListener, KeyListener {
   private static final int CACHE_WIDTH = 64;
   private static final int CACHE_HEIGHT = 64;
   private static final long serialVersionUID = 1L;
   private int currentRender = 0;
   private int zoom = 2;
   private boolean showHelp = true;
   private Level level;
   private File workDir = this.getWorkingDirectory();
   private boolean running = true;
   private List<Zone> zonesToRender = Collections.synchronizedList(new LinkedList());
   private Zone[][] zoneMap = new Zone[64][64];
   private int xCam;
   private int yCam;
   private int xDrag;
   private int yDrag;

   public File getWorkingDirectory() {
      if (this.workDir == null) {
         this.workDir = this.getWorkingDirectory("minecraft");
      }

      return this.workDir;
   }

   public File getWorkingDirectory(String applicationName) {
      String userHome = System.getProperty("user.home", ".");
      File workingDirectory;
      switch (getPlatform()) {
         case linux:
         case solaris:
            workingDirectory = new File(userHome, '.' + applicationName + '/');
            break;
         case windows:
            String applicationData = System.getenv("APPDATA");
            if (applicationData != null) {
               workingDirectory = new File(applicationData, "." + applicationName + '/');
            } else {
               workingDirectory = new File(userHome, '.' + applicationName + '/');
            }
            break;
         case macos:
            workingDirectory = new File(userHome, "Library/Application Support/" + applicationName);
            break;
         default:
            workingDirectory = new File(userHome, applicationName + '/');
      }

      if (!workingDirectory.exists() && !workingDirectory.mkdirs()) {
         throw new RuntimeException("The working directory could not be created: " + workingDirectory);
      } else {
         return workingDirectory;
      }
   }

   private static OS getPlatform() {
      String osName = System.getProperty("os.name").toLowerCase();
      if (osName.contains("win")) {
         return IsomPreview.OS.windows;
      } else if (osName.contains("mac")) {
         return IsomPreview.OS.macos;
      } else if (osName.contains("solaris")) {
         return IsomPreview.OS.solaris;
      } else if (osName.contains("sunos")) {
         return IsomPreview.OS.solaris;
      } else if (osName.contains("linux")) {
         return IsomPreview.OS.linux;
      } else {
         return osName.contains("unix") ? IsomPreview.OS.linux : IsomPreview.OS.unknown;
      }
   }

   public IsomPreview() {
      for(int x = 0; x < 64; ++x) {
         for(int y = 0; y < 64; ++y) {
            this.zoneMap[x][y] = new Zone((Level)null, x, y);
         }
      }

      this.addMouseListener(this);
      this.addMouseMotionListener(this);
      this.addKeyListener(this);
      this.setFocusable(true);
      this.requestFocus();
      this.setBackground(Color.red);
   }

   public void loadLevel(String levelName) {
      this.xCam = this.yCam = 0;
      this.level = new Level(new File(this.workDir, "saves"), levelName) {
         protected ChunkSource createChunkSource(File dir) {
            return new ReadOnlyChunkCache(this, new OldChunkStorage(dir, false));
         }
      };
      this.level.skyDarken = 0;
      synchronized(this.zonesToRender) {
         this.zonesToRender.clear();

         for(int x = 0; x < 64; ++x) {
            for(int y = 0; y < 64; ++y) {
               this.zoneMap[x][y].init(this.level, x, y);
            }
         }

      }
   }

   private void setBrightness(int i) {
      synchronized(this.zonesToRender) {
         this.level.skyDarken = i;
         this.zonesToRender.clear();

         for(int x = 0; x < 64; ++x) {
            for(int y = 0; y < 64; ++y) {
               this.zoneMap[x][y].init(this.level, x, y);
            }
         }

      }
   }

   public void start() {
      (new Thread() {
         public void run() {
            while(IsomPreview.this.running) {
               IsomPreview.this.render();

               try {
                  Thread.sleep(1L);
               } catch (Exception var2) {
               }
            }

         }
      }).start();

      for(int i = 0; i < 8; ++i) {
         (new Thread(this)).start();
      }

   }

   public void stop() {
      this.running = false;
   }

   private Zone getZone(int x, int y) {
      int xSlot = x & 63;
      int ySlot = y & 63;
      Zone z = this.zoneMap[xSlot][ySlot];
      if (z.x == x && z.y == y) {
         return z;
      } else {
         synchronized(this.zonesToRender) {
            this.zonesToRender.remove(z);
         }

         z.init(x, y);
         return z;
      }
   }

   public void run() {
      ZoneRenderer zoneRenderer = new ZoneRenderer();

      while(this.running) {
         Zone zone = null;
         synchronized(this.zonesToRender) {
            if (this.zonesToRender.size() > 0) {
               zone = (Zone)this.zonesToRender.remove(0);
            }
         }

         if (zone != null) {
            if (this.currentRender - zone.lastVisible < 2) {
               zoneRenderer.render(zone);
               this.repaint();
            } else {
               zone.addedToRenderQueue = false;
            }
         }

         try {
            Thread.sleep(2L);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }

   }

   public void update(Graphics g) {
   }

   public void paint(Graphics _g) {
   }

   public void render() {
      BufferStrategy bs = this.getBufferStrategy();
      if (bs == null) {
         this.createBufferStrategy(2);
      } else {
         this.render((Graphics2D)bs.getDrawGraphics());
         bs.show();
      }
   }

   public void render(Graphics2D g) {
      ++this.currentRender;
      AffineTransform at = g.getTransform();
      g.setClip(0, 0, this.getWidth(), this.getHeight());
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      g.translate(this.getWidth() / 2, this.getHeight() / 2);
      g.scale((double)this.zoom, (double)this.zoom);
      g.translate(this.xCam, this.yCam);
      if (this.level != null) {
         g.translate(-(this.level.xSpawn + this.level.zSpawn), -(-this.level.xSpawn + this.level.zSpawn) + 64);
      }

      Rectangle r = g.getClipBounds();
      g.setColor(new Color(-15724512));
      g.fillRect(r.x, r.y, r.width, r.height);
      int w = 16;
      int rr = 3;
      int x0 = r.x / w / 2 - 2 - rr;
      int x1 = (r.x + r.width) / w / 2 + 1 + rr;
      int y0 = r.y / w - 1 - rr * 2;
      int y1 = (r.y + r.height + 16 + 128) / w + 1 + rr * 2;

      for(int y = y0; y <= y1; ++y) {
         for(int x = x0; x <= x1; ++x) {
            int xSlot = x - (y >> 1);
            int ySlot = x + (y + 1 >> 1);
            Zone zone = this.getZone(xSlot, ySlot);
            zone.lastVisible = this.currentRender;
            if (!zone.rendered) {
               if (!zone.addedToRenderQueue) {
                  zone.addedToRenderQueue = true;
                  this.zonesToRender.add(zone);
               }
            } else {
               zone.addedToRenderQueue = false;
               if (!zone.noContent) {
                  int xp = x * w * 2 + (y & 1) * w;
                  int yp = y * w - 128 - 16;
                  g.drawImage(zone.image, xp, yp, (ImageObserver)null);
               }
            }
         }
      }

      if (this.showHelp) {
         g.setTransform(at);
         int y = this.getHeight() - 32 - 4;
         g.setColor(new Color(Integer.MIN_VALUE, true));
         g.fillRect(4, this.getHeight() - 32 - 4, this.getWidth() - 8, 32);
         g.setColor(Color.WHITE);
         String msg = "F1 - F5: load levels   |   0-9: Set time of day   |   Space: return to spawn   |   Double click: zoom   |   Escape: hide this text";
         g.drawString(msg, this.getWidth() / 2 - g.getFontMetrics().stringWidth(msg) / 2, y + 20);
      }

      g.dispose();
   }

   public static void main(String[] args) {
      IsomPreview isomPreview = new IsomPreview();
      JFrame frame = new JFrame("IsomPreview");
      frame.setDefaultCloseOperation(3);
      frame.setLayout(new BorderLayout());
      frame.add(isomPreview, "Center");
      frame.setSize(854, 480);
      frame.setLocationRelativeTo((Component)null);
      frame.setVisible(true);
      isomPreview.start();
   }

   public void mouseDragged(MouseEvent m) {
      int x = m.getX() / this.zoom;
      int y = m.getY() / this.zoom;
      this.xCam += x - this.xDrag;
      this.yCam += y - this.yDrag;
      this.xDrag = x;
      this.yDrag = y;
      this.repaint();
   }

   public void mouseMoved(MouseEvent arg0) {
   }

   public void mouseClicked(MouseEvent m) {
      if (m.getClickCount() == 2) {
         this.zoom = 3 - this.zoom;
         this.repaint();
      }

   }

   public void mouseEntered(MouseEvent arg0) {
   }

   public void mouseExited(MouseEvent arg0) {
   }

   public void mousePressed(MouseEvent m) {
      int x = m.getX() / this.zoom;
      int y = m.getY() / this.zoom;
      this.xDrag = x;
      this.yDrag = y;
   }

   public void mouseReleased(MouseEvent arg0) {
   }

   public void keyPressed(KeyEvent ke) {
      if (ke.getKeyCode() == 48) {
         this.setBrightness(11);
      }

      if (ke.getKeyCode() == 49) {
         this.setBrightness(10);
      }

      if (ke.getKeyCode() == 50) {
         this.setBrightness(9);
      }

      if (ke.getKeyCode() == 51) {
         this.setBrightness(7);
      }

      if (ke.getKeyCode() == 52) {
         this.setBrightness(6);
      }

      if (ke.getKeyCode() == 53) {
         this.setBrightness(5);
      }

      if (ke.getKeyCode() == 54) {
         this.setBrightness(3);
      }

      if (ke.getKeyCode() == 55) {
         this.setBrightness(2);
      }

      if (ke.getKeyCode() == 56) {
         this.setBrightness(1);
      }

      if (ke.getKeyCode() == 57) {
         this.setBrightness(0);
      }

      if (ke.getKeyCode() == 112) {
         this.loadLevel("World1");
      }

      if (ke.getKeyCode() == 113) {
         this.loadLevel("World2");
      }

      if (ke.getKeyCode() == 114) {
         this.loadLevel("World3");
      }

      if (ke.getKeyCode() == 115) {
         this.loadLevel("World4");
      }

      if (ke.getKeyCode() == 116) {
         this.loadLevel("World5");
      }

      if (ke.getKeyCode() == 32) {
         this.xCam = this.yCam = 0;
      }

      if (ke.getKeyCode() == 27) {
         this.showHelp = !this.showHelp;
      }

      this.repaint();
   }

   public void keyReleased(KeyEvent arg0) {
   }

   public void keyTyped(KeyEvent arg0) {
   }

   private static enum OS {
      linux,
      solaris,
      windows,
      macos,
      unknown;

      private OS() {
      }
   }
}
