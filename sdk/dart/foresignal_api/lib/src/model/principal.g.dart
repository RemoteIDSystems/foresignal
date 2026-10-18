// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'principal.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrincipalCWProxy {
  Principal id(String id);

  Principal handle(String handle);

  Principal displayName(String displayName);

  Principal kind(PrincipalKindEnum kind);

  Principal email(String? email);

  Principal avatarMediaId(String? avatarMediaId);

  Principal timezone(String? timezone);

  Principal organization(String? organization);

  Principal disabled(bool disabled);

  Principal tokenCount(int? tokenCount);

  Principal createdAt(DateTime createdAt);

  Principal updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Principal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Principal(...).copyWith(id: 12, name: "My name")
  /// ```
  Principal call({
    String id,
    String handle,
    String displayName,
    PrincipalKindEnum kind,
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
/// Use as `instanceOfPrincipal.copyWith(...)` or call `instanceOfPrincipal.copyWith.fieldName(value)` for a single field.
class _$PrincipalCWProxyImpl implements _$PrincipalCWProxy {
  const _$PrincipalCWProxyImpl(this._value);

  final Principal _value;

  @override
  Principal id(String id) => call(id: id);

  @override
  Principal handle(String handle) => call(handle: handle);

  @override
  Principal displayName(String displayName) => call(displayName: displayName);

  @override
  Principal kind(PrincipalKindEnum kind) => call(kind: kind);

  @override
  Principal email(String? email) => call(email: email);

  @override
  Principal avatarMediaId(String? avatarMediaId) =>
      call(avatarMediaId: avatarMediaId);

  @override
  Principal timezone(String? timezone) => call(timezone: timezone);

  @override
  Principal organization(String? organization) =>
      call(organization: organization);

  @override
  Principal disabled(bool disabled) => call(disabled: disabled);

  @override
  Principal tokenCount(int? tokenCount) => call(tokenCount: tokenCount);

  @override
  Principal createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Principal updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Principal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Principal(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Principal call({
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
    return Principal(
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
          : kind as PrincipalKindEnum,
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

extension $PrincipalCopyWith on Principal {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrincipal.copyWith(...)` or `instanceOfPrincipal.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrincipalCWProxy get copyWith => _$PrincipalCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Principal _$PrincipalFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Principal', json, ($checkedConvert) {
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
      final val = Principal(
        id: $checkedConvert('id', (v) => v as String),
        handle: $checkedConvert('handle', (v) => v as String),
        displayName: $checkedConvert('displayName', (v) => v as String),
        kind: $checkedConvert(
          'kind',
          (v) => $enumDecode(_$PrincipalKindEnumEnumMap, v),
        ),
        email: $checkedConvert('email', (v) => v as String?),
        avatarMediaId: $checkedConvert('avatarMediaId', (v) => v as String?),
        timezone: $checkedConvert('timezone', (v) => v as String?),
        organization: $checkedConvert('organization', (v) => v as String?),
        disabled: $checkedConvert('disabled', (v) => v as bool),
        tokenCount: $checkedConvert('tokenCount', (v) => (v as num?)?.toInt()),
        createdAt: $checkedConvert(
          'createdAt',
          (v) => DateTime.parse(v as String),
        ),
        updatedAt: $checkedConvert(
          'updatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PrincipalToJson(Principal instance) => <String, dynamic>{
  'id': instance.id,
  'handle': instance.handle,
  'displayName': instance.displayName,
  'kind': _$PrincipalKindEnumEnumMap[instance.kind]!,
  'email': ?instance.email,
  'avatarMediaId': ?instance.avatarMediaId,
  'timezone': ?instance.timezone,
  'organization': ?instance.organization,
  'disabled': instance.disabled,
  'tokenCount': ?instance.tokenCount,
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
};

const _$PrincipalKindEnumEnumMap = {
  PrincipalKindEnum.person: 'person',
  PrincipalKindEnum.agent: 'agent',
  PrincipalKindEnum.service: 'service',
};
