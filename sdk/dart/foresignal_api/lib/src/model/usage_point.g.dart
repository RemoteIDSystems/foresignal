// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'usage_point.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UsagePointCWProxy {
  UsagePoint t(DateTime t);

  UsagePoint requests(int requests);

  UsagePoint bytes(int bytes);

  UsagePoint errors(int errors);

  UsagePoint refused(int refused);

  UsagePoint recordsSeen(int? recordsSeen);

  UsagePoint versions(int? versions);

  UsagePoint liveUpdates(int? liveUpdates);

  UsagePoint touches(int? touches);

  UsagePoint late_(int? late_);

  UsagePoint skipped(int? skipped);

  UsagePoint rejected(int? rejected);

  UsagePoint versionBytes(int? versionBytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UsagePoint(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UsagePoint(...).copyWith(id: 12, name: "My name")
  /// ```
  UsagePoint call({
    DateTime t,
    int requests,
    int bytes,
    int errors,
    int refused,
    int? recordsSeen,
    int? versions,
    int? liveUpdates,
    int? touches,
    int? late_,
    int? skipped,
    int? rejected,
    int? versionBytes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUsagePoint.copyWith(...)` or call `instanceOfUsagePoint.copyWith.fieldName(value)` for a single field.
class _$UsagePointCWProxyImpl implements _$UsagePointCWProxy {
  const _$UsagePointCWProxyImpl(this._value);

  final UsagePoint _value;

  @override
  UsagePoint t(DateTime t) => call(t: t);

  @override
  UsagePoint requests(int requests) => call(requests: requests);

  @override
  UsagePoint bytes(int bytes) => call(bytes: bytes);

  @override
  UsagePoint errors(int errors) => call(errors: errors);

  @override
  UsagePoint refused(int refused) => call(refused: refused);

  @override
  UsagePoint recordsSeen(int? recordsSeen) => call(recordsSeen: recordsSeen);

  @override
  UsagePoint versions(int? versions) => call(versions: versions);

  @override
  UsagePoint liveUpdates(int? liveUpdates) => call(liveUpdates: liveUpdates);

  @override
  UsagePoint touches(int? touches) => call(touches: touches);

  @override
  UsagePoint late_(int? late_) => call(late_: late_);

  @override
  UsagePoint skipped(int? skipped) => call(skipped: skipped);

  @override
  UsagePoint rejected(int? rejected) => call(rejected: rejected);

  @override
  UsagePoint versionBytes(int? versionBytes) =>
      call(versionBytes: versionBytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UsagePoint(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UsagePoint(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  UsagePoint call({
    Object? t = const $CopyWithPlaceholder(),
    Object? requests = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? errors = const $CopyWithPlaceholder(),
    Object? refused = const $CopyWithPlaceholder(),
    Object? recordsSeen = const $CopyWithPlaceholder(),
    Object? versions = const $CopyWithPlaceholder(),
    Object? liveUpdates = const $CopyWithPlaceholder(),
    Object? touches = const $CopyWithPlaceholder(),
    Object? late_ = const $CopyWithPlaceholder(),
    Object? skipped = const $CopyWithPlaceholder(),
    Object? rejected = const $CopyWithPlaceholder(),
    Object? versionBytes = const $CopyWithPlaceholder(),
  }) {
    return UsagePoint(
      t: t == const $CopyWithPlaceholder() || t == null
          ? _value.t
          // ignore: cast_nullable_to_non_nullable
          : t as DateTime,
      requests: requests == const $CopyWithPlaceholder() || requests == null
          ? _value.requests
          // ignore: cast_nullable_to_non_nullable
          : requests as int,
      bytes: bytes == const $CopyWithPlaceholder() || bytes == null
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int,
      errors: errors == const $CopyWithPlaceholder() || errors == null
          ? _value.errors
          // ignore: cast_nullable_to_non_nullable
          : errors as int,
      refused: refused == const $CopyWithPlaceholder() || refused == null
          ? _value.refused
          // ignore: cast_nullable_to_non_nullable
          : refused as int,
      recordsSeen: recordsSeen == const $CopyWithPlaceholder()
          ? _value.recordsSeen
          // ignore: cast_nullable_to_non_nullable
          : recordsSeen as int?,
      versions: versions == const $CopyWithPlaceholder()
          ? _value.versions
          // ignore: cast_nullable_to_non_nullable
          : versions as int?,
      liveUpdates: liveUpdates == const $CopyWithPlaceholder()
          ? _value.liveUpdates
          // ignore: cast_nullable_to_non_nullable
          : liveUpdates as int?,
      touches: touches == const $CopyWithPlaceholder()
          ? _value.touches
          // ignore: cast_nullable_to_non_nullable
          : touches as int?,
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
      versionBytes: versionBytes == const $CopyWithPlaceholder()
          ? _value.versionBytes
          // ignore: cast_nullable_to_non_nullable
          : versionBytes as int?,
    );
  }
}

extension $UsagePointCopyWith on UsagePoint {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUsagePoint.copyWith(...)` or `instanceOfUsagePoint.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UsagePointCWProxy get copyWith => _$UsagePointCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UsagePoint _$UsagePointFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('UsagePoint', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['t', 'requests', 'bytes', 'errors', 'refused'],
  );
  final val = UsagePoint(
    t: $checkedConvert('t', (v) => DateTime.parse(v as String)),
    requests: $checkedConvert('requests', (v) => (v as num).toInt()),
    bytes: $checkedConvert('bytes', (v) => (v as num).toInt()),
    errors: $checkedConvert('errors', (v) => (v as num).toInt()),
    refused: $checkedConvert('refused', (v) => (v as num).toInt()),
    recordsSeen: $checkedConvert('recordsSeen', (v) => (v as num?)?.toInt()),
    versions: $checkedConvert('versions', (v) => (v as num?)?.toInt()),
    liveUpdates: $checkedConvert('liveUpdates', (v) => (v as num?)?.toInt()),
    touches: $checkedConvert('touches', (v) => (v as num?)?.toInt()),
    late_: $checkedConvert('late', (v) => (v as num?)?.toInt()),
    skipped: $checkedConvert('skipped', (v) => (v as num?)?.toInt()),
    rejected: $checkedConvert('rejected', (v) => (v as num?)?.toInt()),
    versionBytes: $checkedConvert('versionBytes', (v) => (v as num?)?.toInt()),
  );
  return val;
}, fieldKeyMap: const {'late_': 'late'});

Map<String, dynamic> _$UsagePointToJson(UsagePoint instance) =>
    <String, dynamic>{
      't': instance.t.toIso8601String(),
      'requests': instance.requests,
      'bytes': instance.bytes,
      'errors': instance.errors,
      'refused': instance.refused,
      'recordsSeen': ?instance.recordsSeen,
      'versions': ?instance.versions,
      'liveUpdates': ?instance.liveUpdates,
      'touches': ?instance.touches,
      'late': ?instance.late_,
      'skipped': ?instance.skipped,
      'rejected': ?instance.rejected,
      'versionBytes': ?instance.versionBytes,
    };
