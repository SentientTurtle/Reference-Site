EVE Online item-type icon export

Naming convention follows that of the (now-deprecated) official Image-Export-Collection:
* For regular types, including blueprints/reactions/relics, "[type_id]_64.png";
* For blueprints, the blueprint-copy icon is available as, "[type_id]_64_bpc.png"
e.g.
* "648_64.png" for the 'Badger' ship (typeID 648)
* "983_64.png" for the 'Badger Blueprint' (typeID 983)
* "983_64_bpc.png" for the 'Badger Blueprint' blueprint-copy icon (typeID 983)

Notes:
* Icons are not guaranteed to be 64x64 pixels, and may be larger for types where higher resolutions are available.
  Currently, sizes vary between 64x64 to 512x512 pixels.
  For web use, <img> size must be explicitly specified.
* Icons are all in PNG format
* Only 'published' types are included, this may change in the future
* Not all types have an icon, an index is available in "index.json" within the zip file