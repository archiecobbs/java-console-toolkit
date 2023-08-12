// This is a demonstration of a JShell startup script

import java.io.PrintStream;

// Get the ClassLoader in use
var loader = Thread.currentThread().getContextClassLoader();

//
// Get the output stream for the JShell session so we can print stuff to the user.
// This will be System.out when connected to the demo from the command line, but not
// if connected via an SSH connection (for example), so we can't assume that.
//
// Note we are using reflection here to do the equivalent of this:
//
//  session = org.dellroad.jct.jshell.JShellShellSession.getCurrent();
//  demo = org.dellroad.jct.demo.DemoMain.getInstance();
//
// This is to avoid problems with JShell source compilation failing due to lack of synchronization
// between the source classpath and the execution classpath. The source classpath has to be derived
// from the current ClassLoader. This is possible if the current ClassLoader is an URLClassLoader
// (e.g., within Tomcat container) but not from the normal command line, unless we are able to use
// illegal access reflection; this requires the following JVM flag be used:
//
//  --add-opens=java.base/jdk.internal.loader=ALL-UNNAMED
//
// See also API Javadoc for LocalContextExecutionControlProvider.
//

// Get session
var session = loader.loadClass("org.dellroad.jct.jshell.JShellShellSession").getMethod("getCurrent").invoke(null);
PrintStream out = (PrintStream)session.getClass().getMethod("getOutputStream").invoke(session);

// Get DemoMain singleton
var demo = loader.loadClass("org.dellroad.jct.demo.DemoMain").getMethod("getInstance").invoke(null);

// Emit a greeting
out.println();
out.println("*** Welcome to the java-console-toolkit JShell demo.");
for (String[] pair : new String[][] { { "DemoMain", "demo" }, { "JShellShellSession", "session" } })
    out.println(String.format("*** The %s singleton is available as \"%s\".", pair[0], pair[1]));
out.println();
