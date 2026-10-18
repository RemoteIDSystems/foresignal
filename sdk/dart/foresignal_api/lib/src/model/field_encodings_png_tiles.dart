//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'field_encodings_png_tiles.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FieldEncodingsPngTiles {
  /// Returns a new [FieldEncodingsPngTiles] instance.
  FieldEncodingsPngTiles({

     this.template,

     this.tms,

     this.uMin,

     this.uMax,

     this.vMin,

     this.vMax,

     this.minzoom,

     this.maxzoom,
  });

      /// …/fields/{frameKey}/{z}/{x}/{y}.png
  @JsonKey(
    
    name: r'template',
    required: false,
    includeIfNull: false,
  )


  final String? template;



  @JsonKey(
    
    name: r'tms',
    required: false,
    includeIfNull: false,
  )


  final FieldEncodingsPngTilesTmsEnum? tms;



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
    
    name: r'minzoom',
    required: false,
    includeIfNull: false,
  )


  final int? minzoom;



  @JsonKey(
    
    name: r'maxzoom',
    required: false,
    includeIfNull: false,
  )


  final int? maxzoom;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FieldEncodingsPngTiles &&
      other.template == template &&
      other.tms == tms &&
      other.uMin == uMin &&
      other.uMax == uMax &&
      other.vMin == vMin &&
      other.vMax == vMax &&
      other.minzoom == minzoom &&
      other.maxzoom == maxzoom;

    @override
    int get hashCode =>
        template.hashCode +
        tms.hashCode +
        uMin.hashCode +
        uMax.hashCode +
        vMin.hashCode +
        vMax.hashCode +
        minzoom.hashCode +
        maxzoom.hashCode;

  factory FieldEncodingsPngTiles.fromJson(Map<String, dynamic> json) => _$FieldEncodingsPngTilesFromJson(json);

  Map<String, dynamic> toJson() => _$FieldEncodingsPngTilesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum FieldEncodingsPngTilesTmsEnum {
@JsonValue(r'WebMercatorQuad')
webMercatorQuad(r'WebMercatorQuad'),
@JsonValue(r'WorldCRS84Quad')
worldCRS84Quad(r'WorldCRS84Quad');

const FieldEncodingsPngTilesTmsEnum(this.value);

final String value;

@override
String toString() => value;
}


