//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'doctor_report_fixture.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DoctorReportFixture {
  /// Returns a new [DoctorReportFixture] instance.
  DoctorReportFixture({

    required  this.file,

     this.capturedAt,

     this.sha256,

     this.truncated,
  });

  @JsonKey(
    
    name: r'file',
    required: true,
    includeIfNull: false,
  )


  final String file;



  @JsonKey(
    
    name: r'capturedAt',
    required: false,
    includeIfNull: false,
  )


  final String? capturedAt;



  @JsonKey(
    
    name: r'sha256',
    required: false,
    includeIfNull: false,
  )


  final String? sha256;



      /// True when the packaged fixture was cut to the 256 KB cap
  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DoctorReportFixture &&
      other.file == file &&
      other.capturedAt == capturedAt &&
      other.sha256 == sha256 &&
      other.truncated == truncated;

    @override
    int get hashCode =>
        file.hashCode +
        (capturedAt == null ? 0 : capturedAt.hashCode) +
        sha256.hashCode +
        truncated.hashCode;

  factory DoctorReportFixture.fromJson(Map<String, dynamic> json) => _$DoctorReportFixtureFromJson(json);

  Map<String, dynamic> toJson() => _$DoctorReportFixtureToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

