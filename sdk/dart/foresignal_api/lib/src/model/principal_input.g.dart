// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'principal_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrincipalInputCWProxy {
  PrincipalInput handle(String handle);

  PrincipalInput displayName(String displayName);

  PrincipalInput kind(PrincipalInputKindEnum kind);

  PrincipalInput email(String? email);

  PrincipalInput avatarMediaId(String? avatarMediaId);

  PrincipalInput timezone(String? timezone);

  PrincipalInput organization(String? organization);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrincipalInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrincipalInput(...).copyWith(id: 12, name: "My name")
  /// ```
  PrincipalInput call({
    String handle,
    String displayName,
    PrincipalInputKindEnum kind,
    String? email,
    String? avatarMediaId,
    String? timezone,
    String? organization,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrincipalInput.copyWith(...)` or call `instanceOfPrincipalInput.copyWith.fieldName(value)` for a single field.
class _$PrincipalInputCWProxyImpl implements _$PrincipalInputCWProxy {
  const _$PrincipalInputCWProxyImpl(this._value);

  final PrincipalInput _value;

  @override
  PrincipalInput handle(String handle) => call(handle: handle);

  @override
  PrincipalInput displayName(String displayName) =>
      call(displayName: displayName);

  @override
  PrincipalInput kind(PrincipalInputKindEnum kind) => call(kind: kind);

  @override
  PrincipalInput email(String? email) => call(email: email);

  @override
  PrincipalInput avatarMediaId(String? avatarMediaId) =>
      call(avatarMediaId: avatarMediaId);

  @override
  PrincipalInput timezone(String? timezone) => call(timezone: timezone);

  @override
  PrincipalInput organization(String? organization) =>
      call(organization: organization);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrincipalInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrincipalInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrincipalInput call({
    Object? handle = const $CopyWithPlaceholder(),
    Object? displayName = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? email = const $CopyWithPlaceholder(),
    Object? avatarMediaId = const $CopyWithPlaceholder(),
    Object? timezone = const $CopyWithPlaceholder(),
    Object? organization = const $CopyWithPlaceholder(),
  }) {
    return PrincipalInput(
      handle: handle == const $CopyWithPlaceholder() || handle == null
          ? _value.handle
          // ignore: cast_nullable_to_non_nullable
          : handle as String,
      displayName:
          displayName == const $CopyWithPlaceholder() || displayName == null
          ? _value.displayName
          // ignore: cast_nullable_to_non_nullable
          : displayName as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as PrincipalInputKindEnum,
      email: email == const $CopyWithPlaceholder()
          ? _value.email
          // ignore: cast_nullable_to_non_nullable
          : email as String?,
      avatarMediaId: avatarMediaId == const $CopyWithPlaceholder()
          ? _value.avatarMediaId
          // ignore: cast_nullable_to_non_nullable
          : avatarMediaId as String?,
      timezone: timezone == const $CopyWithPlaceholder()
          ? _value.timezone
          // ignore: cast_nullable_to_non_nullable
          : timezone as String?,
      organization: organization == const $CopyWithPlaceholder()
          ? _value.organization
          // ignore: cast_nullable_to_non_nullable
          : organization as String?,
    );
  }
}

extension $PrincipalInputCopyWith on PrincipalInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrincipalInput.copyWith(...)` or `instanceOfPrincipalInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrincipalInputCWProxy get copyWith => _$PrincipalInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrincipalInput _$PrincipalInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PrincipalInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['handle', 'displayName', 'kind']);
      final val = PrincipalInput(
        handle: $checkedConvert('handle', (v) => v as String),
        displayName: $checkedConvert('displayName', (v) => v as String),
        kind: $checkedConvert(
          'kind',
          (v) => $enumDecode(_$PrincipalInputKindEnumEnumMap, v),
        ),
        email: $checkedConvert('email', (v) => v as String?),
        avatarMediaId: $checkedConvert('avatarMediaId', (v) => v as String?),
        timezone: $checkedConvert('timezone', (v) => v as String?),
        organization: $checkedConvert('organization', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PrincipalInputToJson(PrincipalInput instance) =>
    <String, dynamic>{
      'handle': instance.handle,
      'displayName': instance.displayName,
      'kind': _$PrincipalInputKindEnumEnumMap[instance.kind]!,
      'email': ?instance.email,
      'avatarMediaId': ?instance.avatarMediaId,
      'timezone': ?instance.timezone,
      'organization': ?instance.organization,
    };

const _$PrincipalInputKindEnumEnumMap = {
  PrincipalInputKindEnum.person: 'person',
  PrincipalInputKindEnum.agent: 'agent',
  PrincipalInputKindEnum.service: 'service',
};
