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

/**
 * Interface for classes implementing a (class) loader mechanism for hot bean modules.
 * 
 * @author Tobias Löfstrand
 */
public interface HotBeanModuleLoader {

   /**
    * Gets the class loader used to load classes for a {@link HotBeanModule}.
    */
   public ClassLoader getClassLoader();

   /**
    * Initializes this loader and associates it with the specified HotBeanModule. The loader is responsible for putting
    * this hot bean module in the state {@link HotBeanModuleInfo#UNLOADED} by calling the method
    * {@link HotBeanModule#unloaded()}, when this loader is destroyed (in the method {@link #destroy()}).
    */
   public void init(HotBeanModule hotBeanModule);

   /**
    * Called when this loader is no longer needed and is to be taken out of service. A call to this method should
    * directly or indirectly put the associated hot bean module in the state {@link HotBeanModuleInfo#UNLOADED} by
    * calling the method {@link HotBeanModule#unloaded()}.
    */
   public void destroy();
}
