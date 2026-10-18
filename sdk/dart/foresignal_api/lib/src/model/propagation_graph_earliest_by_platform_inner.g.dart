// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'propagation_graph_earliest_by_platform_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PropagationGraphEarliestByPlatformInnerCWProxy {
  PropagationGraphEarliestByPlatformInner platform(String platform);

  PropagationGraphEarliestByPlatformInner postRef(String postRef);

  PropagationGraphEarliestByPlatformInner observedAt(DateTime observedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PropagationGraphEarliestByPlatformInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PropagationGraphEarliestByPlatformInner(...).copyWith(id: 12, name: "My name")
  /// ```
  PropagationGraphEarliestByPlatformInner call({
    String platform,
    String postRef,
    DateTime observedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPropagationGraphEarliestByPlatformInner.copyWith(...)` or call `instanceOfPropagationGraphEarliestByPlatformInner.copyWith.fieldName(value)` for a single field.
class _$PropagationGraphEarliestByPlatformInnerCWProxyImpl
    implements _$PropagationGraphEarliestByPlatformInnerCWProxy {
  const _$PropagationGraphEarliestByPlatformInnerCWProxyImpl(this._value);

  final PropagationGraphEarliestByPlatformInner _value;

  @override
  PropagationGraphEarliestByPlatformInner platform(String platform) =>
      call(platform: platform);

  @override
  PropagationGraphEarliestByPlatformInner postRef(String postRef) =>
      call(postRef: postRef);

  @override
  PropagationGraphEarliestByPlatformInner observedAt(DateTime observedAt) =>
      call(observedAt: observedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PropagationGraphEarliestByPlatformInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PropagationGraphEarliestByPlatformInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PropagationGraphEarliestByPlatformInner call({
    Object? platform = const $CopyWithPlaceholder(),
    Object? postRef = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
  }) {
    return PropagationGraphEarliestByPlatformInner(
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String,
      postRef: postRef == const $CopyWithPlaceholder() || postRef == null
          ? _value.postRef
          // ignore: cast_nullable_to_non_nullable
          : postRef as String,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
    );
  }
}

extension $PropagationGraphEarliestByPlatformInnerCopyWith
    on PropagationGraphEarliestByPlatformInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPropagationGraphEarliestByPlatformInner.copyWith(...)` or `instanceOfPropagationGraphEarliestByPlatformInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PropagationGraphEarliestByPlatformInnerCWProxy get copyWith =>
      _$PropagationGraphEarliestByPlatformInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PropagationGraphEarliestByPlatformInner
_$PropagationGraphEarliestByPlatformInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PropagationGraphEarliestByPlatformInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(
        json,
        requiredKeys: const ['platform', 'postRef', 'observedAt'],
      );
      final val = PropagationGraphEarliestByPlatformInner(
        platform: $checkedConvert('platform', (v) => v as String),
        postRef: $checkedConvert('postRef', (v) => v as String),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PropagationGraphEarliestByPlatformInnerToJson(
  PropagationGraphEarliestByPlatformInner instance,
) => <String, dynamic>{
  'platform': instance.platform,
  'postRef': instance.postRef,
  'observedAt': instance.observedAt.toIso8601String(),
};
