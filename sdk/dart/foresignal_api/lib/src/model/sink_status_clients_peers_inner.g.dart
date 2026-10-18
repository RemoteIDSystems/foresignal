// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sink_status_clients_peers_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SinkStatusClientsPeersInnerCWProxy {
  SinkStatusClientsPeersInner address(String address);

  SinkStatusClientsPeersInner connectedAt(DateTime connectedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkStatusClientsPeersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkStatusClientsPeersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SinkStatusClientsPeersInner call({String address, DateTime connectedAt});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSinkStatusClientsPeersInner.copyWith(...)` or call `instanceOfSinkStatusClientsPeersInner.copyWith.fieldName(value)` for a single field.
class _$SinkStatusClientsPeersInnerCWProxyImpl
    implements _$SinkStatusClientsPeersInnerCWProxy {
  const _$SinkStatusClientsPeersInnerCWProxyImpl(this._value);

  final SinkStatusClientsPeersInner _value;

  @override
  SinkStatusClientsPeersInner address(String address) => call(address: address);

  @override
  SinkStatusClientsPeersInner connectedAt(DateTime connectedAt) =>
      call(connectedAt: connectedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkStatusClientsPeersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkStatusClientsPeersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SinkStatusClientsPeersInner call({
    Object? address = const $CopyWithPlaceholder(),
    Object? connectedAt = const $CopyWithPlaceholder(),
  }) {
    return SinkStatusClientsPeersInner(
      address: address == const $CopyWithPlaceholder() || address == null
          ? _value.address
          // ignore: cast_nullable_to_non_nullable
          : address as String,
      connectedAt:
          connectedAt == const $CopyWithPlaceholder() || connectedAt == null
          ? _value.connectedAt
          // ignore: cast_nullable_to_non_nullable
          : connectedAt as DateTime,
    );
  }
}

extension $SinkStatusClientsPeersInnerCopyWith on SinkStatusClientsPeersInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSinkStatusClientsPeersInner.copyWith(...)` or `instanceOfSinkStatusClientsPeersInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SinkStatusClientsPeersInnerCWProxy get copyWith =>
      _$SinkStatusClientsPeersInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SinkStatusClientsPeersInner _$SinkStatusClientsPeersInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SinkStatusClientsPeersInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['address', 'connectedAt']);
  final val = SinkStatusClientsPeersInner(
    address: $checkedConvert('address', (v) => v as String),
    connectedAt: $checkedConvert(
      'connectedAt',
      (v) => DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$SinkStatusClientsPeersInnerToJson(
  SinkStatusClientsPeersInner instance,
) => <String, dynamic>{
  'address': instance.address,
  'connectedAt': instance.connectedAt.toIso8601String(),
};
