package net.minecraft;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SharedConstants {
   public static final String VERSION_STRING = "Beta 1.2_02";
   public static final int NETWORK_PROTOCOL_VERSION = 8;
   public static final int maxChatLength = 100;
   public static final String acceptableLetters = readAcceptableChars();

   public SharedConstants() {
   }

   private static String readAcceptableChars() {
      String var0 = "";

      try {
         BufferedReader var1 = new BufferedReader(new InputStreamReader(SharedConstants.class.getResourceAsStream("/font.txt"), "UTF-8"));
         String var2 = "";

         while((var2 = var1.readLine()) != null) {
            if (!var2.startsWith("#")) {
               var0 = var0 + var2;
            }
         }

         var1.close();
      } catch (Exception var3) {
      }

      return var0;
   }
}
