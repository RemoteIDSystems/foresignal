// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'setup_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SetupResultCWProxy {
  SetupResult user(User user);

  SetupResult session(SessionToken? session);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SetupResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SetupResult(...).copyWith(id: 12, name: "My name")
  /// ```
  SetupResult call({User user, SessionToken? session});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSetupResult.copyWith(...)` or call `instanceOfSetupResult.copyWith.fieldName(value)` for a single field.
class _$SetupResultCWProxyImpl implements _$SetupResultCWProxy {
  const _$SetupResultCWProxyImpl(this._value);

  final SetupResult _value;

  @override
  SetupResult user(User user) => call(user: user);

  @override
  SetupResult session(SessionToken? session) => call(session: session);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SetupResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SetupResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SetupResult call({
    Object? user = const $CopyWithPlaceholder(),
    Object? session = const $CopyWithPlaceholder(),
  }) {
    return SetupResult(
      user: user == const $CopyWithPlaceholder() || user == null
          ? _value.user
          // ignore: cast_nullable_to_non_nullable
          : user as User,
      session: session == const $CopyWithPlaceholder()
          ? _value.session
          // ignore: cast_nullable_to_non_nullable
          : session as SessionToken?,
    );
  }
}

extension $SetupResultCopyWith on SetupResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSetupResult.copyWith(...)` or `instanceOfSetupResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SetupResultCWProxy get copyWith => _$SetupResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SetupResult _$SetupResultFromJson(Map<String, dynamic> json) => $checkedCreate(
  'SetupResult',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['user']);
    final val = SetupResult(
      user: $checkedConvert(
        'user',
        (v) => User.fromJson(v as Map<String, dynamic>),
      ),
      session: $checkedConvert(
        'session',
        (v) =>
            v == null ? null : SessionToken.fromJson(v as Map<String, dynamic>),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$SetupResultToJson(SetupResult instance) =>
    <String, dynamic>{
      'user': instance.user.toJson(),
      'session': ?instance.session?.toJson(),
    };
