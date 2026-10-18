// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_outcome.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestOutcomeCWProxy {
  IngestOutcome seen(int seen);

  IngestOutcome changed(@Deprecated('changed has been deprecated') int changed);

  IngestOutcome rejected(int rejected);

  IngestOutcome fromSeq(int? fromSeq);

  IngestOutcome toSeq(int? toSeq);

  IngestOutcome rejections(List<IngestOutcomeRejectionsInner>? rejections);

  IngestOutcome persisted(int persisted);

  IngestOutcome live(int? live);

  IngestOutcome touched(int? touched);

  IngestOutcome touchesThrottled(int? touchesThrottled);

  IngestOutcome late_(int? late_);

  IngestOutcome skipped(int? skipped);

  IngestOutcome byReason(Map<String, int>? byReason);

  IngestOutcome versionBytes(int? versionBytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestOutcome(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestOutcome(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestOutcome call({
    int seen,
    @Deprecated('changed has been deprecated') int changed,
    int rejected,
    int? fromSeq,
    int? toSeq,
    List<IngestOutcomeRejectionsInner>? rejections,
    int persisted,
    int? live,
    int? touched,
    int? touchesThrottled,
    int? late_,
    int? skipped,
    Map<String, int>? byReason,
    int? versionBytes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestOutcome.copyWith(...)` or call `instanceOfIngestOutcome.copyWith.fieldName(value)` for a single field.
class _$IngestOutcomeCWProxyImpl implements _$IngestOutcomeCWProxy {
  const _$IngestOutcomeCWProxyImpl(this._value);

  final IngestOutcome _value;

  @override
  IngestOutcome seen(int seen) => call(seen: seen);

  @override
  IngestOutcome changed(
    @Deprecated('changed has been deprecated') int changed,
  ) => call(changed: changed);

  @override
  IngestOutcome rejected(int rejected) => call(rejected: rejected);

  @override
  IngestOutcome fromSeq(int? fromSeq) => call(fromSeq: fromSeq);

  @override
  IngestOutcome toSeq(int? toSeq) => call(toSeq: toSeq);

  @override
  IngestOutcome rejections(List<IngestOutcomeRejectionsInner>? rejections) =>
      call(rejections: rejections);

  @override
  IngestOutcome persisted(int persisted) => call(persisted: persisted);

  @override
  IngestOutcome live(int? live) => call(live: live);

  @override
  IngestOutcome touched(int? touched) => call(touched: touched);

  @override
  IngestOutcome touchesThrottled(int? touchesThrottled) =>
      call(touchesThrottled: touchesThrottled);

  @override
  IngestOutcome late_(int? late_) => call(late_: late_);

  @override
  IngestOutcome skipped(int? skipped) => call(skipped: skipped);

  @override
  IngestOutcome byReason(Map<String, int>? byReason) =>
      call(byReason: byReason);

  @override
  IngestOutcome versionBytes(int? versionBytes) =>
      call(versionBytes: versionBytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestOutcome(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestOutcome(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestOutcome call({
    Object? seen = const $CopyWithPlaceholder(),
    @Deprecated('changed has been deprecated')
    Object? changed = const $CopyWithPlaceholder(),
    Object? rejected = const $CopyWithPlaceholder(),
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
    Object? rejections = const $CopyWithPlaceholder(),
    Object? persisted = const $CopyWithPlaceholder(),
    Object? live = const $CopyWithPlaceholder(),
    Object? touched = const $CopyWithPlaceholder(),
    Object? touchesThrottled = const $CopyWithPlaceholder(),
    Object? late_ = const $CopyWithPlaceholder(),
    Object? skipped = const $CopyWithPlaceholder(),
    Object? byReason = const $CopyWithPlaceholder(),
    Object? versionBytes = const $CopyWithPlaceholder(),
  }) {
    return IngestOutcome(
      seen: seen == const $CopyWithPlaceholder() || seen == null
          ? _value.seen
          // ignore: cast_nullable_to_non_nullable
          : seen as int,
      changed: changed == const $CopyWithPlaceholder() || changed == null
          ? _value.changed
          // ignore: cast_nullable_to_non_nullable
          : changed as int,
      rejected: rejected == const $CopyWithPlaceholder() || rejected == null
          ? _value.rejected
          // ignore: cast_nullable_to_non_nullable
          : rejected as int,
      fromSeq: fromSeq == const $CopyWithPlaceholder()
          ? _value.fromSeq
          // ignore: cast_nullable_to_non_nullable
          : fromSeq as int?,
      toSeq: toSeq == const $CopyWithPlaceholder()
          ? _value.toSeq
          // ignore: cast_nullable_to_non_nullable
          : toSeq as int?,
      rejections: rejections == const $CopyWithPlaceholder()
          ? _value.rejections
          // ignore: cast_nullable_to_non_nullable
          : rejections as List<IngestOutcomeRejectionsInner>?,
      persisted: persisted == const $CopyWithPlaceholder() || persisted == null
          ? _value.persisted
          // ignore: cast_nullable_to_non_nullable
          : persisted as int,
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
      byReason: byReason == const $CopyWithPlaceholder()
          ? _value.byReason
          // ignore: cast_nullable_to_non_nullable
          : byReason as Map<String, int>?,
      versionBytes: versionBytes == const $CopyWithPlaceholder()
          ? _value.versionBytes
          // ignore: cast_nullable_to_non_nullable
          : versionBytes as int?,
    );
  }
}

extension $IngestOutcomeCopyWith on IngestOutcome {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestOutcome.copyWith(...)` or `instanceOfIngestOutcome.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestOutcomeCWProxy get copyWith => _$IngestOutcomeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestOutcome _$IngestOutcomeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IngestOutcome', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['seen', 'changed', 'rejected', 'persisted'],
      );
      final val = IngestOutcome(
        seen: $checkedConvert('seen', (v) => (v as num).toInt()),
        changed: $checkedConvert('changed', (v) => (v as num).toInt()),
        rejected: $checkedConvert('rejected', (v) => (v as num).toInt()),
        fromSeq: $checkedConvert('fromSeq', (v) => (v as num?)?.toInt()),
        toSeq: $checkedConvert('toSeq', (v) => (v as num?)?.toInt()),
        rejections: $checkedConvert(
          'rejections',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => IngestOutcomeRejectionsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        persisted: $checkedConvert('persisted', (v) => (v as num).toInt()),
        live: $checkedConvert('live', (v) => (v as num?)?.toInt()),
        touched: $checkedConvert('touched', (v) => (v as num?)?.toInt()),
        touchesThrottled: $checkedConvert(
          'touchesThrottled',
          (v) => (v as num?)?.toInt(),
        ),
        late_: $checkedConvert('late', (v) => (v as num?)?.toInt()),
        skipped: $checkedConvert('skipped', (v) => (v as num?)?.toInt()),
        byReason: $checkedConvert(
          'byReason',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, (e as num).toInt()),
          ),
        ),
        versionBytes: $checkedConvert(
          'versionBytes',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    }, fieldKeyMap: const {'late_': 'late'});

Map<String, dynamic> _$IngestOutcomeToJson(IngestOutcome instance) =>
    <String, dynamic>{
      'seen': instance.seen,
      'changed': instance.changed,
      'rejected': instance.rejected,
      'fromSeq': ?instance.fromSeq,
      'toSeq': ?instance.toSeq,
      'rejections': ?instance.rejections?.map((e) => e.toJson()).toList(),
      'persisted': instance.persisted,
      'live': ?instance.live,
      'touched': ?instance.touched,
      'touchesThrottled': ?instance.touchesThrottled,
      'late': ?instance.late_,
      'skipped': ?instance.skipped,
      'byReason': ?instance.byReason,
      'versionBytes': ?instance.versionBytes,
    };
