// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'get_principal200_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GetPrincipal200ResponseCWProxy {
  GetPrincipal200Response id(String id);

  GetPrincipal200Response handle(String handle);

  GetPrincipal200Response displayName(String displayName);

  GetPrincipal200Response kind(GetPrincipal200ResponseKindEnum kind);

  GetPrincipal200Response email(String? email);

  GetPrincipal200Response avatarMediaId(String? avatarMediaId);

  GetPrincipal200Response timezone(String? timezone);

  GetPrincipal200Response organization(String? organization);

  GetPrincipal200Response disabled(bool disabled);

  GetPrincipal200Response tokenCount(int? tokenCount);

  GetPrincipal200Response createdAt(DateTime createdAt);

  GetPrincipal200Response updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GetPrincipal200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GetPrincipal200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  GetPrincipal200Response call({
    String id,
    String handle,
    String displayName,
    GetPrincipal200ResponseKindEnum kind,
    String? email,
    String? avatarMediaId,
    String? timezone,
    String? organization,
    bool disabled,
    int? tokenCount,
    DateTime createdAt,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGetPrincipal200Response.copyWith(...)` or call `instanceOfGetPrincipal200Response.copyWith.fieldName(value)` for a single field.
class _$GetPrincipal200ResponseCWProxyImpl
    implements _$GetPrincipal200ResponseCWProxy {
  const _$GetPrincipal200ResponseCWProxyImpl(this._value);

  final GetPrincipal200Response _value;

  @override
  GetPrincipal200Response id(String id) => call(id: id);

  @override
  GetPrincipal200Response handle(String handle) => call(handle: handle);

  @override
  GetPrincipal200Response displayName(String displayName) =>
      call(displayName: displayName);

  @override
  GetPrincipal200Response kind(GetPrincipal200ResponseKindEnum kind) =>
      call(kind: kind);

  @override
  GetPrincipal200Response email(String? email) => call(email: email);

  @override
  GetPrincipal200Response avatarMediaId(String? avatarMediaId) =>
      call(avatarMediaId: avatarMediaId);

  @override
  GetPrincipal200Response timezone(String? timezone) =>
      call(timezone: timezone);

  @override
  GetPrincipal200Response organization(String? organization) =>
      call(organization: organization);

  @override
  GetPrincipal200Response disabled(bool disabled) => call(disabled: disabled);

  @override
  GetPrincipal200Response tokenCount(int? tokenCount) =>
      call(tokenCount: tokenCount);

  @override
  GetPrincipal200Response createdAt(DateTime createdAt) =>
      call(createdAt: createdAt);

  @override
  GetPrincipal200Response updatedAt(DateTime? updatedAt) =>
      call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GetPrincipal200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GetPrincipal200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GetPrincipal200Response call({
    Object? id = const $CopyWithPlaceholder(),
    Object? handle = const $CopyWithPlaceholder(),
    Object? displayName = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? email = const $CopyWithPlaceholder(),
    Object? avatarMediaId = const $CopyWithPlaceholder(),
    Object? timezone = const $CopyWithPlaceholder(),
    Object? organization = const $CopyWithPlaceholder(),
    Object? disabled = const $CopyWithPlaceholder(),
    Object? tokenCount = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return GetPrincipal200Response(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
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
          : kind as GetPrincipal200ResponseKindEnum,
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
      disabled: disabled == const $CopyWithPlaceholder() || disabled == null
          ? _value.disabled
          // ignore: cast_nullable_to_non_nullable
          : disabled as bool,
      tokenCount: tokenCount == const $CopyWithPlaceholder()
          ? _value.tokenCount
          // ignore: cast_nullable_to_non_nullable
          : tokenCount as int?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $GetPrincipal200ResponseCopyWith on GetPrincipal200Response {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGetPrincipal200Response.copyWith(...)` or `instanceOfGetPrincipal200Response.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GetPrincipal200ResponseCWProxy get copyWith =>
      _$GetPrincipal200ResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GetPrincipal200Response _$GetPrincipal200ResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('GetPrincipal200Response', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'handle',
      'displayName',
      'kind',
      'disabled',
      'createdAt',
    ],
  );
  final val = GetPrincipal200Response(
    id: $checkedConvert('id', (v) => v as String),
    handle: $checkedConvert('handle', (v) => v as String),
    displayName: $checkedConvert('displayName', (v) => v as String),
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecode(_$GetPrincipal200ResponseKindEnumEnumMap, v),
    ),
    email: $checkedConvert('email', (v) => v as String?),
    avatarMediaId: $checkedConvert('avatarMediaId', (v) => v as String?),
    timezone: $checkedConvert('timezone', (v) => v as String?),
    organization: $checkedConvert('organization', (v) => v as String?),
    disabled: $checkedConvert('disabled', (v) => v as bool),
    tokenCount: $checkedConvert('tokenCount', (v) => (v as num?)?.toInt()),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$GetPrincipal200ResponseToJson(
  GetPrincipal200Response instance,
) => <String, dynamic>{
  'id': instance.id,
  'handle': instance.handle,
  'displayName': instance.displayName,
  'kind': _$GetPrincipal200ResponseKindEnumEnumMap[instance.kind]!,
  'email': ?instance.email,
  'avatarMediaId': ?instance.avatarMediaId,
  'timezone': ?instance.timezone,
  'organization': ?instance.organization,
  'disabled': instance.disabled,
  'tokenCount': ?instance.tokenCount,
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
};

const _$GetPrincipal200ResponseKindEnumEnumMap = {
  GetPrincipal200ResponseKindEnum.person: 'person',
  GetPrincipal200ResponseKindEnum.agent: 'agent',
  GetPrincipal200ResponseKindEnum.service: 'service',
};
