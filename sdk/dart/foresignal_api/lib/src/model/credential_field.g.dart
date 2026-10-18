// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'credential_field.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CredentialFieldCWProxy {
  CredentialField name(String name);

  CredentialField description(String? description);

  CredentialField required_(bool required_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CredentialField(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CredentialField(...).copyWith(id: 12, name: "My name")
  /// ```
  CredentialField call({String name, String? description, bool required_});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCredentialField.copyWith(...)` or call `instanceOfCredentialField.copyWith.fieldName(value)` for a single field.
class _$CredentialFieldCWProxyImpl implements _$CredentialFieldCWProxy {
  const _$CredentialFieldCWProxyImpl(this._value);

  final CredentialField _value;

  @override
  CredentialField name(String name) => call(name: name);

  @override
  CredentialField description(String? description) =>
      call(description: description);

  @override
  CredentialField required_(bool required_) => call(required_: required_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CredentialField(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CredentialField(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CredentialField call({
    Object? name = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? required_ = const $CopyWithPlaceholder(),
  }) {
    return CredentialField(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      required_: required_ == const $CopyWithPlaceholder() || required_ == null
          ? _value.required_
          // ignore: cast_nullable_to_non_nullable
          : required_ as bool,
    );
  }
}

extension $CredentialFieldCopyWith on CredentialField {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCredentialField.copyWith(...)` or `instanceOfCredentialField.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CredentialFieldCWProxy get copyWith => _$CredentialFieldCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CredentialField _$CredentialFieldFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CredentialField', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'required']);
      final val = CredentialField(
        name: $checkedConvert('name', (v) => v as String),
        description: $checkedConvert('description', (v) => v as String?),
        required_: $checkedConvert('required', (v) => v as bool),
      );
      return val;
    }, fieldKeyMap: const {'required_': 'required'});

Map<String, dynamic> _$CredentialFieldToJson(CredentialField instance) =>
    <String, dynamic>{
      'name': instance.name,
      'description': ?instance.description,
      'required': instance.required_,
    };
