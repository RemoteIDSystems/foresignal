// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'principal_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrincipalPatchCWProxy {
  PrincipalPatch displayName(String? displayName);

  PrincipalPatch email(String? email);

  PrincipalPatch avatarMediaId(String? avatarMediaId);

  PrincipalPatch timezone(String? timezone);

  PrincipalPatch organization(String? organization);

  PrincipalPatch disabled(bool? disabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrincipalPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrincipalPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  PrincipalPatch call({
    String? displayName,
    String? email,
    String? avatarMediaId,
    String? timezone,
    String? organization,
    bool? disabled,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrincipalPatch.copyWith(...)` or call `instanceOfPrincipalPatch.copyWith.fieldName(value)` for a single field.
class _$PrincipalPatchCWProxyImpl implements _$PrincipalPatchCWProxy {
  const _$PrincipalPatchCWProxyImpl(this._value);

  final PrincipalPatch _value;

  @override
  PrincipalPatch displayName(String? displayName) =>
      call(displayName: displayName);

  @override
  PrincipalPatch email(String? email) => call(email: email);

  @override
  PrincipalPatch avatarMediaId(String? avatarMediaId) =>
      call(avatarMediaId: avatarMediaId);

  @override
  PrincipalPatch timezone(String? timezone) => call(timezone: timezone);

  @override
  PrincipalPatch organization(String? organization) =>
      call(organization: organization);

  @override
  PrincipalPatch disabled(bool? disabled) => call(disabled: disabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrincipalPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrincipalPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrincipalPatch call({
    Object? displayName = const $CopyWithPlaceholder(),
    Object? email = const $CopyWithPlaceholder(),
    Object? avatarMediaId = const $CopyWithPlaceholder(),
    Object? timezone = const $CopyWithPlaceholder(),
    Object? organization = const $CopyWithPlaceholder(),
    Object? disabled = const $CopyWithPlaceholder(),
  }) {
    return PrincipalPatch(
      displayName: displayName == const $CopyWithPlaceholder()
          ? _value.displayName
          // ignore: cast_nullable_to_non_nullable
          : displayName as String?,
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
      disabled: disabled == const $CopyWithPlaceholder()
          ? _value.disabled
          // ignore: cast_nullable_to_non_nullable
          : disabled as bool?,
    );
  }
}

extension $PrincipalPatchCopyWith on PrincipalPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrincipalPatch.copyWith(...)` or `instanceOfPrincipalPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrincipalPatchCWProxy get copyWith => _$PrincipalPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrincipalPatch _$PrincipalPatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PrincipalPatch', json, ($checkedConvert) {
      final val = PrincipalPatch(
        displayName: $checkedConvert('displayName', (v) => v as String?),
        email: $checkedConvert('email', (v) => v as String?),
        avatarMediaId: $checkedConvert('avatarMediaId', (v) => v as String?),
        timezone: $checkedConvert('timezone', (v) => v as String?),
        organization: $checkedConvert('organization', (v) => v as String?),
        disabled: $checkedConvert('disabled', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$PrincipalPatchToJson(PrincipalPatch instance) =>
    <String, dynamic>{
      'displayName': ?instance.displayName,
      'email': ?instance.email,
      'avatarMediaId': ?instance.avatarMediaId,
      'timezone': ?instance.timezone,
      'organization': ?instance.organization,
      'disabled': ?instance.disabled,
    };
