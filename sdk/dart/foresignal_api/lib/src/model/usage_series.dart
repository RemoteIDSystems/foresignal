//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/usage_point.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'usage_series.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UsageSeries {
  /// Returns a new [UsageSeries] instance.
  UsageSeries({

    required  this.sourceId,

    required  this.resolution,

    required  this.points,

     this.totals,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'resolution',
    required: true,
    includeIfNull: false,
  )


  final UsageSeriesResolutionEnum resolution;



  @JsonKey(
    
    name: r'points',
    required: true,
    includeIfNull: false,
  )


  final List<UsagePoint> points;



  @JsonKey(
    
    name: r'totals',
    required: false,
    includeIfNull: false,
  )


  final UsagePoint? totals;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UsageSeries &&
      other.sourceId == sourceId &&
      other.resolution == resolution &&
      other.points == points &&
      other.totals == totals;

    @override
    int get hashCode =>
        sourceId.hashCode +
        resolution.hashCode +
        points.hashCode +
        totals.hashCode;

  factory UsageSeries.fromJson(Map<String, dynamic> json) => _$UsageSeriesFromJson(json);

  Map<String, dynamic> toJson() => _$UsageSeriesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum UsageSeriesResolutionEnum {
@JsonValue(r'minute')
minute(r'minute'),
@JsonValue(r'hour')
hour(r'hour'),
@JsonValue(r'day')
day(r'day');

const UsageSeriesResolutionEnum(this.value);

final String value;

@override
String toString() => value;
}


