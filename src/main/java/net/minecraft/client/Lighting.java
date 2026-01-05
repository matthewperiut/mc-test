package net.minecraft.client;

import java.nio.FloatBuffer;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

public class Lighting {
   private static FloatBuffer lb = MemoryTracker.createFloatBuffer(16);

   public Lighting() {
   }

   public static void turnOff() {
      GL11.glDisable(2896);
      GL11.glDisable(16384);
      GL11.glDisable(16385);
      GL11.glDisable(2903);
   }

   public static void turnOn() {
      GL11.glEnable(2896);
      GL11.glEnable(16384);
      GL11.glEnable(16385);
      GL11.glEnable(2903);
      GL11.glColorMaterial(1032, 5634);
      float a = 0.4F;
      float d = 0.6F;
      float s = 0.0F;
      Vec3 l = Vec3.newTemp((double)0.2F, (double)1.0F, (double)-0.7F).normalize();
      GL11.glLight(16384, 4611, getBuffer(l.x, l.y, l.z, (double)0.0F));
      GL11.glLight(16384, 4609, getBuffer(d, d, d, 1.0F));
      GL11.glLight(16384, 4608, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
      GL11.glLight(16384, 4610, getBuffer(s, s, s, 1.0F));
      l = Vec3.newTemp((double)-0.2F, (double)1.0F, (double)0.7F).normalize();
      GL11.glLight(16385, 4611, getBuffer(l.x, l.y, l.z, (double)0.0F));
      GL11.glLight(16385, 4609, getBuffer(d, d, d, 1.0F));
      GL11.glLight(16385, 4608, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
      GL11.glLight(16385, 4610, getBuffer(s, s, s, 1.0F));
      GL11.glShadeModel(7424);
      GL11.glLightModel(2899, getBuffer(a, a, a, 1.0F));
   }

   private static FloatBuffer getBuffer(double a, double b, double c, double d) {
      return getBuffer((float)a, (float)b, (float)c, (float)d);
   }

   private static FloatBuffer getBuffer(float a, float b, float c, float d) {
      lb.clear();
      lb.put(a).put(b).put(c).put(d);
      lb.flip();
      return lb;
   }
}
