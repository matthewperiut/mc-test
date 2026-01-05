package net.minecraft.world.item;

import net.minecraft.world.entity.item.Boat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import util.Mth;

public class BoatItem extends Item {
   public BoatItem(int var1) {
      super(var1);
      this.maxStackSize = 1;
   }

   public ItemInstance use(ItemInstance var1, Level var2, Player var3) {
      float var4 = 1.0F;
      float var5 = var3.xRotO + (var3.xRot - var3.xRotO) * var4;
      float var6 = var3.yRotO + (var3.yRot - var3.yRotO) * var4;
      double var7 = var3.xo + (var3.x - var3.xo) * (double)var4;
      double var9 = var3.yo + (var3.y - var3.yo) * (double)var4 + 1.62 - (double)var3.heightOffset;
      double var11 = var3.zo + (var3.z - var3.zo) * (double)var4;
      Vec3 var13 = Vec3.newTemp(var7, var9, var11);
      float var14 = Mth.cos(-var6 * ((float)Math.PI / 180F) - (float)Math.PI);
      float var15 = Mth.sin(-var6 * ((float)Math.PI / 180F) - (float)Math.PI);
      float var16 = -Mth.cos(-var5 * ((float)Math.PI / 180F));
      float var17 = Mth.sin(-var5 * ((float)Math.PI / 180F));
      float var18 = var15 * var16;
      float var20 = var14 * var16;
      double var21 = (double)5.0F;
      Vec3 var23 = var13.add((double)var18 * var21, (double)var17 * var21, (double)var20 * var21);
      HitResult var24 = var2.clip(var13, var23, true);
      if (var24 == null) {
         return var1;
      } else {
         if (var24.type == HitResult.Type.TILE) {
            int var25 = var24.x;
            int var26 = var24.y;
            int var27 = var24.z;
            if (!var2.isOnline) {
               var2.addEntity(new Boat(var2, (double)((float)var25 + 0.5F), (double)((float)var26 + 1.5F), (double)((float)var27 + 0.5F)));
            }

            --var1.count;
         }

         return var1;
      }
   }
}
