// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_settings_compare.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialSettingsCompareCWProxy {
  SocialSettingsCompare minGroups(int? minGroups);

  SocialSettingsCompare defaultWindow(
    SocialSettingsCompareDefaultWindow? defaultWindow,
  );

  SocialSettingsCompare familyWindows(
    Map<String, SocialSettingsCompareFamilyWindowsValue>? familyWindows,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsCompare(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsCompare(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialSettingsCompare call({
    int? minGroups,
    SocialSettingsCompareDefaultWindow? defaultWindow,
    Map<String, SocialSettingsCompareFamilyWindowsValue>? familyWindows,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialSettingsCompare.copyWith(...)` or call `instanceOfSocialSettingsCompare.copyWith.fieldName(value)` for a single field.
class _$SocialSettingsCompareCWProxyImpl
    implements _$SocialSettingsCompareCWProxy {
  const _$SocialSettingsCompareCWProxyImpl(this._value);

  final SocialSettingsCompare _value;

  @override
  SocialSettingsCompare minGroups(int? minGroups) => call(minGroups: minGroups);

  @override
  SocialSettingsCompare defaultWindow(
    SocialSettingsCompareDefaultWindow? defaultWindow,
  ) => call(defaultWindow: defaultWindow);

  @override
  SocialSettingsCompare familyWindows(
    Map<String, SocialSettingsCompareFamilyWindowsValue>? familyWindows,
  ) => call(familyWindows: familyWindows);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsCompare(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsCompare(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialSettingsCompare call({
    Object? minGroups = const $CopyWithPlaceholder(),
    Object? defaultWindow = const $CopyWithPlaceholder(),
    Object? familyWindows = const $CopyWithPlaceholder(),
  }) {
    return SocialSettingsCompare(
      minGroups: minGroups == const $CopyWithPlaceholder()
          ? _value.minGroups
          // ignore: cast_nullable_to_non_nullable
          : minGroups as int?,
      defaultWindow: defaultWindow == const $CopyWithPlaceholder()
          ? _value.defaultWindow
          // ignore: cast_nullable_to_non_nullable
          : defaultWindow as SocialSettingsCompareDefaultWindow?,
      familyWindows: familyWindows == const $CopyWithPlaceholder()
          ? _value.familyWindows
          // ignore: cast_nullable_to_non_nullable
          : familyWindows
                as Map<String, SocialSettingsCompareFamilyWindowsValue>?,
    );
  }
}

extension $SocialSettingsCompareCopyWith on SocialSettingsCompare {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialSettingsCompare.copyWith(...)` or `instanceOfSocialSettingsCompare.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialSettingsCompareCWProxy get copyWith =>
      _$SocialSettingsCompareCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialSettingsCompare _$SocialSettingsCompareFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialSettingsCompare', json, ($checkedConvert) {
  final val = SocialSettingsCompare(
    minGroups: $checkedConvert('minGroups', (v) => (v as num?)?.toInt() ?? 3),
    defaultWindow: $checkedConvert(
      'defaultWindow',
      (v) => v == null
          ? null
          : SocialSettingsCompareDefaultWindow.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
    familyWindows: $checkedConvert(
      'familyWindows',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(
          k,
          SocialSettingsCompareFamilyWindowsValue.fromJson(
            e as Map<String, dynamic>,
          ),
        ),
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$SocialSettingsCompareToJson(
  SocialSettingsCompare instance,
) => <String, dynamic>{
  'minGroups': ?instance.minGroups,
  'defaultWindow': ?instance.defaultWindow?.toJson(),
  'familyWindows': ?instance.familyWindows?.map(
    (k, e) => MapEntry(k, e.toJson()),
  ),
};
