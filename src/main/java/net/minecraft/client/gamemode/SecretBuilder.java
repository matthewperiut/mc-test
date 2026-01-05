package net.minecraft.client.gamemode;

import com.mojang.nbt.CompoundTag;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

class SecretBuilder extends Animal {
   public static final int SWING_DURATION = 8;
   public Minecraft minecraft;
   public boolean swinging = false;
   public boolean shouldSwing = true;
   public int swingTime = 0;
   public SecretBuilderInventory inventory = new SecretBuilderInventory();

   public SecretBuilder(Level var1) {
      super(var1);
      this.textureName = "/mob/char.png";
      this.modelName = "humanoid";
      this.setSize(0.6F, 1.8F);
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      super.addAdditonalSaveData(var1);
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
   }

   public void swing() {
      this.swingTime = -1;
      this.swinging = true;
   }

   protected void updateAi() {
      super.updateAi();
      if (this.swinging) {
         ++this.swingTime;
         if (this.swingTime == 8) {
            this.swingTime = 0;
            this.swinging = false;
         }
      } else {
         this.swingTime = 0;
      }

      this.attackAnim = (float)this.swingTime / 8.0F;
   }

   public void aiStep() {
      this.attackTarget = this.minecraft.player;
      if (!this.swinging && this.shouldSwing) {
         this.swing();
      }

      super.aiStep();
   }
}
