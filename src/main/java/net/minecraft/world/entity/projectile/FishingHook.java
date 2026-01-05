package net.minecraft.world.entity.projectile;

import com.mojang.nbt.CompoundTag;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import util.Mth;

public class FishingHook extends Entity {
   private int xTile;
   private int yTile;
   private int zTile;
   private int lastTile;
   private boolean inGround;
   public int shakeTime;
   public Player owner;
   private int life;
   private int flightTime;
   private int nibble;
   public Entity hookedIn;
   private int lSteps;
   private double lx;
   private double ly;
   private double lz;
   private double lyr;
   private double lxr;
   private double lxd;
   private double lyd;
   private double lzd;

   public FishingHook(Level var1) {
      super(var1);
      this.xTile = -1;
      this.yTile = -1;
      this.zTile = -1;
      this.lastTile = 0;
      this.inGround = false;
      this.shakeTime = 0;
      this.flightTime = 0;
      this.nibble = 0;
      this.hookedIn = null;
      this.setSize(0.25F, 0.25F);
   }

   protected void defineSynchedData() {
   }

   public boolean shouldRenderAtSqrDistance(double var1) {
      double var3 = this.bb.getSize() * (double)4.0F;
      var3 *= (double)64.0F;
      return var1 < var3 * var3;
   }

   public FishingHook(Level var1, double var2, double var4, double var6) {
      this(var1);
      this.setPos(var2, var4, var6);
   }

