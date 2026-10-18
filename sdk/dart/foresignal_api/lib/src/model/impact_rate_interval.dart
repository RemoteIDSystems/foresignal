//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'impact_rate_interval.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ImpactRateInterval {
  /// Returns a new [ImpactRateInterval] instance.
  ImpactRateInterval({

    required  this.lo,

    required  this.hi,
  });

  @JsonKey(
    
    name: r'lo',
    required: true,
    includeIfNull: false,
  )


  final num lo;



  @JsonKey(
    
    name: r'hi',
    required: true,
    includeIfNull: false,
  )


  final num hi;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ImpactRateInterval &&
      other.lo == lo &&
      other.hi == hi;

    @override
    int get hashCode =>
        lo.hashCode +
        hi.hashCode;

  factory ImpactRateInterval.fromJson(Map<String, dynamic> json) => _$ImpactRateIntervalFromJson(json);

  Map<String, dynamic> toJson() => _$ImpactRateIntervalToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

