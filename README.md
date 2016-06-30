# tool.intellij.config.editor
Liberty config editor plugin for JetBrains IntelliJ IDEA

# Features:

- Support for adding and removing features to the Liberty Server.xml file
- Adding the "OnError" attribute to server.xml files
- Changes made in the Feature Editor GUI automatically get written to the server.xml file and the file displayed is updated

# Building

To build, use IntelliJ to create a new project from version control and clone this repository. Steps:

1) Once the project has been loaded in IntelliJ, go to File -> Project Structure -> SDKs and create a new
   IntelliJ Platform Plugin SDK, using JDK 1.8 or above. An IntelliJ Platform Plugin SDK is required to access
   the IntelliJ API.

2) Go to Build -> Make Project

3) Go to Run -> Edit Configuration. Then click the "+" and add an IntelliJ Plugin configuration. Click apply then
   run the configuration.

4) You may need to create an "out" folder and point IntelliJ to it. (File -> Project Structure -> Project Compiler Output

To export the plugin as a jar, go to build -> Prepare All Plugin Modules for Deployment

# Known Issues

1) If the feature xml file takes too long to be created, the plugin may throw an exception. Reloading the Server.xml
   should resolve this.


# Contributing

Please see our [contributing guide](https://github.com/WASdev/wasdev.github.io/blob/master/CONTRIBUTING.md).


# Support

Use the [issue tracker][] for reporting any bugs or enhancements. For any questions please use the [WASdev forum](https://developer.ibm.com/answers/?community=wasdev).

[issue tracker]: https://github.com/WASdev/tool.intellij.config.editor/issues

This repository is maintained by IBM.

# Notice

(C) Copyright IBM Corporation 2016.

# License

```text
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
