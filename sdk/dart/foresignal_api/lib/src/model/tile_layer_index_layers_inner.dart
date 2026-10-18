//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tile_layer_index_layers_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TileLayerIndexLayersInner {
  /// Returns a new [TileLayerIndexLayersInner] instance.
  TileLayerIndexLayersInner({

    required  this.id,

    required  this.type,

    required  this.name,

    required  this.tilejson,

     this.aggBelowZoom,
  });

      /// Layer id (source id or kind)
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final TileLayerIndexLayersInnerTypeEnum type;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// URL of the layer's TileJSON
  @JsonKey(
    
    name: r'tilejson',
    required: true,
    includeIfNull: false,
  )


  final String tilejson;



      /// Zoom below which `agg=auto` aggregates; null = never
          // minimum: 0
          // maximum: 22
  @JsonKey(
    
    name: r'aggBelowZoom',
    required: false,
    includeIfNull: false,
  )


  final int? aggBelowZoom;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TileLayerIndexLayersInner &&
      other.id == id &&
      other.type == type &&
      other.name == name &&
      other.tilejson == tilejson &&
      other.aggBelowZoom == aggBelowZoom;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        name.hashCode +
        tilejson.hashCode +
        (aggBelowZoom == null ? 0 : aggBelowZoom.hashCode);

  factory TileLayerIndexLayersInner.fromJson(Map<String, dynamic> json) => _$TileLayerIndexLayersInnerFromJson(json);

  Map<String, dynamic> toJson() => _$TileLayerIndexLayersInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TileLayerIndexLayersInnerTypeEnum {
@JsonValue(r'source')
source_(r'source'),
@JsonValue(r'kind')
kind(r'kind');

const TileLayerIndexLayersInnerTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


