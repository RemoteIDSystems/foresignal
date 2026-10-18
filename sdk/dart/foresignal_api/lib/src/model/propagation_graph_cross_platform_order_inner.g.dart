// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'propagation_graph_cross_platform_order_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PropagationGraphCrossPlatformOrderInnerCWProxy {
  PropagationGraphCrossPlatformOrderInner platform(String platform);

  PropagationGraphCrossPlatformOrderInner firstObservedAt(
    DateTime firstObservedAt,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PropagationGraphCrossPlatformOrderInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PropagationGraphCrossPlatformOrderInner(...).copyWith(id: 12, name: "My name")
  /// ```
  PropagationGraphCrossPlatformOrderInner call({
    String platform,
    DateTime firstObservedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPropagationGraphCrossPlatformOrderInner.copyWith(...)` or call `instanceOfPropagationGraphCrossPlatformOrderInner.copyWith.fieldName(value)` for a single field.
class _$PropagationGraphCrossPlatformOrderInnerCWProxyImpl
    implements _$PropagationGraphCrossPlatformOrderInnerCWProxy {
  const _$PropagationGraphCrossPlatformOrderInnerCWProxyImpl(this._value);

  final PropagationGraphCrossPlatformOrderInner _value;

  @override
  PropagationGraphCrossPlatformOrderInner platform(String platform) =>
      call(platform: platform);

  @override
  PropagationGraphCrossPlatformOrderInner firstObservedAt(
    DateTime firstObservedAt,
  ) => call(firstObservedAt: firstObservedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PropagationGraphCrossPlatformOrderInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PropagationGraphCrossPlatformOrderInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PropagationGraphCrossPlatformOrderInner call({
    Object? platform = const $CopyWithPlaceholder(),
    Object? firstObservedAt = const $CopyWithPlaceholder(),
  }) {
    return PropagationGraphCrossPlatformOrderInner(
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String,
      firstObservedAt:
          firstObservedAt == const $CopyWithPlaceholder() ||
              firstObservedAt == null
          ? _value.firstObservedAt
          // ignore: cast_nullable_to_non_nullable
          : firstObservedAt as DateTime,
    );
  }
}

extension $PropagationGraphCrossPlatformOrderInnerCopyWith
    on PropagationGraphCrossPlatformOrderInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPropagationGraphCrossPlatformOrderInner.copyWith(...)` or `instanceOfPropagationGraphCrossPlatformOrderInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PropagationGraphCrossPlatformOrderInnerCWProxy get copyWith =>
      _$PropagationGraphCrossPlatformOrderInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PropagationGraphCrossPlatformOrderInner
_$PropagationGraphCrossPlatformOrderInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PropagationGraphCrossPlatformOrderInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['platform', 'firstObservedAt']);
      final val = PropagationGraphCrossPlatformOrderInner(
        platform: $checkedConvert('platform', (v) => v as String),
        firstObservedAt: $checkedConvert(
          'firstObservedAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PropagationGraphCrossPlatformOrderInnerToJson(
  PropagationGraphCrossPlatformOrderInner instance,
) => <String, dynamic>{
  'platform': instance.platform,
  'firstObservedAt': instance.firstObservedAt.toIso8601String(),
};
