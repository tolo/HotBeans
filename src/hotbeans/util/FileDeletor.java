/*
 * Copyright 2007 the project originators.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hotbeans.util;

import java.io.File;

/**
 * Class for deleting files and subdirectories in a separate thread.
 * 
 * @author Tobias Löfstrand
 */
public class FileDeletor extends Thread {

   private final String fileName;

   /**
    * Creates a new FileDeletor. Note that the methos <code>start()</code> must be called to initiate the deletion.
    */
   public FileDeletor(final File file) {
      this(file.getAbsolutePath());
   }

   /**
    * Creates a new FileDeletor. Note that the methos <code>start()</code> must be called to initiate the deletion.
    */
   public FileDeletor(final String fileName) {
      super("FileDeletorThread(" + fileName + ")");

      super.setDaemon(true);

      this.fileName = fileName;
   }

   /**
    * Deletes the specified file or directory and any sub directories.
    */
   public static void delete(String fileName) {
      new FileDeletor(fileName).start();
   }

   /**
    * Deletes the specified file or directory and any sub directories.
    */
   public static void delete(File file) {
      new FileDeletor(file).start();
   }

   /**
    * The thread method of the FileDeletor. Deletes the file.
    */
   public void run() {
      Thread.yield();

      deleteTreeImpl(this.fileName);
   }

   /**
    * Deletes a tree of files.
    */
   public static void deleteTreeImpl(final String dirName) {
      File dir = new File(dirName);

      if (dir.isDirectory()) {
         File file;

         String[] dirContents = dir.list();

         if (dirContents != null) {
            for (int i = 0; i < dirContents.length; i++) {
               file = new File(dir.getAbsolutePath() + File.separator + dirContents[i]);
               if (file.isDirectory()) deleteTreeImpl(file.getAbsolutePath());
               else deleteFileImpl(file.getAbsolutePath());
               file = null;
            }
         }
      }

      deleteFileImpl(dirName);
   }

   /**
    * Deletes a file.
    */
   public static void deleteFileImpl(final String fileName) {
      boolean deleteSuccess = false;
      File file;

      for (int i = 0; ((i < 50) && (!deleteSuccess)); i++) {
         file = new File(fileName);

         if (!file.exists()) deleteSuccess = true;
         else deleteSuccess = file.delete();

         file = null;

         if (!deleteSuccess) {
            try {
               Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
         }
      }
   }
}
