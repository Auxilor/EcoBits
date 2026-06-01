---
title: "API"
sidebar_position: 5
---

This page is for developers who want to read or modify EcoBits balances from their own plugin. EcoBits is open-source, so you can build against its API directly.

## Source code

The full source is on [GitHub](https://github.com/Auxilor/EcoBits).

## Adding the dependency

1. Add the Auxilor repository to your `build.gradle.kts`:
2. Add EcoBits as a `compileOnly` dependency, swapping `<version>` for the version you target.

```kotlin
repositories {
    maven("https://repo.auxilor.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.willfp:EcoBits:<version>")
}
```

The latest version available on the repo can be found [here](https://github.com/Auxilor/EcoBits/tags).

<hr/>

## Where to go next

- **Shared APIs:** the [eco framework](https://github.com/Auxilor/eco), where the common eco APIs live.
- **Config-side setup:** the [How to Make a Currency](how-to-make-a-currency) walkthrough for defining the currencies your code reads.