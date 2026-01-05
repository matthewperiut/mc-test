package net.minecraft.world.entity;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.DoubleTag;
import com.mojang.nbt.FloatTag;
import com.mojang.nbt.ListTag;
import java.util.List;
import java.util.Random;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.LiquidTile;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import util.Mth;

public abstract class Entity {
   public static final int TOTAL_AIR_SUPPLY = 300;
   private static int entityCounter = 0;
   public int entityId;
   public double viewScale;
   public boolean blocksBuilding;
   public Entity rider;
   public Entity riding;
   public Level level;
   public double xo;
   public double yo;
   public double zo;
   public double x;
   public double y;
   public double z;
   public double xd;
   public double yd;
   public double zd;
   public float yRot;
   public float xRot;
   public float yRotO;
   public float xRotO;
   public final AABB bb;
   public boolean onGround;
   public boolean horizontalCollision;
   public boolean verticalCollision;
   public boolean collision;
   public boolean hurtMarked;
   public boolean slide;
   public boolean removed;
   public float heightOffset;
   public float bbWidth;
   public float bbHeight;
   public float walkDistO;
   public float walkDist;
   protected boolean makeStepSound;
   protected float fallDistance;
   private int nextStep;
   public double xOld;
   public double yOld;
   public double zOld;
   public float ySlideOffset;
   public float footSize;
   public boolean noPhysics;
   public float pushthrough;
   public boolean hovered;
   protected Random random;
   public int tickCount;
   public int flameTime;
   public int onFire;
   protected int airCapacity;
   protected boolean wasInWater;
   public int invulnerableTime;
   public int airSupply;
   private boolean firstTick;
   public String customTextureUrl;
   public String customTextureUrl2;
   protected boolean fireImmune;
   protected SynchedEntityData entityData;
   private static final int DATA_SHARED_FLAGS_ID = 0;
   private static final int FLAG_ONFIRE = 0;
   private static final int FLAG_SNEAKING = 1;
   private static final int FLAG_RIDING = 2;
   private double xRideRotA;
   private double yRideRotA;
   public boolean inChunk;
   public int xChunk;
   public int yChunk;
   public int zChunk;
   public int xp;
   public int yp;
   public int zp;

