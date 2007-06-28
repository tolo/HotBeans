HotBeans Framework, version 1.2

http://www.hotbeans.org
-------------------------------------------------------------------------------


1. Introduction

HotBeans is a framework for Java bean modules that may be deployed and updated 
in runtime, without interrupting any ongoing transactions in the application. 
This eliminates the need to restart the whole application and thus facilitates 
a higher uptime. The framework also keeps a history of deployed modules, 
which means that a rollback to a previous version may be performed.
 
Each bean module consists of a single jar file, containing deployment descriptors 
along with the application libraries. The default implementation of bean 
modules uses the Spring Framework as a bean container and the backbone of the module. 

The HotBeans framework may be deployed in any type of Java application and 
support is added for direct usage in the Spring Framework.

HotBeans was created in 2005 by John-John Roslund and Tobias Löfstrand while working at 
Link Messaging AB. In May 2007, the copyright was donated back from Link Messaging 
AB to the original creators, with the intent to publish the software as open source.

HotBeans is released under the terms of the Apache Software License, 
version 2.0 (see license.txt).


2. Third party library dependencies

The full ("-full") distribution of HotBeans contains all third party libraries that are 
required to build HotBeans. All third party libraries are subject to their respective 
licenses (see below). The dependencies are the following:

* Jakarta Commons Logging (http://jakarta.apache.org/commons/logging/)
Required for building: Yes
Required at runtime: Yes
File(s): lib/jakarta-commons/commons-logging.jar
Version: 1.0.4
License: Apache License Version 2.0

* JUnit (http://www.junit.org)
Required for building: Yes
Required at runtime: No
File(s): lib/junit/junit-4.3.1.jar
Version: 4.3.1
License: Common Public License Version 1.0 (lib/junit/cpl-v10.html)

* Log4J (http://logging.apache.org/log4j)
Required for building: No
Required at runtime: Optional (Used when running test cases)
File(s): lib/log4j/log4j-1.2.14.jar
Version: 1.2.14
License: Apache License Version 2.0

* Spring Framework (http://www.springframework.org)
Required for building: Yes
Required at runtime: Yes  
File(s): lib/spring/spring.jar, lib/spring/spring-mock.jar
Version: 1.2.9
License: Apache License Version 2.0


3. Where to begin?

Sample applications may be found in the "samples" directory and there is API 
documentation available in javadoc format in the "docs" directory.  

