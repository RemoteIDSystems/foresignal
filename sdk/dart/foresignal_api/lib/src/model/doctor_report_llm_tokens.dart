//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'doctor_report_llm_tokens.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DoctorReportLlmTokens {
  /// Returns a new [DoctorReportLlmTokens] instance.
  DoctorReportLlmTokens({

     this.input,

     this.output,
  });

  @JsonKey(
    
    name: r'input',
    required: false,
    includeIfNull: false,
  )


  final int? input;



  @JsonKey(
    
    name: r'output',
    required: false,
    includeIfNull: false,
  )


  final int? output;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DoctorReportLlmTokens &&
      other.input == input &&
      other.output == output;

    @override
    int get hashCode =>
        input.hashCode +
        output.hashCode;

  factory DoctorReportLlmTokens.fromJson(Map<String, dynamic> json) => _$DoctorReportLlmTokensFromJson(json);

  Map<String, dynamic> toJson() => _$DoctorReportLlmTokensToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

