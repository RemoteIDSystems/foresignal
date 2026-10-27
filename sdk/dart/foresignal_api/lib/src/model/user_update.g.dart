// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_update.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UserUpdateCWProxy {
  UserUpdate fullName(String? fullName);

  UserUpdate email(String? email);

  UserUpdate dataRestriction(UserDataRestriction? dataRestriction);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  UserUpdate call({
    String? fullName,
    String? email,
    UserDataRestriction? dataRestriction,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUserUpdate.copyWith(...)` or call `instanceOfUserUpdate.copyWith.fieldName(value)` for a single field.
class _$UserUpdateCWProxyImpl implements _$UserUpdateCWProxy {
  const _$UserUpdateCWProxyImpl(this._value);

  final UserUpdate _value;

  @override
  UserUpdate fullName(String? fullName) => call(fullName: fullName);

  @override
  UserUpdate email(String? email) => call(email: email);

  @override
  UserUpdate dataRestriction(UserDataRestriction? dataRestriction) =>
      call(dataRestriction: dataRestriction);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  UserUpdate call({
    Object? fullName = const $CopyWithPlaceholder(),
    Object? email = const $CopyWithPlaceholder(),
    Object? dataRestriction = const $CopyWithPlaceholder(),
  }) {
    return UserUpdate(
      fullName: fullName == const $CopyWithPlaceholder()
          ? _value.fullName
          // ignore: cast_nullable_to_non_nullable
          : fullName as String?,
      email: email == const $CopyWithPlaceholder()
          ? _value.email
          // ignore: cast_nullable_to_non_nullable
          : email as String?,
      dataRestriction: dataRestriction == const $CopyWithPlaceholder()
          ? _value.dataRestriction
          // ignore: cast_nullable_to_non_nullable
          : dataRestriction as UserDataRestriction?,
    );
  }
}

extension $UserUpdateCopyWith on UserUpdate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUserUpdate.copyWith(...)` or `instanceOfUserUpdate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UserUpdateCWProxy get copyWith => _$UserUpdateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserUpdate _$UserUpdateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('UserUpdate', json, ($checkedConvert) {
      final val = UserUpdate(
        fullName: $checkedConvert('fullName', (v) => v as String?),
        email: $checkedConvert('email', (v) => v as String?),
        dataRestriction: $checkedConvert(
          'dataRestriction',
          (v) => v == null
              ? null
              : UserDataRestriction.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$UserUpdateToJson(UserUpdate instance) =>
    <String, dynamic>{
      'fullName': ?instance.fullName,
      'email': ?instance.email,
      'dataRestriction': ?instance.dataRestriction?.toJson(),
    };
