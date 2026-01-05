package net.minecraft.client.sound;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import paulscode.sound.codecs.CodecJOrbis;

public class CodecMus extends CodecJOrbis {
   protected URL myUrl;
   protected URLConnection myUrlConnection;

   public CodecMus() {
   }

   @Override
   public boolean initialize(URL url) {
      this.myUrl = url;
      try {
         this.myUrlConnection = url.openConnection();
      } catch (IOException e) {
         return false;
      }
      return super.initialize(url);
   }

   protected InputStream openInputStream() throws IOException {
      return new DecoderInputStream(this.myUrl, this.myUrlConnection.getInputStream());
   }

   private class DecoderInputStream extends InputStream {
      private int seed;
      private InputStream in;
      byte[] buff = new byte[1];

      public DecoderInputStream(URL url, InputStream in) {
         this.in = in;
         String name = url.getPath();
         name = name.substring(name.lastIndexOf("/") + 1);
         this.seed = name.hashCode();
      }

      public int read() throws IOException {
         int result = this.read(this.buff, 0, 1);
         return result < 0 ? result : this.buff[0];
      }

      public int read(byte[] buff, int off, int len) throws IOException {
         len = this.in.read(buff, off, len);

         for(int i = 0; i < len; ++i) {
            byte val = buff[off + i] = (byte)(buff[off + i] ^ this.seed >> 8);
            this.seed = this.seed * 498729871 + 85731 * val;
         }

         return len;
      }
   }
}
