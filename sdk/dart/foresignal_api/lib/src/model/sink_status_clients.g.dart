// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sink_status_clients.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SinkStatusClientsCWProxy {
  SinkStatusClients connected(int connected);

  SinkStatusClients max(int max);

  SinkStatusClients rejected(int? rejected);

  SinkStatusClients peers(List<SinkStatusClientsPeersInner>? peers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkStatusClients(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkStatusClients(...).copyWith(id: 12, name: "My name")
  /// ```
  SinkStatusClients call({
    int connected,
    int max,
    int? rejected,
    List<SinkStatusClientsPeersInner>? peers,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSinkStatusClients.copyWith(...)` or call `instanceOfSinkStatusClients.copyWith.fieldName(value)` for a single field.
class _$SinkStatusClientsCWProxyImpl implements _$SinkStatusClientsCWProxy {
  const _$SinkStatusClientsCWProxyImpl(this._value);

  final SinkStatusClients _value;

  @override
  SinkStatusClients connected(int connected) => call(connected: connected);

  @override
  SinkStatusClients max(int max) => call(max: max);

  @override
  SinkStatusClients rejected(int? rejected) => call(rejected: rejected);

  @override
  SinkStatusClients peers(List<SinkStatusClientsPeersInner>? peers) =>
      call(peers: peers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkStatusClients(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkStatusClients(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SinkStatusClients call({
    Object? connected = const $CopyWithPlaceholder(),
    Object? max = const $CopyWithPlaceholder(),
    Object? rejected = const $CopyWithPlaceholder(),
    Object? peers = const $CopyWithPlaceholder(),
  }) {
    return SinkStatusClients(
      connected: connected == const $CopyWithPlaceholder() || connected == null
          ? _value.connected
          // ignore: cast_nullable_to_non_nullable
          : connected as int,
      max: max == const $CopyWithPlaceholder() || max == null
          ? _value.max
          // ignore: cast_nullable_to_non_nullable
          : max as int,
      rejected: rejected == const $CopyWithPlaceholder()
          ? _value.rejected
          // ignore: cast_nullable_to_non_nullable
          : rejected as int?,
      peers: peers == const $CopyWithPlaceholder()
          ? _value.peers
          // ignore: cast_nullable_to_non_nullable
          : peers as List<SinkStatusClientsPeersInner>?,
    );
  }
}

extension $SinkStatusClientsCopyWith on SinkStatusClients {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSinkStatusClients.copyWith(...)` or `instanceOfSinkStatusClients.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SinkStatusClientsCWProxy get copyWith =>
      _$SinkStatusClientsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SinkStatusClients _$SinkStatusClientsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SinkStatusClients', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['connected', 'max']);
      final val = SinkStatusClients(
        connected: $checkedConvert('connected', (v) => (v as num).toInt()),
        max: $checkedConvert('max', (v) => (v as num).toInt()),
        rejected: $checkedConvert('rejected', (v) => (v as num?)?.toInt()),
        peers: $checkedConvert(
          'peers',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => SinkStatusClientsPeersInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SinkStatusClientsToJson(SinkStatusClients instance) =>
    <String, dynamic>{
      'connected': instance.connected,
      'max': instance.max,
      'rejected': ?instance.rejected,
      'peers': ?instance.peers?.map((e) => e.toJson()).toList(),
    };
