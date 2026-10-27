// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'resource_threshold.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ResourceThresholdCWProxy {
  ResourceThreshold warnFreePercent(num? warnFreePercent);

  ResourceThreshold warnFreeBytes(int? warnFreeBytes);

  ResourceThreshold criticalFreePercent(num? criticalFreePercent);

  ResourceThreshold criticalFreeBytes(int? criticalFreeBytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResourceThreshold(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResourceThreshold(...).copyWith(id: 12, name: "My name")
  /// ```
  ResourceThreshold call({
    num? warnFreePercent,
    int? warnFreeBytes,
    num? criticalFreePercent,
    int? criticalFreeBytes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfResourceThreshold.copyWith(...)` or call `instanceOfResourceThreshold.copyWith.fieldName(value)` for a single field.
class _$ResourceThresholdCWProxyImpl implements _$ResourceThresholdCWProxy {
  const _$ResourceThresholdCWProxyImpl(this._value);

  final ResourceThreshold _value;

  @override
  ResourceThreshold warnFreePercent(num? warnFreePercent) =>
      call(warnFreePercent: warnFreePercent);

  @override
  ResourceThreshold warnFreeBytes(int? warnFreeBytes) =>
      call(warnFreeBytes: warnFreeBytes);

  @override
  ResourceThreshold criticalFreePercent(num? criticalFreePercent) =>
      call(criticalFreePercent: criticalFreePercent);

  @override
  ResourceThreshold criticalFreeBytes(int? criticalFreeBytes) =>
      call(criticalFreeBytes: criticalFreeBytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResourceThreshold(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResourceThreshold(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ResourceThreshold call({
    Object? warnFreePercent = const $CopyWithPlaceholder(),
    Object? warnFreeBytes = const $CopyWithPlaceholder(),
    Object? criticalFreePercent = const $CopyWithPlaceholder(),
    Object? criticalFreeBytes = const $CopyWithPlaceholder(),
  }) {
    return ResourceThreshold(
      warnFreePercent: warnFreePercent == const $CopyWithPlaceholder()
          ? _value.warnFreePercent
          // ignore: cast_nullable_to_non_nullable
          : warnFreePercent as num?,
      warnFreeBytes: warnFreeBytes == const $CopyWithPlaceholder()
          ? _value.warnFreeBytes
          // ignore: cast_nullable_to_non_nullable
          : warnFreeBytes as int?,
      criticalFreePercent: criticalFreePercent == const $CopyWithPlaceholder()
          ? _value.criticalFreePercent
          // ignore: cast_nullable_to_non_nullable
          : criticalFreePercent as num?,
      criticalFreeBytes: criticalFreeBytes == const $CopyWithPlaceholder()
          ? _value.criticalFreeBytes
          // ignore: cast_nullable_to_non_nullable
          : criticalFreeBytes as int?,
    );
  }
}

extension $ResourceThresholdCopyWith on ResourceThreshold {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfResourceThreshold.copyWith(...)` or `instanceOfResourceThreshold.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ResourceThresholdCWProxy get copyWith =>
      _$ResourceThresholdCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ResourceThreshold _$ResourceThresholdFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ResourceThreshold', json, ($checkedConvert) {
      final val = ResourceThreshold(
        warnFreePercent: $checkedConvert('warnFreePercent', (v) => v as num?),
        warnFreeBytes: $checkedConvert(
          'warnFreeBytes',
          (v) => (v as num?)?.toInt(),
        ),
        criticalFreePercent: $checkedConvert(
          'criticalFreePercent',
          (v) => v as num?,
        ),
        criticalFreeBytes: $checkedConvert(
          'criticalFreeBytes',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ResourceThresholdToJson(ResourceThreshold instance) =>
    <String, dynamic>{
      'warnFreePercent': ?instance.warnFreePercent,
      'warnFreeBytes': ?instance.warnFreeBytes,
      'criticalFreePercent': ?instance.criticalFreePercent,
      'criticalFreeBytes': ?instance.criticalFreeBytes,
    };
