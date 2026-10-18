//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/map_style_metadata.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'map_style.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MapStyle {
  /// Returns a new [MapStyle] instance.
  MapStyle({

    required  this.version,

     this.name,

    required  this.sources,

    required  this.sprite,

    required  this.layers,

    required  this.metadata,
  });

  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final MapStyleVersionEnum version;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



      /// One `vector` source per requested layer (`url` = 016 per-layer TileJSON) plus `agg=hex` sources used below `aggBelowZoom`
  @JsonKey(
    
    name: r'sources',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Map<String, Object>> sources;



      /// `<base>/v1/map/sprites/foresignal-{sha8}` (MapLibre appends `.json`/`.png` and `@2x`)
  @JsonKey(
    
    name: r'sprite',
    required: true,
    includeIfNull: false,
  )


  final String sprite;



      /// MapLibre layers: `symbol` (SDF icon, colour match, rotation), `circle` (zoom/size interpolation), `line`, `fill` (opacity 0.25) plus outline, and aggregation `fill` ramps
  @JsonKey(
    
    name: r'layers',
    required: true,
    includeIfNull: false,
  )


  final List<Map<String, Object>> layers;



  @JsonKey(
    
    name: r'metadata',
    required: true,
    includeIfNull: false,
  )


  final MapStyleMetadata metadata;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MapStyle &&
      other.version == version &&
      other.name == name &&
      other.sources == sources &&
      other.sprite == sprite &&
      other.layers == layers &&
      other.metadata == metadata;

    @override
    int get hashCode =>
        version.hashCode +
        name.hashCode +
        sources.hashCode +
        sprite.hashCode +
        layers.hashCode +
        metadata.hashCode;

  factory MapStyle.fromJson(Map<String, dynamic> json) => _$MapStyleFromJson(json);

  Map<String, dynamic> toJson() => _$MapStyleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MapStyleVersionEnum {
@JsonValue(8)
number8('8');

const MapStyleVersionEnum(this.value);

final String value;

@override
String toString() => value;
}


