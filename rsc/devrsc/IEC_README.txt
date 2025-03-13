Item icon & render export

Naming convention follows that of the (now-deprecated) official Image-Export-Collection:
* For regular types, including blueprints/reactions/relics, "[type_id]_64.png";
* For blueprints, the blueprint-copy icon is available as, "[type_id]_64_bpc.png"
e.g.
* "648_64.png" for the 'Badger' ship (typeID 648)
* "983_64.png" for the 'Badger Blueprint' (typeID 983)
* "983_64_bpc.png" for the 'Badger Blueprint' blueprint-copy icon (typeID 983)

* For renders "[type_id]_512.jpg";
e.g.
* "648_512.jpg" for the 'Badger' ship (typeID 648)

Notes:
* Not all types have an icon or render
* Icons are all in PNG format
* Renders are all in JPG format, directly from the game files.
* Icons are not guaranteed to be 64x64 pixels, and may be larger for types where higher resolutions are available.
  Currently, sizes vary between 64x64 to 512x512 pixels.
  For web use, <img> size must be explicitly specified.
* Renders are not guaranteed to be 512x512 pixels, and may be larger for types where higher resolutions are available.
  Currently, all sizes are 512x512
  For web use, <img> size must be explicitly specified.