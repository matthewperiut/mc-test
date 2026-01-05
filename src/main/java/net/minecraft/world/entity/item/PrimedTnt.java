package net.minecraft.world.entity.item;

import com.mojang.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import util.Mth;

public class PrimedTnt extends Entity {
   public static final long serialVersionUID = 0L;
   public int life;

   public PrimedTnt(Level var1) {
      super(var1);
      this.life = 0;
      this.blocksBuilding = true;
      this.setSize(0.98F, 0.98F);
      this.heightOffset = this.bbHeight / 2.0F;
   }

   public PrimedTnt(Level var1, double var2, double var4, double var6) {
      this(var1);
      this.setPos(var2, var4, var6);
      float var8 = (float)(Math.random() * (double)(float)Math.PI * (double)2.0F);
      this.xd = (double)(-Mth.sin(var8 * (float)Math.PI / 180.0F) * 0.02F);
      this.yd = (double)0.2F;
      this.zd = (double)(-Mth.cos(var8 * (float)Math.PI / 180.0F) * 0.02F);
      this.makeStepSound = false;
      this.life = 80;
      this.xo = var2;
      this.yo = var4;
      this.zo = var6;
   }

   protected void defineSynchedData() {
   }

   public boolean isPickable() {
      return !this.removed;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.yd -= (double)0.04F;
      this.move(this.xd, this.yd, this.zd);
      this.xd *= (double)0.98F;
      this.yd *= (double)0.98F;
      this.zd *= (double)0.98F;
      if (this.onGround) {
         this.xd *= (double)0.7F;
         this.zd *= (double)0.7F;
         this.yd *= (double)-0.5F;
      }

      if (this.life-- <= 0) {
         this.remove();
         this.explode();
      } else {
         this.level.addParticle("smoke", this.x, this.y + (double)0.5F, this.z, (double)0.0F, (double)0.0F, (double)0.0F);
      }

   }

   private void explode() {
      float var1 = 4.0F;
      this.level.explode((Entity)null, this.x, this.y, this.z, var1);
   }

   protected void addAdditonalSaveData(CompoundTag var1) {
      var1.putByte("Fuse", (byte)this.life);
   }

   protected void readAdditionalSaveData(CompoundTag var1) {
      this.life = var1.getByte("Fuse");
   }

   public float getShadowHeightOffs() {
      return 0.0F;
   }
}
