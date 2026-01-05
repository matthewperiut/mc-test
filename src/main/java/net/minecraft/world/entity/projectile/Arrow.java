package net.minecraft.world.entity.projectile;

import com.mojang.nbt.CompoundTag;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import util.Mth;

public class Arrow extends Entity {
   private int xTile = -1;
   private int yTile = -1;
   private int zTile = -1;
   private int lastTile = 0;
   private boolean inGround = false;
   public int shakeTime = 0;
   public Mob owner;
   private int life;
   private int flightTime = 0;

   public Arrow(Level var1) {
      super(var1);
      this.setSize(0.5F, 0.5F);
   }

   public Arrow(Level var1, double var2, double var4, double var6) {
      super(var1);
      this.setSize(0.5F, 0.5F);
      this.setPos(var2, var4, var6);
      this.heightOffset = 0.0F;
   }

   public Arrow(Level var1, Mob var2) {
      super(var1);
      this.owner = var2;
      this.setSize(0.5F, 0.5F);
      this.moveTo(var2.x, var2.y + (double)var2.getHeadHeight(), var2.z, var2.yRot, var2.xRot);
      this.x -= (double)(Mth.cos(this.yRot / 180.0F * (float)Math.PI) * 0.16F);
      this.y -= (double)0.1F;
      this.z -= (double)(Mth.sin(this.yRot / 180.0F * (float)Math.PI) * 0.16F);
      this.setPos(this.x, this.y, this.z);
      this.heightOffset = 0.0F;
      this.xd = (double)(-Mth.sin(this.yRot / 180.0F * (float)Math.PI) * Mth.cos(this.xRot / 180.0F * (float)Math.PI));
      this.zd = (double)(Mth.cos(this.yRot / 180.0F * (float)Math.PI) * Mth.cos(this.xRot / 180.0F * (float)Math.PI));
      this.yd = (double)(-Mth.sin(this.xRot / 180.0F * (float)Math.PI));
      this.shoot(this.xd, this.yd, this.zd, 1.5F, 1.0F);
   }

   protected void defineSynchedData() {
   }

   public void shoot(double var1, double var3, double var5, float var7, float var8) {
      float var9 = Mth.sqrt(var1 * var1 + var3 * var3 + var5 * var5);
      var1 /= (double)var9;
      var3 /= (double)var9;
      var5 /= (double)var9;
      var1 += this.random.nextGaussian() * (double)0.0075F * (double)var8;
      var3 += this.random.nextGaussian() * (double)0.0075F * (double)var8;
      var5 += this.random.nextGaussian() * (double)0.0075F * (double)var8;
      var1 *= (double)var7;
      var3 *= (double)var7;
      var5 *= (double)var7;
      this.xd = var1;
      this.yd = var3;
      this.zd = var5;
      float var10 = Mth.sqrt(var1 * var1 + var5 * var5);
      this.yRotO = this.yRot = (float)(Math.atan2(var1, var5) * (double)180.0F / (double)(float)Math.PI);
      this.xRotO = this.xRot = (float)(Math.atan2(var3, (double)var10) * (double)180.0F / (double)(float)Math.PI);
      this.life = 0;
   }

