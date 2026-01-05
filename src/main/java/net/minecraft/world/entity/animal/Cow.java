package net.minecraft.world.entity.animal;

import com.mojang.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;

public class Cow extends Animal {
   public Cow(Level var1) {
      super(var1);
      this.textureName = "/mob/cow.png";
      this.setSize(0.9F, 1.3F);
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      super.addAdditonalSaveData(var1);
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
   }

   protected String getAmbientSound() {
      return "mob.cow";
   }

   protected String getHurtSound() {
      return "mob.cowhurt";
   }

   protected String getDeathSound() {
      return "mob.cowhurt";
   }

   protected float getSoundVolume() {
      return 0.4F;
   }

   protected int getDeathLoot() {
      return Item.leather.id;
   }

   public boolean interact(Player var1) {
      ItemInstance var2 = var1.inventory.getSelected();
      if (var2 != null && var2.id == Item.bucket_empty.id) {
         var1.inventory.setItem(var1.inventory.selected, new ItemInstance(Item.milk));
         return true;
      } else {
         return false;
      }
   }
}
