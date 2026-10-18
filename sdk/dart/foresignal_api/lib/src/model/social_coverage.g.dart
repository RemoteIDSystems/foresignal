// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_coverage.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialCoverageCWProxy {
  SocialCoverage platform(String platform);

  SocialCoverage regionCode(String? regionCode);

  SocialCoverage since(DateTime since);

  SocialCoverage until(DateTime until);

  SocialCoverage buckets(List<SocialCoverageBucket> buckets);

  SocialCoverage completeShare(num completeShare);

  SocialCoverage baselinePostsPerHour(num? baselinePostsPerHour);

  SocialCoverage absenceOk(bool absenceOk);

  SocialCoverage reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCoverage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCoverage(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialCoverage call({
    String platform,
    String? regionCode,
    DateTime since,
    DateTime until,
    List<SocialCoverageBucket> buckets,
    num completeShare,
    num? baselinePostsPerHour,
    bool absenceOk,
    String? reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialCoverage.copyWith(...)` or call `instanceOfSocialCoverage.copyWith.fieldName(value)` for a single field.
class _$SocialCoverageCWProxyImpl implements _$SocialCoverageCWProxy {
  const _$SocialCoverageCWProxyImpl(this._value);

  final SocialCoverage _value;

  @override
  SocialCoverage platform(String platform) => call(platform: platform);

  @override
  SocialCoverage regionCode(String? regionCode) => call(regionCode: regionCode);

  @override
  SocialCoverage since(DateTime since) => call(since: since);

  @override
  SocialCoverage until(DateTime until) => call(until: until);

  @override
  SocialCoverage buckets(List<SocialCoverageBucket> buckets) =>
      call(buckets: buckets);

  @override
  SocialCoverage completeShare(num completeShare) =>
      call(completeShare: completeShare);

  @override
  SocialCoverage baselinePostsPerHour(num? baselinePostsPerHour) =>
      call(baselinePostsPerHour: baselinePostsPerHour);

  @override
  SocialCoverage absenceOk(bool absenceOk) => call(absenceOk: absenceOk);

  @override
  SocialCoverage reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCoverage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCoverage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialCoverage call({
    Object? platform = const $CopyWithPlaceholder(),
    Object? regionCode = const $CopyWithPlaceholder(),
    Object? since = const $CopyWithPlaceholder(),
    Object? until = const $CopyWithPlaceholder(),
    Object? buckets = const $CopyWithPlaceholder(),
    Object? completeShare = const $CopyWithPlaceholder(),
    Object? baselinePostsPerHour = const $CopyWithPlaceholder(),
    Object? absenceOk = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return SocialCoverage(
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String,
      regionCode: regionCode == const $CopyWithPlaceholder()
          ? _value.regionCode
          // ignore: cast_nullable_to_non_nullable
          : regionCode as String?,
      since: since == const $CopyWithPlaceholder() || since == null
          ? _value.since
          // ignore: cast_nullable_to_non_nullable
          : since as DateTime,
      until: until == const $CopyWithPlaceholder() || until == null
          ? _value.until
          // ignore: cast_nullable_to_non_nullable
          : until as DateTime,
      buckets: buckets == const $CopyWithPlaceholder() || buckets == null
          ? _value.buckets
          // ignore: cast_nullable_to_non_nullable
          : buckets as List<SocialCoverageBucket>,
      completeShare:
          completeShare == const $CopyWithPlaceholder() || completeShare == null
          ? _value.completeShare
          // ignore: cast_nullable_to_non_nullable
          : completeShare as num,
      baselinePostsPerHour: baselinePostsPerHour == const $CopyWithPlaceholder()
          ? _value.baselinePostsPerHour
          // ignore: cast_nullable_to_non_nullable
          : baselinePostsPerHour as num?,
      absenceOk: absenceOk == const $CopyWithPlaceholder() || absenceOk == null
          ? _value.absenceOk
          // ignore: cast_nullable_to_non_nullable
          : absenceOk as bool,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $SocialCoverageCopyWith on SocialCoverage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialCoverage.copyWith(...)` or `instanceOfSocialCoverage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialCoverageCWProxy get copyWith => _$SocialCoverageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialCoverage _$SocialCoverageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialCoverage', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'platform',
          'since',
          'until',
          'buckets',
          'completeShare',
          'absenceOk',
        ],
      );
      final val = SocialCoverage(
        platform: $checkedConvert('platform', (v) => v as String),
        regionCode: $checkedConvert('regionCode', (v) => v as String?),
        since: $checkedConvert('since', (v) => DateTime.parse(v as String)),
        until: $checkedConvert('until', (v) => DateTime.parse(v as String)),
        buckets: $checkedConvert(
          'buckets',
          (v) => (v as List<dynamic>)
              .map(
                (e) => SocialCoverageBucket.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
        completeShare: $checkedConvert('completeShare', (v) => v as num),
        baselinePostsPerHour: $checkedConvert(
          'baselinePostsPerHour',
          (v) => v as num?,
        ),
        absenceOk: $checkedConvert('absenceOk', (v) => v as bool),
        reason: $checkedConvert('reason', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SocialCoverageToJson(SocialCoverage instance) =>
    <String, dynamic>{
      'platform': instance.platform,
      'regionCode': ?instance.regionCode,
      'since': instance.since.toIso8601String(),
      'until': instance.until.toIso8601String(),
      'buckets': instance.buckets.map((e) => e.toJson()).toList(),
      'completeShare': instance.completeShare,
      'baselinePostsPerHour': ?instance.baselinePostsPerHour,
      'absenceOk': instance.absenceOk,
      'reason': ?instance.reason,
    };
