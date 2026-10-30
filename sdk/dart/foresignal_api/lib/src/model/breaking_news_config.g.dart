// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'breaking_news_config.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BreakingNewsConfigCWProxy {
  BreakingNewsConfig enabled(bool enabled);

  BreakingNewsConfig ruleIds(List<String>? ruleIds);

  BreakingNewsConfig minSeverity(Severity? minSeverity);

  BreakingNewsConfig cooldownMinutes(int? cooldownMinutes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BreakingNewsConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BreakingNewsConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  BreakingNewsConfig call({
    bool enabled,
    List<String>? ruleIds,
    Severity? minSeverity,
    int? cooldownMinutes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBreakingNewsConfig.copyWith(...)` or call `instanceOfBreakingNewsConfig.copyWith.fieldName(value)` for a single field.
class _$BreakingNewsConfigCWProxyImpl implements _$BreakingNewsConfigCWProxy {
  const _$BreakingNewsConfigCWProxyImpl(this._value);

  final BreakingNewsConfig _value;

  @override
  BreakingNewsConfig enabled(bool enabled) => call(enabled: enabled);

  @override
  BreakingNewsConfig ruleIds(List<String>? ruleIds) => call(ruleIds: ruleIds);

  @override
  BreakingNewsConfig minSeverity(Severity? minSeverity) =>
      call(minSeverity: minSeverity);

  @override
  BreakingNewsConfig cooldownMinutes(int? cooldownMinutes) =>
      call(cooldownMinutes: cooldownMinutes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BreakingNewsConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BreakingNewsConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BreakingNewsConfig call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? ruleIds = const $CopyWithPlaceholder(),
    Object? minSeverity = const $CopyWithPlaceholder(),
    Object? cooldownMinutes = const $CopyWithPlaceholder(),
  }) {
    return BreakingNewsConfig(
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      ruleIds: ruleIds == const $CopyWithPlaceholder()
          ? _value.ruleIds
          // ignore: cast_nullable_to_non_nullable
          : ruleIds as List<String>?,
      minSeverity: minSeverity == const $CopyWithPlaceholder()
          ? _value.minSeverity
          // ignore: cast_nullable_to_non_nullable
          : minSeverity as Severity?,
      cooldownMinutes: cooldownMinutes == const $CopyWithPlaceholder()
          ? _value.cooldownMinutes
          // ignore: cast_nullable_to_non_nullable
          : cooldownMinutes as int?,
    );
  }
}

extension $BreakingNewsConfigCopyWith on BreakingNewsConfig {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBreakingNewsConfig.copyWith(...)` or `instanceOfBreakingNewsConfig.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BreakingNewsConfigCWProxy get copyWith =>
      _$BreakingNewsConfigCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BreakingNewsConfig _$BreakingNewsConfigFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BreakingNewsConfig', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['enabled']);
      final val = BreakingNewsConfig(
        enabled: $checkedConvert('enabled', (v) => v as bool? ?? false),
        ruleIds: $checkedConvert(
          'ruleIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        minSeverity: $checkedConvert(
          'minSeverity',
          (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
        ),
        cooldownMinutes: $checkedConvert(
          'cooldownMinutes',
          (v) => (v as num?)?.toInt() ?? 60,
        ),
      );
      return val;
    });

Map<String, dynamic> _$BreakingNewsConfigToJson(BreakingNewsConfig instance) =>
    <String, dynamic>{
      'enabled': instance.enabled,
      'ruleIds': ?instance.ruleIds,
      'minSeverity': ?_$SeverityEnumMap[instance.minSeverity],
      'cooldownMinutes': ?instance.cooldownMinutes,
    };

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};
