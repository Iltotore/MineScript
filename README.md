# MineScript
MineScript is a polyglot scripting system powered by GraalVM, allowing JVM interactions from other languages.

This project comes with a Sponge implementation, including Minecraft-script creation and book-shell.

**Server owners, [click here](sponge/README.md)**

# Script Loading
You can load a Script using `ScriptLoader#load`. This method will match files in-depth using the given extension then
create a script using the passed language.

A ScriptLoader requires three arguments:
- 1st `root: File`: the root directory
- 2nd `ext: String`: the extension used to match the desired files
- 3rd `language: Language`: the programming language used to parse this script

You can execute a loaded Script using `Script#execute(Context)`

## The template file system
Scripts inheritance is like a tree. Each node inherit from their parent's template if exists.
```
+ root
  |
  + template.py
  + some_script.py #Inherit from root/template.py
  + sponge
    + template.py
    + hello.py #Inherit from root/template.py and root/sponge/template.py
```

# Requirements
This project requires [GraalVM](https://github.com/oracle/graal). To use a language, you will need to install its Graal implementation.
For example, you need [Graal Python](https://github.com/graalvm/graalpython) to execute Python scripts.

Please note that some Graal extensions don't support Windows. You can use WSL as an alternative.