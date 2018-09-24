## How to install
* Run the gradle JAR task
* Drop the jar into the plugin directory
* Highlight all files under the cse directory and compress them into a zip
* Drop the zip into the plugin directory

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
