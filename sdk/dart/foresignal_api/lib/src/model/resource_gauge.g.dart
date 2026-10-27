// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'resource_gauge.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ResourceGaugeCWProxy {
  ResourceGauge totalBytes(int? totalBytes);

  ResourceGauge usedBytes(int? usedBytes);

  ResourceGauge freeBytes(int? freeBytes);

  ResourceGauge usedPercent(num? usedPercent);

  ResourceGauge status(String status);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResourceGauge(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResourceGauge(...).copyWith(id: 12, name: "My name")
  /// ```
  ResourceGauge call({
    int? totalBytes,
    int? usedBytes,
    int? freeBytes,
    num? usedPercent,
    String status,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfResourceGauge.copyWith(...)` or call `instanceOfResourceGauge.copyWith.fieldName(value)` for a single field.
class _$ResourceGaugeCWProxyImpl implements _$ResourceGaugeCWProxy {
  const _$ResourceGaugeCWProxyImpl(this._value);

  final ResourceGauge _value;

  @override
  ResourceGauge totalBytes(int? totalBytes) => call(totalBytes: totalBytes);

  @override
  ResourceGauge usedBytes(int? usedBytes) => call(usedBytes: usedBytes);

  @override
  ResourceGauge freeBytes(int? freeBytes) => call(freeBytes: freeBytes);

  @override
  ResourceGauge usedPercent(num? usedPercent) => call(usedPercent: usedPercent);

  @override
  ResourceGauge status(String status) => call(status: status);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResourceGauge(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResourceGauge(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ResourceGauge call({
    Object? totalBytes = const $CopyWithPlaceholder(),
    Object? usedBytes = const $CopyWithPlaceholder(),
    Object? freeBytes = const $CopyWithPlaceholder(),
    Object? usedPercent = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
  }) {
    return ResourceGauge(
      totalBytes: totalBytes == const $CopyWithPlaceholder()
          ? _value.totalBytes
          // ignore: cast_nullable_to_non_nullable
          : totalBytes as int?,
      usedBytes: usedBytes == const $CopyWithPlaceholder()
          ? _value.usedBytes
          // ignore: cast_nullable_to_non_nullable
          : usedBytes as int?,
      freeBytes: freeBytes == const $CopyWithPlaceholder()
          ? _value.freeBytes
          // ignore: cast_nullable_to_non_nullable
          : freeBytes as int?,
      usedPercent: usedPercent == const $CopyWithPlaceholder()
          ? _value.usedPercent
          // ignore: cast_nullable_to_non_nullable
          : usedPercent as num?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as String,
    );
  }
}

extension $ResourceGaugeCopyWith on ResourceGauge {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfResourceGauge.copyWith(...)` or `instanceOfResourceGauge.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ResourceGaugeCWProxy get copyWith => _$ResourceGaugeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ResourceGauge _$ResourceGaugeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ResourceGauge', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = ResourceGauge(
        totalBytes: $checkedConvert('totalBytes', (v) => (v as num?)?.toInt()),
        usedBytes: $checkedConvert('usedBytes', (v) => (v as num?)?.toInt()),
        freeBytes: $checkedConvert('freeBytes', (v) => (v as num?)?.toInt()),
        usedPercent: $checkedConvert('usedPercent', (v) => v as num?),
        status: $checkedConvert('status', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$ResourceGaugeToJson(ResourceGauge instance) =>
    <String, dynamic>{
      'totalBytes': ?instance.totalBytes,
      'usedBytes': ?instance.usedBytes,
      'freeBytes': ?instance.freeBytes,
      'usedPercent': ?instance.usedPercent,
      'status': instance.status,
    };
