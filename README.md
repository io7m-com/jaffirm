jaffirm
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.jaffirm/com.io7m.jaffirm.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.jaffirm%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/com.io7m.jaffirm/com.io7m.jaffirm?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/jaffirm/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/jaffirm.svg?style=flat-square)](https://codecov.io/gh/io7m-com/jaffirm)

![com.io7m.jaffirm](./src/site/resources/jaffirm.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/jaffirm/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/jaffirm/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/jaffirm/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/jaffirm/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/jaffirm/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/jaffirm/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/jaffirm/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/jaffirm/actions?query=workflow%3Amain.windows.temurin.lts)|

## jaffirm

The `jaffirm` package provides simple and fast pre/postcondition and invariant
checking functions.

## Features

* Static invocation, zero-allocation code paths for the common case of non-failing contracts.
* Specialized and generic variants of all functions for use in low-latency software.
* Detailed contract failure messages by construction.
* Written in pure Java 17.
* High coverage test suite.
* [OSGi-ready](https://www.osgi.org/)
* [JPMS-ready](https://en.wikipedia.org/wiki/Java_Platform_Module_System)
* ISC license.

## Usage

Declare preconditions with the `Preconditions` class.
Declare postconditions with the `Postconditions` class.
Declare invariants with the `Invariants` class.

```
import static com.io7m.jaffirm.core.Contracts.conditionI;
import static com.io7m.jaffirm.core.Preconditions.checkPreconditionI;
import static com.io7m.jaffirm.core.Preconditions.checkPreconditionsI;

int exampleSingles(final int x)
{
  checkPreconditionI(x, x > 0,      i -> "Input " + i + " must be > 0");
  checkPreconditionI(x, x % 2 == 0, i -> "Input " + i + " must be even");
  return x * 2;
}

int exampleMultis(final int x)
{
  checkPreconditionsI(
    x,
    conditionI(i -> i > 0,      i -> "Input " + i + " must be > 0"),
    conditionI(i -> i % 2 == 0, i -> "Input " + i + " must be even"));
  return x * 2;
}

> exampleSingles(0)
Exception in thread "main" com.io7m.jaffirm.core.PreconditionViolationException: Precondition violation.
  Received: 0
  Violated conditions:
    [0]: Input 0 must be > 0

> exampleSingles(1)
Exception in thread "main" com.io7m.jaffirm.core.PreconditionViolationException: Precondition violation.
  Received: 1
  Violated conditions:
    [0]: Input 1 must be even

> exampleMultis(-1)
Exception in thread "main" com.io7m.jaffirm.core.PreconditionViolationException: Precondition violation.
  Received: -1
  Violated conditions:
    [0]: Input -1 must be > 0
    [1]: Input -1 must be even
```

