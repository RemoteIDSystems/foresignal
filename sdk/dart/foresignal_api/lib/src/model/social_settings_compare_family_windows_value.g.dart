// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_settings_compare_family_windows_value.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialSettingsCompareFamilyWindowsValueCWProxy {
  SocialSettingsCompareFamilyWindowsValue before(String? before);

  SocialSettingsCompareFamilyWindowsValue after(String? after);

  SocialSettingsCompareFamilyWindowsValue toleranceKm(num? toleranceKm);

  SocialSettingsCompareFamilyWindowsValue publicationLag(
    String? publicationLag,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsCompareFamilyWindowsValue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsCompareFamilyWindowsValue(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialSettingsCompareFamilyWindowsValue call({
    String? before,
    String? after,
    num? toleranceKm,
    String? publicationLag,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialSettingsCompareFamilyWindowsValue.copyWith(...)` or call `instanceOfSocialSettingsCompareFamilyWindowsValue.copyWith.fieldName(value)` for a single field.
class _$SocialSettingsCompareFamilyWindowsValueCWProxyImpl
    implements _$SocialSettingsCompareFamilyWindowsValueCWProxy {
  const _$SocialSettingsCompareFamilyWindowsValueCWProxyImpl(this._value);

  final SocialSettingsCompareFamilyWindowsValue _value;

  @override
  SocialSettingsCompareFamilyWindowsValue before(String? before) =>
      call(before: before);

  @override
  SocialSettingsCompareFamilyWindowsValue after(String? after) =>
      call(after: after);

  @override
  SocialSettingsCompareFamilyWindowsValue toleranceKm(num? toleranceKm) =>
      call(toleranceKm: toleranceKm);

  @override
  SocialSettingsCompareFamilyWindowsValue publicationLag(
    String? publicationLag,
  ) => call(publicationLag: publicationLag);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsCompareFamilyWindowsValue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsCompareFamilyWindowsValue(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialSettingsCompareFamilyWindowsValue call({
    Object? before = const $CopyWithPlaceholder(),
    Object? after = const $CopyWithPlaceholder(),
    Object? toleranceKm = const $CopyWithPlaceholder(),
    Object? publicationLag = const $CopyWithPlaceholder(),
  }) {
    return SocialSettingsCompareFamilyWindowsValue(
      before: before == const $CopyWithPlaceholder()
          ? _value.before
          // ignore: cast_nullable_to_non_nullable
          : before as String?,
      after: after == const $CopyWithPlaceholder()
          ? _value.after
          // ignore: cast_nullable_to_non_nullable
          : after as String?,
      toleranceKm: toleranceKm == const $CopyWithPlaceholder()
          ? _value.toleranceKm
          // ignore: cast_nullable_to_non_nullable
          : toleranceKm as num?,
      publicationLag: publicationLag == const $CopyWithPlaceholder()
          ? _value.publicationLag
          // ignore: cast_nullable_to_non_nullable
          : publicationLag as String?,
    );
  }
}

extension $SocialSettingsCompareFamilyWindowsValueCopyWith
    on SocialSettingsCompareFamilyWindowsValue {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialSettingsCompareFamilyWindowsValue.copyWith(...)` or `instanceOfSocialSettingsCompareFamilyWindowsValue.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialSettingsCompareFamilyWindowsValueCWProxy get copyWith =>
      _$SocialSettingsCompareFamilyWindowsValueCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialSettingsCompareFamilyWindowsValue
_$SocialSettingsCompareFamilyWindowsValueFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialSettingsCompareFamilyWindowsValue', json, (
      $checkedConvert,
    ) {
      final val = SocialSettingsCompareFamilyWindowsValue(
        before: $checkedConvert('before', (v) => v as String?),
        after: $checkedConvert('after', (v) => v as String?),
        toleranceKm: $checkedConvert('toleranceKm', (v) => v as num?),
        publicationLag: $checkedConvert('publicationLag', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SocialSettingsCompareFamilyWindowsValueToJson(
  SocialSettingsCompareFamilyWindowsValue instance,
) => <String, dynamic>{
  'before': ?instance.before,
  'after': ?instance.after,
  'toleranceKm': ?instance.toleranceKm,
  'publicationLag': ?instance.publicationLag,
};
