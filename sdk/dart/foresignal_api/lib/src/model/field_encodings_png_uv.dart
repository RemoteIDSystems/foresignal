//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'field_encodings_png_uv.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FieldEncodingsPngUv {
  /// Returns a new [FieldEncodingsPngUv] instance.
  FieldEncodingsPngUv({

     this.url,

     this.uMin,

     this.uMax,

     this.vMin,

     this.vMax,

     this.width,

     this.height,

     this.bounds,
  });

  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'uMin',
    required: false,
    includeIfNull: false,
  )


  final num? uMin;



  @JsonKey(
    
    name: r'uMax',
    required: false,
    includeIfNull: false,
  )


  final num? uMax;



  @JsonKey(
    
    name: r'vMin',
    required: false,
    includeIfNull: false,
  )


  final num? vMin;



  @JsonKey(
    
    name: r'vMax',
    required: false,
    includeIfNull: false,
  )


  final num? vMax;



  @JsonKey(
    
    name: r'width',
    required: false,
    includeIfNull: false,
  )


  final int? width;



  @JsonKey(
    
    name: r'height',
    required: false,
    includeIfNull: false,
  )


  final int? height;



  @JsonKey(
    
    name: r'bounds',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bounds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FieldEncodingsPngUv &&
      other.url == url &&
      other.uMin == uMin &&
      other.uMax == uMax &&
      other.vMin == vMin &&
      other.vMax == vMax &&
      other.width == width &&
      other.height == height &&
      other.bounds == bounds;

    @override
    int get hashCode =>
        url.hashCode +
        uMin.hashCode +
        uMax.hashCode +
        vMin.hashCode +
        vMax.hashCode +
        width.hashCode +
        height.hashCode +
        bounds.hashCode;

  factory FieldEncodingsPngUv.fromJson(Map<String, dynamic> json) => _$FieldEncodingsPngUvFromJson(json);

  Map<String, dynamic> toJson() => _$FieldEncodingsPngUvToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

