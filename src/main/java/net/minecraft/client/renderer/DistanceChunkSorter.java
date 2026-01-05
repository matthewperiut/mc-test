package net.minecraft.client.renderer;

import java.util.Comparator;
import net.minecraft.world.entity.Entity;

public class DistanceChunkSorter implements Comparator<Chunk> {
   private Entity player;

   public DistanceChunkSorter(Entity player) {
      this.player = player;
   }

   public int compare(Chunk c0, Chunk c1) {
      return c0.distanceToSqr(this.player) < c1.distanceToSqr(this.player) ? -1 : 1;
   }
}
