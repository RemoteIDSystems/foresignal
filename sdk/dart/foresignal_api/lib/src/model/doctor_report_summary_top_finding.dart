//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'doctor_report_summary_top_finding.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DoctorReportSummaryTopFinding {
  /// Returns a new [DoctorReportSummaryTopFinding] instance.
  DoctorReportSummaryTopFinding({

    required  this.code,

     this.suggestedAction,
  });

  @JsonKey(
    
    name: r'code',
    required: true,
    includeIfNull: false,
  )


  final DoctorReportSummaryTopFindingCodeEnum code;



  @JsonKey(
    
    name: r'suggestedAction',
    required: false,
    includeIfNull: false,
  )


  final String? suggestedAction;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DoctorReportSummaryTopFinding &&
      other.code == code &&
      other.suggestedAction == suggestedAction;

    @override
    int get hashCode =>
        code.hashCode +
        suggestedAction.hashCode;

  factory DoctorReportSummaryTopFinding.fromJson(Map<String, dynamic> json) => _$DoctorReportSummaryTopFindingFromJson(json);

  Map<String, dynamic> toJson() => _$DoctorReportSummaryTopFindingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DoctorReportSummaryTopFindingCodeEnum {
@JsonValue(r'AUTH_FAILED')
AUTH_FAILED(r'AUTH_FAILED'),
@JsonValue(r'UA_BLOCKED')
UA_BLOCKED(r'UA_BLOCKED'),
@JsonValue(r'ENDPOINT_MOVED')
ENDPOINT_MOVED(r'ENDPOINT_MOVED'),
@JsonValue(r'RATE_LIMITED')
RATE_LIMITED(r'RATE_LIMITED'),
@JsonValue(r'UPSTREAM_ERROR')
UPSTREAM_ERROR(r'UPSTREAM_ERROR'),
@JsonValue(r'HTML_INSTEAD_OF_DATA')
HTML_INSTEAD_OF_DATA(r'HTML_INSTEAD_OF_DATA'),
@JsonValue(r'EMPTY_RESULT')
EMPTY_RESULT(r'EMPTY_RESULT'),
@JsonValue(r'FIELD_RENAMED')
FIELD_RENAMED(r'FIELD_RENAMED'),
@JsonValue(r'FIELD_REMOVED')
FIELD_REMOVED(r'FIELD_REMOVED'),
@JsonValue(r'TYPE_CHANGED')
TYPE_CHANGED(r'TYPE_CHANGED'),
@JsonValue(r'FORMAT_CHANGED')
FORMAT_CHANGED(r'FORMAT_CHANGED'),
@JsonValue(r'PARSER_REJECTS')
PARSER_REJECTS(r'PARSER_REJECTS'),
@JsonValue(r'COVERAGE_DROPPED')
COVERAGE_DROPPED(r'COVERAGE_DROPPED'),
@JsonValue(r'NO_CHANGE_DETECTED')
NO_CHANGE_DETECTED(r'NO_CHANGE_DETECTED'),
@JsonValue(r'PROBE_SKIPPED_BUDGET')
PROBE_SKIPPED_BUDGET(r'PROBE_SKIPPED_BUDGET');

const DoctorReportSummaryTopFindingCodeEnum(this.value);

final String value;

@override
String toString() => value;
}


