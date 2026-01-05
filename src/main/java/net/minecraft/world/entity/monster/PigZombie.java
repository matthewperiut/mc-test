package net.minecraft.world.entity.monster;

import com.mojang.nbt.CompoundTag;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;

public class PigZombie extends Zombie {
   private int angerTime = 0;
   private int playAngrySoundIn = 0;
   private static final ItemInstance sword;

   public PigZombie(Level var1) {
      super(var1);
      this.textureName = "/mob/pigzombie.png";
      this.runSpeed = 0.5F;
      this.attackDamage = 5;
      this.fireImmune = true;
   }

   public void tick() {
      this.runSpeed = this.attackTarget != null ? 0.95F : 0.5F;
      if (this.playAngrySoundIn > 0 && --this.playAngrySoundIn == 0) {
         this.level.playSound(this, "mob.zombiepig.zpigangry", this.getSoundVolume() * 2.0F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 1.8F);
      }

      super.tick();
   }

   public boolean canSpawn() {
      return this.level.difficulty > 0 && this.level.isUnobstructed(this.bb) && this.level.getCubes(this, this.bb).size() == 0 && !this.level.containsAnyLiquid(this.bb);
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      super.addAdditonalSaveData(var1);
      var1.putShort("Anger", (short)this.angerTime);
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
      this.angerTime = var1.getShort("Anger");
   }

   protected Entity findAttackTarget() {
      return this.angerTime == 0 ? null : super.findAttackTarget();
   }

   public void aiStep() {
      super.aiStep();
   }

   public boolean hurt(Entity var1, int var2) {
      if (var1 instanceof Player) {
         List var3 = this.level.getEntities(this, this.bb.grow((double)32.0F, (double)32.0F, (double)32.0F));

         for(int var4 = 0; var4 < var3.size(); ++var4) {
            Entity var5 = (Entity)var3.get(var4);
            if (var5 instanceof PigZombie) {
               PigZombie var6 = (PigZombie)var5;
               var6.alert(var1);
            }
         }

         this.alert(var1);
      }

      return super.hurt(var1, var2);
   }

   private void alert(Entity var1) {
      this.attackTarget = var1;
      this.angerTime = 400 + this.random.nextInt(400);
      this.playAngrySoundIn = this.random.nextInt(40);
   }

   protected String getAmbientSound() {
      return "mob.zombiepig.zpig";
   }

   protected String getHurtSound() {
      return "mob.zombiepig.zpighurt";
   }

   protected String getDeathSound() {
      return "mob.zombiepig.zpigdeath";
   }

   protected int getDeathLoot() {
      return Item.porkChop_cooked.id;
   }

   public ItemInstance getCarriedItem() {
      return sword;
   }

   static {
      sword = new ItemInstance(Item.sword_gold, 1);
   }
}
