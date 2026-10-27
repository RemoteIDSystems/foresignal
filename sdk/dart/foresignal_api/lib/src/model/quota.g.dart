// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'quota.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$QuotaCWProxy {
  Quota apiRateLimitPerMinute(int? apiRateLimitPerMinute);

  Quota maxStorageBytes(int? maxStorageBytes);

  Quota maxAlerts(int? maxAlerts);

  Quota maxChannels(int? maxChannels);

  Quota maxSavedViews(int? maxSavedViews);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Quota(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Quota(...).copyWith(id: 12, name: "My name")
  /// ```
  Quota call({
    int? apiRateLimitPerMinute,
    int? maxStorageBytes,
    int? maxAlerts,
    int? maxChannels,
    int? maxSavedViews,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfQuota.copyWith(...)` or call `instanceOfQuota.copyWith.fieldName(value)` for a single field.
class _$QuotaCWProxyImpl implements _$QuotaCWProxy {
  const _$QuotaCWProxyImpl(this._value);

  final Quota _value;

  @override
  Quota apiRateLimitPerMinute(int? apiRateLimitPerMinute) =>
      call(apiRateLimitPerMinute: apiRateLimitPerMinute);

  @override
  Quota maxStorageBytes(int? maxStorageBytes) =>
      call(maxStorageBytes: maxStorageBytes);

  @override
  Quota maxAlerts(int? maxAlerts) => call(maxAlerts: maxAlerts);

  @override
  Quota maxChannels(int? maxChannels) => call(maxChannels: maxChannels);

  @override
  Quota maxSavedViews(int? maxSavedViews) => call(maxSavedViews: maxSavedViews);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Quota(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Quota(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Quota call({
    Object? apiRateLimitPerMinute = const $CopyWithPlaceholder(),
    Object? maxStorageBytes = const $CopyWithPlaceholder(),
    Object? maxAlerts = const $CopyWithPlaceholder(),
    Object? maxChannels = const $CopyWithPlaceholder(),
    Object? maxSavedViews = const $CopyWithPlaceholder(),
  }) {
    return Quota(
      apiRateLimitPerMinute:
          apiRateLimitPerMinute == const $CopyWithPlaceholder()
          ? _value.apiRateLimitPerMinute
          // ignore: cast_nullable_to_non_nullable
          : apiRateLimitPerMinute as int?,
      maxStorageBytes: maxStorageBytes == const $CopyWithPlaceholder()
          ? _value.maxStorageBytes
          // ignore: cast_nullable_to_non_nullable
          : maxStorageBytes as int?,
      maxAlerts: maxAlerts == const $CopyWithPlaceholder()
          ? _value.maxAlerts
          // ignore: cast_nullable_to_non_nullable
          : maxAlerts as int?,
      maxChannels: maxChannels == const $CopyWithPlaceholder()
          ? _value.maxChannels
          // ignore: cast_nullable_to_non_nullable
          : maxChannels as int?,
      maxSavedViews: maxSavedViews == const $CopyWithPlaceholder()
          ? _value.maxSavedViews
          // ignore: cast_nullable_to_non_nullable
          : maxSavedViews as int?,
    );
  }
}

extension $QuotaCopyWith on Quota {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfQuota.copyWith(...)` or `instanceOfQuota.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$QuotaCWProxy get copyWith => _$QuotaCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Quota _$QuotaFromJson(Map<String, dynamic> json) => $checkedCreate(
  'Quota',
  json,
  ($checkedConvert) {
    final val = Quota(
      apiRateLimitPerMinute: $checkedConvert(
        'apiRateLimitPerMinute',
        (v) => (v as num?)?.toInt(),
      ),
      maxStorageBytes: $checkedConvert(
        'maxStorageBytes',
        (v) => (v as num?)?.toInt(),
      ),
      maxAlerts: $checkedConvert('maxAlerts', (v) => (v as num?)?.toInt()),
      maxChannels: $checkedConvert('maxChannels', (v) => (v as num?)?.toInt()),
      maxSavedViews: $checkedConvert(
        'maxSavedViews',
        (v) => (v as num?)?.toInt(),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$QuotaToJson(Quota instance) => <String, dynamic>{
  'apiRateLimitPerMinute': ?instance.apiRateLimitPerMinute,
  'maxStorageBytes': ?instance.maxStorageBytes,
  'maxAlerts': ?instance.maxAlerts,
  'maxChannels': ?instance.maxChannels,
  'maxSavedViews': ?instance.maxSavedViews,
};
