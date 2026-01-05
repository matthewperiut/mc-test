package net.minecraft.client.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.MemoryTracker;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

public class Tesselator {
   private static boolean TRIANGLE_MODE = true;
   private static boolean USE_VBO = false;
   private static final int MAX_MEMORY_USE = 16777216;
   private static final int MAX_FLOATS = 2097152;
   private ByteBuffer buffer;
   private IntBuffer ib;
   private FloatBuffer fb;
   private int[] array;
   private int vertices = 0;
   private double u;
   private double v;
   private int col;
   private boolean hasColor = false;
   private boolean hasTexture = false;
   private boolean hasNormal = false;
   private int p = 0;
   private int count = 0;
   private boolean noColor = false;
   private int mode;
   private double xo;
   private double yo;
   private double zo;
   private int normal;
   public static final Tesselator instance = new Tesselator(2097152);
   private boolean tesselating = false;
   private boolean vboMode = false;
   private IntBuffer vboIds;
   private int vboId = 0;
   private int vboCounts = 10;
   private int size;

   private Tesselator(int size) {
      this.size = size;
      this.buffer = MemoryTracker.createByteBuffer(size * 4);
      this.ib = this.buffer.asIntBuffer();
      this.fb = this.buffer.asFloatBuffer();
      this.array = new int[size];
      this.vboMode = USE_VBO && GLContext.getCapabilities().GL_ARB_vertex_buffer_object;
      if (this.vboMode) {
         this.vboIds = MemoryTracker.createIntBuffer(this.vboCounts);
         ARBVertexBufferObject.glGenBuffersARB(this.vboIds);
      }

   }

   public Tesselator getUniqueInstance(int size) {
      return new Tesselator(size);
   }

   public void end() {
      if (!this.tesselating) {
         throw new IllegalStateException("Not tesselating!");
      } else {
         this.tesselating = false;
         if (this.vertices > 0) {
            this.ib.clear();
            this.ib.put(this.array, 0, this.p);
            this.buffer.position(0);
            this.buffer.limit(this.p * 4);
            if (this.vboMode) {
               this.vboId = (this.vboId + 1) % this.vboCounts;
               ARBVertexBufferObject.glBindBufferARB(34962, this.vboIds.get(this.vboId));
               ARBVertexBufferObject.glBufferDataARB(34962, this.buffer, 35040);
            }

            if (this.hasTexture) {
               if (this.vboMode) {
                  GL11.glTexCoordPointer(2, 5126, 32, 12L);
               } else {
                  this.fb.position(3);
                  GL11.glTexCoordPointer(2, 32, this.fb);
               }

               GL11.glEnableClientState(32888);
            }

            if (this.hasColor) {
               if (this.vboMode) {
                  GL11.glColorPointer(4, 5121, 32, 20L);
               } else {
                  this.buffer.position(20);
                  GL11.glColorPointer(4, true, 32, this.buffer);
               }

               GL11.glEnableClientState(32886);
            }

            if (this.hasNormal) {
               if (this.vboMode) {
                  GL11.glNormalPointer(5120, 32, 24L);
               } else {
                  this.buffer.position(24);
                  GL11.glNormalPointer(32, this.buffer);
               }

               GL11.glEnableClientState(32885);
            }

            if (this.vboMode) {
               GL11.glVertexPointer(3, 5126, 32, 0L);
            } else {
               this.fb.position(0);
               GL11.glVertexPointer(3, 32, this.fb);
            }

            GL11.glEnableClientState(32884);
            if (this.mode == 7 && TRIANGLE_MODE) {
               GL11.glDrawArrays(4, 0, this.vertices);
            } else {
               GL11.glDrawArrays(this.mode, 0, this.vertices);
            }

            GL11.glDisableClientState(32884);
            if (this.hasTexture) {
               GL11.glDisableClientState(32888);
            }

            if (this.hasColor) {
               GL11.glDisableClientState(32886);
            }

            if (this.hasNormal) {
               GL11.glDisableClientState(32885);
            }
         }

         this.clear();
      }
   }

   private void clear() {
      this.vertices = 0;
      this.buffer.clear();
      this.p = 0;
      this.count = 0;
   }

   public void begin() {
      this.begin(7);
   }

