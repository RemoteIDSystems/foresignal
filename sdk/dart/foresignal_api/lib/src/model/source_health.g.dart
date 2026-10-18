// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'source_health.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SourceHealthCWProxy {
  SourceHealth sourceId(String sourceId);

  SourceHealth status(HealthStatus status);

  SourceHealth statusReason(String? statusReason);

  SourceHealth lastAttemptAt(DateTime? lastAttemptAt);

  SourceHealth lastSuccessAt(DateTime? lastSuccessAt);

  SourceHealth lastErrorAt(DateTime? lastErrorAt);

  SourceHealth lastError(String? lastError);

  SourceHealth lastHttpStatus(int? lastHttpStatus);

  SourceHealth consecutiveFailures(int consecutiveFailures);

  SourceHealth lastRecordsSeen(int? lastRecordsSeen);

  SourceHealth lastRecordsChanged(int? lastRecordsChanged);

  SourceHealth errorRateLastHour(num? errorRateLastHour);

  SourceHealth backoffUntil(DateTime? backoffUntil);

  SourceHealth budget(Budget? budget);

  SourceHealth budgetUsedToday(int? budgetUsedToday);

  SourceHealth budgetRemainingToday(int? budgetRemainingToday);

  SourceHealth refusedLastHour(int? refusedLastHour);

  SourceHealth requestsLastHour(int? requestsLastHour);

  SourceHealth bytesLastHour(int? bytesLastHour);

  SourceHealth coverage(Coverage? coverage);

  SourceHealth storage(SourceStorage? storage);

  SourceHealth lastDataAt(DateTime? lastDataAt);

  SourceHealth budgetHoursRemaining(num? budgetHoursRemaining);

  SourceHealth newestFrameAt(DateTime? newestFrameAt);

  SourceHealth frameAgeSeconds(int? frameAgeSeconds);

  SourceHealth details(Map<String, Object>? details);

  SourceHealth lastDoctorReport(DoctorReportSummary? lastDoctorReport);

  SourceHealth spentUsdThisMonth(num? spentUsdThisMonth);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceHealth(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceHealth(...).copyWith(id: 12, name: "My name")
  /// ```
  SourceHealth call({
    String sourceId,
    HealthStatus status,
    String? statusReason,
    DateTime? lastAttemptAt,
    DateTime? lastSuccessAt,
    DateTime? lastErrorAt,
    String? lastError,
    int? lastHttpStatus,
    int consecutiveFailures,
    int? lastRecordsSeen,
    int? lastRecordsChanged,
    num? errorRateLastHour,
    DateTime? backoffUntil,
    Budget? budget,
    int? budgetUsedToday,
    int? budgetRemainingToday,
    int? refusedLastHour,
    int? requestsLastHour,
    int? bytesLastHour,
    Coverage? coverage,
    SourceStorage? storage,
    DateTime? lastDataAt,
    num? budgetHoursRemaining,
    DateTime? newestFrameAt,
    int? frameAgeSeconds,
    Map<String, Object>? details,
    DoctorReportSummary? lastDoctorReport,
    num? spentUsdThisMonth,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSourceHealth.copyWith(...)` or call `instanceOfSourceHealth.copyWith.fieldName(value)` for a single field.
class _$SourceHealthCWProxyImpl implements _$SourceHealthCWProxy {
  const _$SourceHealthCWProxyImpl(this._value);

  final SourceHealth _value;

  @override
  SourceHealth sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  SourceHealth status(HealthStatus status) => call(status: status);

  @override
  SourceHealth statusReason(String? statusReason) =>
      call(statusReason: statusReason);

  @override
  SourceHealth lastAttemptAt(DateTime? lastAttemptAt) =>
      call(lastAttemptAt: lastAttemptAt);

  @override
  SourceHealth lastSuccessAt(DateTime? lastSuccessAt) =>
      call(lastSuccessAt: lastSuccessAt);

  @override
  SourceHealth lastErrorAt(DateTime? lastErrorAt) =>
      call(lastErrorAt: lastErrorAt);

  @override
  SourceHealth lastError(String? lastError) => call(lastError: lastError);

  @override
  SourceHealth lastHttpStatus(int? lastHttpStatus) =>
      call(lastHttpStatus: lastHttpStatus);

  @override
  SourceHealth consecutiveFailures(int consecutiveFailures) =>
      call(consecutiveFailures: consecutiveFailures);

  @override
  SourceHealth lastRecordsSeen(int? lastRecordsSeen) =>
      call(lastRecordsSeen: lastRecordsSeen);

  @override
  SourceHealth lastRecordsChanged(int? lastRecordsChanged) =>
      call(lastRecordsChanged: lastRecordsChanged);

  @override
  SourceHealth errorRateLastHour(num? errorRateLastHour) =>
      call(errorRateLastHour: errorRateLastHour);

  @override
  SourceHealth backoffUntil(DateTime? backoffUntil) =>
      call(backoffUntil: backoffUntil);

  @override
  SourceHealth budget(Budget? budget) => call(budget: budget);

  @override
  SourceHealth budgetUsedToday(int? budgetUsedToday) =>
      call(budgetUsedToday: budgetUsedToday);

  @override
  SourceHealth budgetRemainingToday(int? budgetRemainingToday) =>
      call(budgetRemainingToday: budgetRemainingToday);

  @override
  SourceHealth refusedLastHour(int? refusedLastHour) =>
      call(refusedLastHour: refusedLastHour);

  @override
  SourceHealth requestsLastHour(int? requestsLastHour) =>
      call(requestsLastHour: requestsLastHour);

  @override
  SourceHealth bytesLastHour(int? bytesLastHour) =>
      call(bytesLastHour: bytesLastHour);

  @override
  SourceHealth coverage(Coverage? coverage) => call(coverage: coverage);

  @override
  SourceHealth storage(SourceStorage? storage) => call(storage: storage);

  @override
  SourceHealth lastDataAt(DateTime? lastDataAt) => call(lastDataAt: lastDataAt);

  @override
  SourceHealth budgetHoursRemaining(num? budgetHoursRemaining) =>
      call(budgetHoursRemaining: budgetHoursRemaining);

  @override
  SourceHealth newestFrameAt(DateTime? newestFrameAt) =>
      call(newestFrameAt: newestFrameAt);

  @override
  SourceHealth frameAgeSeconds(int? frameAgeSeconds) =>
      call(frameAgeSeconds: frameAgeSeconds);

  @override
  SourceHealth details(Map<String, Object>? details) => call(details: details);

  @override
  SourceHealth lastDoctorReport(DoctorReportSummary? lastDoctorReport) =>
      call(lastDoctorReport: lastDoctorReport);

  @override
  SourceHealth spentUsdThisMonth(num? spentUsdThisMonth) =>
      call(spentUsdThisMonth: spentUsdThisMonth);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceHealth(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceHealth(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SourceHealth call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? statusReason = const $CopyWithPlaceholder(),
    Object? lastAttemptAt = const $CopyWithPlaceholder(),
    Object? lastSuccessAt = const $CopyWithPlaceholder(),
    Object? lastErrorAt = const $CopyWithPlaceholder(),
    Object? lastError = const $CopyWithPlaceholder(),
    Object? lastHttpStatus = const $CopyWithPlaceholder(),
    Object? consecutiveFailures = const $CopyWithPlaceholder(),
    Object? lastRecordsSeen = const $CopyWithPlaceholder(),
    Object? lastRecordsChanged = const $CopyWithPlaceholder(),
    Object? errorRateLastHour = const $CopyWithPlaceholder(),
    Object? backoffUntil = const $CopyWithPlaceholder(),
    Object? budget = const $CopyWithPlaceholder(),
    Object? budgetUsedToday = const $CopyWithPlaceholder(),
    Object? budgetRemainingToday = const $CopyWithPlaceholder(),
    Object? refusedLastHour = const $CopyWithPlaceholder(),
    Object? requestsLastHour = const $CopyWithPlaceholder(),
    Object? bytesLastHour = const $CopyWithPlaceholder(),
    Object? coverage = const $CopyWithPlaceholder(),
    Object? storage = const $CopyWithPlaceholder(),
    Object? lastDataAt = const $CopyWithPlaceholder(),
    Object? budgetHoursRemaining = const $CopyWithPlaceholder(),
    Object? newestFrameAt = const $CopyWithPlaceholder(),
    Object? frameAgeSeconds = const $CopyWithPlaceholder(),
    Object? details = const $CopyWithPlaceholder(),
    Object? lastDoctorReport = const $CopyWithPlaceholder(),
    Object? spentUsdThisMonth = const $CopyWithPlaceholder(),
  }) {
    return SourceHealth(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as HealthStatus,
      statusReason: statusReason == const $CopyWithPlaceholder()
          ? _value.statusReason
          // ignore: cast_nullable_to_non_nullable
          : statusReason as String?,
      lastAttemptAt: lastAttemptAt == const $CopyWithPlaceholder()
          ? _value.lastAttemptAt
          // ignore: cast_nullable_to_non_nullable
          : lastAttemptAt as DateTime?,
      lastSuccessAt: lastSuccessAt == const $CopyWithPlaceholder()
          ? _value.lastSuccessAt
          // ignore: cast_nullable_to_non_nullable
          : lastSuccessAt as DateTime?,
      lastErrorAt: lastErrorAt == const $CopyWithPlaceholder()
          ? _value.lastErrorAt
          // ignore: cast_nullable_to_non_nullable
          : lastErrorAt as DateTime?,
      lastError: lastError == const $CopyWithPlaceholder()
          ? _value.lastError
          // ignore: cast_nullable_to_non_nullable
          : lastError as String?,
      lastHttpStatus: lastHttpStatus == const $CopyWithPlaceholder()
          ? _value.lastHttpStatus
          // ignore: cast_nullable_to_non_nullable
          : lastHttpStatus as int?,
      consecutiveFailures:
          consecutiveFailures == const $CopyWithPlaceholder() ||
              consecutiveFailures == null
          ? _value.consecutiveFailures
          // ignore: cast_nullable_to_non_nullable
          : consecutiveFailures as int,
      lastRecordsSeen: lastRecordsSeen == const $CopyWithPlaceholder()
          ? _value.lastRecordsSeen
          // ignore: cast_nullable_to_non_nullable
          : lastRecordsSeen as int?,
      lastRecordsChanged: lastRecordsChanged == const $CopyWithPlaceholder()
          ? _value.lastRecordsChanged
          // ignore: cast_nullable_to_non_nullable
          : lastRecordsChanged as int?,
      errorRateLastHour: errorRateLastHour == const $CopyWithPlaceholder()
          ? _value.errorRateLastHour
          // ignore: cast_nullable_to_non_nullable
          : errorRateLastHour as num?,
      backoffUntil: backoffUntil == const $CopyWithPlaceholder()
          ? _value.backoffUntil
          // ignore: cast_nullable_to_non_nullable
          : backoffUntil as DateTime?,
      budget: budget == const $CopyWithPlaceholder()
          ? _value.budget
          // ignore: cast_nullable_to_non_nullable
          : budget as Budget?,
      budgetUsedToday: budgetUsedToday == const $CopyWithPlaceholder()
          ? _value.budgetUsedToday
          // ignore: cast_nullable_to_non_nullable
          : budgetUsedToday as int?,
      budgetRemainingToday: budgetRemainingToday == const $CopyWithPlaceholder()
          ? _value.budgetRemainingToday
          // ignore: cast_nullable_to_non_nullable
          : budgetRemainingToday as int?,
      refusedLastHour: refusedLastHour == const $CopyWithPlaceholder()
          ? _value.refusedLastHour
          // ignore: cast_nullable_to_non_nullable
          : refusedLastHour as int?,
      requestsLastHour: requestsLastHour == const $CopyWithPlaceholder()
          ? _value.requestsLastHour
          // ignore: cast_nullable_to_non_nullable
          : requestsLastHour as int?,
      bytesLastHour: bytesLastHour == const $CopyWithPlaceholder()
          ? _value.bytesLastHour
          // ignore: cast_nullable_to_non_nullable
          : bytesLastHour as int?,
      coverage: coverage == const $CopyWithPlaceholder()
          ? _value.coverage
          // ignore: cast_nullable_to_non_nullable
          : coverage as Coverage?,
      storage: storage == const $CopyWithPlaceholder()
          ? _value.storage
          // ignore: cast_nullable_to_non_nullable
          : storage as SourceStorage?,
      lastDataAt: lastDataAt == const $CopyWithPlaceholder()
          ? _value.lastDataAt
          // ignore: cast_nullable_to_non_nullable
          : lastDataAt as DateTime?,
      budgetHoursRemaining: budgetHoursRemaining == const $CopyWithPlaceholder()
          ? _value.budgetHoursRemaining
          // ignore: cast_nullable_to_non_nullable
          : budgetHoursRemaining as num?,
      newestFrameAt: newestFrameAt == const $CopyWithPlaceholder()
          ? _value.newestFrameAt
          // ignore: cast_nullable_to_non_nullable
          : newestFrameAt as DateTime?,
      frameAgeSeconds: frameAgeSeconds == const $CopyWithPlaceholder()
          ? _value.frameAgeSeconds
          // ignore: cast_nullable_to_non_nullable
          : frameAgeSeconds as int?,
      details: details == const $CopyWithPlaceholder()
          ? _value.details
          // ignore: cast_nullable_to_non_nullable
          : details as Map<String, Object>?,
      lastDoctorReport: lastDoctorReport == const $CopyWithPlaceholder()
          ? _value.lastDoctorReport
          // ignore: cast_nullable_to_non_nullable
          : lastDoctorReport as DoctorReportSummary?,
      spentUsdThisMonth: spentUsdThisMonth == const $CopyWithPlaceholder()
          ? _value.spentUsdThisMonth
          // ignore: cast_nullable_to_non_nullable
          : spentUsdThisMonth as num?,
    );
  }
}

extension $SourceHealthCopyWith on SourceHealth {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSourceHealth.copyWith(...)` or `instanceOfSourceHealth.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SourceHealthCWProxy get copyWith => _$SourceHealthCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SourceHealth _$SourceHealthFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SourceHealth', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['sourceId', 'status', 'consecutiveFailures'],
  );
  final val = SourceHealth(
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$HealthStatusEnumMap, v),
    ),
    statusReason: $checkedConvert('statusReason', (v) => v as String?),
    lastAttemptAt: $checkedConvert(
      'lastAttemptAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastSuccessAt: $checkedConvert(
      'lastSuccessAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastErrorAt: $checkedConvert(
      'lastErrorAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastError: $checkedConvert('lastError', (v) => v as String?),
    lastHttpStatus: $checkedConvert(
      'lastHttpStatus',
      (v) => (v as num?)?.toInt(),
    ),
    consecutiveFailures: $checkedConvert(
      'consecutiveFailures',
      (v) => (v as num).toInt(),
    ),
    lastRecordsSeen: $checkedConvert(
      'lastRecordsSeen',
      (v) => (v as num?)?.toInt(),
    ),
    lastRecordsChanged: $checkedConvert(
      'lastRecordsChanged',
      (v) => (v as num?)?.toInt(),
    ),
    errorRateLastHour: $checkedConvert('errorRateLastHour', (v) => v as num?),
    backoffUntil: $checkedConvert(
      'backoffUntil',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    budget: $checkedConvert(
      'budget',
      (v) => v == null ? null : Budget.fromJson(v as Map<String, dynamic>),
    ),
    budgetUsedToday: $checkedConvert(
      'budgetUsedToday',
      (v) => (v as num?)?.toInt(),
    ),
    budgetRemainingToday: $checkedConvert(
      'budgetRemainingToday',
      (v) => (v as num?)?.toInt(),
    ),
    refusedLastHour: $checkedConvert(
      'refusedLastHour',
      (v) => (v as num?)?.toInt(),
    ),
    requestsLastHour: $checkedConvert(
      'requestsLastHour',
      (v) => (v as num?)?.toInt(),
    ),
    bytesLastHour: $checkedConvert(
      'bytesLastHour',
      (v) => (v as num?)?.toInt(),
    ),
    coverage: $checkedConvert(
      'coverage',
      (v) => v == null ? null : Coverage.fromJson(v as Map<String, dynamic>),
    ),
    storage: $checkedConvert(
      'storage',
      (v) =>
          v == null ? null : SourceStorage.fromJson(v as Map<String, dynamic>),
    ),
    lastDataAt: $checkedConvert(
      'lastDataAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    budgetHoursRemaining: $checkedConvert(
      'budgetHoursRemaining',
      (v) => v as num?,
    ),
    newestFrameAt: $checkedConvert(
      'newestFrameAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    frameAgeSeconds: $checkedConvert(
      'frameAgeSeconds',
      (v) => (v as num?)?.toInt(),
    ),
    details: $checkedConvert(
      'details',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    lastDoctorReport: $checkedConvert(
      'lastDoctorReport',
      (v) => v == null
          ? null
          : DoctorReportSummary.fromJson(v as Map<String, dynamic>),
    ),
    spentUsdThisMonth: $checkedConvert('spentUsdThisMonth', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$SourceHealthToJson(SourceHealth instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'status': _$HealthStatusEnumMap[instance.status]!,
      'statusReason': ?instance.statusReason,
      'lastAttemptAt': ?instance.lastAttemptAt?.toIso8601String(),
      'lastSuccessAt': ?instance.lastSuccessAt?.toIso8601String(),
      'lastErrorAt': ?instance.lastErrorAt?.toIso8601String(),
      'lastError': ?instance.lastError,
      'lastHttpStatus': ?instance.lastHttpStatus,
      'consecutiveFailures': instance.consecutiveFailures,
      'lastRecordsSeen': ?instance.lastRecordsSeen,
      'lastRecordsChanged': ?instance.lastRecordsChanged,
      'errorRateLastHour': ?instance.errorRateLastHour,
      'backoffUntil': ?instance.backoffUntil?.toIso8601String(),
      'budget': ?instance.budget?.toJson(),
      'budgetUsedToday': ?instance.budgetUsedToday,
      'budgetRemainingToday': ?instance.budgetRemainingToday,
      'refusedLastHour': ?instance.refusedLastHour,
      'requestsLastHour': ?instance.requestsLastHour,
      'bytesLastHour': ?instance.bytesLastHour,
      'coverage': ?instance.coverage?.toJson(),
      'storage': ?instance.storage?.toJson(),
      'lastDataAt': ?instance.lastDataAt?.toIso8601String(),
      'budgetHoursRemaining': ?instance.budgetHoursRemaining,
      'newestFrameAt': ?instance.newestFrameAt?.toIso8601String(),
      'frameAgeSeconds': ?instance.frameAgeSeconds,
      'details': ?instance.details,
      'lastDoctorReport': ?instance.lastDoctorReport?.toJson(),
      'spentUsdThisMonth': ?instance.spentUsdThisMonth,
    };

const _$HealthStatusEnumMap = {
  HealthStatus.healthy: 'healthy',
  HealthStatus.degraded: 'degraded',
  HealthStatus.failing: 'failing',
  HealthStatus.idle: 'idle',
  HealthStatus.disabled: 'disabled',
};
