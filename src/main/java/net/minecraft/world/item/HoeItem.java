package net.minecraft.world.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;

public class HoeItem extends Item {
   public HoeItem(int var1, Item.Tier var2) {
      super(var1);
      this.maxStackSize = 1;
      this.maxDamage = var2.getUses();
   }

   public boolean useOn(ItemInstance var1, Player var2, Level var3, int var4, int var5, int var6, int var7) {
      int var8 = var3.getTile(var4, var5, var6);
      Material var9 = var3.getMaterial(var4, var5 + 1, var6);
      if ((var9.isSolid() || var8 != Tile.grass.id) && var8 != Tile.dirt.id) {
         return false;
      } else {
         Tile var10 = Tile.farmland;
         var3.playSound((double)((float)var4 + 0.5F), (double)((float)var5 + 0.5F), (double)((float)var6 + 0.5F), var10.soundType.getStepSound(), (var10.soundType.getVolume() + 1.0F) / 2.0F, var10.soundType.getPitch() * 0.8F);
         if (var3.isOnline) {
            return true;
         } else {
            var3.setTile(var4, var5, var6, var10.id);
            var1.hurt(1);
            if (var3.random.nextInt(8) == 0 && var8 == Tile.grass.id) {
               byte var11 = 1;

               for(int var12 = 0; var12 < var11; ++var12) {
                  float var13 = 0.7F;
                  float var14 = var3.random.nextFloat() * var13 + (1.0F - var13) * 0.5F;
                  float var15 = 1.2F;
                  float var16 = var3.random.nextFloat() * var13 + (1.0F - var13) * 0.5F;
                  ItemEntity var17 = new ItemEntity(var3, (double)((float)var4 + var14), (double)((float)var5 + var15), (double)((float)var6 + var16), new ItemInstance(Item.seeds));
                  var17.throwTime = 10;
                  var3.addEntity(var17);
               }
            }

            return true;
         }
      }
   }

   public boolean isHandEquipped() {
      return true;
   }
}
