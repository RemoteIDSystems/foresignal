// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_lead_times.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialLeadTimesCWProxy {
  SocialLeadTimes socialFirst(DateTime socialFirst);

  SocialLeadTimes physicalFirst(DateTime? physicalFirst);

  SocialLeadTimes newsFirst(DateTime? newsFirst);

  SocialLeadTimes officialFirst(DateTime? officialFirst);

  SocialLeadTimes leadOverPhysicalSeconds(num? leadOverPhysicalSeconds);

  SocialLeadTimes leadOverNewsSeconds(num? leadOverNewsSeconds);

  SocialLeadTimes leadOverOfficialSeconds(num? leadOverOfficialSeconds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialLeadTimes(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialLeadTimes(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialLeadTimes call({
    DateTime socialFirst,
    DateTime? physicalFirst,
    DateTime? newsFirst,
    DateTime? officialFirst,
    num? leadOverPhysicalSeconds,
    num? leadOverNewsSeconds,
    num? leadOverOfficialSeconds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialLeadTimes.copyWith(...)` or call `instanceOfSocialLeadTimes.copyWith.fieldName(value)` for a single field.
class _$SocialLeadTimesCWProxyImpl implements _$SocialLeadTimesCWProxy {
  const _$SocialLeadTimesCWProxyImpl(this._value);

  final SocialLeadTimes _value;

  @override
  SocialLeadTimes socialFirst(DateTime socialFirst) =>
      call(socialFirst: socialFirst);

  @override
  SocialLeadTimes physicalFirst(DateTime? physicalFirst) =>
      call(physicalFirst: physicalFirst);

  @override
  SocialLeadTimes newsFirst(DateTime? newsFirst) => call(newsFirst: newsFirst);

  @override
  SocialLeadTimes officialFirst(DateTime? officialFirst) =>
      call(officialFirst: officialFirst);

  @override
  SocialLeadTimes leadOverPhysicalSeconds(num? leadOverPhysicalSeconds) =>
      call(leadOverPhysicalSeconds: leadOverPhysicalSeconds);

  @override
  SocialLeadTimes leadOverNewsSeconds(num? leadOverNewsSeconds) =>
      call(leadOverNewsSeconds: leadOverNewsSeconds);

  @override
  SocialLeadTimes leadOverOfficialSeconds(num? leadOverOfficialSeconds) =>
      call(leadOverOfficialSeconds: leadOverOfficialSeconds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialLeadTimes(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialLeadTimes(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialLeadTimes call({
    Object? socialFirst = const $CopyWithPlaceholder(),
    Object? physicalFirst = const $CopyWithPlaceholder(),
    Object? newsFirst = const $CopyWithPlaceholder(),
    Object? officialFirst = const $CopyWithPlaceholder(),
    Object? leadOverPhysicalSeconds = const $CopyWithPlaceholder(),
    Object? leadOverNewsSeconds = const $CopyWithPlaceholder(),
    Object? leadOverOfficialSeconds = const $CopyWithPlaceholder(),
  }) {
    return SocialLeadTimes(
      socialFirst:
          socialFirst == const $CopyWithPlaceholder() || socialFirst == null
          ? _value.socialFirst
          // ignore: cast_nullable_to_non_nullable
          : socialFirst as DateTime,
      physicalFirst: physicalFirst == const $CopyWithPlaceholder()
          ? _value.physicalFirst
          // ignore: cast_nullable_to_non_nullable
          : physicalFirst as DateTime?,
      newsFirst: newsFirst == const $CopyWithPlaceholder()
          ? _value.newsFirst
          // ignore: cast_nullable_to_non_nullable
          : newsFirst as DateTime?,
      officialFirst: officialFirst == const $CopyWithPlaceholder()
          ? _value.officialFirst
          // ignore: cast_nullable_to_non_nullable
          : officialFirst as DateTime?,
      leadOverPhysicalSeconds:
          leadOverPhysicalSeconds == const $CopyWithPlaceholder()
          ? _value.leadOverPhysicalSeconds
          // ignore: cast_nullable_to_non_nullable
          : leadOverPhysicalSeconds as num?,
      leadOverNewsSeconds: leadOverNewsSeconds == const $CopyWithPlaceholder()
          ? _value.leadOverNewsSeconds
          // ignore: cast_nullable_to_non_nullable
          : leadOverNewsSeconds as num?,
      leadOverOfficialSeconds:
          leadOverOfficialSeconds == const $CopyWithPlaceholder()
          ? _value.leadOverOfficialSeconds
          // ignore: cast_nullable_to_non_nullable
          : leadOverOfficialSeconds as num?,
    );
  }
}

extension $SocialLeadTimesCopyWith on SocialLeadTimes {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialLeadTimes.copyWith(...)` or `instanceOfSocialLeadTimes.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialLeadTimesCWProxy get copyWith => _$SocialLeadTimesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialLeadTimes _$SocialLeadTimesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialLeadTimes', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['socialFirst']);
      final val = SocialLeadTimes(
        socialFirst: $checkedConvert(
          'socialFirst',
          (v) => DateTime.parse(v as String),
        ),
        physicalFirst: $checkedConvert(
          'physicalFirst',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        newsFirst: $checkedConvert(
          'newsFirst',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        officialFirst: $checkedConvert(
          'officialFirst',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        leadOverPhysicalSeconds: $checkedConvert(
          'leadOverPhysicalSeconds',
          (v) => v as num?,
        ),
        leadOverNewsSeconds: $checkedConvert(
          'leadOverNewsSeconds',
          (v) => v as num?,
        ),
        leadOverOfficialSeconds: $checkedConvert(
          'leadOverOfficialSeconds',
          (v) => v as num?,
        ),
      );
      return val;
    });

Map<String, dynamic> _$SocialLeadTimesToJson(SocialLeadTimes instance) =>
    <String, dynamic>{
      'socialFirst': instance.socialFirst.toIso8601String(),
      'physicalFirst': ?instance.physicalFirst?.toIso8601String(),
      'newsFirst': ?instance.newsFirst?.toIso8601String(),
      'officialFirst': ?instance.officialFirst?.toIso8601String(),
      'leadOverPhysicalSeconds': ?instance.leadOverPhysicalSeconds,
      'leadOverNewsSeconds': ?instance.leadOverNewsSeconds,
      'leadOverOfficialSeconds': ?instance.leadOverOfficialSeconds,
    };
