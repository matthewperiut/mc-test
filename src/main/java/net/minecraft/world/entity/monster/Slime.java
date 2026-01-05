package net.minecraft.world.entity.monster;

import com.mojang.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import util.Mth;

public class Slime extends Mob implements Enemy {
   public float squish;
   public float oSquish;
   private int jumpDelay = 0;
   public int size = 1;

   public Slime(Level var1) {
      super(var1);
      this.textureName = "/mob/slime.png";
      this.size = 1 << this.random.nextInt(3);
      this.heightOffset = 0.0F;
      this.jumpDelay = this.random.nextInt(20) + 10;
      this.setSize(this.size);
   }

   public void setSize(int var1) {
      this.size = var1;
      this.setSize(0.6F * (float)var1, 0.6F * (float)var1);
      this.health = var1 * var1;
      this.setPos(this.x, this.y, this.z);
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      super.addAdditonalSaveData(var1);
      var1.putInt("Size", this.size - 1);
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
      this.size = var1.getInt("Size") + 1;
   }

   public void tick() {
      this.oSquish = this.squish;
      boolean var1 = this.onGround;
      super.tick();
      if (this.onGround && !var1) {
         for(int var2 = 0; var2 < this.size * 8; ++var2) {
            float var3 = this.random.nextFloat() * (float)Math.PI * 2.0F;
            float var4 = this.random.nextFloat() * 0.5F + 0.5F;
            float var5 = Mth.sin(var3) * (float)this.size * 0.5F * var4;
            float var6 = Mth.cos(var3) * (float)this.size * 0.5F * var4;
            this.level.addParticle("slime", this.x + (double)var5, this.bb.y0, this.z + (double)var6, (double)0.0F, (double)0.0F, (double)0.0F);
         }

         if (this.size > 2) {
            this.level.playSound(this, "mob.slime", this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) / 0.8F);
         }

         this.squish = -0.5F;
      }

      this.squish *= 0.6F;
   }

   protected void updateAi() {
      Player var1 = this.level.getNearestPlayer(this, (double)16.0F);
      if (var1 != null) {
         this.lookAt(var1, 10.0F);
      }

      if (this.onGround && this.jumpDelay-- <= 0) {
         this.jumpDelay = this.random.nextInt(20) + 10;
         if (var1 != null) {
            this.jumpDelay /= 3;
         }

         this.jumping = true;
         if (this.size > 1) {
            this.level.playSound(this, "mob.slime", this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
         }

         this.squish = 1.0F;
         this.xxa = 1.0F - this.random.nextFloat() * 2.0F;
         this.yya = (float)(1 * this.size);
      } else {
         this.jumping = false;
         if (this.onGround) {
            this.xxa = this.yya = 0.0F;
         }
      }

   }

   public void remove() {
      if (this.size > 1 && this.health == 0) {
         for(int var1 = 0; var1 < 4; ++var1) {
            float var2 = ((float)(var1 % 2) - 0.5F) * (float)this.size / 4.0F;
            float var3 = ((float)(var1 / 2) - 0.5F) * (float)this.size / 4.0F;
            Slime var4 = new Slime(this.level);
            var4.setSize(this.size / 2);
            var4.moveTo(this.x + (double)var2, this.y + (double)0.5F, this.z + (double)var3, this.random.nextFloat() * 360.0F, 0.0F);
            this.level.addEntity(var4);
         }
      }

      super.remove();
   }

   public void playerTouch(Player var1) {
      if (this.size > 1 && this.canSee(var1) && (double)this.distanceTo(var1) < 0.6 * (double)this.size && var1.hurt(this, this.size)) {
         this.level.playSound(this, "mob.slimeattack", 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
      }

   }

   protected String getHurtSound() {
      return "mob.slime";
   }

   protected String getDeathSound() {
      return "mob.slime";
   }

   protected int getDeathLoot() {
      return this.size == 1 ? Item.slimeBall.id : 0;
   }

   public boolean canSpawn() {
      LevelChunk var1 = this.level.getChunkAt(Mth.floor(this.x), Mth.floor(this.z));
      return (this.size == 1 || this.level.difficulty > 0) && this.random.nextInt(10) == 0 && var1.getRandom(987234911L).nextInt(10) == 0 && this.y < (double)16.0F;
   }

   protected float getSoundVolume() {
      return 0.6F;
   }
}
