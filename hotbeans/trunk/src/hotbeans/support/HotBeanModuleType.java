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
import hotbeans.HotBeanModuleInfo;

import java.util.ArrayList;

/**
 * HotBeanModuleType keeps track on all hot bean modules (revisions) associated with a certain module id.
 * 
 * @author Tobias Löfstrand
 */
public class HotBeanModuleType {

   private final String name;

   public boolean removeType = false;

   private final ArrayList modules = new ArrayList();

   /**
    * Creates a new HotBeanModuleType.
    */
   public HotBeanModuleType(String name) {
      this.name = name;
   }

   /**
    * Gets the name.
    */
   public String getName() {
      return name;
   }

   /**
    * Gets the flag indicating if the type is to be removed.
    */
   public boolean isRemoveType() {
      return removeType;
   }

   /**
    * Sets the flag indicating if the type is to be removed.
    */
   public void setRemoveType(boolean removeType) {
      this.removeType = removeType;
   }

   /**
    * Gets the number of module (revisions) in this type.
    */
   public int moduleCount() {
      return this.modules.size();
   }

   /**
    * Adds a module to this type.
    */
   public void addModule(HotBeanModule module) {
      if (!this.modules.contains(module)) this.modules.add(module);
   }

   /**
    * Removes a module from this type.
    */
   public void removeModule(HotBeanModule module) {
      this.modules.remove(module);
   }

   /**
    * Gets the current module of this type.
    */
   public HotBeanModule getCurrentModule() {
      if (this.modules.size() > 0) return (HotBeanModule) this.modules.get(this.modules.size() - 1); // Get last
                                                                                                      // revision
      else return null;
   }

   /**
    * Gets information about the current module of this type.
    */
   public HotBeanModuleInfo getCurrentModuleInfo() {
      if (this.modules.size() > 0) return ((HotBeanModule) this.modules.get(this.modules.size() - 1))
               .getHotBeanModuleInfo(); // Get last revision
      else return null;
   }

   /**
    * Gets the modules.
    */
   public HotBeanModule[] getModules() {
      return (HotBeanModule[]) this.modules.toArray(new HotBeanModule[0]);
   }

   /**
    * Gets information about the modules.
    */
   public HotBeanModuleInfo[] getModuleInfo() {
      HotBeanModuleInfo[] info = new HotBeanModuleInfo[this.modules.size()];

      for (int i = 0; i < info.length; i++) {
         info[i] = ((HotBeanModule) this.modules.get(i)).getHotBeanModuleInfo();
      }

      return info;
   }
}
