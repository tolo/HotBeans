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
 * Exception class thrown when a name conflict arises when deploying a new module.
 * 
 * @author Tobias L�fstrand
 */
public class ModuleAlreadyExistsException extends HotBeansException {

   private static final long serialVersionUID = -8053999241068925968L;

   /**
    * Creates a new ModuleAlreadyExistsException.
    */
   public ModuleAlreadyExistsException(String message) {
      super(message);
   }

   /**
    * Creates a new ModuleAlreadyExistsException.
    */
   public ModuleAlreadyExistsException(String message, Throwable cause) {
      super(message, cause);
   }
}
