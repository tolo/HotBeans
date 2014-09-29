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

import hotbeans.BeanNotFoundException;
import hotbeans.HotBeanContext;
import hotbeans.HotBeanContextFactory;
import hotbeans.HotBeanModule;
import hotbeans.HotBeanModuleInfo;
import hotbeans.HotBeanModuleLoader;
import hotbeans.HotBeanModuleLoaderFactory;
import hotbeans.HotBeanModuleRepository;
import hotbeans.ModuleNotFoundException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;

/**
 * Abstract HotBeanModuleRepository implementation providing basic support for HotBeanModuleRepository implementations.<br>
 * <br>
 * This class makes it possible to specify factories for creating {@link HotBeanContext} objects ({@link HotBeanContextFactory})
 * and {@link HotBeanModuleLoader} objects ({@link HotBeanModuleLoaderFactory}). If no factories are specified,
 * default implementations will be used. These default implementations will simply create implementations of the types
 * {@link SpringHotBeanContext} and {@link JarFileHotBeanModuleLoader}.
 * 
 * @author Tobias Löfstrand
 */
public abstract class AbstractHotBeanModuleRepository implements ProxyAccessHotBeanModuleRepository, BeanNameAware {

   private Log log;

   private String name = "AbstractHotBeanModuleRepository";

   private final Object lock;

   private final HashMap moduleRegistry; // Module name (String) -> ArrayList (HotBeanModule)

   private HotBeanContextFactory hotBeanContextFactory;

   private HotBeanModuleLoaderFactory hotBeanModuleLoaderFactory;

   /**
    * Creates a new AbstractHotBeanModuleRepository, using this as lock (mutex) object.
    */
   protected AbstractHotBeanModuleRepository() {
      this(null);
   }

   /**
    * Creates a new AbstractHotBeanModuleRepository, using parameter <code>lock</code> as lock (mutex) object.
    */
   protected AbstractHotBeanModuleRepository(Object lock) {
      this.moduleRegistry = new HashMap();

      if (lock == null) this.lock = this;
      else this.lock = lock;
   }

   /**
    * Gets the log used by this component. This method will create the logger, if not created, using the name of this
    * class combined with the name ({@link #getName()}) of this component. This means that this method shouldn't be
    * called before the name is set.
    */
   protected Log getLog() {
      synchronized (this.getLock()) {
         if (this.log == null) this.log = LogFactory.getLog(this.getClass().getName() + "." + this.getName());
         return this.log;
      }
   }

   /**
    * Set the name of the bean in the bean factory that created this bean.
    * <p>
    * Invoked after population of normal bean properties but before an init callback like InitializingBean's
    * afterPropertiesSet or a custom init-method.
    * 
    * @param name
    *           the name of the bean in the factory
    */
   public void setBeanName(String name) {
      this.setName(name);
   }

   /**
    * Gets the name of this HotBeanModuleRepository.
    */
   public String getName() {
      return name;
   }

   /**
    * Sets the name of this HotBeanModuleRepository.
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * Get the which is used for synchronizing thread access to protected resources.
    */
   protected Object getLock() {
      return lock;
   }

   /**
    * Get the {@link HotBeanContextFactory} for creating {@link HotBeanContext} objects. If no factory is specified,
    * this class created a default implementation.
    */
   public HotBeanContextFactory getHotBeanContextFactory() {
      return hotBeanContextFactory;
   }

   /**
    * Set the {@link HotBeanContextFactory} for creating {@link HotBeanContext} objects. If no factory is specified,
    * this class created a default implementation.
    */
   public void setHotBeanContextFactory(HotBeanContextFactory hotBeanContextFactory) {
      this.hotBeanContextFactory = hotBeanContextFactory;
   }

   /**
    * Get the {@link HotBeanModuleLoaderFactory} for creating {@link HotBeanModuleLoader} objects. If no factory is
    * specified, this class created a default implementation.
    */
   public HotBeanModuleLoaderFactory getHotBeanModuleLoaderFactory() {
      return hotBeanModuleLoaderFactory;
   }

   /**
    * Set the {@link HotBeanModuleLoaderFactory} for creating {@link HotBeanModuleLoader} objects. If no factory is
    * specified, this class created a default implementation.
    */
   public void setHotBeanModuleLoaderFactory(HotBeanModuleLoaderFactory hotBeanModuleLoaderFactory) {
      this.hotBeanModuleLoaderFactory = hotBeanModuleLoaderFactory;
   }

