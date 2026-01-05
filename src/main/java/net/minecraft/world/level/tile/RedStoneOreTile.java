package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;

public class RedStoneOreTile extends Tile {
   private boolean lit;

   public RedStoneOreTile(int var1, int var2, boolean var3) {
      super(var1, var2, Material.stone);
      if (var3) {
         this.setTicking(true);
      }

      this.lit = var3;
   }

   public int getTickDelay() {
      return 30;
   }

   public void attack(Level var1, int var2, int var3, int var4, Player var5) {
      this.interact(var1, var2, var3, var4);
      super.attack(var1, var2, var3, var4, var5);
   }

   public void stepOn(Level var1, int var2, int var3, int var4, Entity var5) {
      this.interact(var1, var2, var3, var4);
      super.stepOn(var1, var2, var3, var4, var5);
   }

   public boolean use(Level var1, int var2, int var3, int var4, Player var5) {
      this.interact(var1, var2, var3, var4);
      return super.use(var1, var2, var3, var4, var5);
   }

   private void interact(Level var1, int var2, int var3, int var4) {
      this.poofParticles(var1, var2, var3, var4);
      if (this.id == Tile.redStoneOre.id) {
         var1.setTile(var2, var3, var4, Tile.redStoneOre_lit.id);
      }

   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      if (this.id == Tile.redStoneOre_lit.id) {
         var1.setTile(var2, var3, var4, Tile.redStoneOre.id);
      }

   }

   public int getResource(int var1, Random var2) {
      return Item.redStone.id;
   }

   public int getResourceCount(Random var1) {
      return 4 + var1.nextInt(2);
   }

   public void animateTick(Level var1, int var2, int var3, int var4, Random var5) {
      if (this.lit) {
         this.poofParticles(var1, var2, var3, var4);
      }

   }

   private void poofParticles(Level var1, int var2, int var3, int var4) {
      Random var5 = var1.random;
      double var6 = (double)0.0625F;

      for(int var8 = 0; var8 < 6; ++var8) {
         double var9 = (double)((float)var2 + var5.nextFloat());
         double var11 = (double)((float)var3 + var5.nextFloat());
         double var13 = (double)((float)var4 + var5.nextFloat());
         if (var8 == 0 && !var1.isSolidTile(var2, var3 + 1, var4)) {
            var11 = (double)(var3 + 1) + var6;
         }

         if (var8 == 1 && !var1.isSolidTile(var2, var3 - 1, var4)) {
            var11 = (double)(var3 + 0) - var6;
         }

         if (var8 == 2 && !var1.isSolidTile(var2, var3, var4 + 1)) {
            var13 = (double)(var4 + 1) + var6;
         }

         if (var8 == 3 && !var1.isSolidTile(var2, var3, var4 - 1)) {
            var13 = (double)(var4 + 0) - var6;
         }

         if (var8 == 4 && !var1.isSolidTile(var2 + 1, var3, var4)) {
            var9 = (double)(var2 + 1) + var6;
         }

         if (var8 == 5 && !var1.isSolidTile(var2 - 1, var3, var4)) {
            var9 = (double)(var2 + 0) - var6;
         }

         if (var9 < (double)var2 || var9 > (double)(var2 + 1) || var11 < (double)0.0F || var11 > (double)(var3 + 1) || var13 < (double)var4 || var13 > (double)(var4 + 1)) {
            var1.addParticle("reddust", var9, var11, var13, (double)0.0F, (double)0.0F, (double)0.0F);
         }
      }

   }
}
