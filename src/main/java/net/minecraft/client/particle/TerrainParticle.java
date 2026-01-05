package net.minecraft.client.particle;

import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class TerrainParticle extends Particle {
   private Tile tile;

   public TerrainParticle(Level level, double x, double y, double z, double xa, double ya, double za, Tile tile) {
      super(level, x, y, z, xa, ya, za);
      this.tile = tile;
      this.tex = tile.tex;
      this.gravity = tile.gravity;
      this.rCol = this.gCol = this.bCol = 0.6F;
      this.size /= 2.0F;
   }

   public TerrainParticle init(int x, int y, int z) {
      if (this.tile == Tile.grass) {
         return this;
      } else {
         int col = this.tile.getColor(this.level, x, y, z);
         this.rCol *= (float)(col >> 16 & 255) / 255.0F;
         this.gCol *= (float)(col >> 8 & 255) / 255.0F;
         this.bCol *= (float)(col & 255) / 255.0F;
         return this;
      }
   }

   public int getParticleTexture() {
      return 1;
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
