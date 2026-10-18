// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_settings_velocity.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialSettingsVelocityCWProxy {
  SocialSettingsVelocity minRatio(num? minRatio);

  SocialSettingsVelocity minUniqueAuthors(int? minUniqueAuthors);

  SocialSettingsVelocity dedupHours(int? dedupHours);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsVelocity(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsVelocity(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialSettingsVelocity call({
    num? minRatio,
    int? minUniqueAuthors,
    int? dedupHours,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialSettingsVelocity.copyWith(...)` or call `instanceOfSocialSettingsVelocity.copyWith.fieldName(value)` for a single field.
class _$SocialSettingsVelocityCWProxyImpl
    implements _$SocialSettingsVelocityCWProxy {
  const _$SocialSettingsVelocityCWProxyImpl(this._value);

  final SocialSettingsVelocity _value;

  @override
  SocialSettingsVelocity minRatio(num? minRatio) => call(minRatio: minRatio);

  @override
  SocialSettingsVelocity minUniqueAuthors(int? minUniqueAuthors) =>
      call(minUniqueAuthors: minUniqueAuthors);

  @override
  SocialSettingsVelocity dedupHours(int? dedupHours) =>
      call(dedupHours: dedupHours);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsVelocity(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsVelocity(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialSettingsVelocity call({
    Object? minRatio = const $CopyWithPlaceholder(),
    Object? minUniqueAuthors = const $CopyWithPlaceholder(),
    Object? dedupHours = const $CopyWithPlaceholder(),
  }) {
    return SocialSettingsVelocity(
      minRatio: minRatio == const $CopyWithPlaceholder()
          ? _value.minRatio
          // ignore: cast_nullable_to_non_nullable
          : minRatio as num?,
      minUniqueAuthors: minUniqueAuthors == const $CopyWithPlaceholder()
          ? _value.minUniqueAuthors
          // ignore: cast_nullable_to_non_nullable
          : minUniqueAuthors as int?,
      dedupHours: dedupHours == const $CopyWithPlaceholder()
          ? _value.dedupHours
          // ignore: cast_nullable_to_non_nullable
          : dedupHours as int?,
    );
  }
}

extension $SocialSettingsVelocityCopyWith on SocialSettingsVelocity {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialSettingsVelocity.copyWith(...)` or `instanceOfSocialSettingsVelocity.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialSettingsVelocityCWProxy get copyWith =>
      _$SocialSettingsVelocityCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialSettingsVelocity _$SocialSettingsVelocityFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialSettingsVelocity', json, ($checkedConvert) {
  final val = SocialSettingsVelocity(
    minRatio: $checkedConvert('minRatio', (v) => v as num? ?? 5),
    minUniqueAuthors: $checkedConvert(
      'minUniqueAuthors',
      (v) => (v as num?)?.toInt() ?? 20,
    ),
    dedupHours: $checkedConvert('dedupHours', (v) => (v as num?)?.toInt() ?? 6),
  );
  return val;
});

Map<String, dynamic> _$SocialSettingsVelocityToJson(
  SocialSettingsVelocity instance,
) => <String, dynamic>{
  'minRatio': ?instance.minRatio,
  'minUniqueAuthors': ?instance.minUniqueAuthors,
  'dedupHours': ?instance.dedupHours,
};
