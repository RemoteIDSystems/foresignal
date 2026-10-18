// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_quota.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialQuotaCWProxy {
  SocialQuota unit(SocialQuotaUnitEnum unit);

  SocialQuota limit(int? limit);

  SocialQuota window(String window);

  SocialQuota rolling(bool? rolling);

  SocialQuota used(int used);

  SocialQuota remaining(int? remaining);

  SocialQuota resetsAt(DateTime? resetsAt);

  SocialQuota description(String? description);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialQuota(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialQuota(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialQuota call({
    SocialQuotaUnitEnum unit,
    int? limit,
    String window,
    bool? rolling,
    int used,
    int? remaining,
    DateTime? resetsAt,
    String? description,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialQuota.copyWith(...)` or call `instanceOfSocialQuota.copyWith.fieldName(value)` for a single field.
class _$SocialQuotaCWProxyImpl implements _$SocialQuotaCWProxy {
  const _$SocialQuotaCWProxyImpl(this._value);

  final SocialQuota _value;

  @override
  SocialQuota unit(SocialQuotaUnitEnum unit) => call(unit: unit);

  @override
  SocialQuota limit(int? limit) => call(limit: limit);

  @override
  SocialQuota window(String window) => call(window: window);

  @override
  SocialQuota rolling(bool? rolling) => call(rolling: rolling);

  @override
  SocialQuota used(int used) => call(used: used);

  @override
  SocialQuota remaining(int? remaining) => call(remaining: remaining);

  @override
  SocialQuota resetsAt(DateTime? resetsAt) => call(resetsAt: resetsAt);

  @override
  SocialQuota description(String? description) =>
      call(description: description);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialQuota(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialQuota(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialQuota call({
    Object? unit = const $CopyWithPlaceholder(),
    Object? limit = const $CopyWithPlaceholder(),
    Object? window = const $CopyWithPlaceholder(),
    Object? rolling = const $CopyWithPlaceholder(),
    Object? used = const $CopyWithPlaceholder(),
    Object? remaining = const $CopyWithPlaceholder(),
    Object? resetsAt = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
  }) {
    return SocialQuota(
      unit: unit == const $CopyWithPlaceholder() || unit == null
          ? _value.unit
          // ignore: cast_nullable_to_non_nullable
          : unit as SocialQuotaUnitEnum,
      limit: limit == const $CopyWithPlaceholder()
          ? _value.limit
          // ignore: cast_nullable_to_non_nullable
          : limit as int?,
      window: window == const $CopyWithPlaceholder() || window == null
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as String,
      rolling: rolling == const $CopyWithPlaceholder()
          ? _value.rolling
          // ignore: cast_nullable_to_non_nullable
          : rolling as bool?,
      used: used == const $CopyWithPlaceholder() || used == null
          ? _value.used
          // ignore: cast_nullable_to_non_nullable
          : used as int,
      remaining: remaining == const $CopyWithPlaceholder()
          ? _value.remaining
          // ignore: cast_nullable_to_non_nullable
          : remaining as int?,
      resetsAt: resetsAt == const $CopyWithPlaceholder()
          ? _value.resetsAt
          // ignore: cast_nullable_to_non_nullable
          : resetsAt as DateTime?,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
    );
  }
}

extension $SocialQuotaCopyWith on SocialQuota {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialQuota.copyWith(...)` or `instanceOfSocialQuota.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialQuotaCWProxy get copyWith => _$SocialQuotaCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialQuota _$SocialQuotaFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialQuota', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['unit', 'window', 'used']);
      final val = SocialQuota(
        unit: $checkedConvert(
          'unit',
          (v) => $enumDecode(_$SocialQuotaUnitEnumEnumMap, v),
        ),
        limit: $checkedConvert('limit', (v) => (v as num?)?.toInt()),
        window: $checkedConvert('window', (v) => v as String),
        rolling: $checkedConvert('rolling', (v) => v as bool?),
        used: $checkedConvert('used', (v) => (v as num).toInt()),
        remaining: $checkedConvert('remaining', (v) => (v as num?)?.toInt()),
        resetsAt: $checkedConvert(
          'resetsAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        description: $checkedConvert('description', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SocialQuotaToJson(SocialQuota instance) =>
    <String, dynamic>{
      'unit': _$SocialQuotaUnitEnumEnumMap[instance.unit]!,
      'limit': ?instance.limit,
      'window': instance.window,
      'rolling': ?instance.rolling,
      'used': instance.used,
      'remaining': ?instance.remaining,
      'resetsAt': ?instance.resetsAt?.toIso8601String(),
      'description': ?instance.description,
    };

const _$SocialQuotaUnitEnumEnumMap = {
  SocialQuotaUnitEnum.requests: 'requests',
  SocialQuotaUnitEnum.units: 'units',
  SocialQuotaUnitEnum.queries: 'queries',
  SocialQuotaUnitEnum.posts: 'posts',
  SocialQuotaUnitEnum.hashtags: 'hashtags',
};
