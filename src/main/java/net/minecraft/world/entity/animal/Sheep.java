package net.minecraft.world.entity.animal;

import com.mojang.nbt.CompoundTag;
import java.util.Random;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class Sheep extends Animal {
   private static final int DATA_WOOL_ID = 16;
   public static final float[][] COLOR = new float[][]{{1.0F, 1.0F, 1.0F}, {0.95F, 0.7F, 0.2F}, {0.9F, 0.5F, 0.85F}, {0.6F, 0.7F, 0.95F}, {0.9F, 0.9F, 0.2F}, {0.5F, 0.8F, 0.1F}, {0.95F, 0.7F, 0.8F}, {0.3F, 0.3F, 0.3F}, {0.6F, 0.6F, 0.6F}, {0.3F, 0.6F, 0.7F}, {0.7F, 0.4F, 0.9F}, {0.2F, 0.4F, 0.8F}, {0.5F, 0.4F, 0.3F}, {0.4F, 0.5F, 0.2F}, {0.8F, 0.3F, 0.3F}, {0.1F, 0.1F, 0.1F}};

   public Sheep(Level var1) {
      super(var1);
      this.textureName = "/mob/sheep.png";
      this.setSize(0.9F, 1.3F);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(16, new Byte((byte)0));
   }

   public boolean hurt(Entity var1, int var2) {
      if (!this.level.isOnline && !this.isSheared() && var1 instanceof Mob) {
         this.setSheared(true);
         int var3 = 1 + this.random.nextInt(3);

         for(int var4 = 0; var4 < var3; ++var4) {
            ItemEntity var5 = this.spawnAtLocation(new ItemInstance(Tile.cloth.id, 1, this.getColor()), 1.0F);
            var5.yd += (double)(this.random.nextFloat() * 0.05F);
            var5.xd += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F);
            var5.zd += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F);
         }
      }

      return super.hurt(var1, var2);
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      super.addAdditonalSaveData(var1);
      var1.putBoolean("Sheared", this.isSheared());
      var1.putByte("Color", (byte)this.getColor());
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
      this.setSheared(var1.getBoolean("Sheared"));
      this.setColor(var1.getByte("Color"));
   }

   protected String getAmbientSound() {
      return "mob.sheep";
   }

   protected String getHurtSound() {
      return "mob.sheep";
   }

   protected String getDeathSound() {
      return "mob.sheep";
   }

   public int getColor() {
      return this.entityData.getByte(16) & 15;
   }

   public void setColor(int var1) {
      byte var2 = this.entityData.getByte(16);
      this.entityData.set(16, (byte)(var2 & 240 | var1 & 15));
   }

   public boolean isSheared() {
      return (this.entityData.getByte(16) & 16) != 0;
   }

   public void setSheared(boolean var1) {
      byte var2 = this.entityData.getByte(16);
      if (var1) {
         this.entityData.set(16, (byte)(var2 | 16));
      } else {
         this.entityData.set(16, (byte)(var2 & -17));
      }

   }

   public static int getSheepColor(Random var0) {
      int var1 = var0.nextInt(100);
      if (var1 < 5) {
         return 15;
      } else if (var1 < 10) {
         return 7;
      } else {
         return var1 < 15 ? 8 : 0;
      }
   }
}
