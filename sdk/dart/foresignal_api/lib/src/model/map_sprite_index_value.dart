//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'map_sprite_index_value.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MapSpriteIndexValue {
  /// Returns a new [MapSpriteIndexValue] instance.
  MapSpriteIndexValue({

    required  this.x,

    required  this.y,

    required  this.width,

    required  this.height,

    required  this.pixelRatio,

     this.sdf,
  });

  @JsonKey(
    
    name: r'x',
    required: true,
    includeIfNull: false,
  )


  final int x;



  @JsonKey(
    
    name: r'y',
    required: true,
    includeIfNull: false,
  )


  final int y;



  @JsonKey(
    
    name: r'width',
    required: true,
    includeIfNull: false,
  )


  final int width;



  @JsonKey(
    
    name: r'height',
    required: true,
    includeIfNull: false,
  )


  final int height;



  @JsonKey(
    
    name: r'pixelRatio',
    required: true,
    includeIfNull: false,
  )


  final num pixelRatio;



  @JsonKey(
    
    name: r'sdf',
    required: false,
    includeIfNull: false,
  )


  final bool? sdf;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MapSpriteIndexValue &&
      other.x == x &&
      other.y == y &&
      other.width == width &&
      other.height == height &&
      other.pixelRatio == pixelRatio &&
      other.sdf == sdf;

    @override
    int get hashCode =>
        x.hashCode +
        y.hashCode +
        width.hashCode +
        height.hashCode +
        pixelRatio.hashCode +
        sdf.hashCode;

  factory MapSpriteIndexValue.fromJson(Map<String, dynamic> json) => _$MapSpriteIndexValueFromJson(json);

  Map<String, dynamic> toJson() => _$MapSpriteIndexValueToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

