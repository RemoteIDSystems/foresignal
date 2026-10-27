// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UserCWProxy {
  User id(String id);

  User email(String email);

  User fullName(String fullName);

  User status(String status);

  User roles(List<RoleRef> roles);

  User mfaEnabled(bool? mfaEnabled);

  User createdBy(String? createdBy);

  User createdAt(DateTime? createdAt);

  User updatedAt(DateTime? updatedAt);

  User lastLoginAt(DateTime? lastLoginAt);

  User activation(UserLinkIssued? activation);

  User dataRestriction(UserDataRestriction? dataRestriction);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `User(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// User(...).copyWith(id: 12, name: "My name")
  /// ```
  User call({
    String id,
    String email,
    String fullName,
    String status,
    List<RoleRef> roles,
    bool? mfaEnabled,
    String? createdBy,
    DateTime? createdAt,
    DateTime? updatedAt,
    DateTime? lastLoginAt,
    UserLinkIssued? activation,
    UserDataRestriction? dataRestriction,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUser.copyWith(...)` or call `instanceOfUser.copyWith.fieldName(value)` for a single field.
class _$UserCWProxyImpl implements _$UserCWProxy {
  const _$UserCWProxyImpl(this._value);

  final User _value;

  @override
  User id(String id) => call(id: id);

  @override
  User email(String email) => call(email: email);

  @override
  User fullName(String fullName) => call(fullName: fullName);

  @override
  User status(String status) => call(status: status);

  @override
  User roles(List<RoleRef> roles) => call(roles: roles);

  @override
  User mfaEnabled(bool? mfaEnabled) => call(mfaEnabled: mfaEnabled);

  @override
  User createdBy(String? createdBy) => call(createdBy: createdBy);

  @override
  User createdAt(DateTime? createdAt) => call(createdAt: createdAt);

  @override
  User updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  @override
  User lastLoginAt(DateTime? lastLoginAt) => call(lastLoginAt: lastLoginAt);

  @override
  User activation(UserLinkIssued? activation) => call(activation: activation);

  @override
  User dataRestriction(UserDataRestriction? dataRestriction) =>
      call(dataRestriction: dataRestriction);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `User(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// User(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  User call({
    Object? id = const $CopyWithPlaceholder(),
    Object? email = const $CopyWithPlaceholder(),
    Object? fullName = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? roles = const $CopyWithPlaceholder(),
    Object? mfaEnabled = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? lastLoginAt = const $CopyWithPlaceholder(),
    Object? activation = const $CopyWithPlaceholder(),
    Object? dataRestriction = const $CopyWithPlaceholder(),
  }) {
    return User(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      email: email == const $CopyWithPlaceholder() || email == null
          ? _value.email
          // ignore: cast_nullable_to_non_nullable
          : email as String,
      fullName: fullName == const $CopyWithPlaceholder() || fullName == null
          ? _value.fullName
          // ignore: cast_nullable_to_non_nullable
          : fullName as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as String,
      roles: roles == const $CopyWithPlaceholder() || roles == null
          ? _value.roles
          // ignore: cast_nullable_to_non_nullable
          : roles as List<RoleRef>,
      mfaEnabled: mfaEnabled == const $CopyWithPlaceholder()
          ? _value.mfaEnabled
          // ignore: cast_nullable_to_non_nullable
          : mfaEnabled as bool?,
      createdBy: createdBy == const $CopyWithPlaceholder()
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String?,
      createdAt: createdAt == const $CopyWithPlaceholder()
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      lastLoginAt: lastLoginAt == const $CopyWithPlaceholder()
          ? _value.lastLoginAt
          // ignore: cast_nullable_to_non_nullable
          : lastLoginAt as DateTime?,
      activation: activation == const $CopyWithPlaceholder()
          ? _value.activation
          // ignore: cast_nullable_to_non_nullable
          : activation as UserLinkIssued?,
      dataRestriction: dataRestriction == const $CopyWithPlaceholder()
          ? _value.dataRestriction
          // ignore: cast_nullable_to_non_nullable
          : dataRestriction as UserDataRestriction?,
    );
  }
}

extension $UserCopyWith on User {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUser.copyWith(...)` or `instanceOfUser.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UserCWProxy get copyWith => _$UserCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

User _$UserFromJson(Map<String, dynamic> json) => $checkedCreate('User', json, (
  $checkedConvert,
) {
  $checkKeys(
    json,
    requiredKeys: const ['id', 'email', 'fullName', 'status', 'roles'],
  );
  final val = User(
    id: $checkedConvert('id', (v) => v as String),
    email: $checkedConvert('email', (v) => v as String),
    fullName: $checkedConvert('fullName', (v) => v as String),
    status: $checkedConvert('status', (v) => v as String),
    roles: $checkedConvert(
      'roles',
      (v) => (v as List<dynamic>)
          .map((e) => RoleRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    mfaEnabled: $checkedConvert('mfaEnabled', (v) => v as bool? ?? false),
    createdBy: $checkedConvert('createdBy', (v) => v as String?),
    createdAt: $checkedConvert(
      'createdAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastLoginAt: $checkedConvert(
      'lastLoginAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    activation: $checkedConvert(
      'activation',
      (v) =>
          v == null ? null : UserLinkIssued.fromJson(v as Map<String, dynamic>),
    ),
    dataRestriction: $checkedConvert(
      'dataRestriction',
      (v) => v == null
          ? null
          : UserDataRestriction.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$UserToJson(User instance) => <String, dynamic>{
  'id': instance.id,
  'email': instance.email,
  'fullName': instance.fullName,
  'status': instance.status,
  'roles': instance.roles.map((e) => e.toJson()).toList(),
  'mfaEnabled': ?instance.mfaEnabled,
  'createdBy': ?instance.createdBy,
  'createdAt': ?instance.createdAt?.toIso8601String(),
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'lastLoginAt': ?instance.lastLoginAt?.toIso8601String(),
  'activation': ?instance.activation?.toJson(),
  'dataRestriction': ?instance.dataRestriction?.toJson(),
};