   /**
    * Initializes this AbstractHotBeanModuleRepository. Subclasses may override this method, but should call the super
    * class implementation.
    */
   public void init() throws Exception {
      if (getLog().isInfoEnabled())
         getLog().info(
                  "Initializing " + this.getName() + " (version "
                           + AbstractHotBeanModuleRepository.class.getPackage().getImplementationVersion() + ").");

      if (this.hotBeanContextFactory == null) {
         // this.hotBeanContextFactory = new DefaultHotBeanContextFactory();
         this.hotBeanContextFactory = new HotBeanContextFactory() {

            public HotBeanContext createHotBeanContext(final HotBeanModuleRepository hotBeanModuleRepository,
                     final Manifest moduleManifest, final ClassLoader classLoader) throws Exception {
               return new SpringHotBeanContext(classLoader);
            }
         };
      }

      if (this.hotBeanModuleLoaderFactory == null) {
         this.hotBeanModuleLoaderFactory = new HotBeanModuleLoaderFactory() {

            public HotBeanModuleLoader createHotBeanModuleLoader(final File moduleJarFile, final File tempDir)
                     throws Exception {
               return new JarFileHotBeanModuleLoader(moduleJarFile, tempDir);
            }
         };
      }
   }

   /**
    * Destroys this AbstractHotBeanModuleRepository. Subclasses may override this method, but should call the super
    * class implementation.
    */
   public void destroy() throws Exception {
      Log logger = this.getLog();

      synchronized (this.lock) {
         String[] moduleNames = this.getHotBeanModuleNames();
         if (logger.isInfoEnabled())
            logger.info("Destroying " + this.getName() + " (" + ((moduleNames != null) ? moduleNames.length : 0)
                     + " modules).");
         HotBeanModuleType moduleType;
         HotBeanModule[] modules;
         for (int i = 0; i < moduleNames.length; i++) {
            if (logger.isDebugEnabled()) logger.debug("Unloading revisions of module " + moduleNames[i] + ".");

            moduleType = this.getHotBeanModuleType(moduleNames[i]);
            if (moduleType != null) {
               modules = moduleType.getModules();
               for (int j = 0; j < modules.length; j++) {
                  if (logger.isDebugEnabled()) logger.debug("Checking" + modules[i] + ".");

                  if (modules[j].isActive() || modules[j].isInactive()) {
                     if (logger.isDebugEnabled()) logger.debug("Unloading " + modules[j] + ".");
                     modules[j].unload();
                  }
                  this.unregisterHotBeanModule(modules[j]);
               }
            }
         }
      }
   }

   /**
    * Reinitializes this AbstractHotBeanModuleRepository.
    */
   public void reinitialize() {
      try {
         this.destroy();
         this.init();
      } catch (Exception e) {
         throw new RuntimeException("Error reinitializing - " + e + "!", e);
      }
   }

   /**
    * Creates HotBeanContext, using the configured factory (see {@link #setHotBeanContextFactory(HotBeanContextFactory)}.
    */
   protected HotBeanContext createHotBeanContext(final HotBeanModuleRepository hotBeanModuleRepository,
            final Manifest moduleManifest, final ClassLoader classLoader) throws Exception {
      return this.hotBeanContextFactory.createHotBeanContext(hotBeanModuleRepository, moduleManifest, classLoader);
   }

   /**
    * Creates HotBeanModuleLoader, using the configured factory (see
    * {@link #setHotBeanModuleLoaderFactory(HotBeanModuleLoaderFactory)}.
    */
   protected HotBeanModuleLoader createHotBeanModuleLoader(final File moduleJarFile, final File tempDir)
            throws Exception {
      return this.hotBeanModuleLoaderFactory.createHotBeanModuleLoader(moduleJarFile, tempDir);
   }

   /* ### HotBeanModuleRepository METHODS BEGIN ### */

   /**
    * Gets the names of all existing hot bean modules.
    */
   public String[] getHotBeanModuleNames() {
      synchronized (this.lock) {
         Set moduleNames = (Set) this.moduleRegistry.keySet();
         if (moduleNames != null) { return (String[]) moduleNames.toArray(new String[] {}); }
      }

      return new String[0];
   }

