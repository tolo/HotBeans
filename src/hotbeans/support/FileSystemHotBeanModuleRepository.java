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

/*
 TODO: Limit on numer of revisions in history. 
 */
package hotbeans.support;

import hotbeans.HotBeanContext;
import hotbeans.HotBeanModule;
import hotbeans.HotBeanModuleInfo;
import hotbeans.HotBeanModuleLoader;
import hotbeans.HotBeansException;
import hotbeans.InvalidModuleNameException;
import hotbeans.ModuleAlreadyExistsException;
import hotbeans.util.FileDeletor;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.FileCopyUtils;

/**
 * File system based HotBeanModuleRepository implementation.
 * 
 * @author Tobias Löfstrand
 */
public class FileSystemHotBeanModuleRepository extends PeriodicCheckHotBeanModuleRepository implements
         InitializingBean, DisposableBean, ApplicationContextAware, ApplicationListener {

   private static class RepositoryFileLock {

      private RandomAccessFile file;

      private FileLock lock;

      public RepositoryFileLock(final RandomAccessFile file, final FileLock lock) {
         this.file = file;
         this.lock = lock;
      }

      public void release() {
         try {
            this.file.close();
         } catch (Exception e) {
         }
         try {
            this.lock.release();
         } catch (Exception e) {
         }
      }
   }

   public static final String LOCK_FILE_NAME = "moduleRepository.lck";

   private static final String MODULE_FILE_SUFFIX = ".jar";

   private static final int MODULE_FILE_SUFFIX_LENGTH = MODULE_FILE_SUFFIX.length();

   private static final FileFilter ModuleFileFilter = new FileFilter() {

      public boolean accept(File pathname) {
         return pathname.getName().toLowerCase().endsWith(MODULE_FILE_SUFFIX);
      }
   };

   private File moduleRepositoryDirectory = null;

   private File temporaryDirectory = null;

   // max history revisions

   private ApplicationContext parentApplicationContext;

   private boolean initialized = false;

   private boolean applicationContextInitialized = false;

   /**
    * Creates a new FileSystemHotBeanModuleRepository.
    */
   public FileSystemHotBeanModuleRepository() {
      this(null);
   }

   /**
    * Creates a new FileSystemHotBeanModuleRepository.
    */
   public FileSystemHotBeanModuleRepository(Object lock) {
      super(lock);
      super.setName("FileSystemHotBeanModuleRepository");
   }

   /**
    * Gets the module repository directory.
    */
   public File getModuleRepositoryDirectory() {
      return moduleRepositoryDirectory;
   }

   /**
    * Sets the module repository directory.
    */
   public void setModuleRepositoryDirectory(File moduleRepositoryDirectory) {
      synchronized (super.getLock()) {
         this.moduleRepositoryDirectory = moduleRepositoryDirectory;
         if (this.initialized) super.reinitialize();
      }
   }

   /**
    * Gets temporary directory.
    */
   public File getTemporaryDirectory() {
      return temporaryDirectory;
   }

   /**
    * Sets temporary directory.
    */
   public void setTemporaryDirectory(File temporaryDirectory) {
      synchronized (super.getLock()) {
         this.temporaryDirectory = temporaryDirectory;
         if (this.initialized) super.reinitialize();
      }
   }

   /**
    * Invoked by a BeanFactory after it has set all bean properties. This method invokes {@link #init()} to initialize
    * the repository.
    */
   public void afterPropertiesSet() throws Exception {
      this.init();
   }

   /**
    * Set the ApplicationContext that this object runs in.
    */
   public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
      this.parentApplicationContext = applicationContext;
   }

   /**
    * Called to handle a Spring application context event.
    */
   public void onApplicationEvent(ApplicationEvent applicationEvent) {
      Log logger = this.getLog();

      synchronized (super.getLock()) {
         if (applicationEvent.getSource() == this.parentApplicationContext) {
            if (logger.isDebugEnabled()) logger.debug("Parent Spring ApplicationContext initialized.");
            applicationContextInitialized = true;
         }
      }
   }

   /**
    * Checks if this repository is ready.
    */
   private boolean isReady() {
      synchronized (super.getLock()) {
         if (this.initialized) {
            if (this.parentApplicationContext != null) { return applicationContextInitialized; }
            return true;
         }
         return false;
      }
   }

   /**
    * Initializes this FileSystemHotBeanModuleRepository.
    */
   public void init() throws Exception {
      synchronized (super.getLock()) {
         if (!initialized) {
            Log logger = this.getLog();

            if (this.moduleRepositoryDirectory == null) this.moduleRepositoryDirectory = new File("hotModules");
            if (this.temporaryDirectory == null)
               this.temporaryDirectory = new File(this.moduleRepositoryDirectory, "temp");

            this.moduleRepositoryDirectory.mkdirs();
            this.temporaryDirectory.mkdirs();

            // Delete temporary directory on start up
            FileDeletor.delete(this.temporaryDirectory);

            if (logger.isDebugEnabled())
               logger.debug("Initializing FileSystemHotBeanModuleRepository - moduleRepositoryDirectory: "
                        + this.moduleRepositoryDirectory + ", temporaryDirectory: " + this.temporaryDirectory + ".");

            RepositoryFileLock fileLock = null;
            try {
               fileLock = this.obtainRepositoryFileLock(false);
            } catch (Exception e) {
               logger.error("Error obtaining repository file lock on init!", e);
            } finally {
               this.releaseRepositoryFileLock(fileLock);
               fileLock = null;
            }

            super.init();

            initialized = true;
         }
      }
   }

   /**
    * Destroys this FileSystemHotBeanModuleRepository.
    */
   public void destroy() throws Exception {
      synchronized (super.getLock()) {
         this.initialized = false;
         this.applicationContextInitialized = false;

         super.destroy();
      }
   }

   /**
    * Obtains a file lock on the repository lock file.
    */
   protected RepositoryFileLock obtainRepositoryFileLock(final boolean shared) throws IOException {
      return obtainRepositoryFileLock(shared, 10000);
   }

   /**
    * Obtains a file lock on the repository lock file.
    */
   protected RepositoryFileLock obtainRepositoryFileLockNoRetries(final boolean shared) throws IOException {
      return obtainRepositoryFileLock(shared, -1);
   }

   /**
    * Obtains a file lock on the repository lock file.
    */
   protected RepositoryFileLock obtainRepositoryFileLock(final boolean shared, final int timeout) throws IOException {
      Log logger = this.getLog();

      if (logger.isDebugEnabled()) logger.debug("Obtaining repository file lock (shared: " + shared + ").");

      RepositoryFileLock repositoryFileLock = null;
      FileLock lock = null;
      final long beginWait = System.currentTimeMillis();

      while (repositoryFileLock == null) {
         try {
            RandomAccessFile lockFile = new RandomAccessFile(new File(moduleRepositoryDirectory, LOCK_FILE_NAME), "rws");
            FileChannel channel = lockFile.getChannel();

            // Attempt to obtain a lock on the file
            lock = channel.tryLock(0L, Long.MAX_VALUE, shared);
            if (!shared && (lockFile.length() == 0)) {
               lockFile.write(new String("LOCK").getBytes());
               lockFile.getFD().sync();
            }
            repositoryFileLock = new RepositoryFileLock(lockFile, lock);
         } catch (IOException ioe) {
            if (logger.isDebugEnabled())
               logger.debug("Error obtaining repository file lock (shared: " + shared + ").", ioe);
            if (timeout < 0) throw ioe;
         } catch (OverlappingFileLockException ofle) {
            if (logger.isDebugEnabled())
               logger.debug("Error obtaining repository file lock (shared: " + shared + ").", ofle);
            if (timeout < 0) throw ofle;
         }

         if (repositoryFileLock == null) // This statement shouldn't be reaced if timeout is < 0
         {
            if ((System.currentTimeMillis() - beginWait) > timeout) // Wait a maximum of timeout milliseconds on lock
            {
               throw new IOException("Timeout while waiting for file lock on repository lock file!");
            } else {
               // Otherwise - wait a while before trying to obtain a lock again
               try {
                  Thread.sleep(Math.min(250, timeout - (System.currentTimeMillis() - beginWait)));
               } catch (InterruptedException ie) {
               }
            }
         }
      }

      if (logger.isDebugEnabled()) logger.debug("Repository file lock (shared: " + shared + ") obtained.");

      return repositoryFileLock;
   }

   /**
    * Releases the repository lock file.
    */
   protected void releaseRepositoryFileLock(final RepositoryFileLock repositoryFileLock) {
      Log logger = this.getLog();

      if (logger.isDebugEnabled()) logger.debug("Releasing repository file lock.");

      try {
         if (repositoryFileLock != null) repositoryFileLock.release();
         else if (logger.isDebugEnabled()) logger.debug("Cannot release repository file lock - lock is null.");
      } catch (Exception e) {
         logger.error("Error releasing repository file lock!", e);
      }
   }

   /* ### HOTBEANMODULEREPOSITORY METHODS BEGIN ### */

   /**
    * Adds (installs) a new hot bean module.
    */
   public HotBeanModuleInfo addHotBeanModule(final InputStream moduleFile) {
      Log logger = this.getLog();

      if (logger.isInfoEnabled()) logger.info("Attempting to add module.");

      HotBeanModuleInfo hotBeanModuleInfo = this.updateModuleInternal(null, moduleFile, true);

      if (logger.isInfoEnabled()) logger.info("Done adding module - " + hotBeanModuleInfo + ".");

      if (hotBeanModuleInfo != null) return hotBeanModuleInfo.getClone();
      else return hotBeanModuleInfo;
   }

   /**
    * Updates an existing hot beans module with a new revision.
    */
   public HotBeanModuleInfo updateHotBeanModule(final String moduleName, final InputStream moduleFile) {
      Log logger = this.getLog();

      if (logger.isInfoEnabled()) logger.info("Attempting to update module '" + moduleName + "'.");

      HotBeanModuleInfo hotBeanModuleInfo = this.updateModuleInternal(moduleName, moduleFile, false);

      if (logger.isInfoEnabled()) logger.info("Done updating module - " + hotBeanModuleInfo + ".");

      if (hotBeanModuleInfo != null) return hotBeanModuleInfo.getClone();
      else return hotBeanModuleInfo;
   }

   /**
    * Reverts an hot beans module to a previous revision (which becomes a new revision).
    */
   public HotBeanModuleInfo revertHotBeanModule(final String moduleName, final long revision) {
      Log logger = this.getLog();
      HotBeanModuleInfo hotBeanModuleInfo = null;

      if (logger.isInfoEnabled())
         logger.info("Attempting to revert module '" + moduleName + "' to revision " + revision + ".");

      synchronized (super.getLock()) {
         File moduleDirectory = new File(this.moduleRepositoryDirectory, moduleName);
         File moduleFile = new File(moduleDirectory, revision + MODULE_FILE_SUFFIX);

         if (moduleFile.exists()) {
            try {
               hotBeanModuleInfo = this.updateModuleInternal(moduleName, new FileInputStream(moduleFile), false);

               if (logger.isInfoEnabled()) logger.info("Done reverting module - " + hotBeanModuleInfo + ".");
            } catch (Exception e) {
               logger.error("Error reverting module '" + moduleName + " - " + e + "'!", e);
               throw new HotBeansException("Error reverting module '" + moduleName + " - " + e + "'!");
            }
         } else throw new HotBeansException("Revision " + revision + " doesn't exist for module '" + moduleName + "!");
      }

      if (hotBeanModuleInfo != null) return hotBeanModuleInfo.getClone();
      else return hotBeanModuleInfo;
   }

   /**
    * Removes all revisions of a hot bean module.
    */
   public void removeHotBeanModule(final String moduleName) {
      Log logger = this.getLog();

      if (logger.isInfoEnabled()) logger.info("Removing module '" + moduleName + "'.");

      synchronized (super.getLock()) {
         RepositoryFileLock fileLock = null;
         try {
            HotBeanModuleType moduleType = super.getHotBeanModuleType(moduleName);

            if (moduleType != null) {
               // Mark current module revision as as deleted to indicate that the module should be deleted
               moduleType.setRemoveType(true);

               fileLock = this.obtainRepositoryFileLock(false); // Obtain lock

               File moduleDirectory = new File(this.moduleRepositoryDirectory, moduleName);
               FileDeletor.delete(moduleDirectory);

               this.checkForObsoleteModules(moduleName); // Perform a check on the module at once, to make sure it is
                                                         // removed
            }
         } catch (Exception e) {
            logger.error("Error deleting module '" + moduleName + " - " + e + "'!", e);
            throw new HotBeansException("Error deleting module '" + moduleName + " - " + e + "'!");
         } finally {
            this.releaseRepositoryFileLock(fileLock);
            fileLock = null;
         }
      }
   }

   /* ### HOTBEANMODULEREPOSITORY METHODS END ### */

   /* ### MISC UTILITY METHODS BEGIN ### */

   /**
    * Internal method to update a module.
    */
   protected HotBeanModuleInfo updateModuleInternal(String moduleName, final InputStream moduleFileStream,
            final boolean add) {
      long revisionNumber = -1;
      HotBeanModuleInfo hotBeanModuleInfo = null;
      Log logger = this.getLog();

      synchronized (super.getLock()) {
         // If update - module name must be specified
         if (!add && ((moduleName == null) || (moduleName.trim().length() == 0)))
            throw new HotBeansException("Module name not specified!");

         RepositoryFileLock fileLock = null;
         File moduleTempFile = null;
         InputStream moduleTempFileStream = null;
         try {
            // Save module file to temp file
            moduleTempFile = File.createTempFile("hotBeanModule", ".jar");
            FileCopyUtils.copy(moduleFileStream, new FileOutputStream(moduleTempFile));

            // Get name from mainfest
            Manifest manifest = ModuleManifestUtils.readManifest(moduleTempFile);
            String jarFileModuleName = ModuleManifestUtils.getName(manifest);

            if (logger.isDebugEnabled()) logger.debug("Module name in module manifest: '" + jarFileModuleName + "'.");

            // Validate name
            if (add) {
               if ((jarFileModuleName == null) || (jarFileModuleName.trim().length() == 0)) throw new InvalidModuleNameException(
                        "Module name not specified!");
               else if (super.getHotBeanModule(jarFileModuleName) != null)
                  throw new ModuleAlreadyExistsException("Module name already exists!");
            } else if (!moduleName.equals(jarFileModuleName))
               throw new InvalidModuleNameException("Module name in jar file doesn't match specified module name!");

            moduleName = jarFileModuleName;
            moduleTempFileStream = new FileInputStream(moduleTempFile);

            if (add & logger.isInfoEnabled()) logger.info("Adding module '" + moduleName + "'.");

            fileLock = this.obtainRepositoryFileLock(false); // Obtain lock

            File moduleDirectory = new File(this.moduleRepositoryDirectory, moduleName);
            if (!moduleDirectory.exists()) moduleDirectory.mkdirs();

            // Get next revision number
            revisionNumber = this.getLastRevisionOnFileSystem(moduleName);
            if (logger.isDebugEnabled()) {
               if (add) logger.debug("Adding module - last revision on file system: " + revisionNumber + ".");
               else logger.debug("Updating module - last revision on file system: " + revisionNumber + ".");
            }
            if (revisionNumber < 0) revisionNumber = 0;
            File moduleFile = new File(moduleDirectory, revisionNumber + MODULE_FILE_SUFFIX);

            while (moduleFile.exists()) // This should't really be necessary, but still...
            {
               revisionNumber++;
               moduleFile = new File(moduleDirectory, revisionNumber + MODULE_FILE_SUFFIX);
            }

            if (logger.isDebugEnabled()) {
               if (add) logger.debug("Adding module - revision of new module: " + revisionNumber + ".");
               else logger.debug("Updating module - revision of new module: " + revisionNumber + ".");
            }

            // Save module file
            FileCopyUtils.copy(moduleTempFileStream, new FileOutputStream(moduleFile));

            // Deploy at once
            hotBeanModuleInfo = this.loadModule(moduleName, revisionNumber);
         } catch (Exception e) {
            String moduleNameString = "";
            if (moduleName != null) moduleNameString = "'" + moduleName + "' ";

            if (add) {
               logger.error("Error adding module " + moduleNameString + "- " + e, e);
               if (e instanceof HotBeansException) throw (HotBeansException) e;
               else throw new HotBeansException("Error adding module " + moduleNameString + "- " + e, e);
            } else {
               logger.error("Error updating module " + moduleNameString + "- " + e, e);
               if (e instanceof HotBeansException) throw (HotBeansException) e;
               else throw new HotBeansException("Error updating module " + moduleNameString + "- " + e, e);
            }
         } finally {
            this.releaseRepositoryFileLock(fileLock);
            fileLock = null;

            if (moduleTempFileStream != null) {
               // Delete temp file
               try {
                  moduleTempFileStream.close();
               } catch (Exception e) {
               }
            }
            if (moduleTempFile != null) FileDeletor.delete(moduleTempFile);
         }
      }

      return hotBeanModuleInfo;
   }

   /**
    * Loads a module.
    */
   protected HotBeanModuleInfo loadModule(final String moduleName, final long revision) throws Exception {
      Log logger = this.getLog();
      if (logger.isInfoEnabled()) logger.info("Loading module '" + moduleName + "', revision " + revision + ".");

      File moduleDirectory = new File(this.moduleRepositoryDirectory, moduleName);
      File moduleFile = new File(moduleDirectory, revision + MODULE_FILE_SUFFIX);
      File tempDir = new File(this.temporaryDirectory, moduleName + "." + revision);

      Manifest manifest = ModuleManifestUtils.readManifest(moduleFile);
      // Get version from mainfest
      String version = ModuleManifestUtils.getVersion(manifest);
      if ((version == null) || (version.trim().length() == 0)) version = "n/a";
      // Get description from mainfest
      String description = ModuleManifestUtils.getDescription(manifest);

      HotBeanModuleInfo hotBeanModuleInfo = new HotBeanModuleInfo(moduleName, description, revision, version,
               moduleFile.lastModified());

      HotBeanModuleLoader hotBeanModuleLoader = null;
      HotBeanContext hotBeanContext = null;
      String errorReason = null;

      HotBeanModule hotBeanModule = null;

      try {
         // Create loader
         hotBeanModuleLoader = super.createHotBeanModuleLoader(moduleFile, tempDir);

         // Create context
         hotBeanContext = super.createHotBeanContext(this, manifest, hotBeanModuleLoader.getClassLoader());

         if ((hotBeanContext instanceof ConfigurableApplicationContext) && (this.parentApplicationContext != null)) {
            if (logger.isInfoEnabled())
               logger.info("Setting parent Spring ApplicationContext for HotBeanContext(" + hotBeanContext
                        + ") of module '" + moduleName + "', revision " + revision + ".");
            ((ConfigurableApplicationContext) hotBeanContext).setParent(this.parentApplicationContext);
         }

         // Initialize context
         hotBeanContext.init();

         // Create module
         hotBeanModule = super.createHotBeanModule(hotBeanModuleInfo, hotBeanModuleLoader, hotBeanContext);

         // Init loader
         hotBeanModuleLoader.init(hotBeanModule);
      } catch (Exception e) {
         hotBeanModuleLoader = null;
         hotBeanContext = null;
         hotBeanModule = null;
         errorReason = e.getMessage();
         logger.error("Error loading module '" + moduleName + "', revision " + revision + " - " + errorReason + "!", e);
      }

      if (hotBeanModule == null) {
         hotBeanModule = super.createHotBeanModule(hotBeanModuleInfo);
         hotBeanModule.setError(errorReason);
      }

      // Register HotBeanModule
      super.registerHotBeanModule(hotBeanModule);

      if ((hotBeanContext != null) && logger.isInfoEnabled())
         logger.info("Module '" + moduleName + "', revision " + revision + " loaded.");

      return hotBeanModuleInfo;
   }

   /**
    * Registers an unloaded (history) module.
    */
   protected void registerHistoryModule(final String moduleName, final long revision) throws Exception {
      Log logger = this.getLog();

      if (logger.isInfoEnabled())
         logger.info("Registering unloaded module '" + moduleName + "', revision " + revision + ".");

      File moduleDirectory = new File(this.moduleRepositoryDirectory, moduleName);
      File moduleFile = new File(moduleDirectory, revision + MODULE_FILE_SUFFIX);

      Manifest manifest = ModuleManifestUtils.readManifest(moduleFile);
      // Get version from mainfest
      String version = ModuleManifestUtils.getVersion(manifest);
      if ((version == null) || (version.trim().length() == 0)) version = "n/a";
      // Get description from mainfest
      String description = ModuleManifestUtils.getDescription(manifest);

      HotBeanModuleInfo hotBeanModuleInfo = new HotBeanModuleInfo(moduleName, description, revision, version,
               moduleFile.lastModified());
      HotBeanModule hotBeanModue = super.createHotBeanModule(hotBeanModuleInfo);

      // Register HotBeanModule
      super.registerHotBeanModule(hotBeanModue);

      if (logger.isInfoEnabled())
         logger.info("Unloaded module '" + moduleName + "', revision " + revision + " registered.");
   }

   /**
    * Gets the last revision on the file system.
    */
   private long getLastRevisionOnFileSystem(final String moduleName) {
      long lastRevision = -1;
      long[] revisionNumbers = getRevisionsOnFileSystem(moduleName);
      if ((revisionNumbers != null) && (revisionNumbers.length > 0)) {
         lastRevision = revisionNumbers[revisionNumbers.length - 1];
      }

      return lastRevision;
   }

   /**
    * Gets the revisions on the file system.
    */
   private long[] getRevisionsOnFileSystem(final String moduleName) {
      File moduleDirectory = new File(this.moduleRepositoryDirectory, moduleName);
      long[] revisionNumbers = null;

      File[] moduleFiles = moduleDirectory.listFiles(ModuleFileFilter);
      if (moduleFiles.length > 0) {
         revisionNumbers = new long[moduleFiles.length];
         String fileName;

         // Gets and sort revisions
         for (int i = 0; i < moduleFiles.length; i++) {
            fileName = moduleFiles[i].getName();
            try {
               revisionNumbers[i] = Long
                        .parseLong(fileName.substring(0, fileName.length() - MODULE_FILE_SUFFIX_LENGTH));
            } catch (Exception e) {
               revisionNumbers[i] = -1;
            }
         }
         Arrays.sort(revisionNumbers);
      }

      return revisionNumbers;
   }

   /* ### MISC UTILITY METHODS END ### */

   /* ### PERIODIC CHECK METHODS BEGIN ### */

   /**
    * Performs a repository check by invoking {@link #checkForModuleUpdates()} and the super class implementation of
    * this method. This method is invoked by the timer used by {@link PeriodicCheckHotBeanModuleRepository}.
    */
   protected void performRepositoryCheck() {
      synchronized (super.getLock()) {
         if (this.isReady()) {
            this.checkForModuleUpdates();
            super.performRepositoryCheck();
         }
      }
   }

   /**
    * Checks for module updates.
    */
   protected void checkForModuleUpdates() {
      Log logger = this.getLog();
      if (logger.isDebugEnabled())
         logger.debug("Checking for updated modules in path '" + this.moduleRepositoryDirectory + "'.");

      synchronized (super.getLock()) {
         RepositoryFileLock fileLock = null;
         try {
            fileLock = this.obtainRepositoryFileLockNoRetries(true); // Obtain lock without retries since this method
                                                                     // will be executed again in the
            // near future...

            File[] moduleDirectories = this.moduleRepositoryDirectory.listFiles();
            ArrayList activeModuleNames = new ArrayList(Arrays.asList(super.getHotBeanModuleNames()));

            if (moduleDirectories != null) {
               String moduleName;

               for (int i = 0; i < moduleDirectories.length; i++) {
                  if (moduleDirectories[i].isDirectory()) {
                     moduleName = moduleDirectories[i].getName();

                     if (moduleName != null) {
                        activeModuleNames.remove(moduleName);
                        this.checkModuleDirectory(moduleName, moduleDirectories[i]);
                     }
                  }
               }
            }

            // Check for deleted modules...
            Iterator deletedModulesIterator = activeModuleNames.iterator();
            // HotBeanModule module;
            HotBeanModuleType moduleType;

            while (deletedModulesIterator.hasNext()) {
               moduleType = super.getHotBeanModuleType((String) deletedModulesIterator.next());
               if (moduleType != null) moduleType.setRemoveType(true); // Set remove flag of type
            }
         } catch (Exception e) {
            logger.error("Error checking for updated modules - " + e + "!", e);
            // TODO: Handle/log exception
         } finally {
            this.releaseRepositoryFileLock(fileLock);
            fileLock = null;
         }
      }
   }

   /**
    * Check a module directory.
    */
   private void checkModuleDirectory(final String moduleName, final File moduleDirectory) throws Exception {
      long[] revisionNumbersOnFileSystem = this.getRevisionsOnFileSystem(moduleName);

      if ((revisionNumbersOnFileSystem != null) && (revisionNumbersOnFileSystem.length > 0)) {
         long lastRevision = revisionNumbersOnFileSystem[revisionNumbersOnFileSystem.length - 1];

         HotBeanModule[] registeredRevisions = super.getHotBeanModules(moduleName); // Get registered revisions
         boolean newModule = true;
         boolean lastRevisionAlreadyRegistered = false;

         // Check if the latest revision on file system is already registered
         if ((registeredRevisions != null) && (registeredRevisions.length > 0)) {
            newModule = false;

            for (int i = 0; i < registeredRevisions.length; i++) {
               if (registeredRevisions[i].getRevision() == lastRevision) {
                  lastRevisionAlreadyRegistered = true;
                  break;
               }
            }
         }

         if (newModule) // If this is a new module...
         {
            // ...register revision history (this will only happen once, directly after start up)
            for (int i = 0; i < (revisionNumbersOnFileSystem.length - 1); i++) {
               this.registerHistoryModule(moduleName, revisionNumbersOnFileSystem[i]);
            }
         }

         if (!lastRevisionAlreadyRegistered) {
            this.loadModule(moduleName, lastRevision);
         }
      }
   }

   /* ### PERIODIC CHECK METHODS END ### */
}
