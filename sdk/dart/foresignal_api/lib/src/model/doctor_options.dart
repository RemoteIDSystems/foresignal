//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'doctor_options.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DoctorOptions {
  /// Returns a new [DoctorOptions] instance.
  DoctorOptions({

     this.probe = true,

     this.llm,

     this.detail,
  });

      /// Probe the live upstream through the budgeted client
  @JsonKey(
    defaultValue: true,
    name: r'probe',
    required: false,
    includeIfNull: false,
  )


  final bool? probe;



      /// LLM explanation and patch proposal (026 `chat` role, metered as `doctor`)
  @JsonKey(
    
    name: r'llm',
    required: false,
    includeIfNull: false,
  )


  final DoctorOptionsLlmEnum? llm;



      /// Context size: compact ≤ 6k tokens, full ≤ 30k
  @JsonKey(
    
    name: r'detail',
    required: false,
    includeIfNull: false,
  )


  final DoctorOptionsDetailEnum? detail;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DoctorOptions &&
      other.probe == probe &&
      other.llm == llm &&
      other.detail == detail;

    @override
    int get hashCode =>
        probe.hashCode +
        llm.hashCode +
        detail.hashCode;

  factory DoctorOptions.fromJson(Map<String, dynamic> json) => _$DoctorOptionsFromJson(json);

  Map<String, dynamic> toJson() => _$DoctorOptionsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// LLM explanation and patch proposal (026 `chat` role, metered as `doctor`)
enum DoctorOptionsLlmEnum {
@JsonValue(r'auto')
auto(r'auto'),
@JsonValue(r'on')
on_(r'on'),
@JsonValue(r'off')
off(r'off');

const DoctorOptionsLlmEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Context size: compact ≤ 6k tokens, full ≤ 30k
enum DoctorOptionsDetailEnum {
@JsonValue(r'compact')
compact(r'compact'),
@JsonValue(r'full')
full(r'full');

const DoctorOptionsDetailEnum(this.value);

final String value;

@override
String toString() => value;
}


