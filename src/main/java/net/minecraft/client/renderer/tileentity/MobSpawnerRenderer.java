package net.minecraft.client.renderer.tileentity;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityIO;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.entity.MobSpawnerTileEntity;
import org.lwjgl.opengl.GL11;

public class MobSpawnerRenderer extends TileEntityRenderer<MobSpawnerTileEntity> {
   private Map<String, Entity> models = new HashMap();

   public MobSpawnerRenderer() {
   }

   public void render(MobSpawnerTileEntity spawner, double x, double y, double z, float a) {
      GL11.glPushMatrix();
      GL11.glTranslatef((float)x + 0.5F, (float)y, (float)z + 0.5F);
      Entity e = (Entity)this.models.get(spawner.getEntityId());
      if (e == null) {
         e = EntityIO.newEntity(spawner.getEntityId(), (Level)null);
         this.models.put(spawner.getEntityId(), e);
      }

      if (e != null) {
         e.setLevel(spawner.level);
         float s = 0.4375F;
         GL11.glTranslatef(0.0F, 0.4F, 0.0F);
         GL11.glRotatef((float)(spawner.oSpin + (spawner.spin - spawner.oSpin) * (double)a) * 10.0F, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef(-30.0F, 1.0F, 0.0F, 0.0F);
         GL11.glTranslatef(0.0F, -0.4F, 0.0F);
         GL11.glScalef(s, s, s);
         e.moveTo(x, y, z, 0.0F, 0.0F);
         EntityRenderDispatcher.instance.render(e, (double)0.0F, (double)0.0F, (double)0.0F, 0.0F, a);
      }

      GL11.glPopMatrix();
   }
}
