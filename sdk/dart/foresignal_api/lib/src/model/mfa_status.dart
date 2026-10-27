//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/mfa_recovery_codes.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mfa_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MfaStatus {
  /// Returns a new [MfaStatus] instance.
  MfaStatus({

    required  this.enabled,

     this.enrolledAt,

     this.required_,

     this.recoveryCodes,
  });

  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'enrolledAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? enrolledAt;



      /// True when a role the user holds (Admin/Operator) requires MFA
  @JsonKey(
    
    name: r'required',
    required: false,
    includeIfNull: false,
  )


  final bool? required_;



      /// Present only on the verifyMfa answer that activated MFA: the first single-use recovery codes, shown once (R2-024)
  @JsonKey(
    
    name: r'recoveryCodes',
    required: false,
    includeIfNull: false,
  )


  final MfaRecoveryCodes? recoveryCodes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MfaStatus &&
      other.enabled == enabled &&
      other.enrolledAt == enrolledAt &&
      other.required_ == required_ &&
      other.recoveryCodes == recoveryCodes;

    @override
    int get hashCode =>
        enabled.hashCode +
        (enrolledAt == null ? 0 : enrolledAt.hashCode) +
        required_.hashCode +
        recoveryCodes.hashCode;

  factory MfaStatus.fromJson(Map<String, dynamic> json) => _$MfaStatusFromJson(json);

  Map<String, dynamic> toJson() => _$MfaStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

