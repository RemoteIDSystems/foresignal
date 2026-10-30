// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'news_subscription_view.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NewsSubscriptionViewCWProxy {
  NewsSubscriptionView subscriptions(List<NewsSubscription> subscriptions);

  NewsSubscriptionView updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NewsSubscriptionView(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NewsSubscriptionView(...).copyWith(id: 12, name: "My name")
  /// ```
  NewsSubscriptionView call({
    List<NewsSubscription> subscriptions,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNewsSubscriptionView.copyWith(...)` or call `instanceOfNewsSubscriptionView.copyWith.fieldName(value)` for a single field.
class _$NewsSubscriptionViewCWProxyImpl
    implements _$NewsSubscriptionViewCWProxy {
  const _$NewsSubscriptionViewCWProxyImpl(this._value);

  final NewsSubscriptionView _value;

  @override
  NewsSubscriptionView subscriptions(List<NewsSubscription> subscriptions) =>
      call(subscriptions: subscriptions);

  @override
  NewsSubscriptionView updatedAt(DateTime? updatedAt) =>
      call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NewsSubscriptionView(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NewsSubscriptionView(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NewsSubscriptionView call({
    Object? subscriptions = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return NewsSubscriptionView(
      subscriptions:
          subscriptions == const $CopyWithPlaceholder() || subscriptions == null
          ? _value.subscriptions
          // ignore: cast_nullable_to_non_nullable
          : subscriptions as List<NewsSubscription>,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $NewsSubscriptionViewCopyWith on NewsSubscriptionView {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNewsSubscriptionView.copyWith(...)` or `instanceOfNewsSubscriptionView.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NewsSubscriptionViewCWProxy get copyWith =>
      _$NewsSubscriptionViewCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NewsSubscriptionView _$NewsSubscriptionViewFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NewsSubscriptionView', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['subscriptions']);
  final val = NewsSubscriptionView(
    subscriptions: $checkedConvert(
      'subscriptions',
      (v) => (v as List<dynamic>)
          .map((e) => NewsSubscription.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$NewsSubscriptionViewToJson(
  NewsSubscriptionView instance,
) => <String, dynamic>{
  'subscriptions': instance.subscriptions.map((e) => e.toJson()).toList(),
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
};
