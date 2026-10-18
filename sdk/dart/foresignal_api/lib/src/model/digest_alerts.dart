//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/digest_alerts_by_status.dart';
import 'package:foresignal_api/src/model/alert.dart';
import 'package:foresignal_api/src/model/digest_alerts_by_rule_inner.dart';
import 'package:foresignal_api/src/model/digest_alerts_by_severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'digest_alerts.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DigestAlerts {
  /// Returns a new [DigestAlerts] instance.
  DigestAlerts({

     this.sinceAlertId,

     this.untilAlertId,

     this.bySeverity,

     this.byStatus,

     this.byRule,

     this.items,
  });

  @JsonKey(
    
    name: r'sinceAlertId',
    required: false,
    includeIfNull: false,
  )


  final int? sinceAlertId;



  @JsonKey(
    
    name: r'untilAlertId',
    required: false,
    includeIfNull: false,
  )


  final int? untilAlertId;



  @JsonKey(
    
    name: r'bySeverity',
    required: false,
    includeIfNull: false,
  )


  final DigestAlertsBySeverity? bySeverity;



  @JsonKey(
    
    name: r'byStatus',
    required: false,
    includeIfNull: false,
  )


  final DigestAlertsByStatus? byStatus;



  @JsonKey(
    
    name: r'byRule',
    required: false,
    includeIfNull: false,
  )


  final List<DigestAlertsByRuleInner>? byRule;



      /// Critical first
  @JsonKey(
    
    name: r'items',
    required: false,
    includeIfNull: false,
  )


  final List<Alert>? items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DigestAlerts &&
      other.sinceAlertId == sinceAlertId &&
      other.untilAlertId == untilAlertId &&
      other.bySeverity == bySeverity &&
      other.byStatus == byStatus &&
      other.byRule == byRule &&
      other.items == items;

    @override
    int get hashCode =>
        (sinceAlertId == null ? 0 : sinceAlertId.hashCode) +
        (untilAlertId == null ? 0 : untilAlertId.hashCode) +
        bySeverity.hashCode +
        byStatus.hashCode +
        byRule.hashCode +
        items.hashCode;

  factory DigestAlerts.fromJson(Map<String, dynamic> json) => _$DigestAlertsFromJson(json);

  Map<String, dynamic> toJson() => _$DigestAlertsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

