// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'retention_run_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RetentionRunResultCWProxy {
  RetentionRunResult startedAt(DateTime? startedAt);

  RetentionRunResult durationMs(int? durationMs);

  RetentionRunResult versionsDeleted(int versionsDeleted);

  RetentionRunResult headsDeleted(int headsDeleted);

  RetentionRunResult alertsDeleted(int? alertsDeleted);

  RetentionRunResult usageRowsDeleted(int? usageRowsDeleted);

  RetentionRunResult partitionsDropped(int? partitionsDropped);

  RetentionRunResult headsPurged(int? headsPurged);

  RetentionRunResult fetchLogDeleted(int? fetchLogDeleted);

  RetentionRunResult seqClockDeleted(int? seqClockDeleted);

  RetentionRunResult notificationsDeleted(int? notificationsDeleted);

  RetentionRunResult skippedSources(List<String>? skippedSources);

  RetentionRunResult consumerClamp(
    RetentionRunResultConsumerClamp? consumerClamp,
  );

  RetentionRunResult perSource(
    Map<String, RetentionRunResultPerSourceValue>? perSource,
  );

  RetentionRunResult capWindowDays(int? capWindowDays);

  RetentionRunResult dbCap(RetentionRunResultDbCap? dbCap);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionRunResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionRunResult(...).copyWith(id: 12, name: "My name")
  /// ```
  RetentionRunResult call({
    DateTime? startedAt,
    int? durationMs,
    int versionsDeleted,
    int headsDeleted,
    int? alertsDeleted,
    int? usageRowsDeleted,
    int? partitionsDropped,
    int? headsPurged,
    int? fetchLogDeleted,
    int? seqClockDeleted,
    int? notificationsDeleted,
    List<String>? skippedSources,
    RetentionRunResultConsumerClamp? consumerClamp,
    Map<String, RetentionRunResultPerSourceValue>? perSource,
    int? capWindowDays,
    RetentionRunResultDbCap? dbCap,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRetentionRunResult.copyWith(...)` or call `instanceOfRetentionRunResult.copyWith.fieldName(value)` for a single field.
class _$RetentionRunResultCWProxyImpl implements _$RetentionRunResultCWProxy {
  const _$RetentionRunResultCWProxyImpl(this._value);

  final RetentionRunResult _value;

  @override
  RetentionRunResult startedAt(DateTime? startedAt) =>
      call(startedAt: startedAt);

  @override
  RetentionRunResult durationMs(int? durationMs) =>
      call(durationMs: durationMs);

  @override
  RetentionRunResult versionsDeleted(int versionsDeleted) =>
      call(versionsDeleted: versionsDeleted);

  @override
  RetentionRunResult headsDeleted(int headsDeleted) =>
      call(headsDeleted: headsDeleted);

  @override
  RetentionRunResult alertsDeleted(int? alertsDeleted) =>
      call(alertsDeleted: alertsDeleted);

  @override
  RetentionRunResult usageRowsDeleted(int? usageRowsDeleted) =>
      call(usageRowsDeleted: usageRowsDeleted);

  @override
  RetentionRunResult partitionsDropped(int? partitionsDropped) =>
      call(partitionsDropped: partitionsDropped);

  @override
  RetentionRunResult headsPurged(int? headsPurged) =>
      call(headsPurged: headsPurged);

  @override
  RetentionRunResult fetchLogDeleted(int? fetchLogDeleted) =>
      call(fetchLogDeleted: fetchLogDeleted);

  @override
  RetentionRunResult seqClockDeleted(int? seqClockDeleted) =>
      call(seqClockDeleted: seqClockDeleted);

  @override
  RetentionRunResult notificationsDeleted(int? notificationsDeleted) =>
      call(notificationsDeleted: notificationsDeleted);

  @override
  RetentionRunResult skippedSources(List<String>? skippedSources) =>
      call(skippedSources: skippedSources);

  @override
  RetentionRunResult consumerClamp(
    RetentionRunResultConsumerClamp? consumerClamp,
  ) => call(consumerClamp: consumerClamp);

  @override
  RetentionRunResult perSource(
    Map<String, RetentionRunResultPerSourceValue>? perSource,
  ) => call(perSource: perSource);

  @override
  RetentionRunResult capWindowDays(int? capWindowDays) =>
      call(capWindowDays: capWindowDays);

  @override
  RetentionRunResult dbCap(RetentionRunResultDbCap? dbCap) =>
      call(dbCap: dbCap);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionRunResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionRunResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RetentionRunResult call({
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? durationMs = const $CopyWithPlaceholder(),
    Object? versionsDeleted = const $CopyWithPlaceholder(),
    Object? headsDeleted = const $CopyWithPlaceholder(),
    Object? alertsDeleted = const $CopyWithPlaceholder(),
    Object? usageRowsDeleted = const $CopyWithPlaceholder(),
    Object? partitionsDropped = const $CopyWithPlaceholder(),
    Object? headsPurged = const $CopyWithPlaceholder(),
    Object? fetchLogDeleted = const $CopyWithPlaceholder(),
    Object? seqClockDeleted = const $CopyWithPlaceholder(),
    Object? notificationsDeleted = const $CopyWithPlaceholder(),
    Object? skippedSources = const $CopyWithPlaceholder(),
    Object? consumerClamp = const $CopyWithPlaceholder(),
    Object? perSource = const $CopyWithPlaceholder(),
    Object? capWindowDays = const $CopyWithPlaceholder(),
    Object? dbCap = const $CopyWithPlaceholder(),
  }) {
    return RetentionRunResult(
      startedAt: startedAt == const $CopyWithPlaceholder()
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime?,
      durationMs: durationMs == const $CopyWithPlaceholder()
          ? _value.durationMs
          // ignore: cast_nullable_to_non_nullable
          : durationMs as int?,
      versionsDeleted:
          versionsDeleted == const $CopyWithPlaceholder() ||
              versionsDeleted == null
          ? _value.versionsDeleted
          // ignore: cast_nullable_to_non_nullable
          : versionsDeleted as int,
      headsDeleted:
          headsDeleted == const $CopyWithPlaceholder() || headsDeleted == null
          ? _value.headsDeleted
          // ignore: cast_nullable_to_non_nullable
          : headsDeleted as int,
      alertsDeleted: alertsDeleted == const $CopyWithPlaceholder()
          ? _value.alertsDeleted
          // ignore: cast_nullable_to_non_nullable
          : alertsDeleted as int?,
      usageRowsDeleted: usageRowsDeleted == const $CopyWithPlaceholder()
          ? _value.usageRowsDeleted
          // ignore: cast_nullable_to_non_nullable
          : usageRowsDeleted as int?,
      partitionsDropped: partitionsDropped == const $CopyWithPlaceholder()
          ? _value.partitionsDropped
          // ignore: cast_nullable_to_non_nullable
          : partitionsDropped as int?,
      headsPurged: headsPurged == const $CopyWithPlaceholder()
          ? _value.headsPurged
          // ignore: cast_nullable_to_non_nullable
          : headsPurged as int?,
      fetchLogDeleted: fetchLogDeleted == const $CopyWithPlaceholder()
          ? _value.fetchLogDeleted
          // ignore: cast_nullable_to_non_nullable
          : fetchLogDeleted as int?,
      seqClockDeleted: seqClockDeleted == const $CopyWithPlaceholder()
          ? _value.seqClockDeleted
          // ignore: cast_nullable_to_non_nullable
          : seqClockDeleted as int?,
      notificationsDeleted: notificationsDeleted == const $CopyWithPlaceholder()
          ? _value.notificationsDeleted
          // ignore: cast_nullable_to_non_nullable
          : notificationsDeleted as int?,
      skippedSources: skippedSources == const $CopyWithPlaceholder()
          ? _value.skippedSources
          // ignore: cast_nullable_to_non_nullable
          : skippedSources as List<String>?,
      consumerClamp: consumerClamp == const $CopyWithPlaceholder()
          ? _value.consumerClamp
          // ignore: cast_nullable_to_non_nullable
          : consumerClamp as RetentionRunResultConsumerClamp?,
      perSource: perSource == const $CopyWithPlaceholder()
          ? _value.perSource
          // ignore: cast_nullable_to_non_nullable
          : perSource as Map<String, RetentionRunResultPerSourceValue>?,
      capWindowDays: capWindowDays == const $CopyWithPlaceholder()
          ? _value.capWindowDays
          // ignore: cast_nullable_to_non_nullable
          : capWindowDays as int?,
      dbCap: dbCap == const $CopyWithPlaceholder()
          ? _value.dbCap
          // ignore: cast_nullable_to_non_nullable
          : dbCap as RetentionRunResultDbCap?,
    );
  }
}

extension $RetentionRunResultCopyWith on RetentionRunResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRetentionRunResult.copyWith(...)` or `instanceOfRetentionRunResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RetentionRunResultCWProxy get copyWith =>
      _$RetentionRunResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RetentionRunResult _$RetentionRunResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RetentionRunResult', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['versionsDeleted', 'headsDeleted']);
  final val = RetentionRunResult(
    startedAt: $checkedConvert(
      'startedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    durationMs: $checkedConvert('durationMs', (v) => (v as num?)?.toInt()),
    versionsDeleted: $checkedConvert(
      'versionsDeleted',
      (v) => (v as num).toInt(),
    ),
    headsDeleted: $checkedConvert('headsDeleted', (v) => (v as num).toInt()),
    alertsDeleted: $checkedConvert(
      'alertsDeleted',
      (v) => (v as num?)?.toInt(),
    ),
    usageRowsDeleted: $checkedConvert(
      'usageRowsDeleted',
      (v) => (v as num?)?.toInt(),
    ),
    partitionsDropped: $checkedConvert(
      'partitionsDropped',
      (v) => (v as num?)?.toInt(),
    ),
    headsPurged: $checkedConvert('headsPurged', (v) => (v as num?)?.toInt()),
    fetchLogDeleted: $checkedConvert(
      'fetchLogDeleted',
      (v) => (v as num?)?.toInt(),
    ),
    seqClockDeleted: $checkedConvert(
      'seqClockDeleted',
      (v) => (v as num?)?.toInt(),
    ),
    notificationsDeleted: $checkedConvert(
      'notificationsDeleted',
      (v) => (v as num?)?.toInt(),
    ),
    skippedSources: $checkedConvert(
      'skippedSources',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    consumerClamp: $checkedConvert(
      'consumerClamp',
      (v) => v == null
          ? null
          : RetentionRunResultConsumerClamp.fromJson(v as Map<String, dynamic>),
    ),
    perSource: $checkedConvert(
      'perSource',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(
          k,
          RetentionRunResultPerSourceValue.fromJson(e as Map<String, dynamic>),
        ),
      ),
    ),
    capWindowDays: $checkedConvert(
      'capWindowDays',
      (v) => (v as num?)?.toInt(),
    ),
    dbCap: $checkedConvert(
      'dbCap',
      (v) => v == null
          ? null
          : RetentionRunResultDbCap.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$RetentionRunResultToJson(RetentionRunResult instance) =>
    <String, dynamic>{
      'startedAt': ?instance.startedAt?.toIso8601String(),
      'durationMs': ?instance.durationMs,
      'versionsDeleted': instance.versionsDeleted,
      'headsDeleted': instance.headsDeleted,
      'alertsDeleted': ?instance.alertsDeleted,
      'usageRowsDeleted': ?instance.usageRowsDeleted,
      'partitionsDropped': ?instance.partitionsDropped,
      'headsPurged': ?instance.headsPurged,
      'fetchLogDeleted': ?instance.fetchLogDeleted,
      'seqClockDeleted': ?instance.seqClockDeleted,
      'notificationsDeleted': ?instance.notificationsDeleted,
      'skippedSources': ?instance.skippedSources,
      'consumerClamp': ?instance.consumerClamp?.toJson(),
      'perSource': ?instance.perSource?.map((k, e) => MapEntry(k, e.toJson())),
      'capWindowDays': ?instance.capWindowDays,
      'dbCap': ?instance.dbCap?.toJson(),
    };
