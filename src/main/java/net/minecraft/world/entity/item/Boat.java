package net.minecraft.world.entity.item;

import com.mojang.nbt.CompoundTag;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;

public class Boat extends Entity {
   public static final long serialVersionUID = 0L;
   public int damage;
   public int hurtTime;
   public int hurtDir;
   private int lSteps;
   private double lx;
   private double ly;
   private double lz;
   private double lyr;
   private double lxr;
   private double lxd;
   private double lyd;
   private double lzd;

   public Boat(Level var1) {
      super(var1);
      this.damage = 0;
      this.hurtTime = 0;
      this.hurtDir = 1;
      this.blocksBuilding = true;
      this.setSize(1.5F, 0.6F);
      this.heightOffset = this.bbHeight / 2.0F;
      this.makeStepSound = false;
   }

   protected void defineSynchedData() {
   }

   public AABB getCollideAgainstBox(Entity var1) {
      return var1.bb;
   }

   public AABB getCollideBox() {
      return this.bb;
   }

   public boolean isPushable() {
      return true;
   }

   public Boat(Level var1, double var2, double var4, double var6) {
      this(var1);
      this.setPos(var2, var4 + (double)this.heightOffset, var6);
      this.xd = (double)0.0F;
      this.yd = (double)0.0F;
      this.zd = (double)0.0F;
      this.xo = var2;
      this.yo = var4;
      this.zo = var6;
   }

   public double getRideHeight() {
      return (double)this.bbHeight * (double)0.0F - (double)0.3F;
   }

   public boolean hurt(Entity var1, int var2) {
      if (!this.level.isOnline && !this.removed) {
         this.hurtDir = -this.hurtDir;
         this.hurtTime = 10;
         this.damage += var2 * 10;
         this.markHurt();
         if (this.damage > 40) {
            for(int var3 = 0; var3 < 3; ++var3) {
               this.spawnAtLocation(Tile.wood.id, 1, 0.0F);
            }

            for(int var4 = 0; var4 < 2; ++var4) {
               this.spawnAtLocation(Item.stick.id, 1, 0.0F);
            }

            this.remove();
         }

         return true;
      } else {
         return true;
      }
   }

   public void animateHurt() {
      this.hurtDir = -this.hurtDir;
      this.hurtTime = 10;
      this.damage += this.damage * 10;
   }

   public boolean isPickable() {
      return !this.removed;
   }