   /**
    * Gets information about the latest revisions of all existing hot bean modules.
    */
   public HotBeanModuleInfo[] getHotBeanModuleInfo() {
      ArrayList hotBeanModuleInfo = new ArrayList();

      synchronized (this.lock) {
         Set moduleNames = (Set) this.moduleRegistry.keySet();
         if (moduleNames != null) {
            Iterator it = moduleNames.iterator();
            HotBeanModuleType moduleType;
            HotBeanModuleInfo moduleInfo;

            while (it.hasNext()) // Iterate over module names
            {
               moduleType = (HotBeanModuleType) this.moduleRegistry.get(it.next());
               if (moduleType != null) {
                  moduleInfo = moduleType.getCurrentModuleInfo();
                  if (moduleInfo != null) hotBeanModuleInfo.add(moduleInfo.getClone()); // Get clone of module info
               }
            }
         }
      }

      return (HotBeanModuleInfo[]) hotBeanModuleInfo.toArray(new HotBeanModuleInfo[0]);
   }

   /**
    * Gets information about all revisions for a specific hot bean module.
    */
   public HotBeanModuleInfo[] getHotBeanModuleInfo(final String moduleName) {
      ArrayList hotBeanModuleInfo = new ArrayList();

      synchronized (this.lock) {
         HotBeanModuleType moduleType = (HotBeanModuleType) this.moduleRegistry.get(moduleName);
         if (moduleType != null) {
            HotBeanModuleInfo[] moduleInfo = moduleType.getModuleInfo();
            for (int i = 0; i < moduleInfo.length; i++) {
               hotBeanModuleInfo.add(moduleInfo[i].getClone()); // Get clone of module info
            }
         }
      }

      return (HotBeanModuleInfo[]) hotBeanModuleInfo.toArray(new HotBeanModuleInfo[0]);
   }

   /**
    * Gets information about the current revisions of a specific hot bean module.
    */
   public HotBeanModuleInfo getCurrentHotBeanModuleInfo(String moduleName) {
      synchronized (this.lock) {
         HotBeanModuleType hotBeanModuleType = (HotBeanModuleType) this.moduleRegistry.get(moduleName);
         if (hotBeanModuleType != null) { return hotBeanModuleType.getCurrentModuleInfo(); }
      }

      return null;
   }

   /**
    * Checks if a module with the specified name exists.
    */
   public boolean hasHotBeanModule(final String moduleName) {
      synchronized (this.lock) {
         return this.moduleRegistry.containsKey(moduleName);
      }
   }

   /**
    * Checks if the specified module contains a bean with the specified name.
    * 
    * @since 1.0.1 (20070302)
    */
   public boolean hasHotBean(final String moduleName, final String beanName) {
      HotBeanModule hotBeanModule = getHotBeanModule(moduleName);
      if ((hotBeanModule != null) && (hotBeanModule.getHotBeanContext() != null)) {
         return hotBeanModule.getHotBeanContext().hasHotBean(beanName);
      } else return false;
   }

   /**
    * Gets a reference to a the hot bean with the specified name in the specified module. This method will return an
    * object even if the hot bean module doesn't exist (yet). When invoking methods on the proxy, exceptions of the
    * types {@link ModuleNotFoundException} and {@link BeanNotFoundException} will be thrown to indicate that the module
    * or bean wasn't found.
    */
   public Object getHotBean(final String moduleName, final String beanName, final Class interfaceClass) {
      return getHotBeanProxyFactory(moduleName, beanName, new Class[] { interfaceClass }).getProxy();
   }

   /**
    * Gets a reference to a the hot bean with the specified name in the specified module. This method will return an
    * object even if the hot bean module doesn't exist (yet). When invoking methods on the proxy, exceptions of the
    * types {@link ModuleNotFoundException} and {@link BeanNotFoundException} will be thrown to indicate that the module
    * or bean wasn't found.
    */
   public Object getHotBean(final String moduleName, final String beanName, final Class[] interfaceClasses) {
      return getHotBeanProxyFactory(moduleName, beanName, interfaceClasses).getProxy();
   }

   /**
    * Gets the class name of the bean with the specified name in the specified module.
    * 
    * @since 1.0.1 (20070212)
    */
   public String getHotBeanClassName(final String moduleName, final String beanName) {
      HotBeanModule hotBeanModule = getHotBeanModule(moduleName);
      if ((hotBeanModule != null) && (hotBeanModule.getHotBeanContext() != null)) {
         return hotBeanModule.getHotBeanContext().getHotBeanClassName(beanName);
      } else return null;
   }

   /**
    * Gets the class of the bean with the specified name in the specified module.
    * 
    * @since 1.0.1 (20070212)
    */
   public Class getHotBeanClass(final String moduleName, final String beanName) {
      HotBeanModule hotBeanModule = getHotBeanModule(moduleName);
      if ((hotBeanModule != null) && (hotBeanModule.getHotBeanContext() != null)) {
         return hotBeanModule.getHotBeanContext().getHotBeanClass(beanName);
      } else return null;
   }

