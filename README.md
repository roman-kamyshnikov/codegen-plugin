# 1. Overview

This repository is an example **code-generation plugin** for Android Studio / IntelliJ. You can make small adjustments to use it as-is for straightforward generation, or—preferably—treat it as a **starter** for deeper, project-specific codegen.

Code generation inputs and outputs are **highly project-specific**. While a universal tool is theoretically possible, building and maintaining one would demand significant time and complexity. This project focuses instead on a clear, extensible foundation you can tailor to your architecture and conventions.

# 2. What it does

- **Context action in Project view:** Right-click any source package → **Codegen**. A dialog appears with autocomplete for API functions discovered in your project.
- **Domain model generation:** Clones data and enum classes referenced by the selected API function’s parameters and return type into your target package (under `domain/model/` by default).
- **Type-aware resolution:** Uses Kotlin analysis to resolve real types (nullability, generics) before generating files.
- **Safe conflict handling:** If a target class name exists but the constructor signature or enum entries differ, a `_NEW` variant is created with a clear KDoc TODO to guide merging.
- **Atomic write & formatting:** All changes are queued and applied in a single write command; imports are shortened and code is reformatted automatically.

# 3. Plugin requirements & compatibility

- **IDE:** Android Studio or IntelliJ IDEA Community (same plugin build line).
- **Kotlin:** Required **K2** mode enabled in the IDE.
- **Project pairing (default):** The plugin is currently configured to work with the [`codegen-samples`](https://github.com/rkam88/codegen-samples) repository. You can point it at your own project by adjusting the configuration (see Section 4).
- **Permissions/Indexing:** Run after project indexing completes so function discovery and type resolution are available.

# 4. Making changes to the plugin / custom configuration & install

1. **IntelliJ setup:** Install the [Plugin DevKit](https://plugins.jetbrains.com/plugin/22851-plugin-devkit) plugin & clone this project.
2. **Configure (if needed):** Update the `Config.Input` and `Congif.Output` values in [`Config.kt`](src/main/kotlin/dev/roman/kamyshnikov/codegen/Config.kt) to align discovery and output with your codebase.
3. **Build the plugin:** Run `./gradlew buildPlugin` — the ZIP is created at `build/distributions/<plugin-name>-<version>.zip`.
4. **Open installer:** In your IDE, go to **Settings → Plugins → ⚙ → Install Plugin from Disk…**
5. **Select the ZIP:** Choose the file from `build/distributions`.
6. **Restart the IDE** to activate the plugin.

# 5. How to use

1. Open a project that contains your API interfaces and models.
2. In the **Project** tool window, right-click the **package** where you want code generated.
3. Choose **Codegen**.
4. Start typing the API function name in the dialog (autocomplete will suggest matches).
5. Confirm — generated files appear under `domain/model` (relative to the selected package), with imports shortened and code formatted.

# 6. What gets generated

- **Domain models:** Clones Kotlin **data classes** and **enum classes** referenced by the selected API function’s parameters and return type.
- **Collections:** Supports `List<T>` (recursively clones `T` when it’s a project type).
- **Nullability & stdlib types:** Preserves nullability; standard library types are used as-is.
- **Output location:** Writes under `domain/model` relative to the package you invoked the action from (adjustable via `Config.Output`).
- **Formatting & imports:** Applies a single batched write; imports are shortened and code is reformatted automatically.

# 7. Conflict resolution strategy

- **Existing class with different constructor (data classes):**  
  If a class with the same name already exists at the target location but its constructor signature differs, the generator creates `Foo_NEW` with a KDoc TODO to highlight that merging is needed.
- **Existing enum with different shape (enum classes):**  
  If constructor parameters or enum entries differ, the generator creates `Bar_NEW` with a KDoc TODO pointing to the original.
- **No-op on identical outputs:**  
  If the existing data/enum class match exactly, no duplicate is created.
- **Atomic application:**  
  All changes are queued and written in one batch, ensuring consistency and a single undo step.

# 8. Project structure & points of interest

- [`selectFunction/`](src/main/kotlin/dev/roman/kamyshnikov/codegen/selectFunction/): action entry point and function discovery. Scans for API interfaces, resolves functions, and powers the autocomplete dialog.
- [`GenerationQueue.kt`](src/main/kotlin/dev/roman/kamyshnikov/codegen/generation/GenerationQueue.kt): aggregates all file edits and applies them **in one batch** at the end. This keeps writes atomic, enables consistent import shortening/formatting, and makes undo predictable.
- [`CodeGenerator.kt`](src/main/kotlin/dev/roman/kamyshnikov/codegen/generation/CodeGenerator.kt): orchestrates the generation pipeline. Currently coordinates the domain-model step; designed so you can add further steps (e.g., mappers, test data, unit tests, repositories) that build on previous outputs.
- [`di/`](src/main/kotlin/dev/roman/kamyshnikov/codegen/di/): a minimal, explicit service locator is used instead of IntelliJ’s service wiring to keep the focus of the repository on code analysis and generation logic.
- [`res/`](src/main/kotlin/dev/roman/kamyshnikov/codegen/res/): user-facing strings and UI labels live here, keeping messages consistent and easy to tweak.
- [`Config.kt`](src/main/kotlin/dev/roman/kamyshnikov/codegen/Config.kt): single place to adjust input discovery and output structure. By default, it is set up to work with [`codegen-samples`](https://github.com/rkam88/codegen-samples); point it at your own project by updating `Config.Input`.

# 9. Troubleshooting

- **Autocomplete shows nothing**
    - Make sure you’re invoking the action by right-clicking a **package** (not an arbitrary folder/file).
    - Verify `Config.Input` targets your APIs/models.
    - Confirm the selected API function actually references project types (so there’s something to generate).
    - Rarely, the action can appear before the IDE fully exits dumb mode (timing between `AnAction.update` and `DumbService` state changes). If this happens, retry after indexing completes.
- **Classes generated but imports look odd**
    - Re-run the action from the same package. All edits are batched; the second pass ensures import shortening and formatting complete if the first pass was interrupted.
- **Plugin doesn’t appear after install**
    - Install from **Settings → Plugins → ⚙ → Install Plugin from Disk…**, choose the ZIP from `build/distributions`, then **restart** the IDE.
    - Check that your Android Studio/IntelliJ build is **compatible** with the versions the plugin supports (see `sinceBuild`/`untilBuild` in the project’s [`build.gradle.kts`](build.gradle.kts).

# 10. License

This project is licensed under the **Apache License, Version 2.0**. See [`LICENSE.txt`](LICENSE.txt) for the full text.
