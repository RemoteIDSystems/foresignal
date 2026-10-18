// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mqtt_connections.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MqttConnectionsCWProxy {
  MqttConnections connections(int connections);

  MqttConnections connected(int connected);

  MqttConnections lastError(String? lastError);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MqttConnections(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MqttConnections(...).copyWith(id: 12, name: "My name")
  /// ```
  MqttConnections call({int connections, int connected, String? lastError});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMqttConnections.copyWith(...)` or call `instanceOfMqttConnections.copyWith.fieldName(value)` for a single field.
class _$MqttConnectionsCWProxyImpl implements _$MqttConnectionsCWProxy {
  const _$MqttConnectionsCWProxyImpl(this._value);

  final MqttConnections _value;

  @override
  MqttConnections connections(int connections) =>
      call(connections: connections);

  @override
  MqttConnections connected(int connected) => call(connected: connected);

  @override
  MqttConnections lastError(String? lastError) => call(lastError: lastError);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MqttConnections(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MqttConnections(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MqttConnections call({
    Object? connections = const $CopyWithPlaceholder(),
    Object? connected = const $CopyWithPlaceholder(),
    Object? lastError = const $CopyWithPlaceholder(),
  }) {
    return MqttConnections(
      connections:
          connections == const $CopyWithPlaceholder() || connections == null
          ? _value.connections
          // ignore: cast_nullable_to_non_nullable
          : connections as int,
      connected: connected == const $CopyWithPlaceholder() || connected == null
          ? _value.connected
          // ignore: cast_nullable_to_non_nullable
          : connected as int,
      lastError: lastError == const $CopyWithPlaceholder()
          ? _value.lastError
          // ignore: cast_nullable_to_non_nullable
          : lastError as String?,
    );
  }
}

extension $MqttConnectionsCopyWith on MqttConnections {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMqttConnections.copyWith(...)` or `instanceOfMqttConnections.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MqttConnectionsCWProxy get copyWith => _$MqttConnectionsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MqttConnections _$MqttConnectionsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MqttConnections', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['connections', 'connected']);
      final val = MqttConnections(
        connections: $checkedConvert('connections', (v) => (v as num).toInt()),
        connected: $checkedConvert('connected', (v) => (v as num).toInt()),
        lastError: $checkedConvert('lastError', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$MqttConnectionsToJson(MqttConnections instance) =>
    <String, dynamic>{
      'connections': instance.connections,
      'connected': instance.connected,
      'lastError': ?instance.lastError,
    };
