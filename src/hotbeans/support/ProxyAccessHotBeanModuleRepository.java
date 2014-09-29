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
import hotbeans.HotBeanModuleRepository;
import hotbeans.ModuleNotFoundException;

/**
 * Base interface for HotBeanModuleRepository implementation that use {@link HotBeanProxyFactory} to create bean
 * proxies.
 * 
 * @author Tobias Löfstrand
 */
public interface ProxyAccessHotBeanModuleRepository extends HotBeanModuleRepository {

   /**
    * Gets a HotBeanProxyFactory object used for creating proxies to a hot bean with the specified name in the specified
    * module. This method will return an object even if the hot bean module doesn't exist (yet). When invoking methods
    * on proxies obtained through the returned HotBeanProxyFactory, exceptions of the types
    * {@link ModuleNotFoundException} and {@link BeanNotFoundException} will be thrown to indicate that the module or
    * bean wasn't found.
    */
   public HotBeanProxyFactory getHotBeanProxyFactory(String moduleName, String beanName, Class[] interfaceClasses);

   /**
    * Called to validate a HotBeanProxyFactory and module and bean references.
    */
   public void validateHotBeanProxyFactory(HotBeanProxyFactory hotBeanProxyFactory);
}
