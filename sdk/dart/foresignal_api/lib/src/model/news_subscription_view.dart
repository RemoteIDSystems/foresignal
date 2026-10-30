//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/news_subscription.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'news_subscription_view.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NewsSubscriptionView {
  /// Returns a new [NewsSubscriptionView] instance.
  NewsSubscriptionView({

    required  this.subscriptions,

     this.updatedAt,
  });

  @JsonKey(
    
    name: r'subscriptions',
    required: true,
    includeIfNull: false,
  )


  final List<NewsSubscription> subscriptions;



      /// When the caller last replaced the subscriptions, or null when never
  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NewsSubscriptionView &&
      other.subscriptions == subscriptions &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        subscriptions.hashCode +
        (updatedAt == null ? 0 : updatedAt.hashCode);

  factory NewsSubscriptionView.fromJson(Map<String, dynamic> json) => _$NewsSubscriptionViewFromJson(json);

  Map<String, dynamic> toJson() => _$NewsSubscriptionViewToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

