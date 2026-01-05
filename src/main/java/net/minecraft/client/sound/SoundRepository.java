package net.minecraft.client.sound;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SoundRepository {
   private Random random = new Random();
   private Map<String, List<Sound>> urls = new HashMap();
   private List<Sound> all = new ArrayList();
   public int count = 0;
   public boolean trimDigits = true;

   public SoundRepository() {
   }

   public Sound add(String name, File file) {
      try {
         String orgName = name;
         name = name.substring(0, name.indexOf("."));
         if (this.trimDigits) {
            while(Character.isDigit(name.charAt(name.length() - 1))) {
               name = name.substring(0, name.length() - 1);
            }
         }

         name = name.replaceAll("/", ".");
         if (!this.urls.containsKey(name)) {
            this.urls.put(name, new ArrayList());
         }

         Sound sound = new Sound(orgName, file.toURI().toURL());
         ((List)this.urls.get(name)).add(sound);
         this.all.add(sound);
         ++this.count;
         return sound;
      } catch (MalformedURLException e) {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   public Sound get(String name) {
      List<Sound> values = (List)this.urls.get(name);
      return values == null ? null : (Sound)values.get(this.random.nextInt(values.size()));
   }

   public Sound any() {
      return this.all.size() == 0 ? null : (Sound)this.all.get(this.random.nextInt(this.all.size()));
   }
}
