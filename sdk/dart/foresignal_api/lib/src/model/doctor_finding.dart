//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'doctor_finding.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DoctorFinding {
  /// Returns a new [DoctorFinding] instance.
  DoctorFinding({

    required  this.code,

    required  this.severity,

    required  this.evidence,

     this.path,

    required  this.suggestedAction,

     this.heuristic,
  });

  @JsonKey(
    
    name: r'code',
    required: true,
    includeIfNull: false,
  )


  final DoctorFindingCodeEnum code;



  @JsonKey(
    
    name: r'severity',
    required: true,
    includeIfNull: false,
  )


  final Severity severity;



      /// Facts behind the finding; heuristic evidence (parser path scan) is marked as such
  @JsonKey(
    
    name: r'evidence',
    required: true,
    includeIfNull: false,
  )


  final List<String> evidence;



      /// Structure path concerned, e.g. `features[].properties.mag`
  @JsonKey(
    
    name: r'path',
    required: false,
    includeIfNull: false,
  )


  final String? path;



  @JsonKey(
    
    name: r'suggestedAction',
    required: true,
    includeIfNull: false,
  )


  final String suggestedAction;



  @JsonKey(
    
    name: r'heuristic',
    required: false,
    includeIfNull: false,
  )


  final bool? heuristic;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DoctorFinding &&
      other.code == code &&
      other.severity == severity &&
      other.evidence == evidence &&
      other.path == path &&
      other.suggestedAction == suggestedAction &&
      other.heuristic == heuristic;

    @override
    int get hashCode =>
        code.hashCode +
        severity.hashCode +
        evidence.hashCode +
        (path == null ? 0 : path.hashCode) +
        suggestedAction.hashCode +
        heuristic.hashCode;

  factory DoctorFinding.fromJson(Map<String, dynamic> json) => _$DoctorFindingFromJson(json);

  Map<String, dynamic> toJson() => _$DoctorFindingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DoctorFindingCodeEnum {
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

const DoctorFindingCodeEnum(this.value);

final String value;

@override
String toString() => value;
}


