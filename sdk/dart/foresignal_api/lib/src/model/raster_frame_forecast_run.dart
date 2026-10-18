//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'raster_frame_forecast_run.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RasterFrameForecastRun {
  /// Returns a new [RasterFrameForecastRun] instance.
  RasterFrameForecastRun({

    required  this.productId,

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
    bool operator ==(Object other) => identical(this, other) || other is RasterFrameForecastRun &&
      other.productId == productId &&
      other.run == run &&
      other.steps == steps &&
      other.validTimes == validTimes &&
      other.tilesTemplate == tilesTemplate &&
      other.partial == partial;

    @override
    int get hashCode =>
        productId.hashCode +
        run.hashCode +
        steps.hashCode +
        validTimes.hashCode +
        tilesTemplate.hashCode +
        partial.hashCode;

  factory RasterFrameForecastRun.fromJson(Map<String, dynamic> json) => _$RasterFrameForecastRunFromJson(json);

  Map<String, dynamic> toJson() => _$RasterFrameForecastRunToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