   public void lerpTo(double var1, double var3, double var5, float var7, float var8, int var9) {
      this.lx = var1;
      this.ly = var3;
      this.lz = var5;
      this.lyr = (double)var7;
      this.lxr = (double)var8;
      this.lSteps = var9 + 4;
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
      if (this.hurtTime > 0) {
         --this.hurtTime;
      }

      if (this.damage > 0) {
         --this.damage;
      }

      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      byte var1 = 5;
      double var2 = (double)0.0F;

      for(int var4 = 0; var4 < var1; ++var4) {
         double var5 = this.bb.y0 + (this.bb.y1 - this.bb.y0) * (double)(var4 + 0) / (double)var1 - (double)0.125F;
         double var7 = this.bb.y0 + (this.bb.y1 - this.bb.y0) * (double)(var4 + 1) / (double)var1 - (double)0.125F;
         AABB var9 = AABB.newTemp(this.bb.x0, var5, this.bb.z0, this.bb.x1, var7, this.bb.z1);
         if (this.level.containsLiquid(var9, Material.water)) {
            var2 += (double)1.0F / (double)var1;
         }
      }

      if (this.level.isOnline) {
         if (this.lSteps > 0) {
            double var24 = this.x + (this.lx - this.x) / (double)this.lSteps;
            double var26 = this.y + (this.ly - this.y) / (double)this.lSteps;
            double var28 = this.z + (this.lz - this.z) / (double)this.lSteps;

            double var33;
            for(var33 = this.lyr - (double)this.yRot; var33 < (double)-180.0F; var33 += (double)360.0F) {
            }

            while(var33 >= (double)180.0F) {
               var33 -= (double)360.0F;
            }

            this.yRot = (float)((double)this.yRot + var33 / (double)this.lSteps);
            this.xRot = (float)((double)this.xRot + (this.lxr - (double)this.xRot) / (double)this.lSteps);
            --this.lSteps;
            this.setPos(var24, var26, var28);
            this.setRot(this.yRot, this.xRot);
         } else {
            double var25 = this.x + this.xd;
            double var27 = this.y + this.yd;
            double var29 = this.z + this.zd;
            this.setPos(var25, var27, var29);
            if (this.onGround) {
               this.xd *= (double)0.5F;
               this.yd *= (double)0.5F;
               this.zd *= (double)0.5F;
            }

            this.xd *= (double)0.99F;
            this.yd *= (double)0.95F;
            this.zd *= (double)0.99F;
         }

      } else {
         double var23 = var2 * (double)2.0F - (double)1.0F;
         this.yd += (double)0.04F * var23;
         if (this.rider != null) {
            this.xd += this.rider.xd * 0.2;
            this.zd += this.rider.zd * 0.2;
         }

         double var6 = 0.4;
         if (this.xd < -var6) {
            this.xd = -var6;
         }

         if (this.xd > var6) {
            this.xd = var6;
         }

         if (this.zd < -var6) {
            this.zd = -var6;
         }

         if (this.zd > var6) {
            this.zd = var6;
         }

         if (this.onGround) {
            this.xd *= (double)0.5F;
            this.yd *= (double)0.5F;
            this.zd *= (double)0.5F;
         }

         this.move(this.xd, this.yd, this.zd);
         double var8 = Math.sqrt(this.xd * this.xd + this.zd * this.zd);
         if (var8 > 0.15) {
            double var10 = Math.cos((double)this.yRot * Math.PI / (double)180.0F);
            double var12 = Math.sin((double)this.yRot * Math.PI / (double)180.0F);

            for(int var14 = 0; (double)var14 < (double)1.0F + var8 * (double)60.0F; ++var14) {
               double var15 = (double)(this.random.nextFloat() * 2.0F - 1.0F);
               double var17 = (double)(this.random.nextInt(2) * 2 - 1) * 0.7;
               if (this.random.nextBoolean()) {
                  double var19 = this.x - var10 * var15 * 0.8 + var12 * var17;
                  double var21 = this.z - var12 * var15 * 0.8 - var10 * var17;
                  this.level.addParticle("splash", var19, this.y - (double)0.125F, var21, this.xd, this.yd, this.zd);
               } else {
                  double var36 = this.x + var10 + var12 * var15 * 0.7;
                  double var38 = this.z + var12 - var10 * var15 * 0.7;
                  this.level.addParticle("splash", var36, this.y - (double)0.125F, var38, this.xd, this.yd, this.zd);
               }
            }
         }

         if (this.horizontalCollision && var8 > 0.15) {
            if (!this.level.isOnline) {
               this.remove();

               for(int var30 = 0; var30 < 3; ++var30) {
                  this.spawnAtLocation(Tile.wood.id, 1, 0.0F);
               }

               for(int var31 = 0; var31 < 2; ++var31) {
                  this.spawnAtLocation(Item.stick.id, 1, 0.0F);
               }
            }
         } else {
            this.xd *= (double)0.99F;
            this.yd *= (double)0.95F;
            this.zd *= (double)0.99F;
         }

         this.xRot = 0.0F;
         double var32 = (double)this.yRot;
         double var34 = this.xo - this.x;
         double var35 = this.zo - this.z;
         if (var34 * var34 + var35 * var35 > 0.001) {
            var32 = (double)((float)(Math.atan2(var35, var34) * (double)180.0F / Math.PI));
         }

         double var16;
         for(var16 = var32 - (double)this.yRot; var16 >= (double)180.0F; var16 -= (double)360.0F) {
         }

         while(var16 < (double)-180.0F) {
            var16 += (double)360.0F;
         }

         if (var16 > (double)20.0F) {
            var16 = (double)20.0F;
         }

         if (var16 < (double)-20.0F) {
            var16 = (double)-20.0F;
         }

         this.yRot = (float)((double)this.yRot + var16);
         this.setRot(this.yRot, this.xRot);
         List var18 = this.level.getEntities(this, this.bb.grow((double)0.2F, (double)0.0F, (double)0.2F));
         if (var18 != null && var18.size() > 0) {
            for(int var37 = 0; var37 < var18.size(); ++var37) {
               Entity var20 = (Entity)var18.get(var37);
               if (var20 != this.rider && var20.isPushable() && var20 instanceof Boat) {
                  var20.push(this);
               }
            }
         }

         if (this.rider != null && this.rider.removed) {
            this.rider = null;
         }

      }
   }

   public void positionRider() {
      if (this.rider != null) {
         double var1 = Math.cos((double)this.yRot * Math.PI / (double)180.0F) * 0.4;
         double var3 = Math.sin((double)this.yRot * Math.PI / (double)180.0F) * 0.4;
         this.rider.setPos(this.x + var1, this.y + this.getRideHeight() + this.rider.getRidingHeight(), this.z + var3);
      }
   }

   protected void addAdditonalSaveData(CompoundTag var1) {
   }

   protected void readAdditionalSaveData(CompoundTag var1) {
   }

   public float getShadowHeightOffs() {
      return 0.0F;
   }

   public String getName() {
      return "Boat";
   }

   public void setChanged() {
   }

   public boolean interact(Player var1) {
      if (this.rider != null && this.rider instanceof Player && this.rider != var1) {
         return true;
      } else {
         if (!this.level.isOnline) {
            var1.ride(this);
         }

         return true;
      }
   }
}