   public void lerpMotion(double var1, double var3, double var5) {
      this.xd = var1;
      this.yd = var3;
      this.zd = var5;
      if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
         float var7 = Mth.sqrt(var1 * var1 + var5 * var5);
         this.yRotO = this.yRot = (float)(Math.atan2(var1, var5) * (double)180.0F / (double)(float)Math.PI);
         this.xRotO = this.xRot = (float)(Math.atan2(var3, (double)var7) * (double)180.0F / (double)(float)Math.PI);
      }

   }

   public void tick() {
      super.tick();
      if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
         float var1 = Mth.sqrt(this.xd * this.xd + this.zd * this.zd);
         this.yRotO = this.yRot = (float)(Math.atan2(this.xd, this.zd) * (double)180.0F / (double)(float)Math.PI);
         this.xRotO = this.xRot = (float)(Math.atan2(this.yd, (double)var1) * (double)180.0F / (double)(float)Math.PI);
      }

      if (this.shakeTime > 0) {
         --this.shakeTime;
      }

      if (this.inGround) {
         int var15 = this.level.getTile(this.xTile, this.yTile, this.zTile);
         if (var15 == this.lastTile) {
            ++this.life;
            if (this.life == 1200) {
               this.remove();
            }

            return;
         }

         this.inGround = false;
         this.xd *= (double)(this.random.nextFloat() * 0.2F);
         this.yd *= (double)(this.random.nextFloat() * 0.2F);
         this.zd *= (double)(this.random.nextFloat() * 0.2F);
         this.life = 0;
         this.flightTime = 0;
      } else {
         ++this.flightTime;
      }

      Vec3 var16 = Vec3.newTemp(this.x, this.y, this.z);
      Vec3 var2 = Vec3.newTemp(this.x + this.xd, this.y + this.yd, this.z + this.zd);
      HitResult var3 = this.level.clip(var16, var2);
      var16 = Vec3.newTemp(this.x, this.y, this.z);
      var2 = Vec3.newTemp(this.x + this.xd, this.y + this.yd, this.z + this.zd);
      if (var3 != null) {
         var2 = Vec3.newTemp(var3.pos.x, var3.pos.y, var3.pos.z);
      }

      Entity var4 = null;
      List var5 = this.level.getEntities(this, this.bb.expand(this.xd, this.yd, this.zd).grow((double)1.0F, (double)1.0F, (double)1.0F));
      double var6 = (double)0.0F;

      for(int var8 = 0; var8 < var5.size(); ++var8) {
         Entity var9 = (Entity)var5.get(var8);
         if (var9.isPickable() && (var9 != this.owner || this.flightTime >= 5)) {
            float var10 = 0.3F;
            AABB var11 = var9.bb.grow((double)var10, (double)var10, (double)var10);
            HitResult var12 = var11.clip(var16, var2);
            if (var12 != null) {
               double var13 = var16.distanceTo(var12.pos);
               if (var13 < var6 || var6 == (double)0.0F) {
                  var4 = var9;
                  var6 = var13;
               }
            }
         }
      }

      if (var4 != null) {
         var3 = new HitResult(var4);
      }

      if (var3 != null) {
         if (var3.entity != null) {
            if (var3.entity.hurt(this.owner, 4)) {
               this.level.playSound(this, "random.drr", 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
               this.remove();
            } else {
               this.xd *= (double)-0.1F;
               this.yd *= (double)-0.1F;
               this.zd *= (double)-0.1F;
               this.yRot += 180.0F;
               this.yRotO += 180.0F;
               this.flightTime = 0;
            }
         } else {
            this.xTile = var3.x;
            this.yTile = var3.y;
            this.zTile = var3.z;
            this.lastTile = this.level.getTile(this.xTile, this.yTile, this.zTile);
            this.xd = (double)((float)(var3.pos.x - this.x));
            this.yd = (double)((float)(var3.pos.y - this.y));
            this.zd = (double)((float)(var3.pos.z - this.z));
            float var19 = Mth.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
            this.x -= this.xd / (double)var19 * (double)0.05F;
            this.y -= this.yd / (double)var19 * (double)0.05F;
            this.z -= this.zd / (double)var19 * (double)0.05F;
            this.level.playSound(this, "random.drr", 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            this.inGround = true;
            this.shakeTime = 7;
         }
      }

      this.x += this.xd;
      this.y += this.yd;
      this.z += this.zd;
      float var20 = Mth.sqrt(this.xd * this.xd + this.zd * this.zd);
      this.yRot = (float)(Math.atan2(this.xd, this.zd) * (double)180.0F / (double)(float)Math.PI);

      for(this.xRot = (float)(Math.atan2(this.yd, (double)var20) * (double)180.0F / (double)(float)Math.PI); this.xRot - this.xRotO < -180.0F; this.xRotO -= 360.0F) {
      }

      while(this.xRot - this.xRotO >= 180.0F) {
         this.xRotO += 360.0F;
      }

      while(this.yRot - this.yRotO < -180.0F) {
         this.yRotO -= 360.0F;
      }

      while(this.yRot - this.yRotO >= 180.0F) {
         this.yRotO += 360.0F;
      }

      this.xRot = this.xRotO + (this.xRot - this.xRotO) * 0.2F;
      this.yRot = this.yRotO + (this.yRot - this.yRotO) * 0.2F;
      float var21 = 0.99F;
      float var22 = 0.03F;
      if (this.isInWater()) {
         for(int var23 = 0; var23 < 4; ++var23) {
            float var24 = 0.25F;
            this.level.addParticle("bubble", this.x - this.xd * (double)var24, this.y - this.yd * (double)var24, this.z - this.zd * (double)var24, this.xd, this.yd, this.zd);
         }

         var21 = 0.8F;
      }

      this.xd *= (double)var21;
      this.yd *= (double)var21;
      this.zd *= (double)var21;
      this.yd -= (double)var22;
      this.setPos(this.x, this.y, this.z);
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      var1.putShort("xTile", (short)this.xTile);
      var1.putShort("yTile", (short)this.yTile);
      var1.putShort("zTile", (short)this.zTile);
      var1.putByte("inTile", (byte)this.lastTile);
      var1.putByte("shake", (byte)this.shakeTime);
      var1.putByte("inGround", (byte)(this.inGround ? 1 : 0));
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      this.xTile = var1.getShort("xTile");
      this.yTile = var1.getShort("yTile");
      this.zTile = var1.getShort("zTile");
      this.lastTile = var1.getByte("inTile") & 255;
      this.shakeTime = var1.getByte("shake") & 255;
      this.inGround = var1.getByte("inGround") == 1;
   }

   public void playerTouch(Player var1) {
      if (!this.level.isOnline) {
         if (this.inGround && this.owner == var1 && this.shakeTime <= 0 && var1.inventory.add(new ItemInstance(Item.arrow, 1))) {
            this.level.playSound(this, "random.pop", 0.2F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            var1.take(this, 1);
            this.remove();
         }

      }
   }

   public float getShadowHeightOffs() {
      return 0.0F;
   }
}
