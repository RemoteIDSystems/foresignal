// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cors_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CorsSettingsCWProxy {
  CorsSettings readOrigins(List<String> readOrigins);

  CorsSettings writeOrigins(List<String> writeOrigins);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorsSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorsSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  CorsSettings call({List<String> readOrigins, List<String> writeOrigins});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCorsSettings.copyWith(...)` or call `instanceOfCorsSettings.copyWith.fieldName(value)` for a single field.
class _$CorsSettingsCWProxyImpl implements _$CorsSettingsCWProxy {
  const _$CorsSettingsCWProxyImpl(this._value);

  final CorsSettings _value;

  @override
  CorsSettings readOrigins(List<String> readOrigins) =>
      call(readOrigins: readOrigins);

  @override
  CorsSettings writeOrigins(List<String> writeOrigins) =>
      call(writeOrigins: writeOrigins);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorsSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorsSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CorsSettings call({
    Object? readOrigins = const $CopyWithPlaceholder(),
    Object? writeOrigins = const $CopyWithPlaceholder(),
  }) {
    return CorsSettings(
      readOrigins:
          readOrigins == const $CopyWithPlaceholder() || readOrigins == null
          ? _value.readOrigins
          // ignore: cast_nullable_to_non_nullable
          : readOrigins as List<String>,
      writeOrigins:
          writeOrigins == const $CopyWithPlaceholder() || writeOrigins == null
          ? _value.writeOrigins
          // ignore: cast_nullable_to_non_nullable
          : writeOrigins as List<String>,
    );
  }
}

extension $CorsSettingsCopyWith on CorsSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCorsSettings.copyWith(...)` or `instanceOfCorsSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CorsSettingsCWProxy get copyWith => _$CorsSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CorsSettings _$CorsSettingsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CorsSettings', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['readOrigins', 'writeOrigins']);
      final val = CorsSettings(
        readOrigins: $checkedConvert(
          'readOrigins',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        writeOrigins: $checkedConvert(
          'writeOrigins',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CorsSettingsToJson(CorsSettings instance) =>
    <String, dynamic>{
      'readOrigins': instance.readOrigins,
      'writeOrigins': instance.writeOrigins,
    };
