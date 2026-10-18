//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/stats_series_series_inner_points_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stats_series_series_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StatsSeriesSeriesInner {
  /// Returns a new [StatsSeriesSeriesInner] instance.
  StatsSeriesSeriesInner({

     this.key,

    required  this.points,
  });

      /// Group key; null when groupBy=none
  @JsonKey(
    
    name: r'key',
    required: false,
    includeIfNull: false,
  )


  final String? key;



  @JsonKey(
    
    name: r'points',
    required: true,
    includeIfNull: false,
  )


  final List<StatsSeriesSeriesInnerPointsInner> points;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StatsSeriesSeriesInner &&
      other.key == key &&
      other.points == points;

    @override
    int get hashCode =>
        (key == null ? 0 : key.hashCode) +
        points.hashCode;

  factory StatsSeriesSeriesInner.fromJson(Map<String, dynamic> json) => _$StatsSeriesSeriesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$StatsSeriesSeriesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

