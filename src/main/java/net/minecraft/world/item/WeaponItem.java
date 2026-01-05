package net.minecraft.world.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.tile.Tile;

public class WeaponItem extends Item {
   private int damage;

   public WeaponItem(int var1, Item.Tier var2) {
      super(var1);
      this.maxStackSize = 1;
      this.maxDamage = var2.getUses();
      this.damage = 4 + var2.getAttackDamageBonus() * 2;
   }

   public float getDestroySpeed(ItemInstance var1, Tile var2) {
      return 1.5F;
   }

   public void hurtEnemy(ItemInstance var1, Mob var2) {
      var1.hurt(1);
   }

   public void mineBlock(ItemInstance var1, int var2, int var3, int var4, int var5) {
      var1.hurt(2);
   }

   public int getAttackDamage(Entity var1) {
      return this.damage;
   }

   public boolean isHandEquipped() {
      return true;
   }
}