   /**
    * Gets a HotBeanProxyFactory object used for creating proxies to a hot bean with the specified name in the specified
    * module. This method will return an object even if the hot bean module doesn't exist (yet). When invoking methods
    * on proxies obtained through the returned HotBeanProxyFactory, exceptions of the types
    * {@link ModuleNotFoundException} and {@link BeanNotFoundException} will be thrown to indicate that the module or
    * bean wasn't found.
    */
   public HotBeanProxyFactory getHotBeanProxyFactory(final String moduleName, final String beanName,
            final Class[] interfaceClasses) {
      return new HotBeanProxyFactory(this, moduleName, beanName, interfaceClasses);
   }

   /**
    * Called to validate a HotBeanProxyFactory and module and bean references.
    */
   public void validateHotBeanProxyFactory(final HotBeanProxyFactory hotBeanProxyFactory) {
      boolean hotModuleSwapped = false;
      HotBeanModule currentModule = hotBeanProxyFactory.getCurrentModule();

      Log logger = this.getLog();
      if (logger.isDebugEnabled())
         logger.debug("Validating " + hotBeanProxyFactory + " - current module: " + currentModule + ".");

      if ((currentModule != null) && !currentModule.isActive()) currentModule = null; // If current module is
                                                                                       // inactive...

      if (currentModule == null) {
         currentModule = this.getHotBeanModule(hotBeanProxyFactory.getModuleName());
         hotModuleSwapped = true;
      }

      // Swap target
      if (hotModuleSwapped) {
         if (logger.isDebugEnabled())
            logger.debug("Swapping module of " + hotBeanProxyFactory + " - new current module: " + currentModule + ".");

         Object hotBean = null;

         // Get bean from hot bean context
         if (currentModule != null) hotBean = currentModule.getHotBean(hotBeanProxyFactory.getBeanName());
         hotBeanProxyFactory.updateHotBeanModuleAndBean(currentModule, hotBean);
      }
   }

   /* ### HotBeanModuleRepository METHODS END ### */

   /* ### INTERNAL/SUBCLASS METHODS BEGIN ### */

   /**
    * Creates an unloaded {@link HotBeanModule}. This method is provided for use by sub classes to create a new
    * HotBeanModule.
    */
   protected HotBeanModule createHotBeanModule(HotBeanModuleInfo hotBeanModuleInfo) {
      return new HotBeanModule(hotBeanModuleInfo);
   }

   /**
    * Creates a loaded and active {@link HotBeanModule}. This method is provided for use by sub classes to create a new
    * HotBeanModule.
    */
   protected HotBeanModule createHotBeanModule(HotBeanModuleInfo hotBeanModuleInfo,
            HotBeanModuleLoader hotBeanModuleLoader, HotBeanContext hotBeanContext) {
      return new HotBeanModule(hotBeanModuleInfo, hotBeanModuleLoader, hotBeanContext);
   }

   /**
    * Gets the {@link HotBeanModule} with the specified name.
    */
   protected HotBeanModule getHotBeanModule(final String moduleName) {
      synchronized (this.lock) {
         HotBeanModuleType hotBeanModuleType = (HotBeanModuleType) this.moduleRegistry.get(moduleName);
         if (hotBeanModuleType != null) { return hotBeanModuleType.getCurrentModule(); }
      }

      return null;
   }

   /**
    * Gets the HotBeanModuleType, containing information about all modules (revisions) associated with a specific module
    * name. Note that the returned object must not be manipulated without holding a lock on {@link #lock}.
    */
   protected HotBeanModuleType getHotBeanModuleType(final String moduleName) {
      synchronized (this.lock) {
         return (HotBeanModuleType) this.moduleRegistry.get(moduleName);
      }
   }

   /**
    * Gets all revisions of a module with the specified name.
    */
   protected HotBeanModule[] getHotBeanModules(final String moduleName) {
      synchronized (this.lock) {
         HotBeanModuleType hotBeanModuleType = this.getHotBeanModuleType(moduleName);
         if (hotBeanModuleType != null) return hotBeanModuleType.getModules();
      }

      return new HotBeanModule[0];
   }

