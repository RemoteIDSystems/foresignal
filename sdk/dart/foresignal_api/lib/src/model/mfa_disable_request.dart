//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mfa_disable_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MfaDisableRequest {
  /// Returns a new [MfaDisableRequest] instance.
  MfaDisableRequest({

     this.code,

     this.password,
  });

      /// A current TOTP or recovery code
  @JsonKey(
    
    name: r'code',
    required: false,
    includeIfNull: false,
  )


  final String? code;



  @JsonKey(
    
    name: r'password',
    required: false,
    includeIfNull: false,
  )


  final String? password;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MfaDisableRequest &&
      other.code == code &&
      other.password == password;

    @override
    int get hashCode =>
        (code == null ? 0 : code.hashCode) +
        (password == null ? 0 : password.hashCode);

  factory MfaDisableRequest.fromJson(Map<String, dynamic> json) => _$MfaDisableRequestFromJson(json);

  Map<String, dynamic> toJson() => _$MfaDisableRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

