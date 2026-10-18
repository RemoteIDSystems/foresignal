//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/frame_stats.dart';
import 'package:foresignal_api/src/model/raster_frame_forecast_run.dart';
import 'package:foresignal_api/src/model/raster_frame_observation.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'raster_frame_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RasterFrameEvent {
  /// Returns a new [RasterFrameEvent] instance.
  RasterFrameEvent({

    required  this.productId,

    required  this.frameKey,

    required  this.time,

    required  this.timeKey,

    required  this.tiles,

     this.bounds,

     this.stats,

     this.expiresAt,

    required  this.run,

    required  this.steps,

    required  this.validTimes,

    required  this.tilesTemplate,

     this.partial,
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



      /// Run reference time (ISO-8601)
  @JsonKey(
    
    name: r'run',
    required: true,
    includeIfNull: false,
  )


  final String run;



      /// Forecast steps (hours)
  @JsonKey(
    
    name: r'steps',
    required: true,
    includeIfNull: false,
  )


  final List<int> steps;



      /// Valid time per step
  @JsonKey(
    
    name: r'validTimes',
    required: true,
    includeIfNull: false,
  )


  final List<DateTime> validTimes;



      /// Tile template with `{timeKey}` (or `{frameKey}`) plus `{z}/{x}/{y}`, run pinned
  @JsonKey(
    
    name: r'tilesTemplate',
    required: true,
    includeIfNull: false,
  )


  final String tilesTemplate;



      /// True only on the optional `{productId}:partial` record (run in progress)
  @JsonKey(
    
    name: r'partial',
    required: false,
    includeIfNull: false,
  )


  final bool? partial;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RasterFrameEvent &&
      other.productId == productId &&
      other.frameKey == frameKey &&
      other.time == time &&
      other.timeKey == timeKey &&
      other.tiles == tiles &&
      other.bounds == bounds &&
      other.stats == stats &&
      other.expiresAt == expiresAt &&
      other.run == run &&
      other.steps == steps &&
      other.validTimes == validTimes &&
      other.tilesTemplate == tilesTemplate &&
      other.partial == partial;

    @override
    int get hashCode =>
        productId.hashCode +
        frameKey.hashCode +
        time.hashCode +
        timeKey.hashCode +
        tiles.hashCode +
        bounds.hashCode +
        stats.hashCode +
        (expiresAt == null ? 0 : expiresAt.hashCode) +
        run.hashCode +
        steps.hashCode +
        validTimes.hashCode +
        tilesTemplate.hashCode +
        partial.hashCode;

  factory RasterFrameEvent.fromJson(Map<String, dynamic> json) => _$RasterFrameEventFromJson(json);

  Map<String, dynamic> toJson() => _$RasterFrameEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

