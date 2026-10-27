// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'quota_usage.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$QuotaUsageCWProxy {
  QuotaUsage quota(Quota quota);

  QuotaUsage usage(Map<String, Object> usage);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `QuotaUsage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// QuotaUsage(...).copyWith(id: 12, name: "My name")
  /// ```
  QuotaUsage call({Quota quota, Map<String, Object> usage});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfQuotaUsage.copyWith(...)` or call `instanceOfQuotaUsage.copyWith.fieldName(value)` for a single field.
class _$QuotaUsageCWProxyImpl implements _$QuotaUsageCWProxy {
  const _$QuotaUsageCWProxyImpl(this._value);

  final QuotaUsage _value;

  @override
  QuotaUsage quota(Quota quota) => call(quota: quota);

  @override
  QuotaUsage usage(Map<String, Object> usage) => call(usage: usage);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `QuotaUsage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// QuotaUsage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  QuotaUsage call({
    Object? quota = const $CopyWithPlaceholder(),
    Object? usage = const $CopyWithPlaceholder(),
  }) {
    return QuotaUsage(
      quota: quota == const $CopyWithPlaceholder() || quota == null
          ? _value.quota
          // ignore: cast_nullable_to_non_nullable
          : quota as Quota,
      usage: usage == const $CopyWithPlaceholder() || usage == null
          ? _value.usage
          // ignore: cast_nullable_to_non_nullable
          : usage as Map<String, Object>,
    );
  }
}

extension $QuotaUsageCopyWith on QuotaUsage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfQuotaUsage.copyWith(...)` or `instanceOfQuotaUsage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$QuotaUsageCWProxy get copyWith => _$QuotaUsageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

QuotaUsage _$QuotaUsageFromJson(Map<String, dynamic> json) => $checkedCreate(
  'QuotaUsage',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['quota', 'usage']);
    final val = QuotaUsage(
      quota: $checkedConvert(
        'quota',
        (v) => Quota.fromJson(v as Map<String, dynamic>),
      ),
      usage: $checkedConvert(
        'usage',
        (v) =>
            (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$QuotaUsageToJson(QuotaUsage instance) =>
    <String, dynamic>{
      'quota': instance.quota.toJson(),
      'usage': instance.usage,
    };
