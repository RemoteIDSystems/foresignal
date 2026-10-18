//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/pack_spec_raster.dart';
import 'package:foresignal_api/src/model/pack_spec_vector.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'pack_spec.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PackSpec {
  /// Returns a new [PackSpec] instance.
  PackSpec({

    required  this.name,

     this.bbox,

     this.placeId,

    required  this.minZoom,

    required  this.maxZoom,

     this.vector,

     this.raster,

     this.includePrivate = false,

     this.personalUse = false,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// [minLon, minLat, maxLon, maxLat] (WGS84)
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



          // minimum: 0
          // maximum: 14
  @JsonKey(
    
    name: r'minZoom',
    required: true,
    includeIfNull: false,
  )


  final int minZoom;



          // minimum: 0
          // maximum: 14
  @JsonKey(
    
    name: r'maxZoom',
    required: true,
    includeIfNull: false,
  )


  final int maxZoom;



  @JsonKey(
    
    name: r'vector',
    required: false,
    includeIfNull: false,
  )


  final PackSpecVector? vector;



  @JsonKey(
    
    name: r'raster',
    required: false,
    includeIfNull: false,
  )


  final PackSpecRaster? raster;



      /// Admin only
  @JsonKey(
    defaultValue: false,
    name: r'includePrivate',
    required: false,
    includeIfNull: false,
  )


  final bool? includePrivate;



      /// As ExportSpec.personalUse
  @JsonKey(
    defaultValue: false,
    name: r'personalUse',
    required: false,
    includeIfNull: false,
  )


  final bool? personalUse;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PackSpec &&
      other.name == name &&
      other.bbox == bbox &&
      other.placeId == placeId &&
      other.minZoom == minZoom &&
      other.maxZoom == maxZoom &&
      other.vector == vector &&
      other.raster == raster &&
      other.includePrivate == includePrivate &&
      other.personalUse == personalUse;

    @override
    int get hashCode =>
        name.hashCode +
        (bbox == null ? 0 : bbox.hashCode) +
        (placeId == null ? 0 : placeId.hashCode) +
        minZoom.hashCode +
        maxZoom.hashCode +
        (vector == null ? 0 : vector.hashCode) +
        (raster == null ? 0 : raster.hashCode) +
        includePrivate.hashCode +
        personalUse.hashCode;

  factory PackSpec.fromJson(Map<String, dynamic> json) => _$PackSpecFromJson(json);

  Map<String, dynamic> toJson() => _$PackSpecToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

