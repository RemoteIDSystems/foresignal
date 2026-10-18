//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'doctor_run_accepted.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DoctorRunAccepted {
  /// Returns a new [DoctorRunAccepted] instance.
  DoctorRunAccepted({

    required  this.reportId,

    required  this.status,

    required  this.location,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'reportId',
    required: true,
    includeIfNull: false,
  )


  final String reportId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final DoctorRunAcceptedStatusEnum status;



  @JsonKey(
    
    name: r'location',
    required: true,
    includeIfNull: false,
  )


  final String location;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DoctorRunAccepted &&
      other.reportId == reportId &&
      other.status == status &&
      other.location == location;

    @override
    int get hashCode =>
        reportId.hashCode +
        status.hashCode +
        location.hashCode;

  factory DoctorRunAccepted.fromJson(Map<String, dynamic> json) => _$DoctorRunAcceptedFromJson(json);

  Map<String, dynamic> toJson() => _$DoctorRunAcceptedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DoctorRunAcceptedStatusEnum {
@JsonValue(r'running')
running(r'running');

const DoctorRunAcceptedStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


