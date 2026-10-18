//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/frame_stats.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'raster_frame_observation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RasterFrameObservation {
  /// Returns a new [RasterFrameObservation] instance.
  RasterFrameObservation({

    required  this.productId,

    required  this.frameKey,

    required  this.time,

    required  this.timeKey,

    required  this.tiles,

     this.bounds,

     this.stats,

     this.expiresAt,
  });

  @JsonKey(
    
    name: r'productId',
    required: true,
    includeIfNull: false,
  )


  final String productId;



  @JsonKey(
    
    name: r'frameKey',
    required: true,
    includeIfNull: false,
  )


  final String frameKey;



  @JsonKey(
    
    name: r'time',
    required: true,
    includeIfNull: false,
  )


  final DateTime time;



  @JsonKey(
    
    name: r'timeKey',
    required: true,
    includeIfNull: false,
  )


  final String timeKey;



      /// Expanded content-addressed tile template
  @JsonKey(
    
    name: r'tiles',
    required: true,
    includeIfNull: false,
  )


  final String tiles;



      /// [west, south, east, north] in WGS84 degrees
  @JsonKey(
    
    name: r'bounds',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bounds;



  @JsonKey(
    
    name: r'stats',
    required: false,
    includeIfNull: false,
  )


  final FrameStats? stats;



  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RasterFrameObservation &&
      other.productId == productId &&
      other.frameKey == frameKey &&
      other.time == time &&
      other.timeKey == timeKey &&
      other.tiles == tiles &&
      other.bounds == bounds &&
      other.stats == stats &&
      other.expiresAt == expiresAt;

    @override
    int get hashCode =>
        productId.hashCode +
        frameKey.hashCode +
        time.hashCode +
        timeKey.hashCode +
        tiles.hashCode +
        bounds.hashCode +
        stats.hashCode +
        (expiresAt == null ? 0 : expiresAt.hashCode);

  factory RasterFrameObservation.fromJson(Map<String, dynamic> json) => _$RasterFrameObservationFromJson(json);

  Map<String, dynamic> toJson() => _$RasterFrameObservationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

