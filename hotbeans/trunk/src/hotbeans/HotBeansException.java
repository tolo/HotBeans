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
 * Base exception class.
 * 
 * @author Tobias L�fstrand
 */
public class HotBeansException extends RuntimeException {

   private static final long serialVersionUID = -6080128993436463849L;

   /**
    * Creates a new HotBeansException.
    */
   public HotBeansException(String message) {
      super(message);
   }

   /**
    * Creates a new HotBeansException.
    */
   public HotBeansException(String message, Throwable cause) {
      super(message, cause);
   }
}
