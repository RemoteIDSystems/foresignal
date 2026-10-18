//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_subscription_verify.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushSubscriptionVerify {
  /// Returns a new [PushSubscriptionVerify] instance.
  PushSubscriptionVerify({

    required  this.code,
  });

      /// From the `verify` push (16 random bytes, base64url)
  @JsonKey(
    
    name: r'code',
    required: true,
    includeIfNull: false,
  )


  final String code;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushSubscriptionVerify &&
      other.code == code;

    @override
    int get hashCode =>
        code.hashCode;

  factory PushSubscriptionVerify.fromJson(Map<String, dynamic> json) => _$PushSubscriptionVerifyFromJson(json);

  Map<String, dynamic> toJson() => _$PushSubscriptionVerifyToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

