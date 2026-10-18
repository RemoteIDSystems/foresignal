//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rule_quality_precision_ci.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RuleQualityPrecisionCi {
  /// Returns a new [RuleQualityPrecisionCi] instance.
  RuleQualityPrecisionCi({

     this.lower,

     this.upper,
  });

  @JsonKey(
    
    name: r'lower',
    required: false,
    includeIfNull: false,
  )


  final num? lower;



  @JsonKey(
    
    name: r'upper',
    required: false,
    includeIfNull: false,
  )


  final num? upper;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RuleQualityPrecisionCi &&
      other.lower == lower &&
      other.upper == upper;

    @override
    int get hashCode =>
        lower.hashCode +
        upper.hashCode;

  factory RuleQualityPrecisionCi.fromJson(Map<String, dynamic> json) => _$RuleQualityPrecisionCiFromJson(json);

  Map<String, dynamic> toJson() => _$RuleQualityPrecisionCiToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

