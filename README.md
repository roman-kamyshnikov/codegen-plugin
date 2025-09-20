# Welcome to the Codegen Plugin project!

## How to install the plugin in Android Studio
- Open the [releases](https://github.com/rkam88/codegen/releases) section
- Download the `*.zip` from the latest release
- In Android Studio, open Settings > Plugins and select "Install Plugin from Disk..."

## How to run
- Right-click on any package in the Project Tool Window
- Select the "Codegen" option at the bottom
- Enter the name of the API function to generate code for and click on OK

## Making changes to the plugin
- Install the [Plugin DevKit](https://plugins.jetbrains.com/plugin/22851-plugin-devkit) plugin

### Viewing the PSI tree for a file 
- In any IntelliJ-based IDE, got to Help > Edit Custom Properties...
- Add `idea.is.internal=true` to the file and restart the IDE
- Go to Tools > View PSI Structure of Current File...

### Running the plugin in Android Studio
- Select and run "Run Plugin Locally" in the Run Configurations dropdown