   /**
    * Registers a module revision.
    */
   protected void registerHotBeanModule(final HotBeanModule module) {
      Log logger = this.getLog();
      if (logger.isDebugEnabled()) logger.debug("Registering module " + module.toString(false) + ".");

      synchronized (this.lock) {
         final String moduleName = module.getName();
         HotBeanModuleType hotBeanModuleType = this.getHotBeanModuleType(moduleName);
         if (hotBeanModuleType == null) {
            hotBeanModuleType = new HotBeanModuleType(moduleName);
            this.moduleRegistry.put(moduleName, hotBeanModuleType);
         }

         hotBeanModuleType.addModule(module); // Add as the last module revision
      }
   }

   /**
    * Unregisters a module revision, and possibly the whole module type.
    */
   protected void unregisterHotBeanModule(final HotBeanModule module) {
      Log logger = this.getLog();
      if (logger.isDebugEnabled()) logger.debug("Unregistering module " + module.toString(false) + ".");

      synchronized (this.lock) {
         String moduleName = module.getName();
         HotBeanModuleType hotBeanModuleType = this.getHotBeanModuleType(moduleName);
         if (hotBeanModuleType != null) {
            hotBeanModuleType.removeModule(module);
            if (hotBeanModuleType.moduleCount() == 0) this.moduleRegistry.remove(moduleName); // If no revisions left
                                                                                                // - remove key
         }
      }
   }

   /**
    * Checks if the specified module revision is the current revision of the module type.
    */
   protected boolean isCurrentRevision(final HotBeanModule module) {
      if (module == null) return false;
      synchronized (this.lock) {
         return module.equals(this.getHotBeanModule(module.getName()));
      }
   }

   /**
    * Gets the last revision number for the module with the specified name.
    */
   protected long getLastRevision(final String moduleName) {
      synchronized (this.lock) {
         HotBeanModule lastModule = this.getHotBeanModule(moduleName);
         if (lastModule != null) return lastModule.getRevision();
         else return -1;
      }
   }

   /**
    * Checks for obsolete modules.
    */
   protected void checkForObsoleteModules() {
      Log logger = this.getLog();
      if (logger.isDebugEnabled()) logger.debug("Checking for obsolete/inactive modules.");

      synchronized (this.lock) {
         String[] moduleNames = this.getHotBeanModuleNames();

         for (int n = 0; n < moduleNames.length; n++) {
            this.checkForObsoleteModules(moduleNames[n]);
         }
      }
   }

   /**
    * Checks for obsolete revisions of a specific module name.
    */
   protected void checkForObsoleteModules(final String moduleName) {
      Log logger = this.getLog();
      if (logger.isDebugEnabled())
         logger.debug("Checking for obsolete/inactive module revisions for module " + moduleName + ".");

      synchronized (this.lock) {
         HotBeanModule currentModule = this.getHotBeanModule(moduleName); // Get current module for name
         HotBeanModule[] modules = this.getHotBeanModules(moduleName); // Get all revisions for module name
         HotBeanModuleType moduleType = this.getHotBeanModuleType(moduleName);
         boolean isRemoveType = moduleType.isRemoveType();

         for (int r = 0; r < modules.length; r++) {
            if (modules[r] != null) {
               synchronized (modules[r]) {
                  if (logger.isDebugEnabled())
                     logger.debug("Checking module revision " + modules[r] + " - usage count: "
                              + modules[r].getUsageCount());

                  if (modules[r].isActive() || modules[r].isInactive()) // Only check module if active or inactive (i.e
                                                                        // loaded)
                  {
                     boolean obsolete = !modules[r].equals(currentModule); // Module is obsolete if it isn't the
                                                                           // latest(current)

                     if (isRemoveType) {
                        if (logger.isDebugEnabled())
                           logger.debug("Unloading removed module " + modules[r].toString(false) + ".");
                        modules[r].unload();
                     } else if (obsolete && modules[r].isActive()) // If module is obsolete...
                     {
                        if (logger.isDebugEnabled())
                           logger.debug("Marking obsolete module " + modules[r].toString(false) + " as inactive.");
                        modules[r].inactivate(); // ...mark as inactive....
                     } else if (modules[r].isInactive() && !modules[r].inUse()) // ...and unload it during the next
                                                                                 // check (and when no longer in use)
                     {
                        if (logger.isDebugEnabled())
                           logger.debug("Unloading inactive module " + modules[r].toString(false) + ".");
                        modules[r].unload();
                     }
                  }

                  if (isRemoveType) // Unregister removed module
                  {
                     this.unregisterHotBeanModule(modules[r]);
                  }
               }
            }
         }
      }
   }

   /* ### INTERNAL/SUBCLASS METHODS END ### */
}
