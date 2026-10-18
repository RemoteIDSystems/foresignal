//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'product_export_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProductExportRequest {
  /// Returns a new [ProductExportRequest] instance.
  ProductExportRequest({

    required  this.format,

     this.frameKey,

     this.frameKeys,

     this.bbox,

     this.placeId,

     this.radiusKm,

     this.minZoom,

     this.maxZoom,

     this.style,

     this.tms,
  });

  @JsonKey(
    
    name: r'format',
    required: true,
    includeIfNull: false,
  )


  final ProductExportRequestFormatEnum format;



  @JsonKey(
    
    name: r'frameKey',
    required: false,
    includeIfNull: false,
  )


  final String? frameKey;



  @JsonKey(
    
    name: r'frameKeys',
    required: false,
    includeIfNull: false,
  )


  final List<String>? frameKeys;



      /// [w, s, e, n]
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
  @JsonKey(
    
    name: r'radiusKm',
    required: false,
    includeIfNull: false,
  )


  final num? radiusKm;



          // minimum: 0
  @JsonKey(
    
    name: r'minZoom',
    required: false,
    includeIfNull: false,
  )


  final int? minZoom;



          // minimum: 0
  @JsonKey(
    
    name: r'maxZoom',
    required: false,
    includeIfNull: false,
  )


  final int? maxZoom;



  @JsonKey(
    
    name: r'style',
    required: false,
    includeIfNull: false,
  )


  final String? style;



      /// mbtiles and pmtiles hold XYZ WebMercatorQuad tiles only (WorldCRS84Quad is 400 VALIDATION_FAILED). A cog follows the set: WebMercatorQuad writes EPSG:3857, WorldCRS84Quad EPSG:4326.
  @JsonKey(
    
    name: r'tms',
    required: false,
    includeIfNull: false,
  )


  final ProductExportRequestTmsEnum? tms;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProductExportRequest &&
      other.format == format &&
      other.frameKey == frameKey &&
      other.frameKeys == frameKeys &&
      other.bbox == bbox &&
      other.placeId == placeId &&
      other.radiusKm == radiusKm &&
      other.minZoom == minZoom &&
      other.maxZoom == maxZoom &&
      other.style == style &&
      other.tms == tms;

    @override
    int get hashCode =>
        format.hashCode +
        frameKey.hashCode +
        frameKeys.hashCode +
        bbox.hashCode +
        placeId.hashCode +
        radiusKm.hashCode +
        minZoom.hashCode +
        maxZoom.hashCode +
        style.hashCode +
        tms.hashCode;

  factory ProductExportRequest.fromJson(Map<String, dynamic> json) => _$ProductExportRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ProductExportRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ProductExportRequestFormatEnum {
@JsonValue(r'mbtiles')
mbtiles(r'mbtiles'),
@JsonValue(r'pmtiles')
pmtiles(r'pmtiles'),
@JsonValue(r'cog')
cog(r'cog');

const ProductExportRequestFormatEnum(this.value);

final String value;

@override
String toString() => value;
}


/// mbtiles and pmtiles hold XYZ WebMercatorQuad tiles only (WorldCRS84Quad is 400 VALIDATION_FAILED). A cog follows the set: WebMercatorQuad writes EPSG:3857, WorldCRS84Quad EPSG:4326.
enum ProductExportRequestTmsEnum {
@JsonValue(r'WebMercatorQuad')
webMercatorQuad(r'WebMercatorQuad'),
@JsonValue(r'WorldCRS84Quad')
worldCRS84Quad(r'WorldCRS84Quad');

const ProductExportRequestTmsEnum(this.value);

final String value;

@override
String toString() => value;
}


