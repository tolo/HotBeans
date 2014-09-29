Simple sample application to test updating of a simple module. The sample 
consists of two parts; the module (testmodule) and the client application. 
The client application periodically invokes a method on a bean in the module 
and writes the return value (the bean id) on the console.

To build and run the application, simply execute run.bat (or execute "ant run").
To update the module to a new version, copy the file testmodule/1.jar into 
the hotModules/TestModule directory. After a short while a different id should 
be printed on the console.
