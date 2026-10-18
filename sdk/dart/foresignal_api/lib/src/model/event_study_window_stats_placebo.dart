//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_study_window_stats_placebo.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventStudyWindowStatsPlacebo {
  /// Returns a new [EventStudyWindowStatsPlacebo] instance.
  EventStudyWindowStatsPlacebo({

    required  this.count,

     this.mean,

     this.sd,

     this.p05,

     this.p50,

     this.p95,
  });

  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;



  @JsonKey(
    
    name: r'mean',
    required: false,
    includeIfNull: false,
  )


  final num? mean;



  @JsonKey(
    
    name: r'sd',
    required: false,
    includeIfNull: false,
  )


  final num? sd;



  @JsonKey(
    
    name: r'p05',
    required: false,
    includeIfNull: false,
  )


  final num? p05;



  @JsonKey(
    
    name: r'p50',
    required: false,
    includeIfNull: false,
  )


  final num? p50;



  @JsonKey(
    
    name: r'p95',
    required: false,
    includeIfNull: false,
  )


  final num? p95;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventStudyWindowStatsPlacebo &&
      other.count == count &&
      other.mean == mean &&
      other.sd == sd &&
      other.p05 == p05 &&
      other.p50 == p50 &&
      other.p95 == p95;

    @override
    int get hashCode =>
        count.hashCode +
        (mean == null ? 0 : mean.hashCode) +
        (sd == null ? 0 : sd.hashCode) +
        (p05 == null ? 0 : p05.hashCode) +
        (p50 == null ? 0 : p50.hashCode) +
        (p95 == null ? 0 : p95.hashCode);

  factory EventStudyWindowStatsPlacebo.fromJson(Map<String, dynamic> json) => _$EventStudyWindowStatsPlaceboFromJson(json);

  Map<String, dynamic> toJson() => _$EventStudyWindowStatsPlaceboToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

