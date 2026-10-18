//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/tile_layer_index_layers_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tile_layer_index.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TileLayerIndex {
  /// Returns a new [TileLayerIndex] instance.
  TileLayerIndex({

    required  this.tilejson,

    required  this.layers,
  });

  @JsonKey(
    
    name: r'tilejson',
    required: true,
    includeIfNull: false,
  )


  final TileLayerIndexTilejsonEnum tilejson;



  @JsonKey(
    
    name: r'layers',
    required: true,
    includeIfNull: false,
  )


  final List<TileLayerIndexLayersInner> layers;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TileLayerIndex &&
      other.tilejson == tilejson &&
      other.layers == layers;

    @override
    int get hashCode =>
        tilejson.hashCode +
        layers.hashCode;

  factory TileLayerIndex.fromJson(Map<String, dynamic> json) => _$TileLayerIndexFromJson(json);

  Map<String, dynamic> toJson() => _$TileLayerIndexToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TileLayerIndexTilejsonEnum {
@JsonValue(r'3.0.0')
n3period0Period0(r'3.0.0');

const TileLayerIndexTilejsonEnum(this.value);

final String value;

@override
String toString() => value;
}


