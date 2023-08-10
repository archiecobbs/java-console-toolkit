# java-console-toolkit
A toolkit for adding a command line interface (CLI) console to a Java application.

### Why?

There are lots of bits and pieces out there that can be useful for adding a command line interface (CLI) console to a Java application. However, it can be confusing to understand how they all (maybe) fit together. The goal of the java-console-toolkit (JCT) project is to simplify life for Java developers by providing some "glue" around a few of those bits and pieces and providing an easy path to get up and running quickly.

### Considerations

Suppose you have a Java application of some kind and you want to add a command line interface (CLI).

What are some issues that may come up?

* Can I have auto-discovered "pluggable" commands?
* Is is possible the make the console accessible securely via SSH?
* Can I have bash-like line editing and command history?
* Can I attach this console to the system console (stdin, stdout, stderr)?
* Can I implement my own custom read-eval-print loop?
* Is it possible to integrate [JShell](https://en.wikipedia.org/wiki/JShell) so I can access my Java objects?
* Will this console work properly on different operating systems?
* Is there some simple code that gets me started but doesn't ultimately limit me?

This project seeks to clarify these questions and allow you to answer YES.

This project is a work in progress.

### JCT Concepts

Let's nail down some concepts.

An **I/O stream** is a simple byte-oriented conduit, that is, an `InputStream` or an `OutputStream`. Actually we want `PrintStream` instead of `OutputStream` because we're going to assume that there is some known character encoding.

A **command** is a lot like a function. It has a name and it takes zero or more parameters which are all strings. Some of the parameters may be flag-like options, but it's really entirely up to the command as to how it interprets the parameters. It executes for a while, does something, and then it completes. On completion, it may or may not return some value, which could boolean (i.e., success or failure), or an integer code (zero for succes, non-zero for error), etc. In this project, commands return `boolean`.

When a command executes, it is given access to three standard I/O streams: **input**, **output**, and **error**. They will usually contain human-readable content because there's usually a human at the other end, but this is up to the command.

A **terminal** is a text-based user interface. It will be associated with a keyboard of some kind for input and a textual display of some kind for output. Examples include SSH and telnet clients, and the system console from which a Java process is launched. In this project, a terminal is represented by an instance of [JLine3](https://github.com/jline/jline3)'s [`Terminal`](https://www.javadoc.io/doc/org.jline/jline/latest/org/jline/terminal/Terminal.html) class.

A terminal communicates using two underlying I/O streams, one for each direction. The data transmitted on these streams is encoded according to a protocol defined by the **terminal type**. This protocol allows for sending not only normal text "as is" but also special output commands like "clear the screen" and special input commands like "signal an interrupt" (might be sent when someone on the remote end presses Control-C). Using the protocol for sending and receiving "as is" text, a `Terminal` can provide the input and output streams that a command expects. Note, however, terminals don't have a separate stream for error output: instead, any command error output is instead just treated like normal output.

Using this simple trick, a terminal can provide any command with the three I/O streams it needs. Therefore, any command can execute on either three raw streams or on a terminal. However, the converse is not always true: some commands may _require_ a terminal to function properly, for example, a text editor.

A **shell** is software that allows a **terminal** to be used interactively to execute commands via some kind of [Read-Eval-Print Loop](https://en.wikipedia.org/wiki/Read%E2%80%93eval%E2%80%93print_loop). It normally takes a line of text as input, parses it into a command name and arguments, and uses those to choose and execute some command. The shell must implement some kind of syntax and parsing behavior, for example, by splitting on whitespace and also providing some way to quote whitespace. There is no universal standard for command line parsing and quoting, so each shell must define its own syntax. Ideally, a shell should also support terminal-enabled features like command line editing, command history, tab completion, etc.

A **subshell** is a shell that is started by executing a command in another, outer shell. Upon exit from the subshell, the outer shell continues as before.

A **batch script** is a text file containing multiple commands intended to be executed non-interactively. The syntax for the file typically closely mirrors the syntax of some shell's interactive input, but no shell is required to execute a batch script. Instead, batch scripts are typically handled by executing a command that takes the script filename as a parameter or reads the script from standard input.

### Demonstration

The **demo** module allows you to test out the current JCT features (and see some sample code):

```
$ java -jar java-console-toolkit-demo-1.0.0.jar
Welcome to org.dellroad.jct.core.simple.SimpleShell
jct> help
  date    Display the current time and date.
  echo    Echoes command line arguments.
  exit    Exit the shell.
  help    Displays information about available commands.
  jshell  Fire up a JShell console.
  quit    Exit the shell.
  sleep   Sleep for a while.
jct> jshell
|  Welcome to JShell -- Version 20.0.1
|  For an introduction type: /help intro

jshell> 2 + 2
$1 ==> 4

jshell> /exit
|  Goodbye
jct> echo "foo  bar  \nnext line"
foo  bar
next line
jct> sleep 9999
^C
jct> exit
```

### API Docs

* [Javadocs API](https://archiecobbs.github.io/java-console-toolkit/site/apidocs/index.html)
