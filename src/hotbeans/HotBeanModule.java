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
package hotbeans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class representing a specific revision of a hot bean module type.
 * 
 * @author Tobias Löfstrand
 */
public class HotBeanModule {

   final Log logger = LogFactory.getLog(this.getClass());

   private final HotBeanModuleInfo hotBeanModuleInfo;

   private HotBeanModuleLoader hotBeanModuleLoader;

   private HotBeanContext hotBeanContext;

   /**
    * Creates an unloaded HotBeanModule.
    */
   public HotBeanModule(HotBeanModuleInfo hotBeanModuleInfo) {
      this(hotBeanModuleInfo, null, null);
   }

   /**
    * Creates a loaded and active HotBeanModule.
    */
   public HotBeanModule(HotBeanModuleInfo hotBeanModuleInfo, HotBeanModuleLoader hotBeanModuleLoader,
            HotBeanContext hotBeanContext) {
      this.hotBeanModuleInfo = hotBeanModuleInfo;
      this.hotBeanModuleLoader = hotBeanModuleLoader;
      this.hotBeanContext = hotBeanContext;

      if (hotBeanContext != null) this.hotBeanModuleInfo.setState(HotBeanModuleInfo.ACTIVE);
      else this.hotBeanModuleInfo.setState(HotBeanModuleInfo.UNLOADED);
   }

   /**
    * Get information about this hot bean module.
    */
   public HotBeanModuleInfo getHotBeanModuleInfo() {
      return hotBeanModuleInfo;
   }

   /**
    * Gets the loader used by this hot bean module.
    */
   public HotBeanModuleLoader getHotBeanModuleLoader() {
      return hotBeanModuleLoader;
   }

   /**
    * Gets the associated {@link HotBeanContext}, in which the module "runs".
    */
   public HotBeanContext getHotBeanContext() {
      return hotBeanContext;
   }

   /* ### HOTBEANMODULEINFO DELEGATE METHODS BEGIN ### */

   /**
    * Gets the module name. This method delegates to the corresponding method in {@link HotBeanModuleInfo}.
    */
   public String getName() {
      return this.hotBeanModuleInfo.getName();
   }

   /**
    * Gets the description. This method delegates to the corresponding method in {@link HotBeanModuleInfo}.
    */
   public String getDescription() {
      return this.hotBeanModuleInfo.getDescription();
   }

   /**
    * Gets the version. This method delegates to the corresponding method in {@link HotBeanModuleInfo}.
    */
   public long getRevision() {
      return this.hotBeanModuleInfo.getRevision();
   }

   /**
    * Gets the revision. This method delegates to the corresponding method in {@link HotBeanModuleInfo}.
    */
   public String getVersion() {
      return hotBeanModuleInfo.getVersion();
   }

   /**
    * Gets the time when the module was deployed. This method delegates to the corresponding method in
    * {@link HotBeanModuleInfo}.
    */
   public long getDeployedAt() {
      return hotBeanModuleInfo.getDeployedAt();
   }

   /**
    * Gets the usage count. This method delegates to the corresponding method in {@link HotBeanModuleInfo}.
    */
   public long getUsageCount() {
      synchronized (this) {
         return hotBeanModuleInfo.getUsageCount();
      }
   }

   /**
    * Increments usage count.
    */
   public void decrementUsageCount() {
      synchronized (this) {
         hotBeanModuleInfo.setUsageCount(hotBeanModuleInfo.getUsageCount() - 1);
      }
   }

   /**
    * Decrements usage count.
    */
   public void incrementUsageCount() {
      synchronized (this) {
         hotBeanModuleInfo.setUsageCount(hotBeanModuleInfo.getUsageCount() + 1);
      }
   }

   /**
    * Checks if this module is in use.
    */
   public boolean inUse() {
      synchronized (this) {
         return hotBeanModuleInfo.getUsageCount() > 0;
      }
   }

   /**
    * Gets the state of the module. This method delegates to the corresponding method in {@link HotBeanModuleInfo}.
    */
   public int getState() {
      synchronized (this) {
         return hotBeanModuleInfo.getState();
      }
   }

   /**
    * Gets a human readable description of the state of the module. This method delegates to the corresponding method in
    * {@link HotBeanModuleInfo}.
    */
   public String getStateDescription() {
      synchronized (this) {
         return hotBeanModuleInfo.getStateDescription();
      }
   }

   /**
    * Sets the state of the module. This method delegates to the corresponding method in {@link HotBeanModuleInfo}.
    */
   public void setState(int state) {
      synchronized (this) {
         hotBeanModuleInfo.setState(state);

         if (this.logger.isDebugEnabled())
            this.logger.debug("Module " + this.getName() + " rev." + this.getRevision() + " changed state to "
                     + this.getStateDescription() + ".");
      }
   }

   /**
    * Gets the time of the last state change. This method delegates to the corresponding method in
    * {@link HotBeanModuleInfo}.
    */
   public long getLastStateChange() {
      return hotBeanModuleInfo.getLastStateChange();
   }

   /**
    * Checks if this module is in the {@link HotBeanModuleInfo#ERROR} state.
    */
   public boolean isError() {
      return this.getState() == HotBeanModuleInfo.ERROR;
   }

