// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'client_presence.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ClientPresenceCWProxy {
  ClientPresence deviceKey(String deviceKey);

  ClientPresence kind(ClientPresenceKindEnum kind);

  ClientPresence state(ClientPresenceStateEnum state);

  ClientPresence expiresAt(DateTime expiresAt);

  ClientPresence updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClientPresence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClientPresence(...).copyWith(id: 12, name: "My name")
  /// ```
  ClientPresence call({
    String deviceKey,
    ClientPresenceKindEnum kind,
    ClientPresenceStateEnum state,
    DateTime expiresAt,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfClientPresence.copyWith(...)` or call `instanceOfClientPresence.copyWith.fieldName(value)` for a single field.
class _$ClientPresenceCWProxyImpl implements _$ClientPresenceCWProxy {
  const _$ClientPresenceCWProxyImpl(this._value);

  final ClientPresence _value;

  @override
  ClientPresence deviceKey(String deviceKey) => call(deviceKey: deviceKey);

  @override
  ClientPresence kind(ClientPresenceKindEnum kind) => call(kind: kind);

  @override
  ClientPresence state(ClientPresenceStateEnum state) => call(state: state);

  @override
  ClientPresence expiresAt(DateTime expiresAt) => call(expiresAt: expiresAt);

  @override
  ClientPresence updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClientPresence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClientPresence(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ClientPresence call({
    Object? deviceKey = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return ClientPresence(
      deviceKey: deviceKey == const $CopyWithPlaceholder() || deviceKey == null
          ? _value.deviceKey
          // ignore: cast_nullable_to_non_nullable
          : deviceKey as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as ClientPresenceKindEnum,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as ClientPresenceStateEnum,
      expiresAt: expiresAt == const $CopyWithPlaceholder() || expiresAt == null
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $ClientPresenceCopyWith on ClientPresence {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfClientPresence.copyWith(...)` or `instanceOfClientPresence.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ClientPresenceCWProxy get copyWith => _$ClientPresenceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ClientPresence _$ClientPresenceFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ClientPresence', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['deviceKey', 'kind', 'state', 'expiresAt'],
      );
      final val = ClientPresence(
        deviceKey: $checkedConvert('deviceKey', (v) => v as String),
        kind: $checkedConvert(
          'kind',
          (v) => $enumDecode(_$ClientPresenceKindEnumEnumMap, v),
        ),
        state: $checkedConvert(
          'state',
          (v) => $enumDecode(_$ClientPresenceStateEnumEnumMap, v),
        ),
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => DateTime.parse(v as String),
        ),
        updatedAt: $checkedConvert(
          'updatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ClientPresenceToJson(ClientPresence instance) =>
    <String, dynamic>{
      'deviceKey': instance.deviceKey,
      'kind': _$ClientPresenceKindEnumEnumMap[instance.kind]!,
      'state': _$ClientPresenceStateEnumEnumMap[instance.state]!,
      'expiresAt': instance.expiresAt.toIso8601String(),
      'updatedAt': ?instance.updatedAt?.toIso8601String(),
    };

const _$ClientPresenceKindEnumEnumMap = {
  ClientPresenceKindEnum.desktop: 'desktop',
  ClientPresenceKindEnum.mobile: 'mobile',
  ClientPresenceKindEnum.web: 'web',
};

const _$ClientPresenceStateEnumEnumMap = {
  ClientPresenceStateEnum.active: 'active',
  ClientPresenceStateEnum.idle: 'idle',
  ClientPresenceStateEnum.background: 'background',
};
