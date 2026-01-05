package net.minecraft.client.particle;

import com.mojang.nbt.CompoundTag;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import util.Mth;

public class Particle extends Entity {
   protected int tex;
   protected float uo;
   protected float vo;
   protected int age = 0;
   protected int lifetime = 0;
   protected float size;
   protected float gravity;
   protected float rCol;
   protected float gCol;
   protected float bCol;
   public static double xOff;
   public static double yOff;
   public static double zOff;

   public Particle(Level level, double x, double y, double z, double xa, double ya, double za) {
      super(level);
      this.setSize(0.2F, 0.2F);
      this.heightOffset = this.bbHeight / 2.0F;
      this.setPos(x, y, z);
      this.rCol = this.gCol = this.bCol = 1.0F;
      this.xd = xa + (double)((float)(Math.random() * (double)2.0F - (double)1.0F) * 0.4F);
      this.yd = ya + (double)((float)(Math.random() * (double)2.0F - (double)1.0F) * 0.4F);
      this.zd = za + (double)((float)(Math.random() * (double)2.0F - (double)1.0F) * 0.4F);
      float speed = (float)(Math.random() + Math.random() + (double)1.0F) * 0.15F;
      float dd = Mth.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
      this.xd = this.xd / (double)dd * (double)speed * (double)0.4F;
      this.yd = this.yd / (double)dd * (double)speed * (double)0.4F + (double)0.1F;
      this.zd = this.zd / (double)dd * (double)speed * (double)0.4F;
      this.uo = this.random.nextFloat() * 3.0F;
      this.vo = this.random.nextFloat() * 3.0F;
      this.size = (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
      this.lifetime = (int)(4.0F / (this.random.nextFloat() * 0.9F + 0.1F));
      this.age = 0;
      this.makeStepSound = false;
   }

   public Particle setPower(float power) {
      this.xd *= (double)power;
      this.yd = (this.yd - (double)0.1F) * (double)power + (double)0.1F;
      this.zd *= (double)power;
      return this;
   }

   public Particle scale(float scale) {
      this.setSize(0.2F * scale, 0.2F * scale);
      this.size *= scale;
      return this;
   }

   protected void defineSynchedData() {
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      }

      this.yd -= 0.04 * (double)this.gravity;
      this.move(this.xd, this.yd, this.zd);
      this.xd *= (double)0.98F;
      this.yd *= (double)0.98F;
      this.zd *= (double)0.98F;
      if (this.onGround) {
         this.xd *= (double)0.7F;
         this.zd *= (double)0.7F;
      }

   }

   public void render(Tesselator t, float a, float xa, float ya, float za, float xa2, float za2) {
      float u0 = (float)(this.tex % 16) / 16.0F;
      float u1 = u0 + 0.0624375F;
      float v0 = (float)(this.tex / 16) / 16.0F;
      float v1 = v0 + 0.0624375F;
      float r = 0.1F * this.size;
      float x = (float)(this.xo + (this.x - this.xo) * (double)a - xOff);
      float y = (float)(this.yo + (this.y - this.yo) * (double)a - yOff);
      float z = (float)(this.zo + (this.z - this.zo) * (double)a - zOff);
      float br = this.getBrightness(a);
      t.color(this.rCol * br, this.gCol * br, this.bCol * br);
      t.vertexUV((double)(x - xa * r - xa2 * r), (double)(y - ya * r), (double)(z - za * r - za2 * r), (double)u1, (double)v1);
      t.vertexUV((double)(x - xa * r + xa2 * r), (double)(y + ya * r), (double)(z - za * r + za2 * r), (double)u1, (double)v0);
      t.vertexUV((double)(x + xa * r + xa2 * r), (double)(y + ya * r), (double)(z + za * r + za2 * r), (double)u0, (double)v0);
      t.vertexUV((double)(x + xa * r - xa2 * r), (double)(y - ya * r), (double)(z + za * r - za2 * r), (double)u0, (double)v1);
   }

   public int getParticleTexture() {
      return 0;
   }

   public void addAdditonalSaveData(CompoundTag entityTag) {
   }

   public void readAdditionalSaveData(CompoundTag tag) {
   }
}