   /**
    * Put this module in the {@link HotBeanModuleInfo#ERROR} state.
    */
   public void setError(String errorReason) {
      this.setState(HotBeanModuleInfo.ERROR);
      this.hotBeanModuleInfo.setErrorReason(errorReason);
   }

   /**
    * Checks if this module is in the {@link HotBeanModuleInfo#UNLOADED} state.
    */
   public boolean isUnloaded() {
      return this.getState() == HotBeanModuleInfo.UNLOADED;
   }

   /**
    * Put this module in the {@link HotBeanModuleInfo#UNLOADED} state.
    */
   public void unloaded() {
      this.setState(HotBeanModuleInfo.UNLOADED);
   }

   /**
    * Checks if this module is in the {@link HotBeanModuleInfo#ACTIVE} state.
    */
   public boolean isActive() {
      return this.getState() == HotBeanModuleInfo.ACTIVE;
   }

   /**
    * Put this module in the {@link HotBeanModuleInfo#ACTIVE} state.
    */
   public void activate() {
      this.setState(HotBeanModuleInfo.ACTIVE);
   }

   /**
    * Checks if this module is in the {@link HotBeanModuleInfo#INACTIVE} state.
    */
   public boolean isInactive() {
      return this.getState() == HotBeanModuleInfo.INACTIVE;
   }

   /**
    * Put this module in the {@link HotBeanModuleInfo#INACTIVE} state.
    */
   public void inactivate() {
      this.setState(HotBeanModuleInfo.INACTIVE);
   }

   /**
    * Checks if this module is in the {@link HotBeanModuleInfo#UNLOADING} state.
    */
   public boolean isUnloading() {
      return this.getState() == HotBeanModuleInfo.UNLOADING;
   }

   /**
    * Put this module in the {@link HotBeanModuleInfo#UNLOADING} state.
    */
   public void unload() {
      synchronized (this) {
         this.setState(HotBeanModuleInfo.UNLOADING);

         // Execute destruction of loader and context in a separate thread (to avoid synchronization issues - mostly
         // during testing actually...).
         new UnloadThread(this, this.hotBeanModuleLoader, this.hotBeanContext);

         this.hotBeanContext = null;
         this.hotBeanModuleLoader = null;
      }
   }

   /**
    * Gets the error reason. This method delegates to the corresponding method in {@link HotBeanModuleInfo}.
    */
   public String getErrorReason() {
      return hotBeanModuleInfo.getErrorReason();
   }

   /* ### HOTBEANMODULEINFO DELEGATE METHODS END ### */

   /**
    * Gets the bean with the specified name from this module.
    */
   public Object getHotBean(final String beanName) {
      synchronized (this) {
         if (this.hotBeanContext != null) return this.hotBeanContext.getHotBean(beanName);
         else return null;
      }
   }

   /**
    * Gets a string representation of this HotBeanModule.
    */
   public String toString() {
      return this.toString(true);
   }

   /**
    * Gets a string representation of this HotBeanModule.
    */
   public String toString(boolean includeClassName) {
      StringBuffer toStringString = new StringBuffer();

      toStringString.append("[");
      toStringString.append(this.getName()).append(", ");
      if (this.getDescription() != null) toStringString.append(this.getDescription()).append(", ");
      toStringString.append(this.getVersion());
      toStringString.append(" (").append(this.getRevision()).append(")");
      toStringString.append(" - ").append(this.getStateDescription());
      if (this.isError()) toStringString.append(": ").append(this.getErrorReason());
      toStringString.append("]");

      if (includeClassName) return "HotBeanModule" + toStringString;
      else return toStringString.toString();
   }

   /* ### ### */

   /**
    * Thread class for unloading a module.
    */
   private static final class UnloadThread extends Thread {

      private HotBeanModule module;

      private HotBeanModuleLoader hotBeanModuleLoader;

      private HotBeanContext hotBeanContext;

      public UnloadThread(HotBeanModule module, HotBeanModuleLoader hotBeanModuleLoader, HotBeanContext hotBeanContext) {
         super("HotBeanModule(" + module.getName() + " rev." + module.getRevision() + ") unload thread");

         this.module = module;
         this.hotBeanModuleLoader = hotBeanModuleLoader;
         this.hotBeanContext = hotBeanContext;

         this.setDaemon(true);
         this.start();
      }

      public void run() {
         if (this.module.logger.isDebugEnabled())
            this.module.logger.debug("Starting destruction of HotBeanModule(" + this.module.getName() + " rev."
                     + this.module.getRevision() + ").");

         try {
            if (this.hotBeanContext != null) this.hotBeanContext.destroy();
         } catch (Exception e) {
         }

         try {
            if (this.hotBeanModuleLoader != null) this.hotBeanModuleLoader.destroy();
         } catch (Exception e) {
         }

         if (this.module.logger.isDebugEnabled())
            this.module.logger.debug("Destruction of HotBeanModule(" + this.module.getName() + " rev."
                     + this.module.getRevision() + ") complete.");

         this.module = null;
         this.hotBeanModuleLoader = null;
         this.hotBeanContext = null;

         System.runFinalization();
         System.gc();
      }
   }
}
