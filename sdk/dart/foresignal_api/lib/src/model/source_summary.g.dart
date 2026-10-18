// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'source_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SourceSummaryCWProxy {
  SourceSummary id(String id);

  SourceSummary name(String name);

  SourceSummary kinds(List<String> kinds);

  SourceSummary mode(SourceSummaryModeEnum mode);

  SourceSummary custom(bool? custom);

  SourceSummary enabled(bool enabled);

  SourceSummary status(HealthStatus status);

  SourceSummary credentialsRequired(bool? credentialsRequired);

  SourceSummary credentialsSet(bool? credentialsSet);

  SourceSummary interval(String? interval);

  SourceSummary budget(Budget? budget);

  SourceSummary budgetUsedToday(int? budgetUsedToday);

  SourceSummary lastSuccessAt(DateTime? lastSuccessAt);

  SourceSummary historyTier(HistoryTier? historyTier);

  SourceSummary licence(String? licence);

  SourceSummary termsUrl(String? termsUrl);

  SourceSummary lastDataAt(DateTime? lastDataAt);

  SourceSummary access(SourceAccess? access);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  SourceSummary call({
    String id,
    String name,
    List<String> kinds,
    SourceSummaryModeEnum mode,
    bool? custom,
    bool enabled,
    HealthStatus status,
    bool? credentialsRequired,
    bool? credentialsSet,
    String? interval,
    Budget? budget,
    int? budgetUsedToday,
    DateTime? lastSuccessAt,
    HistoryTier? historyTier,
    String? licence,
    String? termsUrl,
    DateTime? lastDataAt,
    SourceAccess? access,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSourceSummary.copyWith(...)` or call `instanceOfSourceSummary.copyWith.fieldName(value)` for a single field.
class _$SourceSummaryCWProxyImpl implements _$SourceSummaryCWProxy {
  const _$SourceSummaryCWProxyImpl(this._value);

  final SourceSummary _value;

  @override
  SourceSummary id(String id) => call(id: id);

  @override
  SourceSummary name(String name) => call(name: name);

  @override
  SourceSummary kinds(List<String> kinds) => call(kinds: kinds);

  @override
  SourceSummary mode(SourceSummaryModeEnum mode) => call(mode: mode);

  @override
  SourceSummary custom(bool? custom) => call(custom: custom);

  @override
  SourceSummary enabled(bool enabled) => call(enabled: enabled);

  @override
  SourceSummary status(HealthStatus status) => call(status: status);

  @override
  SourceSummary credentialsRequired(bool? credentialsRequired) =>
      call(credentialsRequired: credentialsRequired);

  @override
  SourceSummary credentialsSet(bool? credentialsSet) =>
      call(credentialsSet: credentialsSet);

  @override
  SourceSummary interval(String? interval) => call(interval: interval);

  @override
  SourceSummary budget(Budget? budget) => call(budget: budget);

  @override
  SourceSummary budgetUsedToday(int? budgetUsedToday) =>
      call(budgetUsedToday: budgetUsedToday);

  @override
  SourceSummary lastSuccessAt(DateTime? lastSuccessAt) =>
      call(lastSuccessAt: lastSuccessAt);

  @override
  SourceSummary historyTier(HistoryTier? historyTier) =>
      call(historyTier: historyTier);

  @override
  SourceSummary licence(String? licence) => call(licence: licence);

  @override
  SourceSummary termsUrl(String? termsUrl) => call(termsUrl: termsUrl);

  @override
  SourceSummary lastDataAt(DateTime? lastDataAt) =>
      call(lastDataAt: lastDataAt);

  @override
  SourceSummary access(SourceAccess? access) => call(access: access);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SourceSummary call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? custom = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? credentialsRequired = const $CopyWithPlaceholder(),
    Object? credentialsSet = const $CopyWithPlaceholder(),
    Object? interval = const $CopyWithPlaceholder(),
    Object? budget = const $CopyWithPlaceholder(),
    Object? budgetUsedToday = const $CopyWithPlaceholder(),
    Object? lastSuccessAt = const $CopyWithPlaceholder(),
    Object? historyTier = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? termsUrl = const $CopyWithPlaceholder(),
    Object? lastDataAt = const $CopyWithPlaceholder(),
    Object? access = const $CopyWithPlaceholder(),
  }) {
    return SourceSummary(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      kinds: kinds == const $CopyWithPlaceholder() || kinds == null
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>,
      mode: mode == const $CopyWithPlaceholder() || mode == null
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as SourceSummaryModeEnum,
      custom: custom == const $CopyWithPlaceholder()
          ? _value.custom
          // ignore: cast_nullable_to_non_nullable
          : custom as bool?,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as HealthStatus,
      credentialsRequired: credentialsRequired == const $CopyWithPlaceholder()
          ? _value.credentialsRequired
          // ignore: cast_nullable_to_non_nullable
          : credentialsRequired as bool?,
      credentialsSet: credentialsSet == const $CopyWithPlaceholder()
          ? _value.credentialsSet
          // ignore: cast_nullable_to_non_nullable
          : credentialsSet as bool?,
      interval: interval == const $CopyWithPlaceholder()
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as String?,
      budget: budget == const $CopyWithPlaceholder()
          ? _value.budget
          // ignore: cast_nullable_to_non_nullable
          : budget as Budget?,
      budgetUsedToday: budgetUsedToday == const $CopyWithPlaceholder()
          ? _value.budgetUsedToday
          // ignore: cast_nullable_to_non_nullable
          : budgetUsedToday as int?,
      lastSuccessAt: lastSuccessAt == const $CopyWithPlaceholder()
          ? _value.lastSuccessAt
          // ignore: cast_nullable_to_non_nullable
          : lastSuccessAt as DateTime?,
      historyTier: historyTier == const $CopyWithPlaceholder()
          ? _value.historyTier
          // ignore: cast_nullable_to_non_nullable
          : historyTier as HistoryTier?,
      licence: licence == const $CopyWithPlaceholder()
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as String?,
      termsUrl: termsUrl == const $CopyWithPlaceholder()
          ? _value.termsUrl
          // ignore: cast_nullable_to_non_nullable
          : termsUrl as String?,
      lastDataAt: lastDataAt == const $CopyWithPlaceholder()
          ? _value.lastDataAt
          // ignore: cast_nullable_to_non_nullable
          : lastDataAt as DateTime?,
      access: access == const $CopyWithPlaceholder()
          ? _value.access
          // ignore: cast_nullable_to_non_nullable
          : access as SourceAccess?,
    );
  }
}

extension $SourceSummaryCopyWith on SourceSummary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSourceSummary.copyWith(...)` or `instanceOfSourceSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SourceSummaryCWProxy get copyWith => _$SourceSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SourceSummary _$SourceSummaryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SourceSummary', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['id', 'name', 'kinds', 'mode', 'enabled', 'status'],
  );
  final val = SourceSummary(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    kinds: $checkedConvert(
      'kinds',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    mode: $checkedConvert(
      'mode',
      (v) => $enumDecode(_$SourceSummaryModeEnumEnumMap, v),
    ),
    custom: $checkedConvert('custom', (v) => v as bool?),
    enabled: $checkedConvert('enabled', (v) => v as bool),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$HealthStatusEnumMap, v),
    ),
    credentialsRequired: $checkedConvert(
      'credentialsRequired',
      (v) => v as bool?,
    ),
    credentialsSet: $checkedConvert('credentialsSet', (v) => v as bool?),
    interval: $checkedConvert('interval', (v) => v as String?),
    budget: $checkedConvert(
      'budget',
      (v) => v == null ? null : Budget.fromJson(v as Map<String, dynamic>),
    ),
    budgetUsedToday: $checkedConvert(
      'budgetUsedToday',
      (v) => (v as num?)?.toInt(),
    ),
    lastSuccessAt: $checkedConvert(
      'lastSuccessAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    historyTier: $checkedConvert(
      'historyTier',
      (v) => $enumDecodeNullable(_$HistoryTierEnumMap, v),
    ),
    licence: $checkedConvert('licence', (v) => v as String?),
    termsUrl: $checkedConvert('termsUrl', (v) => v as String?),
    lastDataAt: $checkedConvert(
      'lastDataAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    access: $checkedConvert(
      'access',
      (v) =>
          v == null ? null : SourceAccess.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$SourceSummaryToJson(SourceSummary instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'kinds': instance.kinds,
      'mode': _$SourceSummaryModeEnumEnumMap[instance.mode]!,
      'custom': ?instance.custom,
      'enabled': instance.enabled,
      'status': _$HealthStatusEnumMap[instance.status]!,
      'credentialsRequired': ?instance.credentialsRequired,
      'credentialsSet': ?instance.credentialsSet,
      'interval': ?instance.interval,
      'budget': ?instance.budget?.toJson(),
      'budgetUsedToday': ?instance.budgetUsedToday,
      'lastSuccessAt': ?instance.lastSuccessAt?.toIso8601String(),
      'historyTier': ?_$HistoryTierEnumMap[instance.historyTier],
      'licence': ?instance.licence,
      'termsUrl': ?instance.termsUrl,
      'lastDataAt': ?instance.lastDataAt?.toIso8601String(),
      'access': ?instance.access?.toJson(),
    };

const _$SourceSummaryModeEnumEnumMap = {
  SourceSummaryModeEnum.polling: 'polling',
  SourceSummaryModeEnum.streaming: 'streaming',
  SourceSummaryModeEnum.push: 'push',
  SourceSummaryModeEnum.derived: 'derived',
};

const _$HealthStatusEnumMap = {
  HealthStatus.healthy: 'healthy',
  HealthStatus.degraded: 'degraded',
  HealthStatus.failing: 'failing',
  HealthStatus.idle: 'idle',
  HealthStatus.disabled: 'disabled',
};

const _$HistoryTierEnumMap = {
  HistoryTier.STANDARD: 'STANDARD',
  HistoryTier.TRACK: 'TRACK',
};
