// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_ingest_writer.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoIngestWriterCWProxy {
  SystemInfoIngestWriter state(SystemInfoIngestWriterStateEnum? state);

  SystemInfoIngestWriter warnings(List<String>? warnings);

  SystemInfoIngestWriter lastSeq(int? lastSeq);

  SystemInfoIngestWriter lastIngestedAt(DateTime? lastIngestedAt);

  SystemInfoIngestWriter queueDepth(int? queueDepth);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoIngestWriter(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoIngestWriter(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoIngestWriter call({
    SystemInfoIngestWriterStateEnum? state,
    List<String>? warnings,
    int? lastSeq,
    DateTime? lastIngestedAt,
    int? queueDepth,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoIngestWriter.copyWith(...)` or call `instanceOfSystemInfoIngestWriter.copyWith.fieldName(value)` for a single field.
class _$SystemInfoIngestWriterCWProxyImpl
    implements _$SystemInfoIngestWriterCWProxy {
  const _$SystemInfoIngestWriterCWProxyImpl(this._value);

  final SystemInfoIngestWriter _value;

  @override
  SystemInfoIngestWriter state(SystemInfoIngestWriterStateEnum? state) =>
      call(state: state);

  @override
  SystemInfoIngestWriter warnings(List<String>? warnings) =>
      call(warnings: warnings);

  @override
  SystemInfoIngestWriter lastSeq(int? lastSeq) => call(lastSeq: lastSeq);

  @override
  SystemInfoIngestWriter lastIngestedAt(DateTime? lastIngestedAt) =>
      call(lastIngestedAt: lastIngestedAt);

  @override
  SystemInfoIngestWriter queueDepth(int? queueDepth) =>
      call(queueDepth: queueDepth);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoIngestWriter(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoIngestWriter(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoIngestWriter call({
    Object? state = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
    Object? lastSeq = const $CopyWithPlaceholder(),
    Object? lastIngestedAt = const $CopyWithPlaceholder(),
    Object? queueDepth = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoIngestWriter(
      state: state == const $CopyWithPlaceholder()
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as SystemInfoIngestWriterStateEnum?,
      warnings: warnings == const $CopyWithPlaceholder()
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as List<String>?,
      lastSeq: lastSeq == const $CopyWithPlaceholder()
          ? _value.lastSeq
          // ignore: cast_nullable_to_non_nullable
          : lastSeq as int?,
      lastIngestedAt: lastIngestedAt == const $CopyWithPlaceholder()
          ? _value.lastIngestedAt
          // ignore: cast_nullable_to_non_nullable
          : lastIngestedAt as DateTime?,
      queueDepth: queueDepth == const $CopyWithPlaceholder()
          ? _value.queueDepth
          // ignore: cast_nullable_to_non_nullable
          : queueDepth as int?,
    );
  }
}

extension $SystemInfoIngestWriterCopyWith on SystemInfoIngestWriter {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoIngestWriter.copyWith(...)` or `instanceOfSystemInfoIngestWriter.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoIngestWriterCWProxy get copyWith =>
      _$SystemInfoIngestWriterCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoIngestWriter _$SystemInfoIngestWriterFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SystemInfoIngestWriter', json, ($checkedConvert) {
  final val = SystemInfoIngestWriter(
    state: $checkedConvert(
      'state',
      (v) => $enumDecodeNullable(_$SystemInfoIngestWriterStateEnumEnumMap, v),
    ),
    warnings: $checkedConvert(
      'warnings',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    lastSeq: $checkedConvert('lastSeq', (v) => (v as num?)?.toInt()),
    lastIngestedAt: $checkedConvert(
      'lastIngestedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    queueDepth: $checkedConvert('queueDepth', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$SystemInfoIngestWriterToJson(
  SystemInfoIngestWriter instance,
) => <String, dynamic>{
  'state': ?_$SystemInfoIngestWriterStateEnumEnumMap[instance.state],
  'warnings': ?instance.warnings,
  'lastSeq': ?instance.lastSeq,
  'lastIngestedAt': ?instance.lastIngestedAt?.toIso8601String(),
  'queueDepth': ?instance.queueDepth,
};

const _$SystemInfoIngestWriterStateEnumEnumMap = {
  SystemInfoIngestWriterStateEnum.RUNNING: 'RUNNING',
  SystemInfoIngestWriterStateEnum.CLOCK_BEHIND: 'CLOCK_BEHIND',
  SystemInfoIngestWriterStateEnum.LOCK_LOST: 'LOCK_LOST',
  SystemInfoIngestWriterStateEnum.STOPPED: 'STOPPED',
};
