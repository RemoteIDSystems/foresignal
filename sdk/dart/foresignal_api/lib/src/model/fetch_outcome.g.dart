// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'fetch_outcome.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FetchOutcomeCWProxy {
  FetchOutcome sourceId(String sourceId);

  FetchOutcome startedAt(DateTime? startedAt);

  FetchOutcome durationMs(int? durationMs);

  FetchOutcome success(bool success);

  FetchOutcome recordsSeen(int recordsSeen);

  FetchOutcome recordsChanged(int recordsChanged);

  FetchOutcome fromSeq(int? fromSeq);

  FetchOutcome toSeq(int? toSeq);

  FetchOutcome error(String? error);

  FetchOutcome fetchId(int? fetchId);

  FetchOutcome persisted(int? persisted);

  FetchOutcome live(int? live);

  FetchOutcome touched(int? touched);

  FetchOutcome touchesThrottled(int? touchesThrottled);

  FetchOutcome late_(int? late_);

  FetchOutcome skipped(int? skipped);

  FetchOutcome rejected(int? rejected);

  FetchOutcome byReason(PersistReasonCounts? byReason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FetchOutcome(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FetchOutcome(...).copyWith(id: 12, name: "My name")
  /// ```
  FetchOutcome call({
    String sourceId,
    DateTime? startedAt,
    int? durationMs,
    bool success,
    int recordsSeen,
    int recordsChanged,
    int? fromSeq,
    int? toSeq,
    String? error,
    int? fetchId,
    int? persisted,
    int? live,
    int? touched,
    int? touchesThrottled,
    int? late_,
    int? skipped,
    int? rejected,
    PersistReasonCounts? byReason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFetchOutcome.copyWith(...)` or call `instanceOfFetchOutcome.copyWith.fieldName(value)` for a single field.
class _$FetchOutcomeCWProxyImpl implements _$FetchOutcomeCWProxy {
  const _$FetchOutcomeCWProxyImpl(this._value);

  final FetchOutcome _value;

  @override
  FetchOutcome sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  FetchOutcome startedAt(DateTime? startedAt) => call(startedAt: startedAt);

  @override
  FetchOutcome durationMs(int? durationMs) => call(durationMs: durationMs);

  @override
  FetchOutcome success(bool success) => call(success: success);

  @override
  FetchOutcome recordsSeen(int recordsSeen) => call(recordsSeen: recordsSeen);

  @override
  FetchOutcome recordsChanged(int recordsChanged) =>
      call(recordsChanged: recordsChanged);

  @override
  FetchOutcome fromSeq(int? fromSeq) => call(fromSeq: fromSeq);

  @override
  FetchOutcome toSeq(int? toSeq) => call(toSeq: toSeq);

  @override
  FetchOutcome error(String? error) => call(error: error);

  @override
  FetchOutcome fetchId(int? fetchId) => call(fetchId: fetchId);

  @override
  FetchOutcome persisted(int? persisted) => call(persisted: persisted);

  @override
  FetchOutcome live(int? live) => call(live: live);

  @override
  FetchOutcome touched(int? touched) => call(touched: touched);

  @override
  FetchOutcome touchesThrottled(int? touchesThrottled) =>
      call(touchesThrottled: touchesThrottled);

  @override
  FetchOutcome late_(int? late_) => call(late_: late_);

  @override
  FetchOutcome skipped(int? skipped) => call(skipped: skipped);

  @override
  FetchOutcome rejected(int? rejected) => call(rejected: rejected);

  @override
  FetchOutcome byReason(PersistReasonCounts? byReason) =>
      call(byReason: byReason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FetchOutcome(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FetchOutcome(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FetchOutcome call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? durationMs = const $CopyWithPlaceholder(),
    Object? success = const $CopyWithPlaceholder(),
    Object? recordsSeen = const $CopyWithPlaceholder(),
    Object? recordsChanged = const $CopyWithPlaceholder(),
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
    Object? fetchId = const $CopyWithPlaceholder(),
    Object? persisted = const $CopyWithPlaceholder(),
    Object? live = const $CopyWithPlaceholder(),
    Object? touched = const $CopyWithPlaceholder(),
    Object? touchesThrottled = const $CopyWithPlaceholder(),
    Object? late_ = const $CopyWithPlaceholder(),
    Object? skipped = const $CopyWithPlaceholder(),
    Object? rejected = const $CopyWithPlaceholder(),
    Object? byReason = const $CopyWithPlaceholder(),
  }) {
    return FetchOutcome(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      startedAt: startedAt == const $CopyWithPlaceholder()
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime?,
      durationMs: durationMs == const $CopyWithPlaceholder()
          ? _value.durationMs
          // ignore: cast_nullable_to_non_nullable
          : durationMs as int?,
      success: success == const $CopyWithPlaceholder() || success == null
          ? _value.success
          // ignore: cast_nullable_to_non_nullable
          : success as bool,
      recordsSeen:
          recordsSeen == const $CopyWithPlaceholder() || recordsSeen == null
          ? _value.recordsSeen
          // ignore: cast_nullable_to_non_nullable
          : recordsSeen as int,
      recordsChanged:
          recordsChanged == const $CopyWithPlaceholder() ||
              recordsChanged == null
          ? _value.recordsChanged
          // ignore: cast_nullable_to_non_nullable
          : recordsChanged as int,
      fromSeq: fromSeq == const $CopyWithPlaceholder()
          ? _value.fromSeq
          // ignore: cast_nullable_to_non_nullable
          : fromSeq as int?,
      toSeq: toSeq == const $CopyWithPlaceholder()
          ? _value.toSeq
          // ignore: cast_nullable_to_non_nullable
          : toSeq as int?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
      fetchId: fetchId == const $CopyWithPlaceholder()
          ? _value.fetchId
          // ignore: cast_nullable_to_non_nullable
          : fetchId as int?,
      persisted: persisted == const $CopyWithPlaceholder()
          ? _value.persisted
          // ignore: cast_nullable_to_non_nullable
          : persisted as int?,
      live: live == const $CopyWithPlaceholder()
          ? _value.live
          // ignore: cast_nullable_to_non_nullable
          : live as int?,
      touched: touched == const $CopyWithPlaceholder()
          ? _value.touched
          // ignore: cast_nullable_to_non_nullable
          : touched as int?,
      touchesThrottled: touchesThrottled == const $CopyWithPlaceholder()
          ? _value.touchesThrottled
          // ignore: cast_nullable_to_non_nullable
          : touchesThrottled as int?,
      late_: late_ == const $CopyWithPlaceholder()
          ? _value.late_
          // ignore: cast_nullable_to_non_nullable
          : late_ as int?,
      skipped: skipped == const $CopyWithPlaceholder()
          ? _value.skipped
          // ignore: cast_nullable_to_non_nullable
          : skipped as int?,
      rejected: rejected == const $CopyWithPlaceholder()
          ? _value.rejected
          // ignore: cast_nullable_to_non_nullable
          : rejected as int?,
      byReason: byReason == const $CopyWithPlaceholder()
          ? _value.byReason
          // ignore: cast_nullable_to_non_nullable
          : byReason as PersistReasonCounts?,
    );
  }
}

extension $FetchOutcomeCopyWith on FetchOutcome {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFetchOutcome.copyWith(...)` or `instanceOfFetchOutcome.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FetchOutcomeCWProxy get copyWith => _$FetchOutcomeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FetchOutcome _$FetchOutcomeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FetchOutcome', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'sourceId',
          'success',
          'recordsSeen',
          'recordsChanged',
        ],
      );
      final val = FetchOutcome(
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        startedAt: $checkedConvert(
          'startedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        durationMs: $checkedConvert('durationMs', (v) => (v as num?)?.toInt()),
        success: $checkedConvert('success', (v) => v as bool),
        recordsSeen: $checkedConvert('recordsSeen', (v) => (v as num).toInt()),
        recordsChanged: $checkedConvert(
          'recordsChanged',
          (v) => (v as num).toInt(),
        ),
        fromSeq: $checkedConvert('fromSeq', (v) => (v as num?)?.toInt()),
        toSeq: $checkedConvert('toSeq', (v) => (v as num?)?.toInt()),
        error: $checkedConvert('error', (v) => v as String?),
        fetchId: $checkedConvert('fetchId', (v) => (v as num?)?.toInt()),
        persisted: $checkedConvert('persisted', (v) => (v as num?)?.toInt()),
        live: $checkedConvert('live', (v) => (v as num?)?.toInt()),
        touched: $checkedConvert('touched', (v) => (v as num?)?.toInt()),
        touchesThrottled: $checkedConvert(
          'touchesThrottled',
          (v) => (v as num?)?.toInt(),
        ),
        late_: $checkedConvert('late', (v) => (v as num?)?.toInt()),
        skipped: $checkedConvert('skipped', (v) => (v as num?)?.toInt()),
        rejected: $checkedConvert('rejected', (v) => (v as num?)?.toInt()),
        byReason: $checkedConvert(
          'byReason',
          (v) => v == null
              ? null
              : PersistReasonCounts.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    }, fieldKeyMap: const {'late_': 'late'});

Map<String, dynamic> _$FetchOutcomeToJson(FetchOutcome instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'startedAt': ?instance.startedAt?.toIso8601String(),
      'durationMs': ?instance.durationMs,
      'success': instance.success,
      'recordsSeen': instance.recordsSeen,
      'recordsChanged': instance.recordsChanged,
      'fromSeq': ?instance.fromSeq,
      'toSeq': ?instance.toSeq,
      'error': ?instance.error,
      'fetchId': ?instance.fetchId,
      'persisted': ?instance.persisted,
      'live': ?instance.live,
      'touched': ?instance.touched,
      'touchesThrottled': ?instance.touchesThrottled,
      'late': ?instance.late_,
      'skipped': ?instance.skipped,
      'rejected': ?instance.rejected,
      'byReason': ?instance.byReason?.toJson(),
    };
