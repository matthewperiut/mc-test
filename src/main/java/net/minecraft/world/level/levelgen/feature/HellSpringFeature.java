package net.minecraft.world.level.levelgen.feature;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class HellSpringFeature extends Feature {
   private int tile;

   public HellSpringFeature(int var1) {
      this.tile = var1;
   }

   public boolean place(Level var1, Random var2, int var3, int var4, int var5) {
      if (var1.getTile(var3, var4 + 1, var5) != Tile.hellRock.id) {
         return false;
      } else if (var1.getTile(var3, var4, var5) != 0 && var1.getTile(var3, var4, var5) != Tile.hellRock.id) {
         return false;
      } else {
         int var6 = 0;
         if (var1.getTile(var3 - 1, var4, var5) == Tile.hellRock.id) {
            ++var6;
         }

         if (var1.getTile(var3 + 1, var4, var5) == Tile.hellRock.id) {
            ++var6;
         }

         if (var1.getTile(var3, var4, var5 - 1) == Tile.hellRock.id) {
            ++var6;
         }

         if (var1.getTile(var3, var4, var5 + 1) == Tile.hellRock.id) {
            ++var6;
         }

         if (var1.getTile(var3, var4 - 1, var5) == Tile.hellRock.id) {
            ++var6;
         }

         int var7 = 0;
         if (var1.isEmptyTile(var3 - 1, var4, var5)) {
            ++var7;
         }

         if (var1.isEmptyTile(var3 + 1, var4, var5)) {
            ++var7;
         }

         if (var1.isEmptyTile(var3, var4, var5 - 1)) {
            ++var7;
         }

         if (var1.isEmptyTile(var3, var4, var5 + 1)) {
            ++var7;
         }

         if (var1.isEmptyTile(var3, var4 - 1, var5)) {
            ++var7;
         }

         if (var6 == 4 && var7 == 1) {
            var1.setTile(var3, var4, var5, this.tile);
            var1.instaTick = true;
            Tile.tiles[this.tile].tick(var1, var3, var4, var5, var2);
            var1.instaTick = false;
         }

         return true;
      }
   }
}
