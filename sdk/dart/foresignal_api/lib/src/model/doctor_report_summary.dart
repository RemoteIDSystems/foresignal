//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/doctor_report_summary_top_finding.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'doctor_report_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DoctorReportSummary {
  /// Returns a new [DoctorReportSummary] instance.
  DoctorReportSummary({

    required  this.id,

    required  this.status,

     this.topFinding,

    required  this.createdAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final DoctorReportSummaryStatusEnum status;



  @JsonKey(
    
    name: r'topFinding',
    required: false,
    includeIfNull: false,
  )


  final DoctorReportSummaryTopFinding? topFinding;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DoctorReportSummary &&
      other.id == id &&
      other.status == status &&
      other.topFinding == topFinding &&
      other.createdAt == createdAt;

    @override
    int get hashCode =>
        id.hashCode +
        status.hashCode +
        (topFinding == null ? 0 : topFinding.hashCode) +
        createdAt.hashCode;

  factory DoctorReportSummary.fromJson(Map<String, dynamic> json) => _$DoctorReportSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$DoctorReportSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DoctorReportSummaryStatusEnum {
@JsonValue(r'OK')
OK(r'OK'),
@JsonValue(r'DEGRADED_INFO')
DEGRADED_INFO(r'DEGRADED_INFO'),
@JsonValue(r'BROKEN')
BROKEN(r'BROKEN');

const DoctorReportSummaryStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


