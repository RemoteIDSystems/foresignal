// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'presence_event_clients_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PresenceEventClientsInnerCWProxy {
  PresenceEventClientsInner clientId(String clientId);

  PresenceEventClientsInner clientKind(
    PresenceEventClientsInnerClientKindEnum clientKind,
  );

  PresenceEventClientsInner principal(String? principal);

  PresenceEventClientsInner connectedAt(DateTime connectedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PresenceEventClientsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PresenceEventClientsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  PresenceEventClientsInner call({
    String clientId,
    PresenceEventClientsInnerClientKindEnum clientKind,
    String? principal,
    DateTime connectedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPresenceEventClientsInner.copyWith(...)` or call `instanceOfPresenceEventClientsInner.copyWith.fieldName(value)` for a single field.
class _$PresenceEventClientsInnerCWProxyImpl
    implements _$PresenceEventClientsInnerCWProxy {
  const _$PresenceEventClientsInnerCWProxyImpl(this._value);

  final PresenceEventClientsInner _value;

  @override
  PresenceEventClientsInner clientId(String clientId) =>
      call(clientId: clientId);

  @override
  PresenceEventClientsInner clientKind(
    PresenceEventClientsInnerClientKindEnum clientKind,
  ) => call(clientKind: clientKind);

  @override
  PresenceEventClientsInner principal(String? principal) =>
      call(principal: principal);

  @override
  PresenceEventClientsInner connectedAt(DateTime connectedAt) =>
      call(connectedAt: connectedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PresenceEventClientsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PresenceEventClientsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PresenceEventClientsInner call({
    Object? clientId = const $CopyWithPlaceholder(),
    Object? clientKind = const $CopyWithPlaceholder(),
    Object? principal = const $CopyWithPlaceholder(),
    Object? connectedAt = const $CopyWithPlaceholder(),
  }) {
    return PresenceEventClientsInner(
      clientId: clientId == const $CopyWithPlaceholder() || clientId == null
          ? _value.clientId
          // ignore: cast_nullable_to_non_nullable
          : clientId as String,
      clientKind:
          clientKind == const $CopyWithPlaceholder() || clientKind == null
          ? _value.clientKind
          // ignore: cast_nullable_to_non_nullable
          : clientKind as PresenceEventClientsInnerClientKindEnum,
      principal: principal == const $CopyWithPlaceholder()
          ? _value.principal
          // ignore: cast_nullable_to_non_nullable
          : principal as String?,
      connectedAt:
          connectedAt == const $CopyWithPlaceholder() || connectedAt == null
          ? _value.connectedAt
          // ignore: cast_nullable_to_non_nullable
          : connectedAt as DateTime,
    );
  }
}

extension $PresenceEventClientsInnerCopyWith on PresenceEventClientsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPresenceEventClientsInner.copyWith(...)` or `instanceOfPresenceEventClientsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PresenceEventClientsInnerCWProxy get copyWith =>
      _$PresenceEventClientsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PresenceEventClientsInner _$PresenceEventClientsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PresenceEventClientsInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['clientId', 'clientKind', 'connectedAt'],
  );
  final val = PresenceEventClientsInner(
    clientId: $checkedConvert('clientId', (v) => v as String),
    clientKind: $checkedConvert(
      'clientKind',
      (v) => $enumDecode(_$PresenceEventClientsInnerClientKindEnumEnumMap, v),
    ),
    principal: $checkedConvert('principal', (v) => v as String?),
    connectedAt: $checkedConvert(
      'connectedAt',
      (v) => DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$PresenceEventClientsInnerToJson(
  PresenceEventClientsInner instance,
) => <String, dynamic>{
  'clientId': instance.clientId,
  'clientKind':
      _$PresenceEventClientsInnerClientKindEnumEnumMap[instance.clientKind]!,
  'principal': ?instance.principal,
  'connectedAt': instance.connectedAt.toIso8601String(),
};

const _$PresenceEventClientsInnerClientKindEnumEnumMap = {
  PresenceEventClientsInnerClientKindEnum.globe: 'globe',
  PresenceEventClientsInnerClientKindEnum.map: 'map',
  PresenceEventClientsInnerClientKindEnum.analyst: 'analyst',
  PresenceEventClientsInnerClientKindEnum.satellite: 'satellite',
  PresenceEventClientsInnerClientKindEnum.rf: 'rf',
  PresenceEventClientsInnerClientKindEnum.mobile: 'mobile',
  PresenceEventClientsInnerClientKindEnum.agent: 'agent',
  PresenceEventClientsInnerClientKindEnum.other: 'other',
};
