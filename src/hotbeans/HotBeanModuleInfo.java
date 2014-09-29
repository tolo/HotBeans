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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Class containing information about a HotBeanModule.
 * 
 * @author Tobias Löfstrand
 */
public class HotBeanModuleInfo implements Externalizable, Cloneable {

   static final long serialVersionUID = 6675885165364262510L;

   public static final int ERROR = 0;

   public static final int UNLOADED = 1;

   public static final int ACTIVE = 2;

   public static final int INACTIVE = 3;

   public static final int UNLOADING = 4;

   static final String[] StateNames = new String[] { "Error", "Unloaded", "Active", "Inactive", "Unloading" };

   private String name;

   private String description;

   private String version;

   private long revision;

   private long deployedAt;

   private long usageCount;

   private int state;

   private long lastStateChange;

   private String errorReason;

   /**
    * Create a new HotBeanModuleInfo.
    */
   public HotBeanModuleInfo() {
   }

   /**
    * Create a new HotBeanModuleInfo.
    */
   public HotBeanModuleInfo(String name, String description, long revision, String version, long deployedAt) {
      this.name = name;
      this.description = description;
      this.revision = revision;
      this.version = version;
      this.deployedAt = deployedAt;

      this.state = UNLOADED;
      this.lastStateChange = System.currentTimeMillis();
      this.errorReason = null;
   }

   /**
    * Gets the module name.
    */
   public String getName() {
      return name;
   }

   /**
    * Sets the module name.
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * Gets the description.
    */
   public String getDescription() {
      return description;
   }

   /**
    * Sets the description.
    */
   public void setDescription(String description) {
      this.description = description;
   }

   /**
    * Gets the version.
    */
   public String getVersion() {
      return version;
   }

   /**
    * Sets the version.
    */
   public void setVersion(String version) {
      this.version = version;
   }

   /**
    * Gets the revision.
    */
   public long getRevision() {
      return revision;
   }

   /**
    * Sets the revision.
    */
   public void setRevision(long revision) {
      this.revision = revision;
   }

   /**
    * Gets the time when the module was deployed.
    */
   public long getDeployedAt() {
      return deployedAt;
   }

   /**
    * Sets the time when the module was deployed.
    */
   public void setDeployedAt(long deployedAt) {
      this.deployedAt = deployedAt;
   }

   /**
    * Gets the usage count.
    */
   public long getUsageCount() {
      return this.usageCount;
   }

   /**
    * Sets the usage count.
    */
   public void setUsageCount(long useCount) {
      this.usageCount = useCount;
   }

   /**
    * Gets the state of the module.
    */
   public int getState() {
      return this.state;
   }

   /**
    * Gets a human readable description of the state of the module.
    */
   public String getStateDescription() {
      return StateNames[this.state];
   }

   /**
    * Sets the state of the module.
    */
   public void setState(int state) {
      if ((state >= 0) && (state < StateNames.length)) {
         this.state = state;
         this.lastStateChange = System.currentTimeMillis();
      }
   }

   /**
    * Gets the time of the last state change.
    */
   public long getLastStateChange() {
      return lastStateChange;
   }

   /**
    * Gets the error reason.
    */
   public String getErrorReason() {
      return errorReason;
   }

   /**
    * Sets the error reason.
    */
   public void setErrorReason(String errorReason) {
      this.errorReason = errorReason;
   }

   /**
    * Creates a clone of this object.
    */
   public HotBeanModuleInfo getClone() {
      try {
         return (HotBeanModuleInfo) super.clone();
      } catch (CloneNotSupportedException e) {
         throw new RuntimeException("Error cloning!");
      }
   }

   /**
    * De-serialization method.
    */
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readInt(); // Consume version

      this.name = (String) in.readObject();
      this.description = (String) in.readObject();
      this.version = (String) in.readObject();
      this.revision = in.readLong();
      this.deployedAt = in.readLong();
      this.usageCount = in.readLong();
      this.state = in.readInt();
      this.lastStateChange = in.readLong();
      this.errorReason = (String) in.readObject();
   }

   /**
    * Serialization method.
    */
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeInt(1); // Write version

      out.writeObject(this.name);
      out.writeObject(this.description);
      out.writeObject(this.version);
      out.writeLong(this.revision);
      out.writeLong(this.deployedAt);
      out.writeLong(this.usageCount);
      out.writeInt(this.state);
      out.writeLong(this.lastStateChange);
      out.writeObject(this.errorReason);
   }

   /**
    * Gets a string representation of this HotBeanModuleInfo.
    */
   public String toString() {
      return this.toString(true);
   }

   /**
    * Gets a string representation of this HotBeanModuleInfo.
    */
   public String toString(boolean includeClassName) {
      StringBuffer toStringString = new StringBuffer();
      toStringString.append("[");
      toStringString.append(this.name).append(", ");
      if (this.description != null) toStringString.append(this.description).append(", ");
      toStringString.append(this.version);
      toStringString.append(" (").append(this.revision).append(")");
      toStringString.append(" - ").append(this.getStateDescription());
      if (this.state == ERROR) toStringString.append(": ").append(this.getErrorReason());
      toStringString.append("]");

      if (includeClassName) return "HotBeanModuleInfo" + toStringString;
      else return toStringString.toString();
   }
}
