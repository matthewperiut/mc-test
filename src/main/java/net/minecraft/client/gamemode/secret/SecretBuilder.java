package net.minecraft.client.gamemode.secret;

import com.mojang.nbt.CompoundTag;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;

public class SecretBuilder extends Animal {
   public static final int SWING_DURATION = 8;
   public Minecraft minecraft;
   public boolean swinging = false;
   public boolean shouldSwing = false;
   public int swingTime = 0;
   public State state;
   public SecretBuilderInventory inventory;

   public SecretBuilder(Level var1) {
      super(var1);
      this.state = SecretBuilder.State.ROAMING;
      this.inventory = new SecretBuilderInventory();
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
      System.out.println("Our block is: " + this.minecraft.level.getTile(-34, 74, 0));
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
      super.updateAi();
   }

   public ItemInstance getCarriedItem() {
      return this.inventory.getSelected();
   }

   public void aiStep() {
      this.attackTarget = this.minecraft.player;
      if (!this.swinging && this.shouldSwing) {
         this.swing();
      }

      super.aiStep();
   }

   public static enum State {
      BUILDING,
      MOVING,
      ROAMING;

      private State() {
      }
   }
}
