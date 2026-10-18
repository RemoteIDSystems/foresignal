//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tile_json_vector_layers_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TileJsonVectorLayersInner {
  /// Returns a new [TileJsonVectorLayersInner] instance.
  TileJsonVectorLayersInner({

    required  this.id,

     this.description,

     this.minzoom,

     this.maxzoom,

    required  this.fields,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



          // minimum: 0
          // maximum: 16
  @JsonKey(
    
    name: r'minzoom',
    required: false,
    includeIfNull: false,
  )


  final int? minzoom;



          // minimum: 0
          // maximum: 16
  @JsonKey(
    
    name: r'maxzoom',
    required: false,
    includeIfNull: false,
  )


  final int? maxzoom;



      /// Property name → description (fieldSpecs label and unit, else String/Number)
  @JsonKey(
    
    name: r'fields',
    required: true,
    includeIfNull: false,
  )


  final Map<String, String> fields;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TileJsonVectorLayersInner &&
      other.id == id &&
      other.description == description &&
      other.minzoom == minzoom &&
      other.maxzoom == maxzoom &&
      other.fields == fields;

    @override
    int get hashCode =>
        id.hashCode +
        description.hashCode +
        minzoom.hashCode +
        maxzoom.hashCode +
        fields.hashCode;

  factory TileJsonVectorLayersInner.fromJson(Map<String, dynamic> json) => _$TileJsonVectorLayersInnerFromJson(json);

  Map<String, dynamic> toJson() => _$TileJsonVectorLayersInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