   public Entity(Level var1) {
      this.entityId = entityCounter++;
      this.viewScale = (double)1.0F;
      this.blocksBuilding = false;
      this.bb = AABB.newPermanent((double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
      this.onGround = false;
      this.collision = false;
      this.hurtMarked = false;
      this.slide = true;
      this.removed = false;
      this.heightOffset = 0.0F;
      this.bbWidth = 0.6F;
      this.bbHeight = 1.8F;
      this.walkDistO = 0.0F;
      this.walkDist = 0.0F;
      this.makeStepSound = true;
      this.fallDistance = 0.0F;
      this.nextStep = 1;
      this.ySlideOffset = 0.0F;
      this.footSize = 0.0F;
      this.noPhysics = false;
      this.pushthrough = 0.0F;
      this.hovered = false;
      this.random = new Random();
      this.tickCount = 0;
      this.flameTime = 1;
      this.onFire = 0;
      this.airCapacity = 300;
      this.wasInWater = false;
      this.invulnerableTime = 0;
      this.airSupply = 300;
      this.firstTick = true;
      this.fireImmune = false;
      this.entityData = new SynchedEntityData();
      this.inChunk = false;
      this.level = var1;
      this.setPos((double)0.0F, (double)0.0F, (double)0.0F);
      this.entityData.define(0, (byte)0);
      this.defineSynchedData();
   }

   protected abstract void defineSynchedData();

   public SynchedEntityData getEntityData() {
      return this.entityData;
   }

   public boolean equals(Object var1) {
      if (var1 instanceof Entity) {
         return ((Entity)var1).entityId == this.entityId;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.entityId;
   }

   protected void resetPos() {
      if (this.level != null) {
         while(this.y > (double)0.0F) {
            this.setPos(this.x, this.y, this.z);
            if (this.level.getCubes(this, this.bb).size() == 0) {
               break;
            }

            ++this.y;
         }

         this.xd = this.yd = this.zd = (double)0.0F;
         this.xRot = 0.0F;
      }
   }

   public void remove() {
      this.removed = true;
   }

   protected void setSize(float var1, float var2) {
      this.bbWidth = var1;
      this.bbHeight = var2;
   }

   protected void setPos(EntityPos var1) {
      if (var1.move) {
         this.setPos(var1.x, var1.y, var1.z);
      } else {
         this.setPos(this.x, this.y, this.z);
      }

      if (var1.rot) {
         this.setRot(var1.yRot, var1.xRot);
      } else {
         this.setRot(this.yRot, this.xRot);
      }

   }

   protected void setRot(float var1, float var2) {
      this.yRot = var1;
      this.xRot = var2;
   }

   public void setPos(double var1, double var3, double var5) {
      this.x = var1;
      this.y = var3;
      this.z = var5;
      float var7 = this.bbWidth / 2.0F;
      float var8 = this.bbHeight;
      this.bb.set(var1 - (double)var7, var3 - (double)this.heightOffset + (double)this.ySlideOffset, var5 - (double)var7, var1 + (double)var7, var3 - (double)this.heightOffset + (double)this.ySlideOffset + (double)var8, var5 + (double)var7);
   }

   public void turn(float var1, float var2) {
      float var3 = this.xRot;
      float var4 = this.yRot;
      this.yRot = (float)((double)this.yRot + (double)var1 * 0.15);
      this.xRot = (float)((double)this.xRot - (double)var2 * 0.15);
      if (this.xRot < -90.0F) {
         this.xRot = -90.0F;
      }

      if (this.xRot > 90.0F) {
         this.xRot = 90.0F;
      }

      this.xRotO += this.xRot - var3;
      this.yRotO += this.yRot - var4;
   }

   public void interpolateTurn(float var1, float var2) {
      this.yRot = (float)((double)this.yRot + (double)var1 * 0.15);
      this.xRot = (float)((double)this.xRot - (double)var2 * 0.15);
      if (this.xRot < -90.0F) {
         this.xRot = -90.0F;
      }

      if (this.xRot > 90.0F) {
         this.xRot = 90.0F;
      }

   }

   public void tick() {
      this.baseTick();
   }

   public void baseTick() {
      if (this.riding != null && this.riding.removed) {
         this.riding = null;
      }

      ++this.tickCount;
      this.walkDistO = this.walkDist;
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.xRotO = this.xRot;
      this.yRotO = this.yRot;
      if (this.isInWater()) {
         if (!this.wasInWater && !this.firstTick) {
            float var1 = Mth.sqrt(this.xd * this.xd * (double)0.2F + this.yd * this.yd + this.zd * this.zd * (double)0.2F) * 0.2F;
            if (var1 > 1.0F) {
               var1 = 1.0F;
            }

            this.level.playSound(this, "random.splash", var1, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
            float var2 = (float)Mth.floor(this.bb.y0);

            for(int var3 = 0; (float)var3 < 1.0F + this.bbWidth * 20.0F; ++var3) {
               float var4 = (this.random.nextFloat() * 2.0F - 1.0F) * this.bbWidth;
               float var5 = (this.random.nextFloat() * 2.0F - 1.0F) * this.bbWidth;
               this.level.addParticle("bubble", this.x + (double)var4, (double)(var2 + 1.0F), this.z + (double)var5, this.xd, this.yd - (double)(this.random.nextFloat() * 0.2F), this.zd);
            }

            for(int var6 = 0; (float)var6 < 1.0F + this.bbWidth * 20.0F; ++var6) {
               float var7 = (this.random.nextFloat() * 2.0F - 1.0F) * this.bbWidth;
               float var8 = (this.random.nextFloat() * 2.0F - 1.0F) * this.bbWidth;
               this.level.addParticle("splash", this.x + (double)var7, (double)(var2 + 1.0F), this.z + (double)var8, this.xd, this.yd, this.zd);
            }
         }

         this.fallDistance = 0.0F;
         this.wasInWater = true;
         this.onFire = 0;
      } else {
         this.wasInWater = false;
      }

      if (this.level.isOnline) {
         this.onFire = 0;
      } else if (this.onFire > 0) {
         if (this.fireImmune) {
            this.onFire -= 4;
            if (this.onFire < 0) {
               this.onFire = 0;
            }
         } else {
            if (this.onFire % 20 == 0) {
               this.hurt((Entity)null, 1);
            }

            --this.onFire;
         }
      }

      if (this.isInLava()) {
         this.lavaHurt();
      }

      if (this.y < (double)-64.0F) {
         this.outOfWorld();
      }

      if (!this.level.isOnline) {
         this.setSharedFlag(0, this.onFire > 0);
         this.setSharedFlag(2, this.riding != null);
      }

      this.firstTick = false;
   }

   protected void lavaHurt() {
      if (!this.fireImmune) {
         this.hurt((Entity)null, 4);
         this.onFire = 600;
      }

   }

   protected void outOfWorld() {
      this.remove();
   }

   public boolean isFree(float var1, float var2, float var3, float var4) {
      AABB var5 = this.bb.grow((double)var4, (double)var4, (double)var4).cloneMove((double)var1, (double)var2, (double)var3);
      List var6 = this.level.getCubes(this, var5);
      if (var6.size() > 0) {
         return false;
      } else {
         return !this.level.containsAnyLiquid(var5);
      }
   }

   public boolean isFree(double var1, double var3, double var5) {
      AABB var7 = this.bb.cloneMove(var1, var3, var5);
      List var8 = this.level.getCubes(this, var7);
      if (var8.size() > 0) {
         return false;
      } else {
         return !this.level.containsAnyLiquid(var7);
      }
   }

   public void move(double var1, double var3, double var5) {
      if (this.noPhysics) {
         this.bb.move(var1, var3, var5);
         this.x = (this.bb.x0 + this.bb.x1) / (double)2.0F;
         this.y = this.bb.y0 + (double)this.heightOffset - (double)this.ySlideOffset;
         this.z = (this.bb.z0 + this.bb.z1) / (double)2.0F;
      } else {
         double var7 = this.x;
         double var9 = this.z;
         double var11 = var1;
         double var13 = var3;
         double var15 = var5;
         AABB var17 = this.bb.copy();
         boolean var18 = this.onGround && this.isSneaking();
         if (var18) {
            double var19;
            for(var19 = 0.05; var1 != (double)0.0F && this.level.getCubes(this, this.bb.cloneMove(var1, (double)-1.0F, (double)0.0F)).size() == 0; var11 = var1) {
               if (var1 < var19 && var1 >= -var19) {
                  var1 = (double)0.0F;
               } else if (var1 > (double)0.0F) {
                  var1 -= var19;
               } else {
                  var1 += var19;
               }
            }

            for(; var5 != (double)0.0F && this.level.getCubes(this, this.bb.cloneMove((double)0.0F, (double)-1.0F, var5)).size() == 0; var15 = var5) {
               if (var5 < var19 && var5 >= -var19) {
                  var5 = (double)0.0F;
               } else if (var5 > (double)0.0F) {
                  var5 -= var19;
               } else {
                  var5 += var19;
               }
            }
         }

         List var35 = this.level.getCubes(this, this.bb.expand(var1, var3, var5));

         for(int var20 = 0; var20 < var35.size(); ++var20) {
            var3 = ((AABB)var35.get(var20)).clipYCollide(this.bb, var3);
         }

         this.bb.move((double)0.0F, var3, (double)0.0F);
         if (!this.slide && var13 != var3) {
            var5 = (double)0.0F;
            var3 = (double)0.0F;
            var1 = (double)0.0F;
         }

         boolean var37 = this.onGround || var13 != var3 && var13 < (double)0.0F;

         for(int var21 = 0; var21 < var35.size(); ++var21) {
            var1 = ((AABB)var35.get(var21)).clipXCollide(this.bb, var1);
         }

         this.bb.move(var1, (double)0.0F, (double)0.0F);
         if (!this.slide && var11 != var1) {
            var5 = (double)0.0F;
            var3 = (double)0.0F;
            var1 = (double)0.0F;
         }

         for(int var38 = 0; var38 < var35.size(); ++var38) {
            var5 = ((AABB)var35.get(var38)).clipZCollide(this.bb, var5);
         }

         this.bb.move((double)0.0F, (double)0.0F, var5);
         if (!this.slide && var15 != var5) {
            var5 = (double)0.0F;
            var3 = (double)0.0F;
            var1 = (double)0.0F;
         }

         if (this.footSize > 0.0F && var37 && this.ySlideOffset < 0.05F && (var11 != var1 || var15 != var5)) {
            double var39 = var1;
            double var23 = var3;
            double var25 = var5;
            var1 = var11;
            var3 = (double)this.footSize;
            var5 = var15;
            AABB var27 = this.bb.copy();
            this.bb.set(var17);
            var35 = this.level.getCubes(this, this.bb.expand(var11, var3, var15));

            for(int var28 = 0; var28 < var35.size(); ++var28) {
               var3 = ((AABB)var35.get(var28)).clipYCollide(this.bb, var3);
            }

            this.bb.move((double)0.0F, var3, (double)0.0F);
            if (!this.slide && var13 != var3) {
               var5 = (double)0.0F;
               var3 = (double)0.0F;
               var1 = (double)0.0F;
            }

            for(int var48 = 0; var48 < var35.size(); ++var48) {
               var1 = ((AABB)var35.get(var48)).clipXCollide(this.bb, var1);
            }

            this.bb.move(var1, (double)0.0F, (double)0.0F);
            if (!this.slide && var11 != var1) {
               var5 = (double)0.0F;
               var3 = (double)0.0F;
               var1 = (double)0.0F;
            }

            for(int var49 = 0; var49 < var35.size(); ++var49) {
               var5 = ((AABB)var35.get(var49)).clipZCollide(this.bb, var5);
            }

            this.bb.move((double)0.0F, (double)0.0F, var5);
            if (!this.slide && var15 != var5) {
               var5 = (double)0.0F;
               var3 = (double)0.0F;
               var1 = (double)0.0F;
            }

            if (var39 * var39 + var25 * var25 >= var1 * var1 + var5 * var5) {
               var1 = var39;
               var3 = var23;
               var5 = var25;
               this.bb.set(var27);
            } else {
               this.ySlideOffset = (float)((double)this.ySlideOffset + (double)0.5F);
            }
         }

         this.x = (this.bb.x0 + this.bb.x1) / (double)2.0F;
         this.y = this.bb.y0 + (double)this.heightOffset - (double)this.ySlideOffset;
         this.z = (this.bb.z0 + this.bb.z1) / (double)2.0F;
         this.horizontalCollision = var11 != var1 || var15 != var5;
         this.verticalCollision = var13 != var3;
         this.onGround = var13 != var3 && var13 < (double)0.0F;
         this.collision = this.horizontalCollision || this.verticalCollision;
         this.checkFallDamage(var3, this.onGround);
         if (var11 != var1) {
            this.xd = (double)0.0F;
         }

         if (var13 != var3) {
            this.yd = (double)0.0F;
         }

         if (var15 != var5) {
            this.zd = (double)0.0F;
         }

         double var40 = this.x - var7;
         double var41 = this.z - var9;
         if (this.makeStepSound && !var18) {
            this.walkDist = (float)((double)this.walkDist + (double)Mth.sqrt(var40 * var40 + var41 * var41) * 0.6);
            int var42 = Mth.floor(this.x);
            int var26 = Mth.floor(this.y - (double)0.2F - (double)this.heightOffset);
            int var46 = Mth.floor(this.z);
            int var50 = this.level.getTile(var42, var26, var46);
            if (this.walkDist > (float)this.nextStep && var50 > 0) {
               ++this.nextStep;
               Tile.SoundType var29 = Tile.tiles[var50].soundType;
               if (this.level.getTile(var42, var26 + 1, var46) == Tile.topSnow.id) {
                  var29 = Tile.topSnow.soundType;
                  this.level.playSound(this, var29.getStepSound(), var29.getVolume() * 0.15F, var29.getPitch());
               } else if (!Tile.tiles[var50].material.isLiquid()) {
                  this.level.playSound(this, var29.getStepSound(), var29.getVolume() * 0.15F, var29.getPitch());
               }

               Tile.tiles[var50].stepOn(this.level, var42, var26, var46, this);
            }
         }

         int var43 = Mth.floor(this.bb.x0);
         int var45 = Mth.floor(this.bb.y0);
         int var47 = Mth.floor(this.bb.z0);
         int var51 = Mth.floor(this.bb.x1);
         int var53 = Mth.floor(this.bb.y1);
         int var30 = Mth.floor(this.bb.z1);
         if (this.level.hasChunksAt(var43, var45, var47, var51, var53, var30)) {
            for(int var31 = var43; var31 <= var51; ++var31) {
               for(int var32 = var45; var32 <= var53; ++var32) {
                  for(int var33 = var47; var33 <= var30; ++var33) {
                     int var34 = this.level.getTile(var31, var32, var33);
                     if (var34 > 0) {
                        Tile.tiles[var34].entityInside(this.level, var31, var32, var33, this);
                     }
                  }
               }
            }
         }

         this.ySlideOffset *= 0.4F;
         boolean inWater = this.isInWater();
         if (this.level.containsFireTile(this.bb)) {
            this.burn(1);
            if (!inWater) {
               ++this.onFire;
               if (this.onFire == 0) {
                  this.onFire = 300;
               }
            }
         } else if (this.onFire <= 0) {
            this.onFire = -this.flameTime;
         }

         if (inWater && this.onFire > 0) {
            this.level.playSound(this, "random.fizz", 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
            this.onFire = -this.flameTime;
         }

      }
   }

   protected void checkFallDamage(double var1, boolean var3) {
      if (var3) {
         if (this.fallDistance > 0.0F) {
            this.causeFallDamage(this.fallDistance);
            this.fallDistance = 0.0F;
         }
      } else if (var1 < (double)0.0F) {
         this.fallDistance = (float)((double)this.fallDistance - var1);
      }

   }

   public AABB getCollideBox() {
      return null;
   }

   protected void burn(int var1) {
      if (!this.fireImmune) {
         this.hurt((Entity)null, var1);
      }

   }

   protected void causeFallDamage(float var1) {
   }

   public boolean isInWater() {
      return this.level.checkAndHandleWater(this.bb.grow((double)0.0F, (double)-0.4F, (double)0.0F), Material.water, this);
   }

   public boolean isUnderLiquid(Material var1) {
      double var2 = this.y + (double)this.getHeadHeight();
      int var4 = Mth.floor(this.x);
      int var5 = Mth.floor((float)Mth.floor(var2));
      int var6 = Mth.floor(this.z);
      int var7 = this.level.getTile(var4, var5, var6);
      if (var7 != 0 && Tile.tiles[var7].material == var1) {
         float var8 = LiquidTile.getHeight(this.level.getData(var4, var5, var6)) - 0.11111111F;
         float var9 = (float)(var5 + 1) - var8;
         return var2 < (double)var9;
      } else {
         return false;
      }
   }

   public float getHeadHeight() {
      return 0.0F;
   }

   public boolean isInLava() {
      return this.level.containsMaterial(this.bb.grow((double)-0.1F, (double)-0.4F, (double)-0.1F), Material.lava);
   }

   public void moveRelative(float var1, float var2, float var3) {
      float var4 = Mth.sqrt(var1 * var1 + var2 * var2);
      if (!(var4 < 0.01F)) {
         if (var4 < 1.0F) {
            var4 = 1.0F;
         }

         var4 = var3 / var4;
         var1 *= var4;
         var2 *= var4;
         float var5 = Mth.sin(this.yRot * (float)Math.PI / 180.0F);
         float var6 = Mth.cos(this.yRot * (float)Math.PI / 180.0F);
         this.xd += (double)(var1 * var6 - var2 * var5);
         this.zd += (double)(var2 * var6 + var1 * var5);
      }
   }

   public float getBrightness(float var1) {
      int var2 = Mth.floor(this.x);
      double var3 = (this.bb.y1 - this.bb.y0) * 0.66;
      int var5 = Mth.floor(this.y - (double)this.heightOffset + var3);
      int var6 = Mth.floor(this.z);
      return this.level.hasChunksAt(Mth.floor(this.bb.x0), Mth.floor(this.bb.y0), Mth.floor(this.bb.z0), Mth.floor(this.bb.x1), Mth.floor(this.bb.y1), Mth.floor(this.bb.z1)) ? this.level.getBrightness(var2, var5, var6) : 0.0F;
   }

   public void setLevel(Level var1) {
      this.level = var1;
   }

   public void absMoveTo(double var1, double var3, double var5, float var7, float var8) {
      this.xo = this.x = var1;
      this.yo = this.y = var3;
      this.zo = this.z = var5;
      this.yRotO = this.yRot = var7;
      this.xRotO = this.xRot = var8;
      this.ySlideOffset = 0.0F;
      double var9 = (double)(this.yRotO - var7);
      if (var9 < (double)-180.0F) {
         this.yRotO += 360.0F;
      }

      if (var9 >= (double)180.0F) {
         this.yRotO -= 360.0F;
      }

      this.setPos(this.x, this.y, this.z);
      this.setRot(var7, var8);
   }

   public void moveTo(double var1, double var3, double var5, float var7, float var8) {
      this.xOld = this.xo = this.x = var1;
      this.yOld = this.yo = this.y = var3 + (double)this.heightOffset;
      this.zOld = this.zo = this.z = var5;
      this.yRot = var7;
      this.xRot = var8;
      this.setPos(this.x, this.y, this.z);
   }

   public float distanceTo(Entity var1) {
      float var2 = (float)(this.x - var1.x);
      float var3 = (float)(this.y - var1.y);
      float var4 = (float)(this.z - var1.z);
      return Mth.sqrt(var2 * var2 + var3 * var3 + var4 * var4);
   }

   public double distanceToSqr(double var1, double var3, double var5) {
      double var7 = this.x - var1;
      double var9 = this.y - var3;
      double var11 = this.z - var5;
      return var7 * var7 + var9 * var9 + var11 * var11;
   }

   public double distanceTo(double var1, double var3, double var5) {
      double var7 = this.x - var1;
      double var9 = this.y - var3;
      double var11 = this.z - var5;
      return (double)Mth.sqrt(var7 * var7 + var9 * var9 + var11 * var11);
   }

   public double distanceToSqr(Entity var1) {
      double var2 = this.x - var1.x;
      double var4 = this.y - var1.y;
      double var6 = this.z - var1.z;
      return var2 * var2 + var4 * var4 + var6 * var6;
   }

   public void playerTouch(Player var1) {
   }

   public void push(Entity var1) {
      if (var1.rider != this && var1.riding != this) {
         double var2 = var1.x - this.x;
         double var4 = var1.z - this.z;
         double var6 = Mth.asbMax(var2, var4);
         if (var6 >= (double)0.01F) {
            var6 = (double)Mth.sqrt(var6);
            var2 /= var6;
            var4 /= var6;
            double var8 = (double)1.0F / var6;
            if (var8 > (double)1.0F) {
               var8 = (double)1.0F;
            }

            var2 *= var8;
            var4 *= var8;
            var2 *= (double)0.05F;
            var4 *= (double)0.05F;
            var2 *= (double)(1.0F - this.pushthrough);
            var4 *= (double)(1.0F - this.pushthrough);
            this.push(-var2, (double)0.0F, -var4);
            var1.push(var2, (double)0.0F, var4);
         }

      }
   }

   public void push(double var1, double var3, double var5) {
      this.xd += var1;
      this.yd += var3;
      this.zd += var5;
   }

   protected void markHurt() {
      this.hurtMarked = true;
   }

   public boolean hurt(Entity var1, int var2) {
      this.markHurt();
      return false;
   }

   public boolean intersects(double var1, double var3, double var5, double var7, double var9, double var11) {
      return this.bb.intersects(var1, var3, var5, var7, var9, var11);
   }

   public boolean isPickable() {
      return false;
   }

   public boolean isPushable() {
      return false;
   }

   public boolean isShootable() {
      return false;
   }

   public void awardKillScore(Entity var1, int var2) {
   }

   public boolean shouldRender(Vec3 var1) {
      double var2 = this.x - var1.x;
      double var4 = this.y - var1.y;
      double var6 = this.z - var1.z;
      double var8 = var2 * var2 + var4 * var4 + var6 * var6;
      return this.shouldRenderAtSqrDistance(var8);
   }

   public boolean shouldRenderAtSqrDistance(double var1) {
      double var3 = this.bb.getSize();
      var3 *= (double)64.0F * this.viewScale;
      return var1 < var3 * var3;
   }

   public String getTexture() {
      return null;
   }

   public boolean isCreativeModeAllowed() {
      return false;
   }

   public boolean save(CompoundTag var1) {
      String var2 = this.getEncodeId();
      if (!this.removed && var2 != null) {
         var1.putString("id", var2);
         this.saveWithoutId(var1);
         return true;
      } else {
         return false;
      }
   }

   public void saveWithoutId(CompoundTag var1) {
      var1.put("Pos", this.newDoubleList(this.x, this.y, this.z));
      var1.put("Motion", this.newDoubleList(this.xd, this.yd, this.zd));
      var1.put("Rotation", this.newFloatList(this.yRot, this.xRot));
      var1.putFloat("FallDistance", this.fallDistance);
      var1.putShort("Fire", (short)this.onFire);
      var1.putShort("Air", (short)this.airSupply);
      var1.putBoolean("OnGround", this.onGround);
      this.addAdditonalSaveData(var1);
   }

   public void load(CompoundTag var1) {
      ListTag var2 = var1.getList("Pos");
      ListTag var3 = var1.getList("Motion");
      ListTag var4 = var1.getList("Rotation");
      this.setPos((double)0.0F, (double)0.0F, (double)0.0F);
      this.xd = ((DoubleTag)var3.get(0)).data;
      this.yd = ((DoubleTag)var3.get(1)).data;
      this.zd = ((DoubleTag)var3.get(2)).data;
      this.xo = this.xOld = this.x = ((DoubleTag)var2.get(0)).data;
      this.yo = this.yOld = this.y = ((DoubleTag)var2.get(1)).data;
      this.zo = this.zOld = this.z = ((DoubleTag)var2.get(2)).data;
      this.yRotO = this.yRot = ((FloatTag)var4.get(0)).data;
      this.xRotO = this.xRot = ((FloatTag)var4.get(1)).data;
      this.fallDistance = var1.getFloat("FallDistance");
      this.onFire = var1.getShort("Fire");
      this.airSupply = var1.getShort("Air");
      this.onGround = var1.getBoolean("OnGround");
      this.setPos(this.x, this.y, this.z);
      this.readAdditionalSaveData(var1);
   }

   protected final String getEncodeId() {
      return EntityIO.getEncodeId(this);
   }

   protected abstract void readAdditionalSaveData(CompoundTag var1);

   protected abstract void addAdditonalSaveData(CompoundTag var1);

   protected ListTag<DoubleTag> newDoubleList(double... var1) {
      ListTag var2 = new ListTag();

      for(double var6 : var1) {
         var2.add(new DoubleTag(var6));
      }

      return var2;
   }

   protected ListTag<FloatTag> newFloatList(float... var1) {
      ListTag var2 = new ListTag();

      for(float var6 : var1) {
         var2.add(new FloatTag(var6));
      }

      return var2;
   }

   public float getShadowHeightOffs() {
      return this.bbHeight / 2.0F;
   }

   public ItemEntity spawnAtLocation(int var1, int var2) {
      return this.spawnAtLocation(var1, var2, 0.0F);
   }

   public ItemEntity spawnAtLocation(int var1, int var2, float var3) {
      return this.spawnAtLocation(new ItemInstance(var1, var2, 0), var3);
   }

   public ItemEntity spawnAtLocation(ItemInstance var1, float var2) {
      ItemEntity var3 = new ItemEntity(this.level, this.x, this.y + (double)var2, this.z, var1);
      var3.throwTime = 10;
      this.level.addEntity(var3);
      return var3;
   }

   public boolean isAlive() {
      return !this.removed;
   }

   public boolean isInWall() {
      int var1 = Mth.floor(this.x);
      int var2 = Mth.floor(this.y + (double)this.getHeadHeight());
      int var3 = Mth.floor(this.z);
      return this.level.isSolidTile(var1, var2, var3);
   }

   public boolean interact(Player var1) {
      return false;
   }

   public AABB getCollideAgainstBox(Entity var1) {
      return null;
   }

   public void rideTick() {
      if (this.riding.removed) {
         this.riding = null;
      } else {
         this.xd = (double)0.0F;
         this.yd = (double)0.0F;
         this.zd = (double)0.0F;
         this.tick();
         this.riding.positionRider();
         this.yRideRotA += (double)(this.riding.yRot - this.riding.yRotO);

         for(this.xRideRotA += (double)(this.riding.xRot - this.riding.xRotO); this.yRideRotA >= (double)180.0F; this.yRideRotA -= (double)360.0F) {
         }

         while(this.yRideRotA < (double)-180.0F) {
            this.yRideRotA += (double)360.0F;
         }

         while(this.xRideRotA >= (double)180.0F) {
            this.xRideRotA -= (double)360.0F;
         }

         while(this.xRideRotA < (double)-180.0F) {
            this.xRideRotA += (double)360.0F;
         }

         double var1 = this.yRideRotA * (double)0.5F;
         double var3 = this.xRideRotA * (double)0.5F;
         float var5 = 10.0F;
         if (var1 > (double)var5) {
            var1 = (double)var5;
         }

         if (var1 < (double)(-var5)) {
            var1 = (double)(-var5);
         }

         if (var3 > (double)var5) {
            var3 = (double)var5;
         }

         if (var3 < (double)(-var5)) {
            var3 = (double)(-var5);
         }

         this.yRideRotA -= var1;
         this.xRideRotA -= var3;
         this.yRot = (float)((double)this.yRot + var1);
         this.xRot = (float)((double)this.xRot + var3);
      }
   }

   public void positionRider() {
      this.rider.setPos(this.x, this.y + this.getRideHeight() + this.rider.getRidingHeight(), this.z);
   }

   public double getRidingHeight() {
      return (double)this.heightOffset;
   }

   public double getRideHeight() {
      return (double)this.bbHeight * (double)0.75F;
   }

   public void ride(Entity var1) {
      this.xRideRotA = (double)0.0F;
      this.yRideRotA = (double)0.0F;
      if (var1 == null) {
         if (this.riding != null) {
            this.moveTo(this.riding.x, this.riding.bb.y0 + (double)this.riding.bbHeight, this.riding.z, this.yRot, this.xRot);
            this.riding.rider = null;
         }

         this.riding = null;
      } else if (this.riding == var1) {
         this.riding.rider = null;
         this.riding = null;
         this.moveTo(var1.x, var1.bb.y0 + (double)var1.bbHeight, var1.z, this.yRot, this.xRot);
      } else {
         if (this.riding != null) {
            this.riding.rider = null;
         }

         if (var1.rider != null) {
            var1.rider.riding = null;
         }

         this.riding = var1;
         var1.rider = this;
      }
   }

   public void lerpTo(double var1, double var3, double var5, float var7, float var8, int var9) {
      this.setPos(var1, var3, var5);
      this.setRot(var7, var8);
   }

   public float getPickRadius() {
      return 0.1F;
   }

   public Vec3 getLookAngle() {
      return null;
   }

   public void handleInsidePortal() {
   }

   public void lerpMotion(double var1, double var3, double var5) {
      this.xd = var1;
      this.yd = var3;
      this.zd = var5;
   }

   public void handleEntityEvent(byte var1) {
   }

   public void animateHurt() {
   }

   public void prepareCustomTextures() {
   }

   public ItemInstance[] getEquipmentSlots() {
      return null;
   }

   public void setEquippedSlot(int var1, int var2, int var3) {
   }

   public boolean isOnFire() {
      return this.onFire > 0 || this.getSharedFlag(0);
   }

   public boolean isRiding() {
      return this.riding != null || this.getSharedFlag(2);
   }

   public boolean isSneaking() {
      return this.getSharedFlag(1);
   }

   public void setSneaking(boolean var1) {
      this.setSharedFlag(1, var1);
   }

   protected boolean getSharedFlag(int var1) {
      return (this.entityData.getByte(0) & 1 << var1) != 0;
   }

   protected void setSharedFlag(int var1, boolean var2) {
      byte var3 = this.entityData.getByte(0);
      if (var2) {
         this.entityData.set(0, (byte)(var3 | 1 << var1));
      } else {
         this.entityData.set(0, (byte)(var3 & ~(1 << var1)));
      }

   }
}
