//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_subscription_input_subscription_keys.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushSubscriptionInputSubscriptionKeys {
  /// Returns a new [PushSubscriptionInputSubscriptionKeys] instance.
  PushSubscriptionInputSubscriptionKeys({

    required  this.p256dh,

    required  this.auth,
  });

  @JsonKey(
    
    name: r'p256dh',
    required: true,
    includeIfNull: false,
  )


  final String p256dh;



  @JsonKey(
    
    name: r'auth',
    required: true,
    includeIfNull: false,
  )


  final String auth;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushSubscriptionInputSubscriptionKeys &&
      other.p256dh == p256dh &&
      other.auth == auth;

    @override
    int get hashCode =>
        p256dh.hashCode +
        auth.hashCode;

  factory PushSubscriptionInputSubscriptionKeys.fromJson(Map<String, dynamic> json) => _$PushSubscriptionInputSubscriptionKeysFromJson(json);

  Map<String, dynamic> toJson() => _$PushSubscriptionInputSubscriptionKeysToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

