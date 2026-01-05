import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Encoder {
   public Encoder() {
   }

   public static void encode(File in, File out) throws IOException {
      System.out.println("Encoding " + in.getName() + " -> " + out.getName());
      String name = out.getName();
      name = name.substring(name.lastIndexOf("/") + 1);
      int seed = name.hashCode();
      DataInputStream dis = new DataInputStream(new FileInputStream(in));
      DataOutputStream dos = new DataOutputStream(new FileOutputStream(out, false));
      byte[] buff = new byte[65536];
      int read = 0;

      while((read = dis.read(buff)) >= 0) {
         for(int i = 0; i < read; ++i) {
            byte val = buff[i];
            buff[i] = (byte)(buff[i] ^ seed >> 8);
            seed = seed * 498729871 + 85731 * val;
         }

         dos.write(buff, 0, read);
      }

      dos.close();
      dis.close();
   }

   public static void main(String[] args) throws Exception {
      File[] files = (new File(".")).listFiles();

      for(int i = 0; i < files.length; ++i) {
         String name = files[i].getName();
         int p = name.lastIndexOf(".");
         if (p >= 0) {
            String fileName = name.substring(0, p);
            String ext = name.substring(p + 1);
            if (ext.equalsIgnoreCase("ogg")) {
               encode(files[i], new File(fileName + ".mus"));
            }
         }
      }

   }
}
