package net.minecraft.world.entity;

import com.mojang.nbt.CompoundTag;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import util.Mth;

public abstract class Mob extends Entity {
   public static final int ATTACK_DURATION = 5;
   public int invulnerableDuration = 20;
   public float timeOffs;
   public float rotA;
   public float yBodyRot = 0.0F;
   public float yBodyRotO = 0.0F;
   protected float oRun;
   protected float run;
   protected float animStep;
   protected float animStepO;
   protected boolean hasHair = true;
   protected String textureName = "/mob/char.png";
   protected boolean allowAlpha = true;
   protected float rotOffs = 0.0F;
   protected String modelName = null;
   protected float bobStrength = 1.0F;
   protected int deathScore = 0;
   protected float renderOffset = 0.0F;
   public boolean interpolateOnly = false;
   public float oAttackAnim;
   public float attackAnim;
   public int health = 10;
   public int lastHealth;
   private int ambientSoundTime;
   public int hurtTime;
   public int hurtDuration;
   public float hurtDir = 0.0F;
   public int deathTime = 0;
   public int attackTime = 0;
   public float oTilt;
   public float tilt;
   protected boolean dead = false;
   public int modelNum = -1;
   public float animSpeed = (float)(Math.random() * (double)0.9F + (double)0.1F);
   public float walkAnimSpeedO;
   public float walkAnimSpeed;
   public float walkAnimPos;
   protected int lSteps;
   protected double lx;
   protected double ly;
   protected double lz;
   protected double lyr;
   protected double lxr;
   float fallTime = 0.0F;
   protected int lastHurt = 0;
   protected int noActionTime = 0;
   protected float xxa;
   protected float yya;
   protected float yRotA;
   protected boolean jumping = false;
   protected float defaultLookAngle = 0.0F;
   protected float runSpeed = 0.7F;
   private Entity lookingAt;
   private int lookTime = 0;

   public Mob(Level var1) {
      super(var1);
      this.blocksBuilding = true;
      this.rotA = (float)(Math.random() + (double)1.0F) * 0.01F;
      this.setPos(this.x, this.y, this.z);
      this.timeOffs = (float)Math.random() * 12398.0F;
      this.yRot = (float)(Math.random() * (double)(float)Math.PI * (double)2.0F);
      this.footSize = 0.5F;
   }

   protected void defineSynchedData() {
   }

   public boolean canSee(Entity var1) {
      return this.level.clip(Vec3.newTemp(this.x, this.y + (double)this.getHeadHeight(), this.z), Vec3.newTemp(var1.x, var1.y + (double)var1.getHeadHeight(), var1.z)) == null;
   }

   public String getTexture() {
      return this.textureName;
   }

   public boolean isPickable() {
      return !this.removed;
   }

   public boolean isPushable() {
      return !this.removed;
   }

   public float getHeadHeight() {
      return this.bbHeight * 0.85F;
   }

   public int getAmbientSoundInterval() {
      return 80;
   }

