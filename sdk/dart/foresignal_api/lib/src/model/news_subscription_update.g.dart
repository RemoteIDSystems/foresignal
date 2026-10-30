// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'news_subscription_update.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NewsSubscriptionUpdateCWProxy {
  NewsSubscriptionUpdate subscriptions(List<NewsSubscription> subscriptions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NewsSubscriptionUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NewsSubscriptionUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  NewsSubscriptionUpdate call({List<NewsSubscription> subscriptions});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNewsSubscriptionUpdate.copyWith(...)` or call `instanceOfNewsSubscriptionUpdate.copyWith.fieldName(value)` for a single field.
class _$NewsSubscriptionUpdateCWProxyImpl
    implements _$NewsSubscriptionUpdateCWProxy {
  const _$NewsSubscriptionUpdateCWProxyImpl(this._value);

  final NewsSubscriptionUpdate _value;

  @override
  NewsSubscriptionUpdate subscriptions(List<NewsSubscription> subscriptions) =>
      call(subscriptions: subscriptions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NewsSubscriptionUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NewsSubscriptionUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NewsSubscriptionUpdate call({
    Object? subscriptions = const $CopyWithPlaceholder(),
  }) {
    return NewsSubscriptionUpdate(
      subscriptions:
          subscriptions == const $CopyWithPlaceholder() || subscriptions == null
          ? _value.subscriptions
          // ignore: cast_nullable_to_non_nullable
          : subscriptions as List<NewsSubscription>,
    );
  }
}

extension $NewsSubscriptionUpdateCopyWith on NewsSubscriptionUpdate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNewsSubscriptionUpdate.copyWith(...)` or `instanceOfNewsSubscriptionUpdate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NewsSubscriptionUpdateCWProxy get copyWith =>
      _$NewsSubscriptionUpdateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NewsSubscriptionUpdate _$NewsSubscriptionUpdateFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NewsSubscriptionUpdate', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['subscriptions']);
  final val = NewsSubscriptionUpdate(
    subscriptions: $checkedConvert(
      'subscriptions',
      (v) => (v as List<dynamic>)
          .map((e) => NewsSubscription.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$NewsSubscriptionUpdateToJson(
  NewsSubscriptionUpdate instance,
) => <String, dynamic>{
  'subscriptions': instance.subscriptions.map((e) => e.toJson()).toList(),
};
