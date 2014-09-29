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

import java.io.InputStream;

/**
 * HotBeanModuleRepository represents a repository of hot bean modules and is the core of the HotBeans framework.
 * 
 * @author Tobias Löfstrand
 */
public interface HotBeanModuleRepository {

   /**
    * Gets the name of this HotBeanModuleRepository.
    */
   public String getName();

   /**
    * Adds (installs) a new hot bean module.
    */
   public HotBeanModuleInfo addHotBeanModule(InputStream moduleFile);

   /**
    * Updates an existing hot beans module with a new revision.
    */
   public HotBeanModuleInfo updateHotBeanModule(String moduleName, InputStream moduleFile);

   /**
    * Reverts an hot beans module to a previous revision (which becomes a new revision).
    */
   public HotBeanModuleInfo revertHotBeanModule(String moduleName, long revision);

   /**
    * Removes all revisions of a hot bean module.
    */
   public void removeHotBeanModule(String moduleName);

   /**
    * Gets the names of all existing hot bean modules.
    */
   public String[] getHotBeanModuleNames();

   /**
    * Gets information about the current revisions of all installed hot bean modules.
    */
   public HotBeanModuleInfo[] getHotBeanModuleInfo();

   /**
    * Gets information about all revisions of a specific hot bean module.
    */
   public HotBeanModuleInfo[] getHotBeanModuleInfo(String moduleName);

   /**
    * Gets information about the current revisions of a specific hot bean module.
    */
   public HotBeanModuleInfo getCurrentHotBeanModuleInfo(String moduleName);

   /**
    * Checks if a module with the specified name exists.
    */
   public boolean hasHotBeanModule(final String moduleName);

   /**
    * Checks if the specified module contains a bean with the specified name.
    * 
    * @since 1.0.1 (20070302)
    */
   public boolean hasHotBean(final String moduleName, final String beanName);

   /**
    * Gets a reference, via a proxy, to a the hot bean with the specified name in the specified module. This method will
    * return an object even if the hot bean module doesn't exist (yet). When invoking methods on the proxy, exceptions
    * of the types {@link ModuleNotFoundException} and {@link BeanNotFoundException} will be thrown to indicate that the
    * module or bean wasn't found.
    */
   public Object getHotBean(String moduleName, String beanName, Class interfaceClass);

   /**
    * Gets a reference, via a proxy, to a the hot bean with the specified name in the specified module. This method will
    * return an object even if the hot bean module doesn't exist (yet). When invoking methods on the proxy, exceptions
    * of the types {@link ModuleNotFoundException} and {@link BeanNotFoundException} will be thrown to indicate that the
    * module or bean wasn't found.
    */
   public Object getHotBean(String moduleName, String beanName, Class[] interfaceClasses);

   /**
    * Gets the class name of the bean with the specified name in the specified module.
    * 
    * @since 1.0.1 (20070212)
    */
   public String getHotBeanClassName(String moduleName, String beanName);

   /**
    * Gets the class of the bean with the specified name in the specified module.
    * 
    * @since 1.0.1 (20070212)
    */
   public Class getHotBeanClass(String moduleName, String beanName);
}
