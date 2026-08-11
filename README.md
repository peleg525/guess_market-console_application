# Guess Market - Exercise 1 (Console Application)

By Peleg Wurzel and Ben Lutenberg

![Windows smoke test](https://github.com/peleg525/guess_market-console_application/actions/workflows/windows-smoke-test.yml/badge.svg)

A console implementation of *Guess Market*, a Polymarket-style prediction-market engine, built for
Exercise 1 of the course. Events are binary (two options), priced and traded using LMSR
(Logarithmic Market Scoring Rule).

> Our actual submission readme - our details, the assumptions we made, and a walkthrough of the
> classes - is `README.docx`, since that's the format the assignment requires. This file here is
> just the regular repo readme.

## Modules

Two Maven modules, matching the two required submission jars:

- **gm-engine** - the passive engine. Loads and validates the XML events file (JAXB + XSD +
  application-level rules), runs the LMSR math, and exposes everything through the `GmEngine`
  interface using immutable DTOs. Knows nothing about the console.
- **gm-ui** - the console UI. The only module that touches `System.out` or `Scanner`; holds `main`
  and the menu loop.

## Build

Requires JDK 25.

```
mvn clean package
```

Produces `gm-engine/target/gm-engine.jar` and `gm-ui/target/gm-ui.jar` (plus `gm-ui/target/lib/`
with the JAXB runtime jars and a copy of `gm-engine.jar`, via `maven-dependency-plugin`).

## Run

```
java -jar gm-ui/target/gm-ui.jar
```

or, from an assembled distribution folder (`gm-ui.jar` + `lib/` sitting next to each other, as in
the submission zip):

```
run.bat
```

Sample XML event files for manual testing are under `testfiles/`.

## CI

We only have Macs, no Windows machine, but the assignment is graded on Windows 10. So
`.github/workflows/windows-smoke-test.yml` builds the project and runs it end-to-end on a real
`windows-latest` GitHub Actions runner on every push - loading valid/invalid files, buying shares,
closing an event, and loading a file from a path containing spaces - so we can actually be sure it
works on Windows without needing to borrow one.