   public FishingHook(Level var1, Player var2) {
      super(var1);
      this.xTile = -1;
      this.yTile = -1;
      this.zTile = -1;
      this.lastTile = 0;
      this.inGround = false;
      this.shakeTime = 0;
      this.flightTime = 0;
      this.nibble = 0;
      this.hookedIn = null;
      this.owner = var2;
      this.owner.fishing = this;
      this.setSize(0.25F, 0.25F);
      this.moveTo(var2.x, var2.y + 1.62 - (double)var2.heightOffset, var2.z, var2.yRot, var2.xRot);
      this.x -= (double)(Mth.cos(this.yRot / 180.0F * (float)Math.PI) * 0.16F);
      this.y -= (double)0.1F;
      this.z -= (double)(Mth.sin(this.yRot / 180.0F * (float)Math.PI) * 0.16F);
      this.setPos(this.x, this.y, this.z);
      this.heightOffset = 0.0F;
      float var3 = 0.4F;
      this.xd = (double)(-Mth.sin(this.yRot / 180.0F * (float)Math.PI) * Mth.cos(this.xRot / 180.0F * (float)Math.PI) * var3);
      this.zd = (double)(Mth.cos(this.yRot / 180.0F * (float)Math.PI) * Mth.cos(this.xRot / 180.0F * (float)Math.PI) * var3);
      this.yd = (double)(-Mth.sin(this.xRot / 180.0F * (float)Math.PI) * var3);
      this.shoot(this.xd, this.yd, this.zd, 1.5F, 1.0F);
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

   public void lerpTo(double var1, double var3, double var5, float var7, float var8, int var9) {
      this.lx = var1;
      this.ly = var3;
      this.lz = var5;
      this.lyr = (double)var7;
      this.lxr = (double)var8;
      this.lSteps = var9;
      this.xd = this.lxd;
      this.yd = this.lyd;
      this.zd = this.lzd;
   }

   public void lerpMotion(double var1, double var3, double var5) {
      this.lxd = this.xd = var1;
      this.lyd = this.yd = var3;
      this.lzd = this.zd = var5;
   }

   public void tick() {
      super.tick();
      if (this.lSteps > 0) {
         double var22 = this.x + (this.lx - this.x) / (double)this.lSteps;
         double var24 = this.y + (this.ly - this.y) / (double)this.lSteps;
         double var25 = this.z + (this.lz - this.z) / (double)this.lSteps;

         double var7;
         for(var7 = this.lyr - (double)this.yRot; var7 < (double)-180.0F; var7 += (double)360.0F) {
         }

         while(var7 >= (double)180.0F) {
            var7 -= (double)360.0F;
         }

         this.yRot = (float)((double)this.yRot + var7 / (double)this.lSteps);
         this.xRot = (float)((double)this.xRot + (this.lxr - (double)this.xRot) / (double)this.lSteps);
         --this.lSteps;
         this.setPos(var22, var24, var25);
         this.setRot(this.yRot, this.xRot);
      } else {
         if (!this.level.isOnline) {
            ItemInstance var1 = this.owner.getSelectedItem();
            if (this.owner.removed || !this.owner.isAlive() || var1 == null || var1.getItem() != Item.fishingRod || this.distanceToSqr(this.owner) > (double)1024.0F) {
               this.remove();
               this.owner.fishing = null;
               return;
            }

            if (this.hookedIn != null) {
               if (!this.hookedIn.removed) {
                  this.x = this.hookedIn.x;
                  this.y = this.hookedIn.bb.y0 + (double)this.hookedIn.bbHeight * 0.8;
                  this.z = this.hookedIn.z;
                  return;
               }

               this.hookedIn = null;
            }
         }

         if (this.shakeTime > 0) {
            --this.shakeTime;
         }

         if (this.inGround) {
            int var19 = this.level.getTile(this.xTile, this.yTile, this.zTile);
            if (var19 == this.lastTile) {
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

         Vec3 var20 = Vec3.newTemp(this.x, this.y, this.z);
         Vec3 var2 = Vec3.newTemp(this.x + this.xd, this.y + this.yd, this.z + this.zd);
         HitResult var3 = this.level.clip(var20, var2);
         var20 = Vec3.newTemp(this.x, this.y, this.z);
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
               HitResult var12 = var11.clip(var20, var2);
               if (var12 != null) {
                  double var13 = var20.distanceTo(var12.pos);
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
               if (var3.entity.hurt(this.owner, 0)) {
                  this.hookedIn = var3.entity;
               }
            } else {
               this.inGround = true;
            }
         }

         if (!this.inGround) {
            this.move(this.xd, this.yd, this.zd);
            float var26 = Mth.sqrt(this.xd * this.xd + this.zd * this.zd);
            this.yRot = (float)(Math.atan2(this.xd, this.zd) * (double)180.0F / (double)(float)Math.PI);

            for(this.xRot = (float)(Math.atan2(this.yd, (double)var26) * (double)180.0F / (double)(float)Math.PI); this.xRot - this.xRotO < -180.0F; this.xRotO -= 360.0F) {
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
            float var27 = 0.92F;
            if (this.onGround || this.horizontalCollision) {
               var27 = 0.5F;
            }

            byte var28 = 5;
            double var29 = (double)0.0F;

            for(int var30 = 0; var30 < var28; ++var30) {
               double var14 = this.bb.y0 + (this.bb.y1 - this.bb.y0) * (double)(var30 + 0) / (double)var28 - (double)0.125F + (double)0.125F;
               double var16 = this.bb.y0 + (this.bb.y1 - this.bb.y0) * (double)(var30 + 1) / (double)var28 - (double)0.125F + (double)0.125F;
               AABB var18 = AABB.newTemp(this.bb.x0, var14, this.bb.z0, this.bb.x1, var16, this.bb.z1);
               if (this.level.containsLiquid(var18, Material.water)) {
                  var29 += (double)1.0F / (double)var28;
               }
            }

            if (var29 > (double)0.0F) {
               if (this.nibble > 0) {
                  --this.nibble;
               } else if (this.random.nextInt(500) == 0) {
                  this.nibble = this.random.nextInt(30) + 10;
                  this.yd -= (double)0.2F;
                  this.level.playSound(this, "random.splash", 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                  float var31 = (float)Mth.floor(this.bb.y0);

                  for(int var33 = 0; (float)var33 < 1.0F + this.bbWidth * 20.0F; ++var33) {
                     float var15 = (this.random.nextFloat() * 2.0F - 1.0F) * this.bbWidth;
                     float var36 = (this.random.nextFloat() * 2.0F - 1.0F) * this.bbWidth;
                     this.level.addParticle("bubble", this.x + (double)var15, (double)(var31 + 1.0F), this.z + (double)var36, this.xd, this.yd - (double)(this.random.nextFloat() * 0.2F), this.zd);
                  }

                  for(int var34 = 0; (float)var34 < 1.0F + this.bbWidth * 20.0F; ++var34) {
                     float var35 = (this.random.nextFloat() * 2.0F - 1.0F) * this.bbWidth;
                     float var37 = (this.random.nextFloat() * 2.0F - 1.0F) * this.bbWidth;
                     this.level.addParticle("splash", this.x + (double)var35, (double)(var31 + 1.0F), this.z + (double)var37, this.xd, this.yd, this.zd);
                  }
               }
            }

            if (this.nibble > 0) {
               this.yd -= (double)(this.random.nextFloat() * this.random.nextFloat() * this.random.nextFloat()) * 0.2;
            }

            double var32 = var29 * (double)2.0F - (double)1.0F;
            this.yd += (double)0.04F * var32;
            if (var29 > (double)0.0F) {
               var27 = (float)((double)var27 * 0.9);
               this.yd *= 0.8;
            }

            this.xd *= (double)var27;
            this.yd *= (double)var27;
            this.zd *= (double)var27;
            this.setPos(this.x, this.y, this.z);
         }
      }
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

   public float getShadowHeightOffs() {
      return 0.0F;
   }

   public int retrieve() {
      byte var1 = 0;
      if (this.hookedIn != null) {
         double var2 = this.owner.x - this.x;
         double var4 = this.owner.y - this.y;
         double var6 = this.owner.z - this.z;
         double var8 = (double)Mth.sqrt(var2 * var2 + var4 * var4 + var6 * var6);
         double var10 = 0.1;
         Entity var10000 = this.hookedIn;
         var10000.xd += var2 * var10;
         var10000 = this.hookedIn;
         var10000.yd += var4 * var10 + (double)Mth.sqrt(var8) * 0.08;
         var10000 = this.hookedIn;
         var10000.zd += var6 * var10;
         var1 = 3;
      } else if (this.nibble > 0) {
         ItemEntity var13 = new ItemEntity(this.level, this.x, this.y, this.z, new ItemInstance(Item.fish_raw));
         double var3 = this.owner.x - this.x;
         double var5 = this.owner.y - this.y;
         double var7 = this.owner.z - this.z;
         double var9 = (double)Mth.sqrt(var3 * var3 + var5 * var5 + var7 * var7);
         double var11 = 0.1;
         var13.xd = var3 * var11;
         var13.yd = var5 * var11 + (double)Mth.sqrt(var9) * 0.08;
         var13.zd = var7 * var11;
         this.level.addEntity(var13);
         var1 = 1;
      }

      if (this.inGround) {
         var1 = 2;
      }

      this.remove();
      this.owner.fishing = null;
      return var1;
   }
}