   public void baseTick() {
      this.oAttackAnim = this.attackAnim;
      super.baseTick();
      if (this.random.nextInt(1000) < this.ambientSoundTime++) {
         this.ambientSoundTime = -this.getAmbientSoundInterval();
         String var1 = this.getAmbientSound();
         if (var1 != null) {
            this.level.playSound(this, var1, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
         }
      }

      if (this.isAlive() && this.isInWall()) {
         this.hurt((Entity)null, 1);
      }

      if (this.fireImmune || this.level.isOnline) {
         this.onFire = 0;
      }

      if (this.isAlive() && this.isUnderLiquid(Material.water) && !this.isWaterMob()) {
         --this.airSupply;
         if (this.airSupply == -20) {
            this.airSupply = 0;

            for(int var8 = 0; var8 < 8; ++var8) {
               float var2 = this.random.nextFloat() - this.random.nextFloat();
               float var3 = this.random.nextFloat() - this.random.nextFloat();
               float var4 = this.random.nextFloat() - this.random.nextFloat();
               this.level.addParticle("bubble", this.x + (double)var2, this.y + (double)var3, this.z + (double)var4, this.xd, this.yd, this.zd);
            }

            this.hurt((Entity)null, 2);
         }

         this.onFire = 0;
      } else {
         this.airSupply = this.airCapacity;
      }

      this.oTilt = this.tilt;
      if (this.attackTime > 0) {
         --this.attackTime;
      }

      if (this.hurtTime > 0) {
         --this.hurtTime;
      }

      if (this.invulnerableTime > 0) {
         --this.invulnerableTime;
      }

      if (this.health <= 0) {
         ++this.deathTime;
         if (this.deathTime > 20) {
            this.beforeRemove();
            this.remove();

            for(int var9 = 0; var9 < 20; ++var9) {
               double var10 = this.random.nextGaussian() * 0.02;
               double var11 = this.random.nextGaussian() * 0.02;
               double var6 = this.random.nextGaussian() * 0.02;
               this.level.addParticle("explode", this.x + (double)(this.random.nextFloat() * this.bbWidth * 2.0F) - (double)this.bbWidth, this.y + (double)(this.random.nextFloat() * this.bbHeight), this.z + (double)(this.random.nextFloat() * this.bbWidth * 2.0F) - (double)this.bbWidth, var10, var11, var6);
            }
         }
      }

      this.animStepO = this.animStep;
      this.yBodyRotO = this.yBodyRot;
      this.yRotO = this.yRot;
      this.xRotO = this.xRot;
   }

   public void spawnAnim() {
      for(int var1 = 0; var1 < 20; ++var1) {
         double var2 = this.random.nextGaussian() * 0.02;
         double var4 = this.random.nextGaussian() * 0.02;
         double var6 = this.random.nextGaussian() * 0.02;
         double var8 = (double)10.0F;
         this.level.addParticle("explode", this.x + (double)(this.random.nextFloat() * this.bbWidth * 2.0F) - (double)this.bbWidth - var2 * var8, this.y + (double)(this.random.nextFloat() * this.bbHeight) - var4 * var8, this.z + (double)(this.random.nextFloat() * this.bbWidth * 2.0F) - (double)this.bbWidth - var6 * var8, var2, var4, var6);
      }

   }

   public void rideTick() {
      super.rideTick();
      this.oRun = this.run;
      this.run = 0.0F;
   }

   public void lerpTo(double var1, double var3, double var5, float var7, float var8, int var9) {
      this.heightOffset = 0.0F;
      this.lx = var1;
      this.ly = var3;
      this.lz = var5;
      this.lyr = (double)var7;
      this.lxr = (double)var8;
      this.lSteps = var9;
   }

   public void superTick() {
      super.tick();
   }

   public void tick() {
      super.tick();
      this.aiStep();
      double var1 = this.x - this.xo;
      double var3 = this.z - this.zo;
      float var5 = Mth.sqrt(var1 * var1 + var3 * var3);
      float var6 = this.yBodyRot;
      float var7 = 0.0F;
      this.oRun = this.run;
      float var8 = 0.0F;
      if (!(var5 <= 0.05F)) {
         var8 = 1.0F;
         var7 = var5 * 3.0F;
         var6 = (float)Math.atan2(var3, var1) * 180.0F / (float)Math.PI - 90.0F;
      }

      if (this.attackAnim > 0.0F) {
         var6 = this.yRot;
      }

      if (!this.onGround) {
         var8 = 0.0F;
      }

      this.run += (var8 - this.run) * 0.3F;

      float var9;
      for(var9 = var6 - this.yBodyRot; var9 < -180.0F; var9 += 360.0F) {
      }

      while(var9 >= 180.0F) {
         var9 -= 360.0F;
      }

      this.yBodyRot += var9 * 0.3F;

      float var10;
      for(var10 = this.yRot - this.yBodyRot; var10 < -180.0F; var10 += 360.0F) {
      }

      while(var10 >= 180.0F) {
         var10 -= 360.0F;
      }

      boolean var11 = var10 < -90.0F || var10 >= 90.0F;
      if (var10 < -75.0F) {
         var10 = -75.0F;
      }

      if (var10 >= 75.0F) {
         var10 = 75.0F;
      }

      this.yBodyRot = this.yRot - var10;
      if (var10 * var10 > 2500.0F) {
         this.yBodyRot += var10 * 0.2F;
      }

      if (var11) {
         var7 *= -1.0F;
      }

      while(this.yRot - this.yRotO < -180.0F) {
         this.yRotO -= 360.0F;
      }

      while(this.yRot - this.yRotO >= 180.0F) {
         this.yRotO += 360.0F;
      }

      while(this.yBodyRot - this.yBodyRotO < -180.0F) {
         this.yBodyRotO -= 360.0F;
      }

      while(this.yBodyRot - this.yBodyRotO >= 180.0F) {
         this.yBodyRotO += 360.0F;
      }

      while(this.xRot - this.xRotO < -180.0F) {
         this.xRotO -= 360.0F;
      }

      while(this.xRot - this.xRotO >= 180.0F) {
         this.xRotO += 360.0F;
      }

      this.animStep += var7;
   }

   protected void setSize(float var1, float var2) {
      super.setSize(var1, var2);
   }

   public void heal(int var1) {
      if (this.health > 0) {
         this.health += var1;
         if (this.health > 20) {
            this.health = 20;
         }

         this.invulnerableTime = this.invulnerableDuration / 2;
      }
   }

   public boolean hurt(Entity var1, int var2) {
      if (this.level.isOnline) {
         return false;
      } else {
         this.noActionTime = 0;
         if (this.health <= 0) {
            return false;
         } else {
            this.walkAnimSpeed = 1.5F;
            boolean var3 = true;
            if ((float)this.invulnerableTime > (float)this.invulnerableDuration / 2.0F) {
               if (var2 <= this.lastHurt) {
                  return false;
               }

               this.actuallyHurt(var2 - this.lastHurt);
               this.lastHurt = var2;
               var3 = false;
            } else {
               this.lastHurt = var2;
               this.lastHealth = this.health;
               this.invulnerableTime = this.invulnerableDuration;
               this.actuallyHurt(var2);
               this.hurtTime = this.hurtDuration = 10;
            }

            this.hurtDir = 0.0F;
            if (var3) {
               this.level.broadcastEntityEvent(this, (byte)2);
               this.markHurt();
               if (var1 != null) {
                  double var4 = var1.x - this.x;

                  double var6;
                  for(var6 = var1.z - this.z; var4 * var4 + var6 * var6 < 1.0E-4; var6 = (Math.random() - Math.random()) * 0.01) {
                     var4 = (Math.random() - Math.random()) * 0.01;
                  }

                  this.hurtDir = (float)(Math.atan2(var6, var4) * (double)180.0F / (double)(float)Math.PI) - this.yRot;
                  this.knockback(var1, var2, var4, var6);
               } else {
                  this.hurtDir = (float)((int)(Math.random() * (double)2.0F) * 180);
               }
            }

            if (this.health <= 0) {
               if (var3) {
                  this.level.playSound(this, this.getDeathSound(), this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
               }

               this.die(var1);
            } else if (var3) {
               this.level.playSound(this, this.getHurtSound(), this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }

            return true;
         }
      }
   }

   public void animateHurt() {
      this.hurtTime = this.hurtDuration = 10;
      this.hurtDir = 0.0F;
   }

   protected void actuallyHurt(int var1) {
      this.health -= var1;
   }

   protected float getSoundVolume() {
      return 1.0F;
   }

   protected String getAmbientSound() {
      return null;
   }

   protected String getHurtSound() {
      return "random.hurt";
   }

   protected String getDeathSound() {
      return "random.hurt";
   }

   public void knockback(Entity var1, int var2, double var3, double var5) {
      float var7 = Mth.sqrt(var3 * var3 + var5 * var5);
      float var8 = 0.4F;
      this.xd /= (double)2.0F;
      this.yd /= (double)2.0F;
      this.zd /= (double)2.0F;
      this.xd -= var3 / (double)var7 * (double)var8;
      this.yd += (double)0.4F;
      this.zd -= var5 / (double)var7 * (double)var8;
      if (this.yd > (double)0.4F) {
         this.yd = (double)0.4F;
      }

   }

   public void die(Entity var1) {
      if (this.deathScore > 0 && var1 != null) {
         var1.awardKillScore(this, this.deathScore);
      }

      this.dead = true;
      if (!this.level.isOnline) {
         this.dropDeathLoot();
      }

      this.level.broadcastEntityEvent(this, (byte)3);
   }

   protected void dropDeathLoot() {
      int var1 = this.getDeathLoot();
      if (var1 > 0) {
         int var2 = this.random.nextInt(3);

         for(int var3 = 0; var3 < var2; ++var3) {
            this.spawnAtLocation(var1, 1);
         }
      }

   }

   protected int getDeathLoot() {
      return 0;
   }

   protected void causeFallDamage(float var1) {
      int var2 = (int)Math.ceil((double)(var1 - 3.0F));
      if (var2 > 0) {
         this.hurt((Entity)null, var2);
         int var3 = this.level.getTile(Mth.floor(this.x), Mth.floor(this.y - (double)0.2F - (double)this.heightOffset), Mth.floor(this.z));
         if (var3 > 0) {
            Tile.SoundType var4 = Tile.tiles[var3].soundType;
            this.level.playSound(this, var4.getStepSound(), var4.getVolume() * 0.5F, var4.getPitch() * 0.75F);
         }
      }

   }

   public void travel(float var1, float var2) {
      if (this.isInWater()) {
         double var3 = this.y;
         this.moveRelative(var1, var2, 0.02F);
         this.move(this.xd, this.yd, this.zd);
         this.xd *= (double)0.8F;
         this.yd *= (double)0.8F;
         this.zd *= (double)0.8F;
         this.yd -= 0.02;
         if (this.horizontalCollision && this.isFree(this.xd, this.yd + (double)0.6F - this.y + var3, this.zd)) {
            this.yd = (double)0.3F;
         }
      } else if (this.isInLava()) {
         double var8 = this.y;
         this.moveRelative(var1, var2, 0.02F);
         this.move(this.xd, this.yd, this.zd);
         this.xd *= (double)0.5F;
         this.yd *= (double)0.5F;
         this.zd *= (double)0.5F;
         this.yd -= 0.02;
         if (this.horizontalCollision && this.isFree(this.xd, this.yd + (double)0.6F - this.y + var8, this.zd)) {
            this.yd = (double)0.3F;
         }
      } else {
         float var9 = 0.91F;
         if (this.onGround) {
            var9 = 0.54600006F;
            int var4 = this.level.getTile(Mth.floor(this.x), Mth.floor(this.bb.y0) - 1, Mth.floor(this.z));
            if (var4 > 0) {
               var9 = Tile.tiles[var4].friction * 0.91F;
            }
         }

         float var12 = 0.16277136F / (var9 * var9 * var9);
         this.moveRelative(var1, var2, this.onGround ? 0.1F * var12 : 0.02F);
         var9 = 0.91F;
         if (this.onGround) {
            var9 = 0.54600006F;
            int var5 = this.level.getTile(Mth.floor(this.x), Mth.floor(this.bb.y0) - 1, Mth.floor(this.z));
            if (var5 > 0) {
               var9 = Tile.tiles[var5].friction * 0.91F;
            }
         }

         if (this.onLadder()) {
            this.fallDistance = 0.0F;
            if (this.yd < -0.15) {
               this.yd = -0.15;
            }
         }

         this.move(this.xd, this.yd, this.zd);
         if (this.horizontalCollision && this.onLadder()) {
            this.yd = 0.2;
         }

         this.yd -= 0.08;
         this.yd *= (double)0.98F;
         this.xd *= (double)var9;
         this.zd *= (double)var9;
      }

      this.walkAnimSpeedO = this.walkAnimSpeed;
      double var11 = this.x - this.xo;
      double var13 = this.z - this.zo;
      float var7 = Mth.sqrt(var11 * var11 + var13 * var13) * 4.0F;
      if (var7 > 1.0F) {
         var7 = 1.0F;
      }

      this.walkAnimSpeed += (var7 - this.walkAnimSpeed) * 0.4F;
      this.walkAnimPos += this.walkAnimSpeed;
   }

   public boolean onLadder() {
      int var1 = Mth.floor(this.x);
      int var2 = Mth.floor(this.bb.y0);
      int var3 = Mth.floor(this.z);
      return this.level.getTile(var1, var2, var3) == Tile.ladder.id || this.level.getTile(var1, var2 + 1, var3) == Tile.ladder.id;
   }

   public boolean isShootable() {
      return true;
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      var1.putShort("Health", (short)this.health);
      var1.putShort("HurtTime", (short)this.hurtTime);
      var1.putShort("DeathTime", (short)this.deathTime);
      var1.putShort("AttackTime", (short)this.attackTime);
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      this.health = var1.getShort("Health");
      if (!var1.contains("Health")) {
         this.health = 10;
      }

      this.hurtTime = var1.getShort("HurtTime");
      this.deathTime = var1.getShort("DeathTime");
      this.attackTime = var1.getShort("AttackTime");
   }

   public boolean isAlive() {
      return !this.removed && this.health > 0;
   }

   public boolean isWaterMob() {
      return false;
   }

   public void aiStep() {
      if (this.lSteps > 0) {
         double var1 = this.x + (this.lx - this.x) / (double)this.lSteps;
         double var3 = this.y + (this.ly - this.y) / (double)this.lSteps;
         double var5 = this.z + (this.lz - this.z) / (double)this.lSteps;

         double var7;
         for(var7 = this.lyr - (double)this.yRot; var7 < (double)-180.0F; var7 += (double)360.0F) {
         }

         while(var7 >= (double)180.0F) {
            var7 -= (double)360.0F;
         }

         this.yRot = (float)((double)this.yRot + var7 / (double)this.lSteps);
         this.xRot = (float)((double)this.xRot + (this.lxr - (double)this.xRot) / (double)this.lSteps);
         --this.lSteps;
         this.setPos(var1, var3, var5);
         this.setRot(this.yRot, this.xRot);
      }

      if (this.health <= 0) {
         this.jumping = false;
         this.xxa = 0.0F;
         this.yya = 0.0F;
         this.yRotA = 0.0F;
      } else if (!this.interpolateOnly) {
         this.updateAi();
      }

      boolean var9 = this.isInWater();
      boolean var2 = this.isInLava();
      if (this.jumping) {
         if (var9) {
            this.yd += (double)0.04F;
         } else if (var2) {
            this.yd += (double)0.04F;
         } else if (this.onGround) {
            this.jumpFromGround();
         }
      }

      this.xxa *= 0.98F;
      this.yya *= 0.98F;
      this.yRotA *= 0.9F;
      this.travel(this.xxa, this.yya);
      List var10 = this.level.getEntities(this, this.bb.grow((double)0.2F, (double)0.0F, (double)0.2F));
      if (var10 != null && var10.size() > 0) {
         for(int var4 = 0; var4 < var10.size(); ++var4) {
            Entity var11 = (Entity)var10.get(var4);
            if (var11.isPushable()) {
               var11.push(this);
            }
         }
      }

   }

   protected void jumpFromGround() {
      this.yd = (double)0.42F;
   }

   protected void updateAi() {
      ++this.noActionTime;
      Player var1 = this.level.getNearestPlayer(this, (double)-1.0F);
      if (var1 != null) {
         double var2 = var1.x - this.x;
         double var4 = var1.y - this.y;
         double var6 = var1.z - this.z;
         double var8 = var2 * var2 + var4 * var4 + var6 * var6;
         if (var8 > (double)16384.0F) {
            this.remove();
         }

         if (this.noActionTime > 600 && this.random.nextInt(800) == 0) {
            if (var8 < (double)1024.0F) {
               this.noActionTime = 0;
            } else {
               this.remove();
            }
         }
      }

      this.xxa = 0.0F;
      this.yya = 0.0F;
      float var11 = 8.0F;
      if (this.random.nextFloat() < 0.02F) {
         var1 = this.level.getNearestPlayer(this, (double)var11);
         if (var1 != null) {
            this.lookingAt = var1;
            this.lookTime = 10 + this.random.nextInt(20);
         } else {
            this.yRotA = (this.random.nextFloat() - 0.5F) * 20.0F;
         }
      }

      if (this.lookingAt != null) {
         this.lookAt(this.lookingAt, 10.0F);
         if (this.lookTime-- <= 0 || this.lookingAt.removed || this.lookingAt.distanceToSqr(this) > (double)(var11 * var11)) {
            this.lookingAt = null;
         }
      } else {
         if (this.random.nextFloat() < 0.05F) {
            this.yRotA = (this.random.nextFloat() - 0.5F) * 20.0F;
         }

         this.yRot += this.yRotA;
         this.xRot = this.defaultLookAngle;
      }

      boolean var3 = this.isInWater();
      boolean var12 = this.isInLava();
      if (var3 || var12) {
         this.jumping = this.random.nextFloat() < 0.8F;
      }

   }

   public void lookAt(Entity var1, float var2) {
      double var3 = var1.x - this.x;
      double var7 = var1.z - this.z;
      double var5;
      if (var1 instanceof Mob) {
         Mob var9 = (Mob)var1;
         var5 = var9.y + (double)var9.getHeadHeight() - (this.y + (double)this.getHeadHeight());
      } else {
         var5 = (var1.bb.y0 + var1.bb.y1) / (double)2.0F - (this.y + (double)this.getHeadHeight());
      }

      double var13 = (double)Mth.sqrt(var3 * var3 + var7 * var7);
      float var11 = (float)(Math.atan2(var7, var3) * (double)180.0F / (double)(float)Math.PI) - 90.0F;
      float var12 = (float)(Math.atan2(var5, var13) * (double)180.0F / (double)(float)Math.PI);
      this.xRot = -this.rotlerp(this.xRot, var12, var2);
      this.yRot = this.rotlerp(this.yRot, var11, var2);
   }

   private float rotlerp(float var1, float var2, float var3) {
      float var4;
      for(var4 = var2 - var1; var4 < -180.0F; var4 += 360.0F) {
      }

      while(var4 >= 180.0F) {
         var4 -= 360.0F;
      }

      if (var4 > var3) {
         var4 = var3;
      }

      if (var4 < -var3) {
         var4 = -var3;
      }

      return var1 + var4;
   }

   public void beforeRemove() {
   }

   public boolean canSpawn() {
      return this.level.isUnobstructed(this.bb) && this.level.getCubes(this, this.bb).size() == 0 && !this.level.containsAnyLiquid(this.bb);
   }

   protected void outOfWorld() {
      this.hurt((Entity)null, 4);
   }

   public float getAttackAnim(float var1) {
      float var2 = this.attackAnim - this.oAttackAnim;
      if (var2 < 0.0F) {
         ++var2;
      }

      return this.oAttackAnim + var2 * var1;
   }

   public Vec3 getPos(float var1) {
      if (var1 == 1.0F) {
         return Vec3.newTemp(this.x, this.y, this.z);
      } else {
         double var2 = this.xo + (this.x - this.xo) * (double)var1;
         double var4 = this.yo + (this.y - this.yo) * (double)var1;
         double var6 = this.zo + (this.z - this.zo) * (double)var1;
         return Vec3.newTemp(var2, var4, var6);
      }
   }

   public Vec3 getLookAngle() {
      return this.getViewVector(1.0F);
   }

   public Vec3 getViewVector(float var1) {
      if (var1 == 1.0F) {
         float var8 = Mth.cos(-this.yRot * ((float)Math.PI / 180F) - (float)Math.PI);
         float var9 = Mth.sin(-this.yRot * ((float)Math.PI / 180F) - (float)Math.PI);
         float var10 = -Mth.cos(-this.xRot * ((float)Math.PI / 180F));
         float var11 = Mth.sin(-this.xRot * ((float)Math.PI / 180F));
         return Vec3.newTemp((double)(var9 * var10), (double)var11, (double)(var8 * var10));
      } else {
         float var2 = this.xRotO + (this.xRot - this.xRotO) * var1;
         float var3 = this.yRotO + (this.yRot - this.yRotO) * var1;
         float var4 = Mth.cos(-var3 * ((float)Math.PI / 180F) - (float)Math.PI);
         float var5 = Mth.sin(-var3 * ((float)Math.PI / 180F) - (float)Math.PI);
         float var6 = -Mth.cos(-var2 * ((float)Math.PI / 180F));
         float var7 = Mth.sin(-var2 * ((float)Math.PI / 180F));
         return Vec3.newTemp((double)(var5 * var6), (double)var7, (double)(var4 * var6));
      }
   }

   public HitResult pick(double var1, float var3) {
      Vec3 var4 = this.getPos(var3);
      Vec3 var5 = this.getViewVector(var3);
      Vec3 var6 = var4.add(var5.x * var1, var5.y * var1, var5.z * var1);
      return this.level.clip(var4, var6);
   }

   public int getMaxSpawnClusterSize() {
      return 4;
   }

   public ItemInstance getCarriedItem() {
      return null;
   }

   public void handleEntityEvent(byte var1) {
      if (var1 == 2) {
         this.walkAnimSpeed = 1.5F;
         this.invulnerableTime = this.invulnerableDuration;
         this.hurtTime = this.hurtDuration = 10;
         this.hurtDir = 0.0F;
         this.level.playSound(this, this.getHurtSound(), this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
         this.hurt((Entity)null, 0);
      } else if (var1 == 3) {
         this.level.playSound(this, this.getDeathSound(), this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
         this.health = 0;
         this.die((Entity)null);
      } else {
         super.handleEntityEvent(var1);
      }

   }
}
