//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/field_encodings_png_tiles.dart';
import 'package:foresignal_api/src/model/field_encodings_png_uv.dart';
import 'package:foresignal_api/src/model/field_encodings_velocity_json.dart';
import 'package:foresignal_api/src/model/field_encodings_i16.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'field_encodings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FieldEncodings {
  /// Returns a new [FieldEncodings] instance.
  FieldEncodings({

     this.velocityJson,

     this.pngUv,

     this.i16,

     this.pngTiles,

     this.tilejson,
  });

  @JsonKey(
    
    name: r'velocityJson',
    required: false,
    includeIfNull: false,
  )


  final FieldEncodingsVelocityJson? velocityJson;



  @JsonKey(
    
    name: r'pngUv',
    required: false,
    includeIfNull: false,
  )


  final FieldEncodingsPngUv? pngUv;



  @JsonKey(
    
    name: r'i16',
    required: false,
    includeIfNull: false,
  )


  final FieldEncodingsI16? i16;



  @JsonKey(
    
    name: r'pngTiles',
    required: false,
    includeIfNull: false,
  )


  final FieldEncodingsPngTiles? pngTiles;



      /// TileJSON-style link
  @JsonKey(
    
    name: r'tilejson',
    required: false,
    includeIfNull: false,
  )


  final String? tilejson;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FieldEncodings &&
      other.velocityJson == velocityJson &&
      other.pngUv == pngUv &&
      other.i16 == i16 &&
      other.pngTiles == pngTiles &&
      other.tilejson == tilejson;

    @override
    int get hashCode =>
        (velocityJson == null ? 0 : velocityJson.hashCode) +
        (pngUv == null ? 0 : pngUv.hashCode) +
        (i16 == null ? 0 : i16.hashCode) +
        (pngTiles == null ? 0 : pngTiles.hashCode) +
        (tilejson == null ? 0 : tilejson.hashCode);

  factory FieldEncodings.fromJson(Map<String, dynamic> json) => _$FieldEncodingsFromJson(json);

  Map<String, dynamic> toJson() => _$FieldEncodingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

