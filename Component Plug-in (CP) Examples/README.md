## How to install
* Compress all files/directories under the example of your choice into a zip
* E.g. `zip -r mapField.zip mapField/mapField mapField/appian-component-plugin.xml`
* Copy the zip into the _admin/plugins directory of your Appian install

## How to get an API key
* Follow [this guide](https://developers.google.com/maps/documentation/javascript/get-api-key) to get an API key from Google

## Sample interface
```
load(
  local!pins,
  {
    a!textField(value: local!pins, label: "Pins", saveInto: local!pins),
    mapField(
      label: "Map",
      labelPosition: "ABOVE",
      validations: {},
      height: "AUTO",
      key: <YOUR_API_KEY>,
      location: "",
      pin: a!save(target: local!pins, fn!append(local!pins, save!value))
    )
  }
)
```
