package net.minecraft.world.item;

import com.mojang.nbt.CompoundTag;
import net.minecraft.locale.Descriptive;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public final class ItemInstance implements Descriptive<ItemInstance> {
   public int count;
   public int popTime;
   public int id;
   private int auxValue;

   public ItemInstance(Tile var1) {
      this((Tile)var1, 1);
   }

   public ItemInstance(Tile var1, int var2) {
      this(var1.id, var2, 0);
   }

   public ItemInstance(Tile var1, int var2, int var3) {
      this(var1.id, var2, var3);
   }

   public ItemInstance(Item var1) {
      this(var1.id, 1, 0);
   }

   public ItemInstance(Item var1, int var2) {
      this(var1.id, var2, 0);
   }

   public ItemInstance(Item var1, int var2, int var3) {
      this(var1.id, var2, var3);
   }

   public ItemInstance(int var1, int var2, int var3) {
      this.count = 0;
      this.id = var1;
      this.count = var2;
      this.auxValue = var3;
   }

   public ItemInstance(CompoundTag var1) {
      this.count = 0;
      this.load(var1);
   }

   public ItemInstance remove(int var1) {
      this.count -= var1;
      return new ItemInstance(this.id, var1, this.auxValue);
   }

   public Item getItem() {
      return Item.items[this.id];
   }

   public int getIcon() {
      return this.getItem().getIcon(this);
   }

   public boolean useOn(Player var1, Level var2, int var3, int var4, int var5, int var6) {
      return this.getItem().useOn(this, var1, var2, var3, var4, var5, var6);
   }

   public float getDestroySpeed(Tile var1) {
      return this.getItem().getDestroySpeed(this, var1);
   }

   public ItemInstance use(Level var1, Player var2) {
      return this.getItem().use(this, var1, var2);
   }

   public CompoundTag save(CompoundTag var1) {
      var1.putShort("id", (short)this.id);
      var1.putByte("Count", (byte)this.count);
      var1.putShort("Damage", (short)this.auxValue);
      return var1;
   }

   public void load(CompoundTag var1) {
      this.id = var1.getShort("id");
      this.count = var1.getByte("Count");
      this.auxValue = var1.getShort("Damage");
   }

   public int getMaxStackSize() {
      return this.getItem().getMaxStackSize();
   }

   public boolean isStackable() {
      return this.getMaxStackSize() > 1 && (!this.isDamageableItem() || !this.isDamaged());
   }

   public boolean isDamageableItem() {
      return Item.items[this.id].getMaxDamage() > 0;
   }

   public boolean isStackedByData() {
      return Item.items[this.id].isStackedByData();
   }

   public boolean isDamaged() {
      return this.isDamageableItem() && this.auxValue > 0;
   }

   public int getDamageValue() {
      return this.auxValue;
   }

   public int getAuxValue() {
      return this.auxValue;
   }

   public int getMaxDamage() {
      return Item.items[this.id].getMaxDamage();
   }

   public void hurt(int var1) {
      if (this.isDamageableItem()) {
         this.auxValue += var1;
         if (this.auxValue > this.getMaxDamage()) {
            --this.count;
            if (this.count < 0) {
               this.count = 0;
            }

            this.auxValue = 0;
         }

      }
   }

   public void hurtEnemy(Mob var1) {
      Item.items[this.id].hurtEnemy(this, var1);
   }

   public void mineBlock(int var1, int var2, int var3, int var4) {
      Item.items[this.id].mineBlock(this, var1, var2, var3, var4);
   }

   public int getAttackDamage(Entity var1) {
      return Item.items[this.id].getAttackDamage(var1);
   }

   public boolean canDestroySpecial(Tile var1) {
      return Item.items[this.id].canDestroySpecial(var1);
   }

   public void snap(Player var1) {
   }

   public void interactEnemy(Mob var1) {
      Item.items[this.id].interactEnemy(this, var1);
   }

   public ItemInstance copy() {
      return new ItemInstance(this.id, this.count, this.auxValue);
   }

   public static boolean matches(ItemInstance var0, ItemInstance var1) {
      if (var0 == null && var1 == null) {
         return true;
      } else {
         return var0 != null && var1 != null ? var0.matches(var1) : false;
      }
   }

   private boolean matches(ItemInstance var1) {
      if (this.count != var1.count) {
         return false;
      } else if (this.id != var1.id) {
         return false;
      } else {
         return this.auxValue == var1.auxValue;
      }
   }

   public boolean sameItem(ItemInstance var1) {
      return this.id == var1.id && this.auxValue == var1.auxValue;
   }

   public String getDescriptionId() {
      return Item.items[this.id].getDescriptionId(this);
   }

   public ItemInstance setDescriptionId(String var1) {
      return this;
   }

   public static ItemInstance clone(ItemInstance var0) {
      return var0 == null ? null : var0.copy();
   }

   public String toString() {
      return this.count + "x" + Item.items[this.id].getDescriptionId() + "@" + this.auxValue;
   }
}
