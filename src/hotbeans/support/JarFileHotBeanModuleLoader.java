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
package hotbeans.support;

import hotbeans.HotBeanModule;
import hotbeans.HotBeanModuleLoader;
import hotbeans.util.FileDeletor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.FileCopyUtils;

/**
 * HotBeanModuleLoader implementation, providing loading of classes and resources from a jar file.
 * 
 * @author Tobias Löfstrand
 */
public class JarFileHotBeanModuleLoader extends URLClassLoader implements HotBeanModuleLoader {

   public static final String LIB_PATH = "lib";

   private final File moduleJarFile;

   private final File tempDir;

   private HotBeanModule hotBeanModule;

   final Log logger = LogFactory.getLog(getClass());

   /**
    * Creates a new JarFileHotBeanModuleLoader.
    */
   public JarFileHotBeanModuleLoader(File moduleJarFile, File tempDir) throws IOException {
      this(moduleJarFile, tempDir, JarFileHotBeanModuleLoader.class.getClassLoader());
   }

   /**
    * Creates a new JarFileHotBeanModuleLoader.
    */
   public JarFileHotBeanModuleLoader(File moduleJarFile, File tempDir, ClassLoader parentClassLoader)
            throws IOException {
      super(new URL[] {}, parentClassLoader);
      this.moduleJarFile = moduleJarFile;
      this.tempDir = tempDir;
      extractLibs();

      if (logger.isDebugEnabled()) {
         String classPath = "";
         try {
            URL[] urls = super.getURLs();
            for (int i = 0; i < urls.length; i++) {
               classPath += urls[i].toString();
               if (i < (urls.length - 1)) classPath += ", ";
            }
         } catch (Exception e) {
         } // We don't want a debug log to ruin the day

         logger.debug("Loader for '" + moduleJarFile + "' initialized. Temp dir: '" + tempDir + "'. Class path: "
                  + classPath + ".");
      }
   }

   /**
    * Extracts all the files in the module jar file, including nested jar files. The reason for extracting the complete
    * contents of the jar file (and not just the nested jar files) is to make sure the module jar file isn't locked, and
    * thus may be deleted.
    */
   private void extractLibs() throws IOException {
      if (logger.isDebugEnabled()) logger.debug("Extracting module jar file '" + moduleJarFile + "'.");

      JarFile jarFile = new JarFile(this.moduleJarFile);
      Enumeration entries = jarFile.entries();
      JarEntry entry;
      String entryName;
      File extractedFile = null;
      FileOutputStream extractedFileOutputStream;

      while (entries.hasMoreElements()) {
         entry = (JarEntry) entries.nextElement();
         if ((entry != null) && (!entry.isDirectory())) {
            entryName = entry.getName();
            if (entryName != null) {
               // if( logger.isDebugEnabled() ) logger.debug("Extracting '" + entryName + "'.");

               // Copy nested jar file to temp dir
               extractedFile = new File(this.tempDir, entryName);
               extractedFile.getParentFile().mkdirs();

               extractedFileOutputStream = new FileOutputStream(extractedFile);
               FileCopyUtils.copy(jarFile.getInputStream(entry), extractedFileOutputStream);
               extractedFileOutputStream = null;

               if ((entryName.startsWith(LIB_PATH)) && (entryName.toLowerCase().endsWith(".jar"))) {
                  // Register nested jar file in "class path"
                  super.addURL(extractedFile.toURI().toURL());
               }
            }
         }
      }

      jarFile.close();
      jarFile = null;

      super.addURL(tempDir.toURI().toURL()); // Add temp dir as class path (note that this must be added after all the
                                             // files have been extracted)

      if (logger.isDebugEnabled()) logger.debug("Done extracting module jar file '" + moduleJarFile + "'.");
   }

   /**
    * Overridden to replace the super class implementation with an implementation that simply throws a
    * ClassNotFoundException.
    */
   protected Class findClass(String name) throws ClassNotFoundException {
      // Override super class implementation with empty implementation...
      throw new ClassNotFoundException(name);
   }

   /**
    * Loads the class with the specified name.
    */
   public synchronized Class loadClass(String name) throws ClassNotFoundException {
      return this.loadClass(name, false);
   }

   /**
    * Override the super class implementation of loadClass to let this class loader make the first attempt at loading
    * the specified class.
    */
   protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
      // First, check if the class has already been loaded...
      Class clazz = findLoadedClass(name);

      if (clazz == null) // ...but if it hasn't, let this class loader attempt to load the class from the class
                           // path...
      {
         try {
            clazz = super.findClass(name); // Call the super class (URLClassLoader) implementation of findClass

            if ((clazz != null) && resolve) {
               resolveClass(clazz);
            }
         } catch (ClassNotFoundException e) {
         }

         if (clazz == null) {
            clazz = super.loadClass(name, resolve);
         }
      }

      return clazz;
   }

   /**
    * Finds the resource with the given name.
    */
   public URL getResource(String name) {
      URL url = findResource(name); // Check locally first

      if (url != null) return url;
      else return super.getResource(name); // Otherwise - handle in super class implementation
   }

   /**
    * Finalizes this object.
    */
   public void finalize() throws Throwable {
      super.finalize();

      if (logger.isDebugEnabled())
         logger.debug("Finalizing loader for " + hotBeanModule + ". Deleting temporary directory '" + tempDir + "'.");

      FileDeletor.delete(tempDir);

      if (this.hotBeanModule != null) this.hotBeanModule.unloaded();
      this.hotBeanModule = null;
   }

   /* ### Methods from HotBeanModuleLoader ### */

   /**
    * Gets the class loader used to load classes for a {@link HotBeanModule}.
    */
   public ClassLoader getClassLoader() {
      return this;
   }

   /**
    * Initializes this loader and associates it with the specified HotBeanModule.
    */
   public void init(HotBeanModule hotBeanModule) {
      this.hotBeanModule = hotBeanModule;
   }

   /**
    * Called when this loader is no longer needed and is to be taken out of service.
    */
   public void destroy() {
   }
}
