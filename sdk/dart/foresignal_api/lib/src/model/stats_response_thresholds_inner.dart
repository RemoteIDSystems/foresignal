//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stats_response_thresholds_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StatsResponseThresholdsInner {
  /// Returns a new [StatsResponseThresholdsInner] instance.
  StatsResponseThresholdsInner({

    required  this.threshold,

    required  this.areaKm2,
  });

  @JsonKey(
    
    name: r'threshold',
    required: true,
    includeIfNull: false,
  )


  final num threshold;



  @JsonKey(
    
    name: r'areaKm2',
    required: true,
    includeIfNull: false,
  )


  final num areaKm2;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StatsResponseThresholdsInner &&
      other.threshold == threshold &&
      other.areaKm2 == areaKm2;

    @override
    int get hashCode =>
        threshold.hashCode +
        areaKm2.hashCode;

  factory StatsResponseThresholdsInner.fromJson(Map<String, dynamic> json) => _$StatsResponseThresholdsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$StatsResponseThresholdsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

