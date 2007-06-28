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

import java.util.Timer;
import java.util.TimerTask;

/**
 * Hot bean module repository implementation that performs periodic checks on the repository using a timer.
 * 
 * @author Tobias Löfstrand
 */
public abstract class PeriodicCheckHotBeanModuleRepository extends AbstractHotBeanModuleRepository {

   public static final long DEFAULT_CHECK_INTERVAL = 15000;

   private long checkInterval = DEFAULT_CHECK_INTERVAL;

   protected Timer timer;

   /**
    * Creates a new PeriodicCheckHotBeanModuleRepository.
    */
   public PeriodicCheckHotBeanModuleRepository() {
      this(null);
   }

   /**
    * Creates a new PeriodicCheckHotBeanModuleRepository.
    */
   public PeriodicCheckHotBeanModuleRepository(Object lock) {
      super(lock);
      super.setName("PeriodicCheckHotBeanModuleRepository");
   }

   /**
    * Initializes this PeriodicCheckHotBeanModuleRepository.
    */
   public void init() throws Exception {
      synchronized (super.getLock()) {
         super.init();

         RepositoryCheckTask repositoryCheckTask = new RepositoryCheckTask(this);

         this.timer = new Timer(true);
         this.timer.schedule(repositoryCheckTask, 500, this.getCheckInterval());
      }
   }

   /**
    * Destroys this PeriodicCheckHotBeanModuleRepository.
    */
   public void destroy() throws Exception {
      synchronized (super.getLock()) {
         this.timer.cancel();
         this.timer = null;

         super.destroy();
      }
   }

   /**
    * Gets the check interval.
    */
   public long getCheckInterval() {
      return checkInterval;
   }

   /**
    * Sets the check interval.
    */
   public void setCheckInterval(long checkInterval) {
      synchronized (super.getLock()) {
         this.checkInterval = checkInterval;
      }
   }

   /**
    * Performs a repository check by invoking {@link AbstractHotBeanModuleRepository#checkForObsoleteModules()}. This
    * method is invoked by the timer used by this class.
    */
   protected void performRepositoryCheck() {
      synchronized (super.getLock()) {
         super.checkForObsoleteModules();
      }
   }

   /**
    * Timer task for performing repository checks.
    */
   protected static class RepositoryCheckTask extends TimerTask {

      private final PeriodicCheckHotBeanModuleRepository periodicCheckHotBeanModuleRepository;

      public RepositoryCheckTask(PeriodicCheckHotBeanModuleRepository periodicCheckHotBeanModuleRepository) {
         this.periodicCheckHotBeanModuleRepository = periodicCheckHotBeanModuleRepository;
      }

      public void run() {
         this.periodicCheckHotBeanModuleRepository.performRepositoryCheck();
      }
   }
}
