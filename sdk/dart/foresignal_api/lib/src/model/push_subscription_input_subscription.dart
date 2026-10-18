//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/push_subscription_input_subscription_keys.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_subscription_input_subscription.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushSubscriptionInputSubscription {
  /// Returns a new [PushSubscriptionInputSubscription] instance.
  PushSubscriptionInputSubscription({

    required  this.endpoint,

    required  this.keys,
  });

  @JsonKey(
    
    name: r'endpoint',
    required: true,
    includeIfNull: false,
  )


  final String endpoint;



  @JsonKey(
    
    name: r'keys',
    required: true,
    includeIfNull: false,
  )


  final PushSubscriptionInputSubscriptionKeys keys;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushSubscriptionInputSubscription &&
      other.endpoint == endpoint &&
      other.keys == keys;

    @override
    int get hashCode =>
        endpoint.hashCode +
        keys.hashCode;

  factory PushSubscriptionInputSubscription.fromJson(Map<String, dynamic> json) => _$PushSubscriptionInputSubscriptionFromJson(json);

  Map<String, dynamic> toJson() => _$PushSubscriptionInputSubscriptionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

