//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'briefing_run_breaking.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BriefingRunBreaking {
  /// Returns a new [BriefingRunBreaking] instance.
  BriefingRunBreaking({

    required  this.alertId,

     this.ruleId,

     this.ruleName,

    required  this.severity,

     this.firedAt,

     this.message,
  });

  @JsonKey(
    
    name: r'alertId',
    required: true,
    includeIfNull: false,
  )


  final int alertId;



  @JsonKey(
    
    name: r'ruleId',
    required: false,
    includeIfNull: false,
  )


  final String? ruleId;



  @JsonKey(
    
    name: r'ruleName',
    required: false,
    includeIfNull: false,
  )


  final String? ruleName;



  @JsonKey(
    
    name: r'severity',
    required: true,
    includeIfNull: false,
  )


  final BriefingRunBreakingSeverityEnum severity;



  @JsonKey(
    
    name: r'firedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? firedAt;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BriefingRunBreaking &&
      other.alertId == alertId &&
      other.ruleId == ruleId &&
      other.ruleName == ruleName &&
      other.severity == severity &&
      other.firedAt == firedAt &&
      other.message == message;

    @override
    int get hashCode =>
        alertId.hashCode +
        (ruleId == null ? 0 : ruleId.hashCode) +
        (ruleName == null ? 0 : ruleName.hashCode) +
        severity.hashCode +
        (firedAt == null ? 0 : firedAt.hashCode) +
        (message == null ? 0 : message.hashCode);

  factory BriefingRunBreaking.fromJson(Map<String, dynamic> json) => _$BriefingRunBreakingFromJson(json);

  Map<String, dynamic> toJson() => _$BriefingRunBreakingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BriefingRunBreakingSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const BriefingRunBreakingSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


