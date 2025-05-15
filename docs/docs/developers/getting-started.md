---
title: Getting Started
sidebar_position: 1
---
## Using:
Currently, EMF has 2 main artifacts for use.
even-more-fish-api & even-more-fish-plugin

In the future these will be further abstracted so you don't need to use the "core" artifact any longer.

## CodeMC
The plugin is available via CodeMC's Maven Repository. To add it to your build tool, follow the examples below:

### Gradle (Kotlin)
```kotlin title="Gradle Kotlin"
repositories {
    maven("https://repo.codemc.io/repository/EvenMoreFish/")
}

dependencies {
    // For the plugin artifact
    compileOnly("com.oheers.evenmorefish:even-more-fish-plugin:2.0.0-SNAPSHOT")
    // For the api artifact
    compileOnly("com.oheers.evenmorefish:even-more-fish-api:2.0.0-SNAPSHOT")
}
```

### Maven
```xml title="Maven"
<repositories>
    <repository>
        <id>evenmorefish</id>
        <url>https://repo.codemc.io/repository/EvenMoreFish</url>
    </repository>
</repositories>

<dependencies>
    <!-- For the plugin artifact -->
    <dependency>
        <groupId>com.oheers.evenmorefish</groupId>
        <artifactId>even-more-fish-plugin</artifactId>
        <version>2.0.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
    <!-- For the api artifact -->
    <dependency>
        <groupId>com.oheers.evenmorefish</groupId>
        <artifactId>even-more-fish-api</artifactId>
        <version>2.0.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```