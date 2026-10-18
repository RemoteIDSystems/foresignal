// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'presence_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PresenceEventCWProxy {
  PresenceEvent workspaceId(String workspaceId);

  PresenceEvent clients(List<PresenceEventClientsInner> clients);

  PresenceEvent change(PresenceEventChange? change);

  PresenceEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PresenceEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PresenceEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  PresenceEvent call({
    String workspaceId,
    List<PresenceEventClientsInner> clients,
    PresenceEventChange? change,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPresenceEvent.copyWith(...)` or call `instanceOfPresenceEvent.copyWith.fieldName(value)` for a single field.
class _$PresenceEventCWProxyImpl implements _$PresenceEventCWProxy {
  const _$PresenceEventCWProxyImpl(this._value);

  final PresenceEvent _value;

  @override
  PresenceEvent workspaceId(String workspaceId) =>
      call(workspaceId: workspaceId);

  @override
  PresenceEvent clients(List<PresenceEventClientsInner> clients) =>
      call(clients: clients);

  @override
  PresenceEvent change(PresenceEventChange? change) => call(change: change);

  @override
  PresenceEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PresenceEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PresenceEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PresenceEvent call({
    Object? workspaceId = const $CopyWithPlaceholder(),
    Object? clients = const $CopyWithPlaceholder(),
    Object? change = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return PresenceEvent(
      workspaceId:
          workspaceId == const $CopyWithPlaceholder() || workspaceId == null
          ? _value.workspaceId
          // ignore: cast_nullable_to_non_nullable
          : workspaceId as String,
      clients: clients == const $CopyWithPlaceholder() || clients == null
          ? _value.clients
          // ignore: cast_nullable_to_non_nullable
          : clients as List<PresenceEventClientsInner>,
      change: change == const $CopyWithPlaceholder()
          ? _value.change
          // ignore: cast_nullable_to_non_nullable
          : change as PresenceEventChange?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $PresenceEventCopyWith on PresenceEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPresenceEvent.copyWith(...)` or `instanceOfPresenceEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PresenceEventCWProxy get copyWith => _$PresenceEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PresenceEvent _$PresenceEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PresenceEvent', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['workspaceId', 'clients', 'at']);
      final val = PresenceEvent(
        workspaceId: $checkedConvert('workspaceId', (v) => v as String),
        clients: $checkedConvert(
          'clients',
          (v) => (v as List<dynamic>)
              .map(
                (e) => PresenceEventClientsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        change: $checkedConvert(
          'change',
          (v) => v == null
              ? null
              : PresenceEventChange.fromJson(v as Map<String, dynamic>),
        ),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$PresenceEventToJson(PresenceEvent instance) =>
    <String, dynamic>{
      'workspaceId': instance.workspaceId,
      'clients': instance.clients.map((e) => e.toJson()).toList(),
      'change': ?instance.change?.toJson(),
      'at': instance.at.toIso8601String(),
    };
