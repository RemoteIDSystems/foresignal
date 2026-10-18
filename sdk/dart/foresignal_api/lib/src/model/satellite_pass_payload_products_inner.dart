//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'satellite_pass_payload_products_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SatellitePassPayloadProductsInner {
  /// Returns a new [SatellitePassPayloadProductsInner] instance.
  SatellitePassPayloadProductsInner({

     this.productId,

     this.frameKey,

    required  this.composite,

     this.thumbMediaId,

     this.tilesTemplate,

     this.instrument,

     this.mediaId,
  });

      /// The polar overlay product `satdump-{station}-{composite}` (054); present when the composite has a GeoTIFF overlay
  @JsonKey(
    
    name: r'productId',
    required: false,
    includeIfNull: false,
  )


  final String? productId;



      /// The overlay frame's content-addressed key (054)
  @JsonKey(
    
    name: r'frameKey',
    required: false,
    includeIfNull: false,
  )


  final String? frameKey;



  @JsonKey(
    
    name: r'composite',
    required: true,
    includeIfNull: false,
  )


  final String composite;



      /// ULID identifier
  @JsonKey(
    
    name: r'thumbMediaId',
    required: false,
    includeIfNull: false,
  )


  final String? thumbMediaId;



      /// Content-addressed tile template (054 polar overlays)
  @JsonKey(
    
    name: r'tilesTemplate',
    required: false,
    includeIfNull: false,
  )


  final String? tilesTemplate;



  @JsonKey(
    
    name: r'instrument',
    required: false,
    includeIfNull: false,
  )


  final String? instrument;



      /// ULID identifier
  @JsonKey(
    
    name: r'mediaId',
    required: false,
    includeIfNull: false,
  )


  final String? mediaId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SatellitePassPayloadProductsInner &&
      other.productId == productId &&
      other.frameKey == frameKey &&
      other.composite == composite &&
      other.thumbMediaId == thumbMediaId &&
      other.tilesTemplate == tilesTemplate &&
      other.instrument == instrument &&
      other.mediaId == mediaId;

    @override
    int get hashCode =>
        productId.hashCode +
        (frameKey == null ? 0 : frameKey.hashCode) +
        composite.hashCode +
        (thumbMediaId == null ? 0 : thumbMediaId.hashCode) +
        (tilesTemplate == null ? 0 : tilesTemplate.hashCode) +
        instrument.hashCode +
        (mediaId == null ? 0 : mediaId.hashCode);

  factory SatellitePassPayloadProductsInner.fromJson(Map<String, dynamic> json) => _$SatellitePassPayloadProductsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SatellitePassPayloadProductsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

