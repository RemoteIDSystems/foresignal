//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'product_spatial.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProductSpatial {
  /// Returns a new [ProductSpatial] instance.
  ProductSpatial({

    required  this.bounds,

    required  this.minZoom,

    required  this.maxZoom,

     this.maxZoomForAnimation,

     this.center,
  });

      /// [west, south, east, north] in WGS84 degrees
  @JsonKey(
    
    name: r'bounds',
    required: true,
    includeIfNull: false,
  )


  final List<num> bounds;



          // minimum: 0
  @JsonKey(
    
    name: r'minZoom',
    required: true,
    includeIfNull: false,
  )


  final int minZoom;



      /// Native max zoom (MRMS 8, GOES C13 7, GFS 4, HRRR 8); clients overzoom above it
          // minimum: 0
  @JsonKey(
    
    name: r'maxZoom',
    required: true,
    includeIfNull: false,
  )


  final int maxZoom;



      /// Highest zoom warmed for animation (radar 7, IR 6)
          // minimum: 0
  @JsonKey(
    
    name: r'maxZoomForAnimation',
    required: false,
    includeIfNull: false,
  )


  final int? maxZoomForAnimation;



      /// [lon, lat, zoom]
  @JsonKey(
    
    name: r'center',
    required: false,
    includeIfNull: false,
  )


  final List<num>? center;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProductSpatial &&
      other.bounds == bounds &&
      other.minZoom == minZoom &&
      other.maxZoom == maxZoom &&
      other.maxZoomForAnimation == maxZoomForAnimation &&
      other.center == center;

    @override
    int get hashCode =>
        bounds.hashCode +
        minZoom.hashCode +
        maxZoom.hashCode +
        maxZoomForAnimation.hashCode +
        center.hashCode;

  factory ProductSpatial.fromJson(Map<String, dynamic> json) => _$ProductSpatialFromJson(json);

  Map<String, dynamic> toJson() => _$ProductSpatialToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

