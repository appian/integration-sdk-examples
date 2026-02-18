## How to install
The Component Plug-in and the Connected System Plug-in are packaged in two separate bundles. Installing both requires creating both bundles and then placing both into the plug-in directory separately.

#### Installing the Connected System Plug-in
* Enter the `connectedSystemPlugin` directory
* Make sure you have `lib/appian-plug-in-sdk.jar`
* Run the gradle JAR task (typically `./gradlew build`)
* Drop the generated jar (which will be located in `build/libs/`) into the plugin directory of your Appian install `<AE_ROOT>/_admin/plugins`

#### Installing the Component Plug-in
* See https://github.com/appian/integration-sdk-examples/blob/master/Component%20Plug-in%20(CP)%20Examples/README.md for installation instructions for the Component Plug-in

## Sample interface
```
newsLoader(
  label: "newsLoader",
  labelPosition: "ABOVE",
  validations: {},
  height: "AUTO",
  connectedSystem: null,
  saveNewsToFolder: null,
  userDetails: a!map(
    "name": user(
      loggedInUser(),
      "firstName"
    ),
    "email": user(
      loggedInUser(),
      "email"
    ),
    "country": user(
      loggedInUser(),
      "country"
    )
  ),
  canAddNews: a!isUserMemberOfGroup(
    loggedInUser(),
    "editors"
  ),
  newsFreshness: "latest"
)
```
