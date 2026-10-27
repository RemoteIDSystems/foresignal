// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_source_pref.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UserSourcePrefCWProxy {
  UserSourcePref sourceId(String sourceId);

  UserSourcePref enabled(bool enabled);

  UserSourcePref displayName(String? displayName);

  UserSourcePref color(String? color);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserSourcePref(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserSourcePref(...).copyWith(id: 12, name: "My name")
  /// ```
  UserSourcePref call({
    String sourceId,
    bool enabled,
    String? displayName,
    String? color,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUserSourcePref.copyWith(...)` or call `instanceOfUserSourcePref.copyWith.fieldName(value)` for a single field.
class _$UserSourcePrefCWProxyImpl implements _$UserSourcePrefCWProxy {
  const _$UserSourcePrefCWProxyImpl(this._value);

  final UserSourcePref _value;

  @override
  UserSourcePref sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  UserSourcePref enabled(bool enabled) => call(enabled: enabled);

  @override
  UserSourcePref displayName(String? displayName) =>
      call(displayName: displayName);

  @override
  UserSourcePref color(String? color) => call(color: color);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserSourcePref(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserSourcePref(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  UserSourcePref call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? displayName = const $CopyWithPlaceholder(),
    Object? color = const $CopyWithPlaceholder(),
  }) {
    return UserSourcePref(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      displayName: displayName == const $CopyWithPlaceholder()
          ? _value.displayName
          // ignore: cast_nullable_to_non_nullable
          : displayName as String?,
      color: color == const $CopyWithPlaceholder()
          ? _value.color
          // ignore: cast_nullable_to_non_nullable
          : color as String?,
    );
  }
}

extension $UserSourcePrefCopyWith on UserSourcePref {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUserSourcePref.copyWith(...)` or `instanceOfUserSourcePref.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UserSourcePrefCWProxy get copyWith => _$UserSourcePrefCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserSourcePref _$UserSourcePrefFromJson(Map<String, dynamic> json) =>
    $checkedCreate('UserSourcePref', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['sourceId', 'enabled']);
      final val = UserSourcePref(
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        enabled: $checkedConvert('enabled', (v) => v as bool),
        displayName: $checkedConvert('displayName', (v) => v as String?),
        color: $checkedConvert('color', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$UserSourcePrefToJson(UserSourcePref instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'enabled': instance.enabled,
      'displayName': ?instance.displayName,
      'color': ?instance.color,
    };
