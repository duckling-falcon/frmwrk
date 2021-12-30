Service Framework
=================

This is a base module of Duckling, providing a framework for HTTP RPC
services. (Sorry its names in package and class imply RESTful, but
strictly speaking it's not.)

Add its .jar into your web applications. Maven is recommended.

Refer to the subproject 'frmwrk-test', which also shows examples of
writing clients and configurations.

In a nutshell, configure servlets in web.xml and take care of the
config file 'Rest2Services.xml' and 'services.xml', respectively for
the new and old version. They could be used at the same time.

