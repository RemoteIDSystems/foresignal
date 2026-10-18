//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/structure_diff.dart';
import 'package:foresignal_api/src/model/doctor_report_probe.dart';
import 'package:foresignal_api/src/model/doctor_finding.dart';
import 'package:foresignal_api/src/model/doctor_report_llm.dart';
import 'package:foresignal_api/src/model/doctor_report_fixture.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'doctor_report.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DoctorReport {
  /// Returns a new [DoctorReport] instance.
  DoctorReport({

    required  this.id,

    required  this.sourceId,

    required  this.createdAt,

    required  this.trigger,

    required  this.status,

    required  this.findings,

     this.diff,

     this.probe,

     this.fixture,

     this.llm,

     this.adapterVersion,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'trigger',
    required: true,
    includeIfNull: false,
  )


  final DoctorReportTriggerEnum trigger;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final DoctorReportStatusEnum status;



  @JsonKey(
    
    name: r'findings',
    required: true,
    includeIfNull: false,
  )


  final List<DoctorFinding> findings;



  @JsonKey(
    
    name: r'diff',
    required: false,
    includeIfNull: false,
  )


  final StructureDiff? diff;



  @JsonKey(
    
    name: r'probe',
    required: false,
    includeIfNull: false,
  )


  final DoctorReportProbe? probe;



  @JsonKey(
    
    name: r'fixture',
    required: false,
    includeIfNull: false,
  )


  final DoctorReportFixture? fixture;



  @JsonKey(
    
    name: r'llm',
    required: false,
    includeIfNull: false,
  )


  final DoctorReportLlm? llm;



  @JsonKey(
    
    name: r'adapterVersion',
    required: false,
    includeIfNull: false,
  )


  final String? adapterVersion;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DoctorReport &&
      other.id == id &&
      other.sourceId == sourceId &&
      other.createdAt == createdAt &&
      other.trigger == trigger &&
      other.status == status &&
      other.findings == findings &&
      other.diff == diff &&
      other.probe == probe &&
      other.fixture == fixture &&
      other.llm == llm &&
      other.adapterVersion == adapterVersion;

    @override
    int get hashCode =>
        id.hashCode +
        sourceId.hashCode +
        createdAt.hashCode +
        trigger.hashCode +
        status.hashCode +
        findings.hashCode +
        (diff == null ? 0 : diff.hashCode) +
        (probe == null ? 0 : probe.hashCode) +
        (fixture == null ? 0 : fixture.hashCode) +
        (llm == null ? 0 : llm.hashCode) +
        (adapterVersion == null ? 0 : adapterVersion.hashCode);

  factory DoctorReport.fromJson(Map<String, dynamic> json) => _$DoctorReportFromJson(json);

  Map<String, dynamic> toJson() => _$DoctorReportToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DoctorReportTriggerEnum {
@JsonValue(r'MANUAL')
MANUAL(r'MANUAL'),
@JsonValue(r'AUTO')
AUTO(r'AUTO'),
@JsonValue(r'MCP')
MCP(r'MCP');

const DoctorReportTriggerEnum(this.value);

final String value;

@override
String toString() => value;
}


enum DoctorReportStatusEnum {
@JsonValue(r'OK')
OK(r'OK'),
@JsonValue(r'DEGRADED_INFO')
DEGRADED_INFO(r'DEGRADED_INFO'),
@JsonValue(r'BROKEN')
BROKEN(r'BROKEN');

const DoctorReportStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


