// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_settings_compare_default_window.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialSettingsCompareDefaultWindowCWProxy {
  SocialSettingsCompareDefaultWindow before(String? before);

  SocialSettingsCompareDefaultWindow after(String? after);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsCompareDefaultWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsCompareDefaultWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialSettingsCompareDefaultWindow call({String? before, String? after});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialSettingsCompareDefaultWindow.copyWith(...)` or call `instanceOfSocialSettingsCompareDefaultWindow.copyWith.fieldName(value)` for a single field.
class _$SocialSettingsCompareDefaultWindowCWProxyImpl
    implements _$SocialSettingsCompareDefaultWindowCWProxy {
  const _$SocialSettingsCompareDefaultWindowCWProxyImpl(this._value);

  final SocialSettingsCompareDefaultWindow _value;

  @override
  SocialSettingsCompareDefaultWindow before(String? before) =>
      call(before: before);

  @override
  SocialSettingsCompareDefaultWindow after(String? after) => call(after: after);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsCompareDefaultWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsCompareDefaultWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialSettingsCompareDefaultWindow call({
    Object? before = const $CopyWithPlaceholder(),
    Object? after = const $CopyWithPlaceholder(),
  }) {
    return SocialSettingsCompareDefaultWindow(
      before: before == const $CopyWithPlaceholder()
          ? _value.before
          // ignore: cast_nullable_to_non_nullable
          : before as String?,
      after: after == const $CopyWithPlaceholder()
          ? _value.after
          // ignore: cast_nullable_to_non_nullable
          : after as String?,
    );
  }
}

extension $SocialSettingsCompareDefaultWindowCopyWith
    on SocialSettingsCompareDefaultWindow {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialSettingsCompareDefaultWindow.copyWith(...)` or `instanceOfSocialSettingsCompareDefaultWindow.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialSettingsCompareDefaultWindowCWProxy get copyWith =>
      _$SocialSettingsCompareDefaultWindowCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialSettingsCompareDefaultWindow _$SocialSettingsCompareDefaultWindowFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialSettingsCompareDefaultWindow', json, (
  $checkedConvert,
) {
  final val = SocialSettingsCompareDefaultWindow(
    before: $checkedConvert('before', (v) => v as String?),
    after: $checkedConvert('after', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$SocialSettingsCompareDefaultWindowToJson(
  SocialSettingsCompareDefaultWindow instance,
) => <String, dynamic>{'before': ?instance.before, 'after': ?instance.after};
