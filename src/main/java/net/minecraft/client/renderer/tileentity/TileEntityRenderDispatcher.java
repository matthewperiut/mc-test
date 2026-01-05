package net.minecraft.client.renderer.tileentity;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Textures;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.entity.MobSpawnerTileEntity;
import net.minecraft.world.level.tile.entity.SignTileEntity;
import net.minecraft.world.level.tile.entity.TileEntity;
import org.lwjgl.opengl.GL11;

public class TileEntityRenderDispatcher {
   private Map<Class<? extends TileEntity>, TileEntityRenderer<? extends TileEntity>> renderers = new HashMap();
   public static TileEntityRenderDispatcher instance = new TileEntityRenderDispatcher();
   private Font font;
   public static double xOff;
   public static double yOff;
   public static double zOff;
   public Textures textures;
   public Level level;
   public Player player;
   public float playerRotY;
   public float playerRotX;
   public double xPlayer;
   public double yPlayer;
   public double zPlayer;

   private TileEntityRenderDispatcher() {
      this.renderers.put(SignTileEntity.class, new SignRenderer());
      this.renderers.put(MobSpawnerTileEntity.class, new MobSpawnerRenderer());

      for(TileEntityRenderer<?> renderer : this.renderers.values()) {
         renderer.init(this);
      }

   }

   @SuppressWarnings("unchecked")
   public <T extends TileEntity> TileEntityRenderer<T> getRenderer(Class<? extends TileEntity> e) {
      TileEntityRenderer<? extends TileEntity> r = (TileEntityRenderer)this.renderers.get(e);
      if (r == null && e != TileEntity.class) {
         r = this.getRenderer((Class<? extends TileEntity>)e.getSuperclass());
         this.renderers.put(e, r);
      }

      return (TileEntityRenderer<T>)r;
   }

   public boolean hasRenderer(TileEntity e) {
      return this.getRenderer(e) != null;
   }

   public <T extends TileEntity> TileEntityRenderer<T> getRenderer(TileEntity e) {
      return e == null ? null : this.getRenderer(e.getClass());
   }

   public void prepare(Level level, Textures textures, Font font, Player player, float a) {
      this.level = level;
      this.textures = textures;
      this.player = player;
      this.font = font;
      this.playerRotY = player.yRotO + (player.yRot - player.yRotO) * a;
      this.playerRotX = player.xRotO + (player.xRot - player.xRotO) * a;
      this.xPlayer = player.xOld + (player.x - player.xOld) * (double)a;
      this.yPlayer = player.yOld + (player.y - player.yOld) * (double)a;
      this.zPlayer = player.zOld + (player.z - player.zOld) * (double)a;
   }

   public void render(TileEntity e, float a) {
      if (e.distanceToSqr(this.xPlayer, this.yPlayer, this.zPlayer) < (double)4096.0F) {
         float br = this.level.getBrightness(e.x, e.y, e.z);
         GL11.glColor3f(br, br, br);
         this.render(e, (double)e.x - xOff, (double)e.y - yOff, (double)e.z - zOff, a);
      }

   }

   public void render(TileEntity entity, double x, double y, double z, float a) {
      TileEntityRenderer<TileEntity> renderer = this.getRenderer(entity);
      if (renderer != null) {
         renderer.render(entity, x, y, z, a);
      }

   }

   public void setLevel(Level level) {
      this.level = level;
   }

   public double distanceToSqr(double x, double y, double z) {
      double xd = x - this.xPlayer;
      double yd = y - this.yPlayer;
      double zd = z - this.zPlayer;
      return xd * xd + yd * yd + zd * zd;
   }

   public Font getFont() {
      return this.font;
   }
}
