//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/frame_stats.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'raster_frame.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RasterFrame {
  /// Returns a new [RasterFrame] instance.
  RasterFrame({

    required  this.productId,

    required  this.time,

    required  this.timeKey,

    required  this.frameKey,

    required  this.status,

     this.expiresAt,

     this.run,

     this.step,

     this.obsStart,

     this.obsEnd,

    required  this.tiles,

     this.tiles2x,

     this.image,

     this.bounds,

     this.stats,

     this.thinned,
  });

  @JsonKey(
    
    name: r'productId',
    required: true,
    includeIfNull: false,
  )


  final String productId;



      /// Valid time (ISO extended UTC)
  @JsonKey(
    
    name: r'time',
    required: true,
    includeIfNull: false,
  )


  final DateTime time;



      /// Path form of the valid time
  @JsonKey(
    
    name: r'timeKey',
    required: true,
    includeIfNull: false,
  )


  final String timeKey;



      /// Content-addressed key `{timeKey}~{sha8}` (sha8 = first 8 hex chars of the decoded-content sha256)
  @JsonKey(
    
    name: r'frameKey',
    required: true,
    includeIfNull: false,
  )


  final String frameKey;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final RasterFrameStatusEnum status;



      /// When retention removes the frame; drop it client-side after this
  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;



      /// Forecast run reference time (ISO-8601); null for observations
  @JsonKey(
    
    name: r'run',
    required: false,
    includeIfNull: false,
  )


  final String? run;



      /// Forecast step in hours; null for observations
  @JsonKey(
    
    name: r'step',
    required: false,
    includeIfNull: false,
  )


  final int? step;



  @JsonKey(
    
    name: r'obsStart',
    required: false,
    includeIfNull: false,
  )


  final DateTime? obsStart;



  @JsonKey(
    
    name: r'obsEnd',
    required: false,
    includeIfNull: false,
  )


  final DateTime? obsEnd;



      /// Expanded content-addressed tile template with `{z}/{x}/{y}`
  @JsonKey(
    
    name: r'tiles',
    required: true,
    includeIfNull: false,
  )


  final String tiles;



      /// Same as `tiles` with the `@2x` (512 px) suffix
  @JsonKey(
    
    name: r'tiles2x',
    required: false,
    includeIfNull: false,
  )


  final String? tiles2x;



      /// Expanded `/image` template with `{bbox}`, `{width}`, `{height}` placeholders
  @JsonKey(
    
    name: r'image',
    required: false,
    includeIfNull: false,
  )


  final String? image;



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



      /// Kept by retention-ladder thinning
  @JsonKey(
    
    name: r'thinned',
    required: false,
    includeIfNull: false,
  )


  final bool? thinned;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RasterFrame &&
      other.productId == productId &&
      other.time == time &&
      other.timeKey == timeKey &&
      other.frameKey == frameKey &&
      other.status == status &&
      other.expiresAt == expiresAt &&
      other.run == run &&
      other.step == step &&
      other.obsStart == obsStart &&
      other.obsEnd == obsEnd &&
      other.tiles == tiles &&
      other.tiles2x == tiles2x &&
      other.image == image &&
      other.bounds == bounds &&
      other.stats == stats &&
      other.thinned == thinned;

    @override
    int get hashCode =>
        productId.hashCode +
        time.hashCode +
        timeKey.hashCode +
        frameKey.hashCode +
        status.hashCode +
        (expiresAt == null ? 0 : expiresAt.hashCode) +
        (run == null ? 0 : run.hashCode) +
        (step == null ? 0 : step.hashCode) +
        (obsStart == null ? 0 : obsStart.hashCode) +
        (obsEnd == null ? 0 : obsEnd.hashCode) +
        tiles.hashCode +
        tiles2x.hashCode +
        image.hashCode +
        (bounds == null ? 0 : bounds.hashCode) +
        stats.hashCode +
        thinned.hashCode;

  factory RasterFrame.fromJson(Map<String, dynamic> json) => _$RasterFrameFromJson(json);

  Map<String, dynamic> toJson() => _$RasterFrameToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RasterFrameStatusEnum {
@JsonValue(r'partial')
partial(r'partial'),
@JsonValue(r'complete')
complete(r'complete');

const RasterFrameStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


