package net.minecraft.world.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.tile.Tile;

public class DiggerItem extends Item {
   private Tile[] tiles;
   private float speed = 4.0F;
   private int attackDamage;
   protected Item.Tier tier;

   protected DiggerItem(int var1, int var2, Item.Tier var3, Tile[] var4) {
      super(var1);
      this.tier = var3;
      this.tiles = var4;
      this.maxStackSize = 1;
      this.maxDamage = var3.getUses();
      this.speed = var3.getSpeed();
      this.attackDamage = var2 + var3.getAttackDamageBonus();
   }

   public float getDestroySpeed(ItemInstance var1, Tile var2) {
      for(int var3 = 0; var3 < this.tiles.length; ++var3) {
         if (this.tiles[var3] == var2) {
            return this.speed;
         }
      }

      return 1.0F;
   }

   public void hurtEnemy(ItemInstance var1, Mob var2) {
      var1.hurt(2);
   }

   public void mineBlock(ItemInstance var1, int var2, int var3, int var4, int var5) {
      var1.hurt(1);
   }

   public int getAttackDamage(Entity var1) {
      return this.attackDamage;
   }

   public boolean isHandEquipped() {
      return true;
   }
}
