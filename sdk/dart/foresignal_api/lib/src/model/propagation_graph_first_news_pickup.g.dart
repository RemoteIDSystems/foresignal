// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'propagation_graph_first_news_pickup.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PropagationGraphFirstNewsPickupCWProxy {
  PropagationGraphFirstNewsPickup storyId(String storyId);

  PropagationGraphFirstNewsPickup articleRef(String? articleRef);

  PropagationGraphFirstNewsPickup observedAt(DateTime observedAt);

  PropagationGraphFirstNewsPickup afterFirstPostSeconds(
    num? afterFirstPostSeconds,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PropagationGraphFirstNewsPickup(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PropagationGraphFirstNewsPickup(...).copyWith(id: 12, name: "My name")
  /// ```
  PropagationGraphFirstNewsPickup call({
    String storyId,
    String? articleRef,
    DateTime observedAt,
    num? afterFirstPostSeconds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPropagationGraphFirstNewsPickup.copyWith(...)` or call `instanceOfPropagationGraphFirstNewsPickup.copyWith.fieldName(value)` for a single field.
class _$PropagationGraphFirstNewsPickupCWProxyImpl
    implements _$PropagationGraphFirstNewsPickupCWProxy {
  const _$PropagationGraphFirstNewsPickupCWProxyImpl(this._value);

  final PropagationGraphFirstNewsPickup _value;

  @override
  PropagationGraphFirstNewsPickup storyId(String storyId) =>
      call(storyId: storyId);

  @override
  PropagationGraphFirstNewsPickup articleRef(String? articleRef) =>
      call(articleRef: articleRef);

  @override
  PropagationGraphFirstNewsPickup observedAt(DateTime observedAt) =>
      call(observedAt: observedAt);

  @override
  PropagationGraphFirstNewsPickup afterFirstPostSeconds(
    num? afterFirstPostSeconds,
  ) => call(afterFirstPostSeconds: afterFirstPostSeconds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PropagationGraphFirstNewsPickup(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PropagationGraphFirstNewsPickup(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PropagationGraphFirstNewsPickup call({
    Object? storyId = const $CopyWithPlaceholder(),
    Object? articleRef = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? afterFirstPostSeconds = const $CopyWithPlaceholder(),
  }) {
    return PropagationGraphFirstNewsPickup(
      storyId: storyId == const $CopyWithPlaceholder() || storyId == null
          ? _value.storyId
          // ignore: cast_nullable_to_non_nullable
          : storyId as String,
      articleRef: articleRef == const $CopyWithPlaceholder()
          ? _value.articleRef
          // ignore: cast_nullable_to_non_nullable
          : articleRef as String?,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
      afterFirstPostSeconds:
          afterFirstPostSeconds == const $CopyWithPlaceholder()
          ? _value.afterFirstPostSeconds
          // ignore: cast_nullable_to_non_nullable
          : afterFirstPostSeconds as num?,
    );
  }
}

extension $PropagationGraphFirstNewsPickupCopyWith
    on PropagationGraphFirstNewsPickup {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPropagationGraphFirstNewsPickup.copyWith(...)` or `instanceOfPropagationGraphFirstNewsPickup.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PropagationGraphFirstNewsPickupCWProxy get copyWith =>
      _$PropagationGraphFirstNewsPickupCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PropagationGraphFirstNewsPickup _$PropagationGraphFirstNewsPickupFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PropagationGraphFirstNewsPickup', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['storyId', 'observedAt']);
  final val = PropagationGraphFirstNewsPickup(
    storyId: $checkedConvert('storyId', (v) => v as String),
    articleRef: $checkedConvert('articleRef', (v) => v as String?),
    observedAt: $checkedConvert(
      'observedAt',
      (v) => DateTime.parse(v as String),
    ),
    afterFirstPostSeconds: $checkedConvert(
      'afterFirstPostSeconds',
      (v) => v as num?,
    ),
  );
  return val;
});

Map<String, dynamic> _$PropagationGraphFirstNewsPickupToJson(
  PropagationGraphFirstNewsPickup instance,
) => <String, dynamic>{
  'storyId': instance.storyId,
  'articleRef': ?instance.articleRef,
  'observedAt': instance.observedAt.toIso8601String(),
  'afterFirstPostSeconds': ?instance.afterFirstPostSeconds,
};
