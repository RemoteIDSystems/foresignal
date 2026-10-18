// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pairing_code_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PairingCodeInputCWProxy {
  PairingCodeInput name(String name);

  PairingCodeInput scope(PairingCodeInputScopeEnum scope);

  PairingCodeInput restrictions(ReadRestriction? restrictions);

  PairingCodeInput ttl(String? ttl);

  PairingCodeInput principalId(String? principalId);

  PairingCodeInput privateAccess(bool? privateAccess);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PairingCodeInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PairingCodeInput(...).copyWith(id: 12, name: "My name")
  /// ```
  PairingCodeInput call({
    String name,
    PairingCodeInputScopeEnum scope,
    ReadRestriction? restrictions,
    String? ttl,
    String? principalId,
    bool? privateAccess,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPairingCodeInput.copyWith(...)` or call `instanceOfPairingCodeInput.copyWith.fieldName(value)` for a single field.
class _$PairingCodeInputCWProxyImpl implements _$PairingCodeInputCWProxy {
  const _$PairingCodeInputCWProxyImpl(this._value);

  final PairingCodeInput _value;

  @override
  PairingCodeInput name(String name) => call(name: name);

  @override
  PairingCodeInput scope(PairingCodeInputScopeEnum scope) => call(scope: scope);

  @override
  PairingCodeInput restrictions(ReadRestriction? restrictions) =>
      call(restrictions: restrictions);

  @override
  PairingCodeInput ttl(String? ttl) => call(ttl: ttl);

  @override
  PairingCodeInput principalId(String? principalId) =>
      call(principalId: principalId);

  @override
  PairingCodeInput privateAccess(bool? privateAccess) =>
      call(privateAccess: privateAccess);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PairingCodeInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PairingCodeInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PairingCodeInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
    Object? restrictions = const $CopyWithPlaceholder(),
    Object? ttl = const $CopyWithPlaceholder(),
    Object? principalId = const $CopyWithPlaceholder(),
    Object? privateAccess = const $CopyWithPlaceholder(),
  }) {
    return PairingCodeInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      scope: scope == const $CopyWithPlaceholder() || scope == null
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as PairingCodeInputScopeEnum,
      restrictions: restrictions == const $CopyWithPlaceholder()
          ? _value.restrictions
          // ignore: cast_nullable_to_non_nullable
          : restrictions as ReadRestriction?,
      ttl: ttl == const $CopyWithPlaceholder()
          ? _value.ttl
          // ignore: cast_nullable_to_non_nullable
          : ttl as String?,
      principalId: principalId == const $CopyWithPlaceholder()
          ? _value.principalId
          // ignore: cast_nullable_to_non_nullable
          : principalId as String?,
      privateAccess: privateAccess == const $CopyWithPlaceholder()
          ? _value.privateAccess
          // ignore: cast_nullable_to_non_nullable
          : privateAccess as bool?,
    );
  }
}

extension $PairingCodeInputCopyWith on PairingCodeInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPairingCodeInput.copyWith(...)` or `instanceOfPairingCodeInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PairingCodeInputCWProxy get copyWith => _$PairingCodeInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PairingCodeInput _$PairingCodeInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PairingCodeInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'scope']);
      final val = PairingCodeInput(
        name: $checkedConvert('name', (v) => v as String),
        scope: $checkedConvert(
          'scope',
          (v) => $enumDecode(_$PairingCodeInputScopeEnumEnumMap, v),
        ),
        restrictions: $checkedConvert(
          'restrictions',
          (v) => v == null
              ? null
              : ReadRestriction.fromJson(v as Map<String, dynamic>),
        ),
        ttl: $checkedConvert('ttl', (v) => v as String?),
        principalId: $checkedConvert('principalId', (v) => v as String?),
        privateAccess: $checkedConvert(
          'privateAccess',
          (v) => v as bool? ?? false,
        ),
      );
      return val;
    });

Map<String, dynamic> _$PairingCodeInputToJson(PairingCodeInput instance) =>
    <String, dynamic>{
      'name': instance.name,
      'scope': _$PairingCodeInputScopeEnumEnumMap[instance.scope]!,
      'restrictions': ?instance.restrictions?.toJson(),
      'ttl': ?instance.ttl,
      'principalId': ?instance.principalId,
      'privateAccess': ?instance.privateAccess,
    };

const _$PairingCodeInputScopeEnumEnumMap = {
  PairingCodeInputScopeEnum.read: 'read',
  PairingCodeInputScopeEnum.agent: 'agent',
};
