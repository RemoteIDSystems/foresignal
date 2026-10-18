// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_rasters_caches_value.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemRastersCachesValueCWProxy {
  SystemRastersCachesValue sizeBytes(int? sizeBytes);

  SystemRastersCachesValue maxBytes(int? maxBytes);

  SystemRastersCachesValue hitRate(num? hitRate);

  SystemRastersCachesValue evictionRate(num? evictionRate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemRastersCachesValue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemRastersCachesValue(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemRastersCachesValue call({
    int? sizeBytes,
    int? maxBytes,
    num? hitRate,
    num? evictionRate,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemRastersCachesValue.copyWith(...)` or call `instanceOfSystemRastersCachesValue.copyWith.fieldName(value)` for a single field.
class _$SystemRastersCachesValueCWProxyImpl
    implements _$SystemRastersCachesValueCWProxy {
  const _$SystemRastersCachesValueCWProxyImpl(this._value);

  final SystemRastersCachesValue _value;

  @override
  SystemRastersCachesValue sizeBytes(int? sizeBytes) =>
      call(sizeBytes: sizeBytes);

  @override
  SystemRastersCachesValue maxBytes(int? maxBytes) => call(maxBytes: maxBytes);

  @override
  SystemRastersCachesValue hitRate(num? hitRate) => call(hitRate: hitRate);

  @override
  SystemRastersCachesValue evictionRate(num? evictionRate) =>
      call(evictionRate: evictionRate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemRastersCachesValue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemRastersCachesValue(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemRastersCachesValue call({
    Object? sizeBytes = const $CopyWithPlaceholder(),
    Object? maxBytes = const $CopyWithPlaceholder(),
    Object? hitRate = const $CopyWithPlaceholder(),
    Object? evictionRate = const $CopyWithPlaceholder(),
  }) {
    return SystemRastersCachesValue(
      sizeBytes: sizeBytes == const $CopyWithPlaceholder()
          ? _value.sizeBytes
          // ignore: cast_nullable_to_non_nullable
          : sizeBytes as int?,
      maxBytes: maxBytes == const $CopyWithPlaceholder()
          ? _value.maxBytes
          // ignore: cast_nullable_to_non_nullable
          : maxBytes as int?,
      hitRate: hitRate == const $CopyWithPlaceholder()
          ? _value.hitRate
          // ignore: cast_nullable_to_non_nullable
          : hitRate as num?,
      evictionRate: evictionRate == const $CopyWithPlaceholder()
          ? _value.evictionRate
          // ignore: cast_nullable_to_non_nullable
          : evictionRate as num?,
    );
  }
}

extension $SystemRastersCachesValueCopyWith on SystemRastersCachesValue {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemRastersCachesValue.copyWith(...)` or `instanceOfSystemRastersCachesValue.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemRastersCachesValueCWProxy get copyWith =>
      _$SystemRastersCachesValueCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemRastersCachesValue _$SystemRastersCachesValueFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SystemRastersCachesValue', json, ($checkedConvert) {
  final val = SystemRastersCachesValue(
    sizeBytes: $checkedConvert('sizeBytes', (v) => (v as num?)?.toInt()),
    maxBytes: $checkedConvert('maxBytes', (v) => (v as num?)?.toInt()),
    hitRate: $checkedConvert('hitRate', (v) => v as num?),
    evictionRate: $checkedConvert('evictionRate', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$SystemRastersCachesValueToJson(
  SystemRastersCachesValue instance,
) => <String, dynamic>{
  'sizeBytes': ?instance.sizeBytes,
  'maxBytes': ?instance.maxBytes,
  'hitRate': ?instance.hitRate,
  'evictionRate': ?instance.evictionRate,
};
