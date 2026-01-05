package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.world.entity.item.FallingTile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.opengl.GL11;
import util.Mth;

public class FallingTileRenderer extends EntityRenderer<FallingTile> {
   private TileRenderer tileRenderer = new TileRenderer();

   public FallingTileRenderer() {
      this.shadowRadius = 0.5F;
   }

   public void render(FallingTile tile, double x, double y, double z, float rot, float a) {
      GL11.glPushMatrix();
      GL11.glTranslatef((float)x, (float)y, (float)z);
      this.bindTexture("/terrain.png");
      Tile tt = Tile.tiles[tile.tile];
      Level level = tile.getLevel();
      GL11.glDisable(2896);
      this.tileRenderer.renderBlock(tt, level, Mth.floor(tile.x), Mth.floor(tile.y), Mth.floor(tile.z));
      GL11.glEnable(2896);
      GL11.glPopMatrix();
   }
}
