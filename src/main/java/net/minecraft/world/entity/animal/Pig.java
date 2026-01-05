package net.minecraft.world.entity.animal;

import com.mojang.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class Pig extends Animal {
   private static final int DATA_SADDLE_ID = 16;

   public Pig(Level var1) {
      super(var1);
      this.textureName = "/mob/pig.png";
      this.setSize(0.9F, 0.9F);
   }

   protected void defineSynchedData() {
      this.entityData.define(16, (byte)0);
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      super.addAdditonalSaveData(var1);
      var1.putBoolean("Saddle", this.hasSaddle());
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
      this.setSaddle(var1.getBoolean("Saddle"));
   }

   protected String getAmbientSound() {
      return "mob.pig";
   }

   protected String getHurtSound() {
      return "mob.pig";
   }

   protected String getDeathSound() {
      return "mob.pigdeath";
   }

   public boolean interact(Player var1) {
      if (!this.hasSaddle() || this.level.isOnline || this.rider != null && this.rider != var1) {
         return false;
      } else {
         var1.ride(this);
         return true;
      }
   }

   protected int getDeathLoot() {
      return Item.porkChop_raw.id;
   }

   public boolean hasSaddle() {
      return (this.entityData.getByte(16) & 1) != 0;
   }

   public void setSaddle(boolean var1) {
      if (var1) {
         this.entityData.set(16, (byte)1);
      } else {
         this.entityData.set(16, (byte)0);
      }

   }
}
