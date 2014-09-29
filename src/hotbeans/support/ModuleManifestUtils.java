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

import java.io.File;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Module manifest utilities.
 * 
 * @author Tobias Löfstrand
 */
public class ModuleManifestUtils {

   public static final String NAME_ATTRIBUTE = "HotBeanModule-Name";

   public static final String DESCRIPTION_ATTRIBUTE = "Implementation-Title";

   public static final String VERSION_ATTRIBUTE = "Implementation-Version";

   /**
    * Reads the manifest from the specified jar file.
    */
   public static Manifest readManifest(final File moduleFile) throws Exception {
      JarFile jarFile = new JarFile(moduleFile);
      Manifest manifest = jarFile.getManifest();
      jarFile.close();

      return manifest;
   }

   /**
    * Reads the module name ({@link #NAME_ATTRIBUTE}) from the manifest.
    */
   public static String getName(final Manifest manifest) {
      if ((manifest != null) && (manifest.getMainAttributes() != null)) { return manifest.getMainAttributes().getValue(
               NAME_ATTRIBUTE); }
      return null;
   }

   /**
    * Reads the module version ({@link #VERSION_ATTRIBUTE}) from the manifest.
    */
   public static String getVersion(final Manifest manifest) {
      if ((manifest != null) && (manifest.getMainAttributes() != null)) { return manifest.getMainAttributes().getValue(
               VERSION_ATTRIBUTE); }
      return null;
   }

   /**
    * Reads the module description ({@link #DESCRIPTION_ATTRIBUTE}) from the manifest.
    */
   public static String getDescription(final Manifest manifest) {
      if ((manifest != null) && (manifest.getMainAttributes() != null)) { return manifest.getMainAttributes().getValue(
               DESCRIPTION_ATTRIBUTE); }
      return null;
   }
}
