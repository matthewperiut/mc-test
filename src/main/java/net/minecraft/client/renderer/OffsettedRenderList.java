package net.minecraft.client.renderer;

import java.nio.IntBuffer;
import net.minecraft.client.MemoryTracker;
import org.lwjgl.opengl.GL11;

public class OffsettedRenderList {
   private int x;
   private int y;
   private int z;
   private float xOff;
   private float yOff;
   private float zOff;
   private IntBuffer lists = MemoryTracker.createIntBuffer(65536);
   private boolean inited = false;
   private boolean rendered = false;

   public OffsettedRenderList() {
   }

   public void init(int x, int y, int z, double xOff, double yOff, double zOff) {
      this.inited = true;
      this.lists.clear();
      this.x = x;
      this.y = y;
      this.z = z;
      this.xOff = (float)xOff;
      this.yOff = (float)yOff;
      this.zOff = (float)zOff;
   }

   public boolean isAt(int x, int y, int z) {
      if (!this.inited) {
         return false;
      } else {
         return x == this.x && y == this.y && z == this.z;
      }
   }

   public void add(int list) {
      this.lists.put(list);
      if (this.lists.remaining() == 0) {
         this.render();
      }

   }

   public void render() {
      if (this.inited) {
         if (!this.rendered) {
            this.lists.flip();
            this.rendered = true;
         }

         if (this.lists.remaining() > 0) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)this.x - this.xOff, (float)this.y - this.yOff, (float)this.z - this.zOff);
            GL11.glCallLists(this.lists);
            GL11.glPopMatrix();
         }

      }
   }

   public void clear() {
      this.inited = false;
      this.rendered = false;
   }
}
