# ☕ JBPL

Java Bytecode Patch Language (or JBPL) is a fully featured JVM assembly language  
and macro assembler.  
It can be used to author new code or patch existing code.  
**We encourage you to use this tool for hacking around!**

### Features

- Standalone [ANTLRv4 frontend](https://github.com/karmakrafts/JBPL/tree/master/jbpl-frontend/src/main/antlr)
- [Interpreter](https://github.com/karmakrafts/JBPL/tree/master/jbpl-assembler/src/main/java/dev/karmakrafts/jbpl/assembler/model)
- [IntelliJ plugin](https://github.com/karmakrafts/JBPL/tree/master/jbpl-intellij-plugin)
- [Rouge lexer](https://github.com/karmakrafts/rouge) (GitLab highlighting)

### Why i made this

JBPL was created in an effort to streamline the process of applying patches to
the Kotlin compiler for another project i'm working on.  
It allows me to easily bundle patches as resource files and load them selectively
based on the current Kotlin version.

### Code Example

```jbpl
// JBPL is statically typed and its typesystem strongly coupled to the JVM
final define my_string: string = "Hello, World!" // string == <java/lang/String>

// JBPL supports mutability..
define my_variable: i32 = 0
my_variable++
info "my_variable is ${my_variable}" // ..and string interpolation

// It has JVM signatures, instructions and opcodes as first-class types.
define my_fld_sig: signature(field) = signature <com/example/Foo>.test: i32
define my_fun_sig: signature(fun) = signature <com/example/Foo>.test(): void
define my_insn: instruction = instruction(ldc "Hello, World!")
define my_op: opcode = opcode invokestatic

// It also supports surprisingly dynamic arrays with a familiar feel..
define my_array: [i32] = []{0, 1, 2, 3}
my_array += 4
my_array -= []{0, 2}

// ..and various flavours of control flow.
for(value in my_array) {
    info "my_array value is: ${value}"
}

// Of course we also support ranges for builtin types.
for(i in 0..<sizeof(my_array)) {
    info "my_array value at ${i} is: ${my_array[i]}"
}

// Inject into existing functions; turn someFunction into a NOOP
inject <com/example/TargetClass>.someFunction(): void {
    define insn: [instruction] = fun.instructions
    return // Inject a return instruction at the head of the function    
}

// AND MUCH MUCH MORE!
```

### How to use it as a CLI tool

First get a prebuilt JAR of the **jbpl-assembler-cli** or build one yourself.  
Afterward the JBPL macro assembler can be invoked as a simple CLI application as follows:  

```shell
java -jar jbpl-assembler-cli.jar --input <inputFile>.jbpl
```

An additional `--output` parameter may be specified to manually set the output directory.  
Use `--help` to get more information.

### How to use it as a library

First, add the official Maven Central repository to your settings.gradle.kts:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven("https://central.sonatype.com/repository/maven-snapshots")
        mavenCentral()
    }
}
```

Then add a dependency on the library in your root buildscript:

```kotlin
dependencies {
    implementation("dev.karmakrafts.jbpl:jbpl-assembler:<version>")
}
```

### How to install the Rouge lexer with a self-hosted GitLab instance

