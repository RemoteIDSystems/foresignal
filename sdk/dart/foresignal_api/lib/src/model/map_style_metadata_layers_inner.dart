//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'map_style_metadata_layers_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MapStyleMetadataLayersInner {
  /// Returns a new [MapStyleMetadataLayersInner] instance.
  MapStyleMetadataLayersInner({

    required  this.id,

    required  this.kinds,

     this.attribution,

     this.minzoom,

     this.aggBelowZoom,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'kinds',
    required: true,
    includeIfNull: false,
  )


  final List<String> kinds;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final String? attribution;



  @JsonKey(
    
    name: r'minzoom',
    required: false,
    includeIfNull: false,
  )


  final num? minzoom;



  @JsonKey(
    
    name: r'aggBelowZoom',
    required: false,
    includeIfNull: false,
  )


  final num? aggBelowZoom;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MapStyleMetadataLayersInner &&
      other.id == id &&
      other.kinds == kinds &&
      other.attribution == attribution &&
      other.minzoom == minzoom &&
      other.aggBelowZoom == aggBelowZoom;

    @override
    int get hashCode =>
        id.hashCode +
        kinds.hashCode +
        attribution.hashCode +
        minzoom.hashCode +
        (aggBelowZoom == null ? 0 : aggBelowZoom.hashCode);

  factory MapStyleMetadataLayersInner.fromJson(Map<String, dynamic> json) => _$MapStyleMetadataLayersInnerFromJson(json);

  Map<String, dynamic> toJson() => _$MapStyleMetadataLayersInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

