// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'session.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SessionCWProxy {
  Session id(String id);

  Session tokenPrefix(String? tokenPrefix);

  Session createdAt(DateTime createdAt);

  Session lastSeenAt(DateTime? lastSeenAt);

  Session expiresAt(DateTime expiresAt);

  Session idleExpiresAt(DateTime? idleExpiresAt);

  Session userAgent(String? userAgent);

  Session ipAddress(String? ipAddress);

  Session current(bool current);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Session(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Session(...).copyWith(id: 12, name: "My name")
  /// ```
  Session call({
    String id,
    String? tokenPrefix,
    DateTime createdAt,
    DateTime? lastSeenAt,
    DateTime expiresAt,
    DateTime? idleExpiresAt,
    String? userAgent,
    String? ipAddress,
    bool current,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSession.copyWith(...)` or call `instanceOfSession.copyWith.fieldName(value)` for a single field.
class _$SessionCWProxyImpl implements _$SessionCWProxy {
  const _$SessionCWProxyImpl(this._value);

  final Session _value;

  @override
  Session id(String id) => call(id: id);

  @override
  Session tokenPrefix(String? tokenPrefix) => call(tokenPrefix: tokenPrefix);

  @override
  Session createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Session lastSeenAt(DateTime? lastSeenAt) => call(lastSeenAt: lastSeenAt);

  @override
  Session expiresAt(DateTime expiresAt) => call(expiresAt: expiresAt);

  @override
  Session idleExpiresAt(DateTime? idleExpiresAt) =>
      call(idleExpiresAt: idleExpiresAt);

  @override
  Session userAgent(String? userAgent) => call(userAgent: userAgent);

  @override
  Session ipAddress(String? ipAddress) => call(ipAddress: ipAddress);

  @override
  Session current(bool current) => call(current: current);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Session(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Session(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Session call({
    Object? id = const $CopyWithPlaceholder(),
    Object? tokenPrefix = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? lastSeenAt = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? idleExpiresAt = const $CopyWithPlaceholder(),
    Object? userAgent = const $CopyWithPlaceholder(),
    Object? ipAddress = const $CopyWithPlaceholder(),
    Object? current = const $CopyWithPlaceholder(),
  }) {
    return Session(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      tokenPrefix: tokenPrefix == const $CopyWithPlaceholder()
          ? _value.tokenPrefix
          // ignore: cast_nullable_to_non_nullable
          : tokenPrefix as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      lastSeenAt: lastSeenAt == const $CopyWithPlaceholder()
          ? _value.lastSeenAt
          // ignore: cast_nullable_to_non_nullable
          : lastSeenAt as DateTime?,
      expiresAt: expiresAt == const $CopyWithPlaceholder() || expiresAt == null
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime,
      idleExpiresAt: idleExpiresAt == const $CopyWithPlaceholder()
          ? _value.idleExpiresAt
          // ignore: cast_nullable_to_non_nullable
          : idleExpiresAt as DateTime?,
      userAgent: userAgent == const $CopyWithPlaceholder()
          ? _value.userAgent
          // ignore: cast_nullable_to_non_nullable
          : userAgent as String?,
      ipAddress: ipAddress == const $CopyWithPlaceholder()
          ? _value.ipAddress
          // ignore: cast_nullable_to_non_nullable
          : ipAddress as String?,
      current: current == const $CopyWithPlaceholder() || current == null
          ? _value.current
          // ignore: cast_nullable_to_non_nullable
          : current as bool,
    );
  }
}

extension $SessionCopyWith on Session {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSession.copyWith(...)` or `instanceOfSession.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SessionCWProxy get copyWith => _$SessionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Session _$SessionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Session', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['id', 'createdAt', 'expiresAt', 'current'],
  );
  final val = Session(
    id: $checkedConvert('id', (v) => v as String),
    tokenPrefix: $checkedConvert('tokenPrefix', (v) => v as String?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    lastSeenAt: $checkedConvert(
      'lastSeenAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    expiresAt: $checkedConvert('expiresAt', (v) => DateTime.parse(v as String)),
    idleExpiresAt: $checkedConvert(
      'idleExpiresAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    userAgent: $checkedConvert('userAgent', (v) => v as String?),
    ipAddress: $checkedConvert('ipAddress', (v) => v as String?),
    current: $checkedConvert('current', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$SessionToJson(Session instance) => <String, dynamic>{
  'id': instance.id,
  'tokenPrefix': ?instance.tokenPrefix,
  'createdAt': instance.createdAt.toIso8601String(),
  'lastSeenAt': ?instance.lastSeenAt?.toIso8601String(),
  'expiresAt': instance.expiresAt.toIso8601String(),
  'idleExpiresAt': ?instance.idleExpiresAt?.toIso8601String(),
  'userAgent': ?instance.userAgent,
  'ipAddress': ?instance.ipAddress,
  'current': instance.current,
};
