package net.minecraft.world.item;

public class ArmorItem extends Item {
   private static final int[] defensePerSlot = new int[]{3, 8, 6, 3};
   private static final int[] healthPerSlot = new int[]{11, 16, 15, 13};
   public final int tier;
   public final int slot;
   public final int defense;
   public final int icon;

   public ArmorItem(int var1, int var2, int var3, int var4) {
      super(var1);
      this.tier = var2;
      this.slot = var4;
      this.icon = var3;
      this.defense = defensePerSlot[var4];
      this.maxDamage = healthPerSlot[var4] * 3 << var2;
      this.maxStackSize = 1;
   }
}
