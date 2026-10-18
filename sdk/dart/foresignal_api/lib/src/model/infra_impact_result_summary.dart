//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'infra_impact_result_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InfraImpactResultSummary {
  /// Returns a new [InfraImpactResultSummary] instance.
  InfraImpactResultSummary({

     this.byType,

     this.byLabel,
  });

  @JsonKey(
    
    name: r'byType',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Map<String, int>>? byType;



  @JsonKey(
    
    name: r'byLabel',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? byLabel;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InfraImpactResultSummary &&
      other.byType == byType &&
      other.byLabel == byLabel;

    @override
    int get hashCode =>
        byType.hashCode +
        byLabel.hashCode;

  factory InfraImpactResultSummary.fromJson(Map<String, dynamic> json) => _$InfraImpactResultSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$InfraImpactResultSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

