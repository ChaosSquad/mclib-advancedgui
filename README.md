# mclib-advancedgui
This library contains utilities for the API of the AdvancedGUI plugin.

### Features
- Synchronizing screens between players
- Dynamic component system
- Converting GUI points to world locations
- Limiting the range a player can interact with the GUI even if the player is in the activation radius (I would consider this as a bug of AdvancedGUI, but they refuse to fix it, so this lib contains a workaround)
- Replacing the ComponentTree of an interaction
- ...

### Javadocs
You can visit the JavaDocs here: [JavaDocs](https://chaossquad.github.io/mclib-advancedgui)

### Import

Using Gradle:
```kotlin
repositories {
    // [...]
    maven {
        name = "chaossquad-releases"
        url = uri("https://maven.chaossquad.net/releases")
    }

    maven {
        name = "chaossquad-snapshots"
        url = uri("https://maven.chaossquad.net/snapshots")
    }
}

dependencies {
    // [...]
    compileOnly("net.chaossquad:mclib-advancedgui:a53a0d2aa137a7ddc8df649d159525cf7ce94847")
}
```