Image Service bundle

Zip file containing images and `service_metadata.json` containing the mapping of image service routes to files.

Structure:
```
{
  "42397": {
    "icon": "3be99fdc9e1243d463e3a2b6c2b518cf.png"
  },
  "41700": {
    "icon": "6415accc393e7632e03dd6e822cc85d1.png",
    "render": "e14be3db876a17fda725b72ad859442f.jpg"
  },
  ...
}
```
Map of typeIDs to which icon types are available for that type. Different icon types may point to the same file:
```
  "41652": {
    "bp": "bp;89ee1b4eb14737ccb3d57471f2c99fd4;.png",
    "bpc": "bpc;89ee1b4eb14737ccb3d57471f2c99fd4;.png",
    "icon": "bp;89ee1b4eb14737ccb3d57471f2c99fd4;.png"
  },
```

Unlike the official CCP Image Service:
* Icons are of varying sizes, where possible, higher resolution icons are provided
* Renders are in jpg format
* Types with "bp", "relic", or "reaction" routes have an "icon" entry as well, enabling the use of the `/{type_id}/icon` route for all types

For full compatibility with the CCP Image Service the following routes need to be directed to the official service:
* /alliances/
* /characters/
* /corporations/