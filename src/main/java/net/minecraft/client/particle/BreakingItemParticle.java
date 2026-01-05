package net.minecraft.client.particle;

import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class BreakingItemParticle extends Particle {
   public BreakingItemParticle(Level level, double x, double y, double z, Item item) {
      super(level, x, y, z, (double)0.0F, (double)0.0F, (double)0.0F);
      this.tex = item.getIcon((ItemInstance)null);
      this.rCol = this.gCol = this.bCol = 1.0F;
      this.gravity = Tile.snow.gravity;
      this.size /= 2.0F;
   }

   public int getParticleTexture() {
      return 2;
   }

   public void render(Tesselator t, float a, float xa, float ya, float za, float xa2, float za2) {
      float u0 = ((float)(this.tex % 16) + this.uo / 4.0F) / 16.0F;
      float u1 = u0 + 0.015609375F;
      float v0 = ((float)(this.tex / 16) + this.vo / 4.0F) / 16.0F;
      float v1 = v0 + 0.015609375F;
      float r = 0.1F * this.size;
      float x = (float)(this.xo + (this.x - this.xo) * (double)a - xOff);
      float y = (float)(this.yo + (this.y - this.yo) * (double)a - yOff);
      float z = (float)(this.zo + (this.z - this.zo) * (double)a - zOff);
      float br = this.getBrightness(a);
      t.color(br * this.rCol, br * this.gCol, br * this.bCol);
      t.vertexUV((double)(x - xa * r - xa2 * r), (double)(y - ya * r), (double)(z - za * r - za2 * r), (double)u0, (double)v1);
      t.vertexUV((double)(x - xa * r + xa2 * r), (double)(y + ya * r), (double)(z - za * r + za2 * r), (double)u0, (double)v0);
      t.vertexUV((double)(x + xa * r + xa2 * r), (double)(y + ya * r), (double)(z + za * r + za2 * r), (double)u1, (double)v0);
      t.vertexUV((double)(x + xa * r - xa2 * r), (double)(y - ya * r), (double)(z + za * r - za2 * r), (double)u1, (double)v1);
   }
}
