// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'egress_health_sources_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EgressHealthSourcesInnerCWProxy {
  EgressHealthSourcesInner sourceId(String sourceId);

  EgressHealthSourcesInner backoffUntil(DateTime backoffUntil);

  EgressHealthSourcesInner consecutiveThrottles(int consecutiveThrottles);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EgressHealthSourcesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EgressHealthSourcesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  EgressHealthSourcesInner call({
    String sourceId,
    DateTime backoffUntil,
    int consecutiveThrottles,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEgressHealthSourcesInner.copyWith(...)` or call `instanceOfEgressHealthSourcesInner.copyWith.fieldName(value)` for a single field.
class _$EgressHealthSourcesInnerCWProxyImpl
    implements _$EgressHealthSourcesInnerCWProxy {
  const _$EgressHealthSourcesInnerCWProxyImpl(this._value);

  final EgressHealthSourcesInner _value;

  @override
  EgressHealthSourcesInner sourceId(String sourceId) =>
      call(sourceId: sourceId);

  @override
  EgressHealthSourcesInner backoffUntil(DateTime backoffUntil) =>
      call(backoffUntil: backoffUntil);

  @override
  EgressHealthSourcesInner consecutiveThrottles(int consecutiveThrottles) =>
      call(consecutiveThrottles: consecutiveThrottles);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EgressHealthSourcesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EgressHealthSourcesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EgressHealthSourcesInner call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? backoffUntil = const $CopyWithPlaceholder(),
    Object? consecutiveThrottles = const $CopyWithPlaceholder(),
  }) {
    return EgressHealthSourcesInner(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      backoffUntil:
          backoffUntil == const $CopyWithPlaceholder() || backoffUntil == null
          ? _value.backoffUntil
          // ignore: cast_nullable_to_non_nullable
          : backoffUntil as DateTime,
      consecutiveThrottles:
          consecutiveThrottles == const $CopyWithPlaceholder() ||
              consecutiveThrottles == null
          ? _value.consecutiveThrottles
          // ignore: cast_nullable_to_non_nullable
          : consecutiveThrottles as int,
    );
  }
}

extension $EgressHealthSourcesInnerCopyWith on EgressHealthSourcesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEgressHealthSourcesInner.copyWith(...)` or `instanceOfEgressHealthSourcesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EgressHealthSourcesInnerCWProxy get copyWith =>
      _$EgressHealthSourcesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EgressHealthSourcesInner _$EgressHealthSourcesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EgressHealthSourcesInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['sourceId', 'backoffUntil', 'consecutiveThrottles'],
  );
  final val = EgressHealthSourcesInner(
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    backoffUntil: $checkedConvert(
      'backoffUntil',
      (v) => DateTime.parse(v as String),
    ),
    consecutiveThrottles: $checkedConvert(
      'consecutiveThrottles',
      (v) => (v as num).toInt(),
    ),
  );
  return val;
});

Map<String, dynamic> _$EgressHealthSourcesInnerToJson(
  EgressHealthSourcesInner instance,
) => <String, dynamic>{
  'sourceId': instance.sourceId,
  'backoffUntil': instance.backoffUntil.toIso8601String(),
  'consecutiveThrottles': instance.consecutiveThrottles,
};
