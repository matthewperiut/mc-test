package net.minecraft.client.locale;

import java.io.IOException;
import java.util.Properties;

public class Language {
   private static Language singleton = new Language();
   private Properties languageRepository = new Properties();

   private Language() {
      try {
         this.languageRepository.load(Language.class.getResourceAsStream("/lang/en_US.lang"));
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   public static Language getInstance() {
      return singleton;
   }

   public String getElement(String elementId) {
      return this.languageRepository.getProperty(elementId, elementId);
   }

   public String getElement(String elementId, Object... objects) {
      String element = this.languageRepository.getProperty(elementId, elementId);
      return String.format(element, objects);
   }

   public String getElementName(String elementId) {
      return this.languageRepository.getProperty(elementId + ".name", "");
   }

   public String getElementDescription(String elementId) {
      return this.languageRepository.getProperty(elementId + ".desc", "");
   }
}
