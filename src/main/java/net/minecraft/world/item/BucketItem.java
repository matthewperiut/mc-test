package net.minecraft.world.item;

import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import util.Mth;

public class BucketItem extends Item {
   private int content;

   public BucketItem(int var1, int var2) {
      super(var1);
      this.maxStackSize = 1;
      this.maxDamage = 64;
      this.content = var2;
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
      HitResult var24 = var2.clip(var13, var23, this.content == 0);
      if (var24 == null) {
         return var1;
      } else {
         if (var24.type == HitResult.Type.TILE) {
            int var25 = var24.x;
            int var26 = var24.y;
            int var27 = var24.z;
            if (!var2.mayInteract(var3, var25, var26, var27)) {
               return var1;
            }

            if (this.content == 0) {
               if (var2.getMaterial(var25, var26, var27) == Material.water && var2.getData(var25, var26, var27) == 0) {
                  var2.setTile(var25, var26, var27, 0);
                  return new ItemInstance(Item.bucket_water);
               }

               if (var2.getMaterial(var25, var26, var27) == Material.lava && var2.getData(var25, var26, var27) == 0) {
                  var2.setTile(var25, var26, var27, 0);
                  return new ItemInstance(Item.bucket_lava);
               }
            } else {
               if (this.content < 0) {
                  return new ItemInstance(Item.bucket_empty);
               }

               if (var24.f == 0) {
                  --var26;
               }

               if (var24.f == 1) {
                  ++var26;
               }

               if (var24.f == 2) {
                  --var27;
               }

               if (var24.f == 3) {
                  ++var27;
               }

               if (var24.f == 4) {
                  --var25;
               }

               if (var24.f == 5) {
                  ++var25;
               }

               if (var2.isEmptyTile(var25, var26, var27) || !var2.getMaterial(var25, var26, var27).isSolid()) {
                  if (var2.dimension.ultraWarm && this.content == Tile.water.id) {
                     var2.playSound(var7 + (double)0.5F, var9 + (double)0.5F, var11 + (double)0.5F, "random.fizz", 0.5F, 2.6F + (var2.random.nextFloat() - var2.random.nextFloat()) * 0.8F);

                     for(int var28 = 0; var28 < 8; ++var28) {
                        var2.addParticle("largesmoke", (double)var25 + Math.random(), (double)var26 + Math.random(), (double)var27 + Math.random(), (double)0.0F, (double)0.0F, (double)0.0F);
                     }
                  } else {
                     var2.setTileAndData(var25, var26, var27, this.content, 0);
                  }

                  return new ItemInstance(Item.bucket_empty);
               }
            }
         } else if (this.content == 0 && var24.entity instanceof Cow) {
            return new ItemInstance(Item.milk);
         }

         return var1;
      }
   }
}