   public void begin(int mode) {
      if (this.tesselating) {
         throw new IllegalStateException("Already tesselating!");
      } else {
         this.tesselating = true;
         this.clear();
         this.mode = mode;
         this.hasNormal = false;
         this.hasColor = false;
         this.hasTexture = false;
         this.noColor = false;
      }
   }

   public void tex(double u, double v) {
      this.hasTexture = true;
      this.u = u;
      this.v = v;
   }

   public void color(float r, float g, float b) {
      this.color((int)(r * 255.0F), (int)(g * 255.0F), (int)(b * 255.0F));
   }

   public void color(float r, float g, float b, float a) {
      this.color((int)(r * 255.0F), (int)(g * 255.0F), (int)(b * 255.0F), (int)(a * 255.0F));
   }

   public void color(int r, int g, int b) {
      this.color(r, g, b, 255);
   }

   public void color(int r, int g, int b, int a) {
      if (!this.noColor) {
         if (r > 255) {
            r = 255;
         }

         if (g > 255) {
            g = 255;
         }

         if (b > 255) {
            b = 255;
         }

         if (a > 255) {
            a = 255;
         }

         if (r < 0) {
            r = 0;
         }

         if (g < 0) {
            g = 0;
         }

         if (b < 0) {
            b = 0;
         }

         if (a < 0) {
            a = 0;
         }

         this.hasColor = true;
         if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            this.col = a << 24 | b << 16 | g << 8 | r;
         } else {
            this.col = r << 24 | g << 16 | b << 8 | a;
         }

      }
   }

   public void color(byte r, byte g, byte b) {
      this.color(r & 255, g & 255, b & 255);
   }

   public void vertexUV(double x, double y, double z, double u, double v) {
      this.tex(u, v);
      this.vertex(x, y, z);
   }

   public void vertex(double x, double y, double z) {
      ++this.count;
      if (this.mode == 7 && TRIANGLE_MODE && this.count % 4 == 0) {
         for(int i = 0; i < 2; ++i) {
            int offs = 8 * (3 - i);
            if (this.hasTexture) {
               this.array[this.p + 3] = this.array[this.p - offs + 3];
               this.array[this.p + 4] = this.array[this.p - offs + 4];
            }

            if (this.hasColor) {
               this.array[this.p + 5] = this.array[this.p - offs + 5];
            }

            this.array[this.p + 0] = this.array[this.p - offs + 0];
            this.array[this.p + 1] = this.array[this.p - offs + 1];
            this.array[this.p + 2] = this.array[this.p - offs + 2];
            ++this.vertices;
            this.p += 8;
         }
      }

      if (this.hasTexture) {
         this.array[this.p + 3] = Float.floatToRawIntBits((float)this.u);
         this.array[this.p + 4] = Float.floatToRawIntBits((float)this.v);
      }

      if (this.hasColor) {
         this.array[this.p + 5] = this.col;
      }

      if (this.hasNormal) {
         this.array[this.p + 6] = this.normal;
      }

      this.array[this.p + 0] = Float.floatToRawIntBits((float)(x + this.xo));
      this.array[this.p + 1] = Float.floatToRawIntBits((float)(y + this.yo));
      this.array[this.p + 2] = Float.floatToRawIntBits((float)(z + this.zo));
      this.p += 8;
      ++this.vertices;
      if (this.vertices % 4 == 0 && this.p >= this.size - 32) {
         this.end();
         this.tesselating = true;
      }

   }

   public void color(int c) {
      int r = c >> 16 & 255;
      int g = c >> 8 & 255;
      int b = c & 255;
      this.color(r, g, b);
   }

   public void color(int c, int alpha) {
      int r = c >> 16 & 255;
      int g = c >> 8 & 255;
      int b = c & 255;
      this.color(r, g, b, alpha);
   }

   public void noColor() {
      this.noColor = true;
   }

   public void normal(float x, float y, float z) {
      if (!this.tesselating) {
         System.out.println("But..");
      }

      this.hasNormal = true;
      byte xx = (byte)((int)(x * 128.0F));
      byte yy = (byte)((int)(y * 127.0F));
      byte zz = (byte)((int)(z * 127.0F));
      this.normal = xx | yy << 8 | zz << 16;
   }

   public void offset(double xo, double yo, double zo) {
      this.xo = xo;
      this.yo = yo;
      this.zo = zo;
   }

   public void addOffset(float x, float y, float z) {
      this.xo += (double)x;
      this.yo += (double)y;
      this.zo += (double)z;
   }
}
