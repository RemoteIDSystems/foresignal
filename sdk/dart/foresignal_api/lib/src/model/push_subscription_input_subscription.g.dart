// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_subscription_input_subscription.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushSubscriptionInputSubscriptionCWProxy {
  PushSubscriptionInputSubscription endpoint(String endpoint);

  PushSubscriptionInputSubscription keys(
    PushSubscriptionInputSubscriptionKeys keys,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushSubscriptionInputSubscription(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushSubscriptionInputSubscription(...).copyWith(id: 12, name: "My name")
  /// ```
  PushSubscriptionInputSubscription call({
    String endpoint,
    PushSubscriptionInputSubscriptionKeys keys,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushSubscriptionInputSubscription.copyWith(...)` or call `instanceOfPushSubscriptionInputSubscription.copyWith.fieldName(value)` for a single field.
class _$PushSubscriptionInputSubscriptionCWProxyImpl
    implements _$PushSubscriptionInputSubscriptionCWProxy {
  const _$PushSubscriptionInputSubscriptionCWProxyImpl(this._value);

  final PushSubscriptionInputSubscription _value;

  @override
  PushSubscriptionInputSubscription endpoint(String endpoint) =>
      call(endpoint: endpoint);

  @override
  PushSubscriptionInputSubscription keys(
    PushSubscriptionInputSubscriptionKeys keys,
  ) => call(keys: keys);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushSubscriptionInputSubscription(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushSubscriptionInputSubscription(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushSubscriptionInputSubscription call({
    Object? endpoint = const $CopyWithPlaceholder(),
    Object? keys = const $CopyWithPlaceholder(),
  }) {
    return PushSubscriptionInputSubscription(
      endpoint: endpoint == const $CopyWithPlaceholder() || endpoint == null
          ? _value.endpoint
          // ignore: cast_nullable_to_non_nullable
          : endpoint as String,
      keys: keys == const $CopyWithPlaceholder() || keys == null
          ? _value.keys
          // ignore: cast_nullable_to_non_nullable
          : keys as PushSubscriptionInputSubscriptionKeys,
    );
  }
}

extension $PushSubscriptionInputSubscriptionCopyWith
    on PushSubscriptionInputSubscription {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushSubscriptionInputSubscription.copyWith(...)` or `instanceOfPushSubscriptionInputSubscription.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushSubscriptionInputSubscriptionCWProxy get copyWith =>
      _$PushSubscriptionInputSubscriptionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushSubscriptionInputSubscription _$PushSubscriptionInputSubscriptionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PushSubscriptionInputSubscription', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['endpoint', 'keys']);
  final val = PushSubscriptionInputSubscription(
    endpoint: $checkedConvert('endpoint', (v) => v as String),
    keys: $checkedConvert(
      'keys',
      (v) => PushSubscriptionInputSubscriptionKeys.fromJson(
        v as Map<String, dynamic>,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$PushSubscriptionInputSubscriptionToJson(
  PushSubscriptionInputSubscription instance,
) => <String, dynamic>{
  'endpoint': instance.endpoint,
  'keys': instance.keys.toJson(),
};
