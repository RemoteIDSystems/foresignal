// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_security.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoSecurityCWProxy {
  SystemInfoSecurity openReads(bool? openReads);

  SystemInfoSecurity anonymousRestricted(bool? anonymousRestricted);

  SystemInfoSecurity warnings(List<SettingsWarning>? warnings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoSecurity(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoSecurity(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoSecurity call({
    bool? openReads,
    bool? anonymousRestricted,
    List<SettingsWarning>? warnings,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoSecurity.copyWith(...)` or call `instanceOfSystemInfoSecurity.copyWith.fieldName(value)` for a single field.
class _$SystemInfoSecurityCWProxyImpl implements _$SystemInfoSecurityCWProxy {
  const _$SystemInfoSecurityCWProxyImpl(this._value);

  final SystemInfoSecurity _value;

  @override
  SystemInfoSecurity openReads(bool? openReads) => call(openReads: openReads);

  @override
  SystemInfoSecurity anonymousRestricted(bool? anonymousRestricted) =>
      call(anonymousRestricted: anonymousRestricted);

  @override
  SystemInfoSecurity warnings(List<SettingsWarning>? warnings) =>
      call(warnings: warnings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoSecurity(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoSecurity(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoSecurity call({
    Object? openReads = const $CopyWithPlaceholder(),
    Object? anonymousRestricted = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoSecurity(
      openReads: openReads == const $CopyWithPlaceholder()
          ? _value.openReads
          // ignore: cast_nullable_to_non_nullable
          : openReads as bool?,
      anonymousRestricted: anonymousRestricted == const $CopyWithPlaceholder()
          ? _value.anonymousRestricted
          // ignore: cast_nullable_to_non_nullable
          : anonymousRestricted as bool?,
      warnings: warnings == const $CopyWithPlaceholder()
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as List<SettingsWarning>?,
    );
  }
}

extension $SystemInfoSecurityCopyWith on SystemInfoSecurity {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoSecurity.copyWith(...)` or `instanceOfSystemInfoSecurity.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoSecurityCWProxy get copyWith =>
      _$SystemInfoSecurityCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoSecurity _$SystemInfoSecurityFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SystemInfoSecurity', json, ($checkedConvert) {
      final val = SystemInfoSecurity(
        openReads: $checkedConvert('openReads', (v) => v as bool?),
        anonymousRestricted: $checkedConvert(
          'anonymousRestricted',
          (v) => v as bool?,
        ),
        warnings: $checkedConvert(
          'warnings',
          (v) => (v as List<dynamic>?)
              ?.map((e) => SettingsWarning.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SystemInfoSecurityToJson(SystemInfoSecurity instance) =>
    <String, dynamic>{
      'openReads': ?instance.openReads,
      'anonymousRestricted': ?instance.anonymousRestricted,
      'warnings': ?instance.warnings?.map((e) => e.toJson()).toList(),
    };
