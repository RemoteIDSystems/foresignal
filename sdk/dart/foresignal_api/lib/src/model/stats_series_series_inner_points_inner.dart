//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stats_series_series_inner_points_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StatsSeriesSeriesInnerPointsInner {
  /// Returns a new [StatsSeriesSeriesInnerPointsInner] instance.
  StatsSeriesSeriesInnerPointsInner({

    required  this.t,

    required  this.value,
  });

  @JsonKey(
    
    name: r't',
    required: true,
    includeIfNull: false,
  )


  final DateTime t;



  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: false,
  )


  final int value;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StatsSeriesSeriesInnerPointsInner &&
      other.t == t &&
      other.value == value;

    @override
    int get hashCode =>
        t.hashCode +
        value.hashCode;

  factory StatsSeriesSeriesInnerPointsInner.fromJson(Map<String, dynamic> json) => _$StatsSeriesSeriesInnerPointsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$StatsSeriesSeriesInnerPointsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

