// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edition_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EditionSummaryCWProxy {
  EditionSummary briefingId(String briefingId);

  EditionSummary name(String name);

  EditionSummary enabled(bool enabled);

  EditionSummary outputLanguage(String? outputLanguage);

  EditionSummary schedule(String schedule);

  EditionSummary timezone(String timezone);

  EditionSummary nextRunAt(DateTime? nextRunAt);

  EditionSummary lastRunAt(DateTime? lastRunAt);

  EditionSummary lastRunStatus(String? lastRunStatus);

  EditionSummary latestRunId(String? latestRunId);

  EditionSummary siteUrl(String? siteUrl);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EditionSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EditionSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  EditionSummary call({
    String briefingId,
    String name,
    bool enabled,
    String? outputLanguage,
    String schedule,
    String timezone,
    DateTime? nextRunAt,
    DateTime? lastRunAt,
    String? lastRunStatus,
    String? latestRunId,
    String? siteUrl,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEditionSummary.copyWith(...)` or call `instanceOfEditionSummary.copyWith.fieldName(value)` for a single field.
class _$EditionSummaryCWProxyImpl implements _$EditionSummaryCWProxy {
  const _$EditionSummaryCWProxyImpl(this._value);

  final EditionSummary _value;

  @override
  EditionSummary briefingId(String briefingId) => call(briefingId: briefingId);

  @override
  EditionSummary name(String name) => call(name: name);

  @override
  EditionSummary enabled(bool enabled) => call(enabled: enabled);

  @override
  EditionSummary outputLanguage(String? outputLanguage) =>
      call(outputLanguage: outputLanguage);

  @override
  EditionSummary schedule(String schedule) => call(schedule: schedule);

  @override
  EditionSummary timezone(String timezone) => call(timezone: timezone);

  @override
  EditionSummary nextRunAt(DateTime? nextRunAt) => call(nextRunAt: nextRunAt);

  @override
  EditionSummary lastRunAt(DateTime? lastRunAt) => call(lastRunAt: lastRunAt);

  @override
  EditionSummary lastRunStatus(String? lastRunStatus) =>
      call(lastRunStatus: lastRunStatus);

  @override
  EditionSummary latestRunId(String? latestRunId) =>
      call(latestRunId: latestRunId);

  @override
  EditionSummary siteUrl(String? siteUrl) => call(siteUrl: siteUrl);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EditionSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EditionSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EditionSummary call({
    Object? briefingId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? outputLanguage = const $CopyWithPlaceholder(),
    Object? schedule = const $CopyWithPlaceholder(),
    Object? timezone = const $CopyWithPlaceholder(),
    Object? nextRunAt = const $CopyWithPlaceholder(),
    Object? lastRunAt = const $CopyWithPlaceholder(),
    Object? lastRunStatus = const $CopyWithPlaceholder(),
    Object? latestRunId = const $CopyWithPlaceholder(),
    Object? siteUrl = const $CopyWithPlaceholder(),
  }) {
    return EditionSummary(
      briefingId:
          briefingId == const $CopyWithPlaceholder() || briefingId == null
          ? _value.briefingId
          // ignore: cast_nullable_to_non_nullable
          : briefingId as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      outputLanguage: outputLanguage == const $CopyWithPlaceholder()
          ? _value.outputLanguage
          // ignore: cast_nullable_to_non_nullable
          : outputLanguage as String?,
      schedule: schedule == const $CopyWithPlaceholder() || schedule == null
          ? _value.schedule
          // ignore: cast_nullable_to_non_nullable
          : schedule as String,
      timezone: timezone == const $CopyWithPlaceholder() || timezone == null
          ? _value.timezone
          // ignore: cast_nullable_to_non_nullable
          : timezone as String,
      nextRunAt: nextRunAt == const $CopyWithPlaceholder()
          ? _value.nextRunAt
          // ignore: cast_nullable_to_non_nullable
          : nextRunAt as DateTime?,
      lastRunAt: lastRunAt == const $CopyWithPlaceholder()
          ? _value.lastRunAt
          // ignore: cast_nullable_to_non_nullable
          : lastRunAt as DateTime?,
      lastRunStatus: lastRunStatus == const $CopyWithPlaceholder()
          ? _value.lastRunStatus
          // ignore: cast_nullable_to_non_nullable
          : lastRunStatus as String?,
      latestRunId: latestRunId == const $CopyWithPlaceholder()
          ? _value.latestRunId
          // ignore: cast_nullable_to_non_nullable
          : latestRunId as String?,
      siteUrl: siteUrl == const $CopyWithPlaceholder()
          ? _value.siteUrl
          // ignore: cast_nullable_to_non_nullable
          : siteUrl as String?,
    );
  }
}

extension $EditionSummaryCopyWith on EditionSummary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEditionSummary.copyWith(...)` or `instanceOfEditionSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EditionSummaryCWProxy get copyWith => _$EditionSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EditionSummary _$EditionSummaryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EditionSummary', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'briefingId',
          'name',
          'enabled',
          'schedule',
          'timezone',
        ],
      );
      final val = EditionSummary(
        briefingId: $checkedConvert('briefingId', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String),
        enabled: $checkedConvert('enabled', (v) => v as bool),
        outputLanguage: $checkedConvert('outputLanguage', (v) => v as String?),
        schedule: $checkedConvert('schedule', (v) => v as String),
        timezone: $checkedConvert('timezone', (v) => v as String),
        nextRunAt: $checkedConvert(
          'nextRunAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        lastRunAt: $checkedConvert(
          'lastRunAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        lastRunStatus: $checkedConvert('lastRunStatus', (v) => v as String?),
        latestRunId: $checkedConvert('latestRunId', (v) => v as String?),
        siteUrl: $checkedConvert('siteUrl', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$EditionSummaryToJson(EditionSummary instance) =>
    <String, dynamic>{
      'briefingId': instance.briefingId,
      'name': instance.name,
      'enabled': instance.enabled,
      'outputLanguage': ?instance.outputLanguage,
      'schedule': instance.schedule,
      'timezone': instance.timezone,
      'nextRunAt': ?instance.nextRunAt?.toIso8601String(),
      'lastRunAt': ?instance.lastRunAt?.toIso8601String(),
      'lastRunStatus': ?instance.lastRunStatus,
      'latestRunId': ?instance.latestRunId,
      'siteUrl': ?instance.siteUrl,
    };
