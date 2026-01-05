package net.minecraft.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BackgroundDownloader extends Thread {
   public File workingDirectory;
   private Minecraft minecraft;
   private boolean stopped = false;

   public BackgroundDownloader(File workDir, Minecraft minecraft) {
      this.minecraft = minecraft;
      this.setName("Resource download thread");
      this.setDaemon(true);
      this.workingDirectory = new File(workDir, "resources/");
      if (!this.workingDirectory.exists() && !this.workingDirectory.mkdirs()) {
         throw new RuntimeException("The working directory could not be created: " + this.workingDirectory);
      }
   }

   public void run() {
      try {
         URL resourceUrl = new URL("http://s3.amazonaws.com/MinecraftResources/");
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         Document doc = db.parse(resourceUrl.openStream());
         NodeList nodeLst = doc.getElementsByTagName("Contents");

         for(int pass = 0; pass < 2; ++pass) {
            for(int s = 0; s < nodeLst.getLength(); ++s) {
               Node fstNode = nodeLst.item(s);
               if (fstNode.getNodeType() == 1) {
                  Element fstElmnt = (Element)fstNode;
                  String key = ((Element)fstElmnt.getElementsByTagName("Key").item(0)).getChildNodes().item(0).getNodeValue();
                  long size = Long.parseLong(((Element)fstElmnt.getElementsByTagName("Size").item(0)).getChildNodes().item(0).getNodeValue());
                  if (size > 0L) {
                     this.checkDownload(resourceUrl, key, size, pass);
                     if (this.stopped) {
                        return;
                     }
                  }
               }
            }
         }
      } catch (Exception e) {
         this.loadAll(this.workingDirectory, "");
         e.printStackTrace();
      }

   }

   public void forceReload() {
      this.loadAll(this.workingDirectory, "");
   }

   private void loadAll(File dir, String prefix) {
      File[] files = dir.listFiles();

      for(int i = 0; i < files.length; ++i) {
         if (files[i].isDirectory()) {
            this.loadAll(files[i], prefix + files[i].getName() + "/");
         } else {
            try {
               this.minecraft.fileDownloaded(prefix + files[i].getName(), files[i]);
            } catch (Exception var6) {
               System.out.println("Failed to add " + prefix + files[i].getName());
            }
         }
      }

   }

   private void checkDownload(URL resourceUrl, String name, long size, int pass) {
      try {
         int p = name.indexOf("/");
         String category = name.substring(0, p);
         if (!category.equals("sound") && !category.equals("newsound")) {
            if (pass != 1) {
               return;
            }
         } else if (pass != 0) {
            return;
         }

         File outFile = new File(this.workingDirectory, name);
         if (!outFile.exists() || outFile.length() != size) {
            outFile.getParentFile().mkdirs();
            String urlName = name.replaceAll(" ", "%20");
            this.download(new URL(resourceUrl, urlName), outFile, size);
            if (this.stopped) {
               return;
            }
         }

         this.minecraft.fileDownloaded(name, outFile);
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   private void download(URL url, File file, long length) throws IOException {
      byte[] buffer = new byte[4096];
      DataInputStream dis = new DataInputStream(url.openStream());
      DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
      int read = 0;

      while((read = dis.read(buffer)) >= 0) {
         dos.write(buffer, 0, read);
         if (this.stopped) {
            return;
         }
      }

      dis.close();
      dos.close();
   }

   public void halt() {
      this.stopped = true;
   }
}
