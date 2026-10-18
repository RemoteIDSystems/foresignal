//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analytic_finding_dismissal.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalyticFindingDismissal {
  /// Returns a new [AnalyticFindingDismissal] instance.
  AnalyticFindingDismissal({

    required  this.reason,

    required  this.at,

    required  this.by,
  });

  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'by',
    required: true,
    includeIfNull: false,
  )


  final String by;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalyticFindingDismissal &&
      other.reason == reason &&
      other.at == at &&
      other.by == by;

    @override
    int get hashCode =>
        reason.hashCode +
        at.hashCode +
        by.hashCode;

  factory AnalyticFindingDismissal.fromJson(Map<String, dynamic> json) => _$AnalyticFindingDismissalFromJson(json);

  Map<String, dynamic> toJson() => _$AnalyticFindingDismissalToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

