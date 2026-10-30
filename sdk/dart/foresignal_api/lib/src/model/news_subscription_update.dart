//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/news_subscription.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'news_subscription_update.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NewsSubscriptionUpdate {
  /// Returns a new [NewsSubscriptionUpdate] instance.
  NewsSubscriptionUpdate({

    required  this.subscriptions,
  });

  @JsonKey(
    
    name: r'subscriptions',
    required: true,
    includeIfNull: false,
  )


  final List<NewsSubscription> subscriptions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NewsSubscriptionUpdate &&
      other.subscriptions == subscriptions;

    @override
    int get hashCode =>
        subscriptions.hashCode;

  factory NewsSubscriptionUpdate.fromJson(Map<String, dynamic> json) => _$NewsSubscriptionUpdateFromJson(json);

  Map<String, dynamic> toJson() => _$NewsSubscriptionUpdateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

