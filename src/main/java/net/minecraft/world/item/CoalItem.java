package net.minecraft.world.item;

public class CoalItem extends Item {
   public static final int STONE_COAL = 0;
   public static final int CHAR_COAL = 1;

   public CoalItem(int var1) {
      super(var1);
      this.setStackedByData(true);
      this.setMaxDamage(0);
   }

   public String getDescriptionId(ItemInstance var1) {
      return var1.getAuxValue() == 1 ? "item.charcoal" : "item.coal";
   }
}
