// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_warning.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingWarningCWProxy {
  MappingWarning code(String code);

  MappingWarning message(String message);

  MappingWarning path(String? path);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingWarning(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingWarning(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingWarning call({String code, String message, String? path});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingWarning.copyWith(...)` or call `instanceOfMappingWarning.copyWith.fieldName(value)` for a single field.
class _$MappingWarningCWProxyImpl implements _$MappingWarningCWProxy {
  const _$MappingWarningCWProxyImpl(this._value);

  final MappingWarning _value;

  @override
  MappingWarning code(String code) => call(code: code);

  @override
  MappingWarning message(String message) => call(message: message);

  @override
  MappingWarning path(String? path) => call(path: path);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingWarning(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingWarning(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingWarning call({
    Object? code = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? path = const $CopyWithPlaceholder(),
  }) {
    return MappingWarning(
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as String,
      message: message == const $CopyWithPlaceholder() || message == null
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String,
      path: path == const $CopyWithPlaceholder()
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String?,
    );
  }
}

extension $MappingWarningCopyWith on MappingWarning {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingWarning.copyWith(...)` or `instanceOfMappingWarning.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingWarningCWProxy get copyWith => _$MappingWarningCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingWarning _$MappingWarningFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MappingWarning', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['code', 'message']);
      final val = MappingWarning(
        code: $checkedConvert('code', (v) => v as String),
        message: $checkedConvert('message', (v) => v as String),
        path: $checkedConvert('path', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$MappingWarningToJson(MappingWarning instance) =>
    <String, dynamic>{
      'code': instance.code,
      'message': instance.message,
      'path': ?instance.path,
    };
