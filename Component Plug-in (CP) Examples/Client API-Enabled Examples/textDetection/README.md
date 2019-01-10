## How to install
* Run the gradle JAR task (typically `./gradlew build`)
* Drop the generated jar (which will be located in `cst/build/libs/`) into the plugin directory of your Appian install `<AE_ROOT>/_admin/plugins`
* See https://github.com/appian/integration-sdk-examples/blob/master/Component%20Plug-in%20(CP)%20Examples/README.md for installation instructions for the Component Plug-in

## Sample interface
```
load(
  local!imageUrl: "https://www.appian.com/wp-content/uploads/2017/05/logo-appian-retina.png",
  local!textFound: null,
  {
    a!textField(
      label: "Text Found",
      labelPosition: "ABOVE",
      value: local!textFound,
      readOnly: true()
    ),
    a!textField(
      label: "Image Url",
      labelPosition: "ABOVE",
      value: local!imageUrl,
      saveInto: local!imageUrl,
      refreshAfter: "UNFOCUS",
      validations: {}
    ),
    fn!textDetection(
      label: "Text Detection",
      labelPosition: "ABOVE",
      validations: {},
      height: "AUTO",
      imageUrl: local!imageUrl,
      connectedSystem: cons!POINTER_TextDetectionConnectedSystem,
      textFound: local!textFound
    )
  }
)
```
