// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_settings_attention_gap.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialSettingsAttentionGapCWProxy {
  SocialSettingsAttentionGap minSeverity(
    SocialSettingsAttentionGapMinSeverityEnum? minSeverity,
  );

  SocialSettingsAttentionGap percentile(num? percentile);

  SocialSettingsAttentionGap lookbackDays(int? lookbackDays);

  SocialSettingsAttentionGap windowHours(int? windowHours);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsAttentionGap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsAttentionGap(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialSettingsAttentionGap call({
    SocialSettingsAttentionGapMinSeverityEnum? minSeverity,
    num? percentile,
    int? lookbackDays,
    int? windowHours,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialSettingsAttentionGap.copyWith(...)` or call `instanceOfSocialSettingsAttentionGap.copyWith.fieldName(value)` for a single field.
class _$SocialSettingsAttentionGapCWProxyImpl
    implements _$SocialSettingsAttentionGapCWProxy {
  const _$SocialSettingsAttentionGapCWProxyImpl(this._value);

  final SocialSettingsAttentionGap _value;

  @override
  SocialSettingsAttentionGap minSeverity(
    SocialSettingsAttentionGapMinSeverityEnum? minSeverity,
  ) => call(minSeverity: minSeverity);

  @override
  SocialSettingsAttentionGap percentile(num? percentile) =>
      call(percentile: percentile);

  @override
  SocialSettingsAttentionGap lookbackDays(int? lookbackDays) =>
      call(lookbackDays: lookbackDays);

  @override
  SocialSettingsAttentionGap windowHours(int? windowHours) =>
      call(windowHours: windowHours);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsAttentionGap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsAttentionGap(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialSettingsAttentionGap call({
    Object? minSeverity = const $CopyWithPlaceholder(),
    Object? percentile = const $CopyWithPlaceholder(),
    Object? lookbackDays = const $CopyWithPlaceholder(),
    Object? windowHours = const $CopyWithPlaceholder(),
  }) {
    return SocialSettingsAttentionGap(
      minSeverity: minSeverity == const $CopyWithPlaceholder()
          ? _value.minSeverity
          // ignore: cast_nullable_to_non_nullable
          : minSeverity as SocialSettingsAttentionGapMinSeverityEnum?,
      percentile: percentile == const $CopyWithPlaceholder()
          ? _value.percentile
          // ignore: cast_nullable_to_non_nullable
          : percentile as num?,
      lookbackDays: lookbackDays == const $CopyWithPlaceholder()
          ? _value.lookbackDays
          // ignore: cast_nullable_to_non_nullable
          : lookbackDays as int?,
      windowHours: windowHours == const $CopyWithPlaceholder()
          ? _value.windowHours
          // ignore: cast_nullable_to_non_nullable
          : windowHours as int?,
    );
  }
}

extension $SocialSettingsAttentionGapCopyWith on SocialSettingsAttentionGap {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialSettingsAttentionGap.copyWith(...)` or `instanceOfSocialSettingsAttentionGap.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialSettingsAttentionGapCWProxy get copyWith =>
      _$SocialSettingsAttentionGapCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialSettingsAttentionGap _$SocialSettingsAttentionGapFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialSettingsAttentionGap', json, ($checkedConvert) {
  final val = SocialSettingsAttentionGap(
    minSeverity: $checkedConvert(
      'minSeverity',
      (v) => $enumDecodeNullable(
        _$SocialSettingsAttentionGapMinSeverityEnumEnumMap,
        v,
      ),
    ),
    percentile: $checkedConvert('percentile', (v) => v as num? ?? 10),
    lookbackDays: $checkedConvert(
      'lookbackDays',
      (v) => (v as num?)?.toInt() ?? 365,
    ),
    windowHours: $checkedConvert(
      'windowHours',
      (v) => (v as num?)?.toInt() ?? 6,
    ),
  );
  return val;
});

Map<String, dynamic> _$SocialSettingsAttentionGapToJson(
  SocialSettingsAttentionGap instance,
) => <String, dynamic>{
  'minSeverity':
      ?_$SocialSettingsAttentionGapMinSeverityEnumEnumMap[instance.minSeverity],
  'percentile': ?instance.percentile,
  'lookbackDays': ?instance.lookbackDays,
  'windowHours': ?instance.windowHours,
};

const _$SocialSettingsAttentionGapMinSeverityEnumEnumMap = {
  SocialSettingsAttentionGapMinSeverityEnum.notable: 'notable',
  SocialSettingsAttentionGapMinSeverityEnum.significant: 'significant',
};
