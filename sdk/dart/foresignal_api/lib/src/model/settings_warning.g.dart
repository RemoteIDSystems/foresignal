// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'settings_warning.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SettingsWarningCWProxy {
  SettingsWarning code(String code);

  SettingsWarning message(String message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SettingsWarning(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SettingsWarning(...).copyWith(id: 12, name: "My name")
  /// ```
  SettingsWarning call({String code, String message});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSettingsWarning.copyWith(...)` or call `instanceOfSettingsWarning.copyWith.fieldName(value)` for a single field.
class _$SettingsWarningCWProxyImpl implements _$SettingsWarningCWProxy {
  const _$SettingsWarningCWProxyImpl(this._value);

  final SettingsWarning _value;

  @override
  SettingsWarning code(String code) => call(code: code);

  @override
  SettingsWarning message(String message) => call(message: message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SettingsWarning(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SettingsWarning(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SettingsWarning call({
    Object? code = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
  }) {
    return SettingsWarning(
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as String,
      message: message == const $CopyWithPlaceholder() || message == null
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String,
    );
  }
}

extension $SettingsWarningCopyWith on SettingsWarning {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSettingsWarning.copyWith(...)` or `instanceOfSettingsWarning.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SettingsWarningCWProxy get copyWith => _$SettingsWarningCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SettingsWarning _$SettingsWarningFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SettingsWarning', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['code', 'message']);
      final val = SettingsWarning(
        code: $checkedConvert('code', (v) => v as String),
        message: $checkedConvert('message', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$SettingsWarningToJson(SettingsWarning instance) =>
    <String, dynamic>{'code': instance.code, 'message': instance.message};
