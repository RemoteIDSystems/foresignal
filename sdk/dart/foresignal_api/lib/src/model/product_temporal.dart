//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/product_temporal_retention.dart';
import 'package:foresignal_api/src/model/product_temporal_runs_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'product_temporal.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProductTemporal {
  /// Returns a new [ProductTemporal] instance.
  ProductTemporal({

    required  this.type,

     this.cadence,

     this.maxGap,

     this.earliest,

     this.latest,

     this.retention,

     this.runs,
  });

      /// observation: one frame per scan; forecast: runs of steps; pass: one frame per satellite pass
  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ProductTemporalTypeEnum type;



      /// Nominal interval between frames (forecasts: between steps). Absent for an external catalog entry without a fixed time dimension
  @JsonKey(
    
    name: r'cadence',
    required: false,
    includeIfNull: false,
  )


  final String? cadence;



      /// Largest gap at which a frame still counts as current (`now` alias, sampling `stale`). Absent for an external catalog entry without a fixed time dimension
  @JsonKey(
    
    name: r'maxGap',
    required: false,
    includeIfNull: false,
  )


  final String? maxGap;



      /// Valid time of the oldest retained frame
  @JsonKey(
    
    name: r'earliest',
    required: false,
    includeIfNull: false,
  )


  final DateTime? earliest;



      /// Valid time of the newest complete frame
  @JsonKey(
    
    name: r'latest',
    required: false,
    includeIfNull: false,
  )


  final DateTime? latest;



  @JsonKey(
    
    name: r'retention',
    required: false,
    includeIfNull: false,
  )


  final ProductTemporalRetention? retention;



      /// Forecast runs, newest first (forecast products only)
  @JsonKey(
    
    name: r'runs',
    required: false,
    includeIfNull: false,
  )


  final List<ProductTemporalRunsInner>? runs;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProductTemporal &&
      other.type == type &&
      other.cadence == cadence &&
      other.maxGap == maxGap &&
      other.earliest == earliest &&
      other.latest == latest &&
      other.retention == retention &&
      other.runs == runs;

    @override
    int get hashCode =>
        type.hashCode +
        cadence.hashCode +
        maxGap.hashCode +
        (earliest == null ? 0 : earliest.hashCode) +
        (latest == null ? 0 : latest.hashCode) +
        retention.hashCode +
        runs.hashCode;

  factory ProductTemporal.fromJson(Map<String, dynamic> json) => _$ProductTemporalFromJson(json);

  Map<String, dynamic> toJson() => _$ProductTemporalToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// observation: one frame per scan; forecast: runs of steps; pass: one frame per satellite pass
enum ProductTemporalTypeEnum {
@JsonValue(r'observation')
observation(r'observation'),
@JsonValue(r'forecast')
forecast(r'forecast'),
@JsonValue(r'pass')
pass(r'pass');

const ProductTemporalTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


