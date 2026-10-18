// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'registration_token_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegistrationTokenInputCWProxy {
  RegistrationTokenInput name(String name);

  RegistrationTokenInput expiresAt(DateTime? expiresAt);

  RegistrationTokenInput maxUses(int? maxUses);

  RegistrationTokenInput sourceIdPrefix(String? sourceIdPrefix);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegistrationTokenInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegistrationTokenInput(...).copyWith(id: 12, name: "My name")
  /// ```
  RegistrationTokenInput call({
    String name,
    DateTime? expiresAt,
    int? maxUses,
    String? sourceIdPrefix,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegistrationTokenInput.copyWith(...)` or call `instanceOfRegistrationTokenInput.copyWith.fieldName(value)` for a single field.
class _$RegistrationTokenInputCWProxyImpl
    implements _$RegistrationTokenInputCWProxy {
  const _$RegistrationTokenInputCWProxyImpl(this._value);

  final RegistrationTokenInput _value;

  @override
  RegistrationTokenInput name(String name) => call(name: name);

  @override
  RegistrationTokenInput expiresAt(DateTime? expiresAt) =>
      call(expiresAt: expiresAt);

  @override
  RegistrationTokenInput maxUses(int? maxUses) => call(maxUses: maxUses);

  @override
  RegistrationTokenInput sourceIdPrefix(String? sourceIdPrefix) =>
      call(sourceIdPrefix: sourceIdPrefix);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegistrationTokenInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegistrationTokenInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegistrationTokenInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? maxUses = const $CopyWithPlaceholder(),
    Object? sourceIdPrefix = const $CopyWithPlaceholder(),
  }) {
    return RegistrationTokenInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
      maxUses: maxUses == const $CopyWithPlaceholder()
          ? _value.maxUses
          // ignore: cast_nullable_to_non_nullable
          : maxUses as int?,
      sourceIdPrefix: sourceIdPrefix == const $CopyWithPlaceholder()
          ? _value.sourceIdPrefix
          // ignore: cast_nullable_to_non_nullable
          : sourceIdPrefix as String?,
    );
  }
}

extension $RegistrationTokenInputCopyWith on RegistrationTokenInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegistrationTokenInput.copyWith(...)` or `instanceOfRegistrationTokenInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegistrationTokenInputCWProxy get copyWith =>
      _$RegistrationTokenInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegistrationTokenInput _$RegistrationTokenInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RegistrationTokenInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name']);
  final val = RegistrationTokenInput(
    name: $checkedConvert('name', (v) => v as String),
    expiresAt: $checkedConvert(
      'expiresAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    maxUses: $checkedConvert('maxUses', (v) => (v as num?)?.toInt() ?? 1),
    sourceIdPrefix: $checkedConvert('sourceIdPrefix', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$RegistrationTokenInputToJson(
  RegistrationTokenInput instance,
) => <String, dynamic>{
  'name': instance.name,
  'expiresAt': ?instance.expiresAt?.toIso8601String(),
  'maxUses': ?instance.maxUses,
  'sourceIdPrefix': ?instance.sourceIdPrefix,
};
