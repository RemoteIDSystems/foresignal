// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'credentials_update.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CredentialsUpdateCWProxy {
  CredentialsUpdate values(Map<String, String> values);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CredentialsUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CredentialsUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  CredentialsUpdate call({Map<String, String> values});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCredentialsUpdate.copyWith(...)` or call `instanceOfCredentialsUpdate.copyWith.fieldName(value)` for a single field.
class _$CredentialsUpdateCWProxyImpl implements _$CredentialsUpdateCWProxy {
  const _$CredentialsUpdateCWProxyImpl(this._value);

  final CredentialsUpdate _value;

  @override
  CredentialsUpdate values(Map<String, String> values) => call(values: values);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CredentialsUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CredentialsUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CredentialsUpdate call({Object? values = const $CopyWithPlaceholder()}) {
    return CredentialsUpdate(
      values: values == const $CopyWithPlaceholder() || values == null
          ? _value.values
          // ignore: cast_nullable_to_non_nullable
          : values as Map<String, String>,
    );
  }
}

extension $CredentialsUpdateCopyWith on CredentialsUpdate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCredentialsUpdate.copyWith(...)` or `instanceOfCredentialsUpdate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CredentialsUpdateCWProxy get copyWith =>
      _$CredentialsUpdateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CredentialsUpdate _$CredentialsUpdateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CredentialsUpdate', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['values']);
      final val = CredentialsUpdate(
        values: $checkedConvert(
          'values',
          (v) => Map<String, String>.from(v as Map),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CredentialsUpdateToJson(CredentialsUpdate instance) =>
    <String, dynamic>{'values': instance.values};
