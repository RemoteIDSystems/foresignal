//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/llm_usage_point.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_usage_series.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmUsageSeries {
  /// Returns a new [LlmUsageSeries] instance.
  LlmUsageSeries({

     this.provider,

    required  this.resolution,

    required  this.points,

     this.totals,
  });

  @JsonKey(
    
    name: r'provider',
    required: false,
    includeIfNull: false,
  )


  final String? provider;



  @JsonKey(
    
    name: r'resolution',
    required: true,
    includeIfNull: false,
  )


  final LlmUsageSeriesResolutionEnum resolution;



  @JsonKey(
    
    name: r'points',
    required: true,
    includeIfNull: false,
  )


  final List<LlmUsagePoint> points;



  @JsonKey(
    
    name: r'totals',
    required: false,
    includeIfNull: false,
  )


  final LlmUsagePoint? totals;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmUsageSeries &&
      other.provider == provider &&
      other.resolution == resolution &&
      other.points == points &&
      other.totals == totals;

    @override
    int get hashCode =>
        (provider == null ? 0 : provider.hashCode) +
        resolution.hashCode +
        points.hashCode +
        totals.hashCode;

  factory LlmUsageSeries.fromJson(Map<String, dynamic> json) => _$LlmUsageSeriesFromJson(json);

  Map<String, dynamic> toJson() => _$LlmUsageSeriesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LlmUsageSeriesResolutionEnum {
@JsonValue(r'minute')
minute(r'minute'),
@JsonValue(r'hour')
hour(r'hour'),
@JsonValue(r'day')
day(r'day');

const LlmUsageSeriesResolutionEnum(this.value);

final String value;

@override
String toString() => value;
}


