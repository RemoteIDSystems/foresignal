// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_settings_track_record.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialSettingsTrackRecordCWProxy {
  SocialSettingsTrackRecord minAssessed(int? minAssessed);

  SocialSettingsTrackRecord gradeEShare(num? gradeEShare);

  SocialSettingsTrackRecord gradeDShare(num? gradeDShare);

  SocialSettingsTrackRecord overrides(
    List<SocialSettingsTrackRecordOverridesInner>? overrides,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsTrackRecord(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsTrackRecord(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialSettingsTrackRecord call({
    int? minAssessed,
    num? gradeEShare,
    num? gradeDShare,
    List<SocialSettingsTrackRecordOverridesInner>? overrides,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialSettingsTrackRecord.copyWith(...)` or call `instanceOfSocialSettingsTrackRecord.copyWith.fieldName(value)` for a single field.
class _$SocialSettingsTrackRecordCWProxyImpl
    implements _$SocialSettingsTrackRecordCWProxy {
  const _$SocialSettingsTrackRecordCWProxyImpl(this._value);

  final SocialSettingsTrackRecord _value;

  @override
  SocialSettingsTrackRecord minAssessed(int? minAssessed) =>
      call(minAssessed: minAssessed);

  @override
  SocialSettingsTrackRecord gradeEShare(num? gradeEShare) =>
      call(gradeEShare: gradeEShare);

  @override
  SocialSettingsTrackRecord gradeDShare(num? gradeDShare) =>
      call(gradeDShare: gradeDShare);

  @override
  SocialSettingsTrackRecord overrides(
    List<SocialSettingsTrackRecordOverridesInner>? overrides,
  ) => call(overrides: overrides);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsTrackRecord(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsTrackRecord(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialSettingsTrackRecord call({
    Object? minAssessed = const $CopyWithPlaceholder(),
    Object? gradeEShare = const $CopyWithPlaceholder(),
    Object? gradeDShare = const $CopyWithPlaceholder(),
    Object? overrides = const $CopyWithPlaceholder(),
  }) {
    return SocialSettingsTrackRecord(
      minAssessed: minAssessed == const $CopyWithPlaceholder()
          ? _value.minAssessed
          // ignore: cast_nullable_to_non_nullable
          : minAssessed as int?,
      gradeEShare: gradeEShare == const $CopyWithPlaceholder()
          ? _value.gradeEShare
          // ignore: cast_nullable_to_non_nullable
          : gradeEShare as num?,
      gradeDShare: gradeDShare == const $CopyWithPlaceholder()
          ? _value.gradeDShare
          // ignore: cast_nullable_to_non_nullable
          : gradeDShare as num?,
      overrides: overrides == const $CopyWithPlaceholder()
          ? _value.overrides
          // ignore: cast_nullable_to_non_nullable
          : overrides as List<SocialSettingsTrackRecordOverridesInner>?,
    );
  }
}

extension $SocialSettingsTrackRecordCopyWith on SocialSettingsTrackRecord {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialSettingsTrackRecord.copyWith(...)` or `instanceOfSocialSettingsTrackRecord.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialSettingsTrackRecordCWProxy get copyWith =>
      _$SocialSettingsTrackRecordCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialSettingsTrackRecord _$SocialSettingsTrackRecordFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialSettingsTrackRecord', json, ($checkedConvert) {
  final val = SocialSettingsTrackRecord(
    minAssessed: $checkedConvert(
      'minAssessed',
      (v) => (v as num?)?.toInt() ?? 10,
    ),
    gradeEShare: $checkedConvert('gradeEShare', (v) => v as num? ?? 0.7),
    gradeDShare: $checkedConvert('gradeDShare', (v) => v as num? ?? 0.85),
    overrides: $checkedConvert(
      'overrides',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => SocialSettingsTrackRecordOverridesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$SocialSettingsTrackRecordToJson(
  SocialSettingsTrackRecord instance,
) => <String, dynamic>{
  'minAssessed': ?instance.minAssessed,
  'gradeEShare': ?instance.gradeEShare,
  'gradeDShare': ?instance.gradeDShare,
  'overrides': ?instance.overrides?.map((e) => e.toJson()).toList(),
};
