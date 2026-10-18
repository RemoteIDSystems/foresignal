// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'client_presence_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ClientPresenceInputCWProxy {
  ClientPresenceInput kind(ClientPresenceInputKindEnum kind);

  ClientPresenceInput state(ClientPresenceInputStateEnum state);

  ClientPresenceInput ttlSeconds(int? ttlSeconds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClientPresenceInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClientPresenceInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ClientPresenceInput call({
    ClientPresenceInputKindEnum kind,
    ClientPresenceInputStateEnum state,
    int? ttlSeconds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfClientPresenceInput.copyWith(...)` or call `instanceOfClientPresenceInput.copyWith.fieldName(value)` for a single field.
class _$ClientPresenceInputCWProxyImpl implements _$ClientPresenceInputCWProxy {
  const _$ClientPresenceInputCWProxyImpl(this._value);

  final ClientPresenceInput _value;

  @override
  ClientPresenceInput kind(ClientPresenceInputKindEnum kind) =>
      call(kind: kind);

  @override
  ClientPresenceInput state(ClientPresenceInputStateEnum state) =>
      call(state: state);

  @override
  ClientPresenceInput ttlSeconds(int? ttlSeconds) =>
      call(ttlSeconds: ttlSeconds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClientPresenceInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClientPresenceInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ClientPresenceInput call({
    Object? kind = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? ttlSeconds = const $CopyWithPlaceholder(),
  }) {
    return ClientPresenceInput(
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as ClientPresenceInputKindEnum,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as ClientPresenceInputStateEnum,
      ttlSeconds: ttlSeconds == const $CopyWithPlaceholder()
          ? _value.ttlSeconds
          // ignore: cast_nullable_to_non_nullable
          : ttlSeconds as int?,
    );
  }
}

extension $ClientPresenceInputCopyWith on ClientPresenceInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfClientPresenceInput.copyWith(...)` or `instanceOfClientPresenceInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ClientPresenceInputCWProxy get copyWith =>
      _$ClientPresenceInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ClientPresenceInput _$ClientPresenceInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ClientPresenceInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['kind', 'state']);
      final val = ClientPresenceInput(
        kind: $checkedConvert(
          'kind',
          (v) => $enumDecode(_$ClientPresenceInputKindEnumEnumMap, v),
        ),
        state: $checkedConvert(
          'state',
          (v) => $enumDecode(_$ClientPresenceInputStateEnumEnumMap, v),
        ),
        ttlSeconds: $checkedConvert(
          'ttlSeconds',
          (v) => (v as num?)?.toInt() ?? 120,
        ),
      );
      return val;
    });

Map<String, dynamic> _$ClientPresenceInputToJson(
  ClientPresenceInput instance,
) => <String, dynamic>{
  'kind': _$ClientPresenceInputKindEnumEnumMap[instance.kind]!,
  'state': _$ClientPresenceInputStateEnumEnumMap[instance.state]!,
  'ttlSeconds': ?instance.ttlSeconds,
};

const _$ClientPresenceInputKindEnumEnumMap = {
  ClientPresenceInputKindEnum.desktop: 'desktop',
  ClientPresenceInputKindEnum.mobile: 'mobile',
  ClientPresenceInputKindEnum.web: 'web',
};

const _$ClientPresenceInputStateEnumEnumMap = {
  ClientPresenceInputStateEnum.active: 'active',
  ClientPresenceInputStateEnum.idle: 'idle',
  ClientPresenceInputStateEnum.background: 'background',
};
