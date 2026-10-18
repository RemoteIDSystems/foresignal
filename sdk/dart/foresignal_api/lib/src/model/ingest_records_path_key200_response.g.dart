// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_records_path_key200_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestRecordsPathKey200ResponseCWProxy {
  IngestRecordsPathKey200Response seen(int seen);

  IngestRecordsPathKey200Response changed(
    @Deprecated('changed has been deprecated') int changed,
  );

  IngestRecordsPathKey200Response rejected(int rejected);

  IngestRecordsPathKey200Response fromSeq(int? fromSeq);

  IngestRecordsPathKey200Response toSeq(int? toSeq);

  IngestRecordsPathKey200Response rejections(
    List<IngestOutcomeRejectionsInner>? rejections,
  );

  IngestRecordsPathKey200Response persisted(int persisted);

  IngestRecordsPathKey200Response live(int? live);

  IngestRecordsPathKey200Response touched(int? touched);

  IngestRecordsPathKey200Response touchesThrottled(int? touchesThrottled);

  IngestRecordsPathKey200Response late_(int? late_);

  IngestRecordsPathKey200Response skipped(int? skipped);

  IngestRecordsPathKey200Response byReason(Map<String, int>? byReason);

  IngestRecordsPathKey200Response versionBytes(int? versionBytes);

  IngestRecordsPathKey200Response result(
    IngestRecordsPathKey200ResponseResultEnum result,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestRecordsPathKey200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestRecordsPathKey200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestRecordsPathKey200Response call({
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
    IngestRecordsPathKey200ResponseResultEnum result,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestRecordsPathKey200Response.copyWith(...)` or call `instanceOfIngestRecordsPathKey200Response.copyWith.fieldName(value)` for a single field.
class _$IngestRecordsPathKey200ResponseCWProxyImpl
    implements _$IngestRecordsPathKey200ResponseCWProxy {
  const _$IngestRecordsPathKey200ResponseCWProxyImpl(this._value);

  final IngestRecordsPathKey200Response _value;

  @override
  IngestRecordsPathKey200Response seen(int seen) => call(seen: seen);

  @override
  IngestRecordsPathKey200Response changed(
    @Deprecated('changed has been deprecated') int changed,
  ) => call(changed: changed);

  @override
  IngestRecordsPathKey200Response rejected(int rejected) =>
      call(rejected: rejected);

  @override
  IngestRecordsPathKey200Response fromSeq(int? fromSeq) =>
      call(fromSeq: fromSeq);

  @override
  IngestRecordsPathKey200Response toSeq(int? toSeq) => call(toSeq: toSeq);

  @override
  IngestRecordsPathKey200Response rejections(
    List<IngestOutcomeRejectionsInner>? rejections,
  ) => call(rejections: rejections);

  @override
  IngestRecordsPathKey200Response persisted(int persisted) =>
      call(persisted: persisted);

  @override
  IngestRecordsPathKey200Response live(int? live) => call(live: live);

  @override
  IngestRecordsPathKey200Response touched(int? touched) =>
      call(touched: touched);

  @override
  IngestRecordsPathKey200Response touchesThrottled(int? touchesThrottled) =>
      call(touchesThrottled: touchesThrottled);

  @override
  IngestRecordsPathKey200Response late_(int? late_) => call(late_: late_);

  @override
  IngestRecordsPathKey200Response skipped(int? skipped) =>
      call(skipped: skipped);

  @override
  IngestRecordsPathKey200Response byReason(Map<String, int>? byReason) =>
      call(byReason: byReason);

  @override
  IngestRecordsPathKey200Response versionBytes(int? versionBytes) =>
      call(versionBytes: versionBytes);

  @override
  IngestRecordsPathKey200Response result(
    IngestRecordsPathKey200ResponseResultEnum result,
  ) => call(result: result);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestRecordsPathKey200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestRecordsPathKey200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestRecordsPathKey200Response call({
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
    Object? result = const $CopyWithPlaceholder(),
  }) {
    return IngestRecordsPathKey200Response(
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
      result: result == const $CopyWithPlaceholder() || result == null
          ? _value.result
          // ignore: cast_nullable_to_non_nullable
          : result as IngestRecordsPathKey200ResponseResultEnum,
    );
  }
}

extension $IngestRecordsPathKey200ResponseCopyWith
    on IngestRecordsPathKey200Response {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestRecordsPathKey200Response.copyWith(...)` or `instanceOfIngestRecordsPathKey200Response.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestRecordsPathKey200ResponseCWProxy get copyWith =>
      _$IngestRecordsPathKey200ResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestRecordsPathKey200Response _$IngestRecordsPathKey200ResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IngestRecordsPathKey200Response', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['seen', 'changed', 'rejected', 'persisted', 'result'],
  );
  final val = IngestRecordsPathKey200Response(
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
    versionBytes: $checkedConvert('versionBytes', (v) => (v as num?)?.toInt()),
    result: $checkedConvert(
      'result',
      (v) => $enumDecode(_$IngestRecordsPathKey200ResponseResultEnumEnumMap, v),
    ),
  );
  return val;
}, fieldKeyMap: const {'late_': 'late'});

Map<String, dynamic> _$IngestRecordsPathKey200ResponseToJson(
  IngestRecordsPathKey200Response instance,
) => <String, dynamic>{
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
  'result':
      _$IngestRecordsPathKey200ResponseResultEnumEnumMap[instance.result]!,
};

const _$IngestRecordsPathKey200ResponseResultEnumEnumMap = {
  IngestRecordsPathKey200ResponseResultEnum.ok: 'ok',
};
