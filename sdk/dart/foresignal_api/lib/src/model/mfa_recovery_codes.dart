//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mfa_recovery_codes.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MfaRecoveryCodes {
  /// Returns a new [MfaRecoveryCodes] instance.
  MfaRecoveryCodes({

    required  this.codes,

    required  this.generatedAt,
  });

      /// Ten single-use codes (xxxxx-xxxxx); stored hashed, accepted case-insensitively with or without the dash
  @JsonKey(
    
    name: r'codes',
    required: true,
    includeIfNull: false,
  )


  final List<String> codes;



  @JsonKey(
    
    name: r'generatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime generatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MfaRecoveryCodes &&
      other.codes == codes &&
      other.generatedAt == generatedAt;

    @override
    int get hashCode =>
        codes.hashCode +
        generatedAt.hashCode;

  factory MfaRecoveryCodes.fromJson(Map<String, dynamic> json) => _$MfaRecoveryCodesFromJson(json);

  Map<String, dynamic> toJson() => _$MfaRecoveryCodesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

