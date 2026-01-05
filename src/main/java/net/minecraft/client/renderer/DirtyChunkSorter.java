package net.minecraft.client.renderer;

import java.util.Comparator;
import net.minecraft.world.entity.player.Player;

public class DirtyChunkSorter implements Comparator<Chunk> {
   private Player player;

   public DirtyChunkSorter(Player player) {
      this.player = player;
   }

   public int compare(Chunk c0, Chunk c1) {
      boolean i0 = c0.visible;
      boolean i1 = c1.visible;
      if (i0 && !i1) {
         return 1;
      } else if (i1 && !i0) {
         return -1;
      } else {
         double d0 = (double)c0.distanceToSqr(this.player);
         double d1 = (double)c1.distanceToSqr(this.player);
         if (d0 < d1) {
            return 1;
         } else if (d0 > d1) {
            return -1;
         } else {
            return c0.id < c1.id ? 1 : -1;
         }
      }
   }
}
