// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stats_response_thresholds_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StatsResponseThresholdsInnerCWProxy {
  StatsResponseThresholdsInner threshold(num threshold);

  StatsResponseThresholdsInner areaKm2(num areaKm2);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsResponseThresholdsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsResponseThresholdsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  StatsResponseThresholdsInner call({num threshold, num areaKm2});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStatsResponseThresholdsInner.copyWith(...)` or call `instanceOfStatsResponseThresholdsInner.copyWith.fieldName(value)` for a single field.
class _$StatsResponseThresholdsInnerCWProxyImpl
    implements _$StatsResponseThresholdsInnerCWProxy {
  const _$StatsResponseThresholdsInnerCWProxyImpl(this._value);

  final StatsResponseThresholdsInner _value;

  @override
  StatsResponseThresholdsInner threshold(num threshold) =>
      call(threshold: threshold);

  @override
  StatsResponseThresholdsInner areaKm2(num areaKm2) => call(areaKm2: areaKm2);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsResponseThresholdsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsResponseThresholdsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StatsResponseThresholdsInner call({
    Object? threshold = const $CopyWithPlaceholder(),
    Object? areaKm2 = const $CopyWithPlaceholder(),
  }) {
    return StatsResponseThresholdsInner(
      threshold: threshold == const $CopyWithPlaceholder() || threshold == null
          ? _value.threshold
          // ignore: cast_nullable_to_non_nullable
          : threshold as num,
      areaKm2: areaKm2 == const $CopyWithPlaceholder() || areaKm2 == null
          ? _value.areaKm2
          // ignore: cast_nullable_to_non_nullable
          : areaKm2 as num,
    );
  }
}

extension $StatsResponseThresholdsInnerCopyWith
    on StatsResponseThresholdsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStatsResponseThresholdsInner.copyWith(...)` or `instanceOfStatsResponseThresholdsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StatsResponseThresholdsInnerCWProxy get copyWith =>
      _$StatsResponseThresholdsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StatsResponseThresholdsInner _$StatsResponseThresholdsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StatsResponseThresholdsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['threshold', 'areaKm2']);
  final val = StatsResponseThresholdsInner(
    threshold: $checkedConvert('threshold', (v) => v as num),
    areaKm2: $checkedConvert('areaKm2', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$StatsResponseThresholdsInnerToJson(
  StatsResponseThresholdsInner instance,
) => <String, dynamic>{
  'threshold': instance.threshold,
  'areaKm2': instance.areaKm2,
};
