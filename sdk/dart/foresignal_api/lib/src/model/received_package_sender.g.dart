// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'received_package_sender.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReceivedPackageSenderCWProxy {
  ReceivedPackageSender instanceId(String instanceId);

  ReceivedPackageSender instanceName(String? instanceName);

  ReceivedPackageSender keyId(String keyId);

  ReceivedPackageSender peerId(String? peerId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReceivedPackageSender(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReceivedPackageSender(...).copyWith(id: 12, name: "My name")
  /// ```
  ReceivedPackageSender call({
    String instanceId,
    String? instanceName,
    String keyId,
    String? peerId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReceivedPackageSender.copyWith(...)` or call `instanceOfReceivedPackageSender.copyWith.fieldName(value)` for a single field.
class _$ReceivedPackageSenderCWProxyImpl
    implements _$ReceivedPackageSenderCWProxy {
  const _$ReceivedPackageSenderCWProxyImpl(this._value);

  final ReceivedPackageSender _value;

  @override
  ReceivedPackageSender instanceId(String instanceId) =>
      call(instanceId: instanceId);

  @override
  ReceivedPackageSender instanceName(String? instanceName) =>
      call(instanceName: instanceName);

  @override
  ReceivedPackageSender keyId(String keyId) => call(keyId: keyId);

  @override
  ReceivedPackageSender peerId(String? peerId) => call(peerId: peerId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReceivedPackageSender(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReceivedPackageSender(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReceivedPackageSender call({
    Object? instanceId = const $CopyWithPlaceholder(),
    Object? instanceName = const $CopyWithPlaceholder(),
    Object? keyId = const $CopyWithPlaceholder(),
    Object? peerId = const $CopyWithPlaceholder(),
  }) {
    return ReceivedPackageSender(
      instanceId:
          instanceId == const $CopyWithPlaceholder() || instanceId == null
          ? _value.instanceId
          // ignore: cast_nullable_to_non_nullable
          : instanceId as String,
      instanceName: instanceName == const $CopyWithPlaceholder()
          ? _value.instanceName
          // ignore: cast_nullable_to_non_nullable
          : instanceName as String?,
      keyId: keyId == const $CopyWithPlaceholder() || keyId == null
          ? _value.keyId
          // ignore: cast_nullable_to_non_nullable
          : keyId as String,
      peerId: peerId == const $CopyWithPlaceholder()
          ? _value.peerId
          // ignore: cast_nullable_to_non_nullable
          : peerId as String?,
    );
  }
}

extension $ReceivedPackageSenderCopyWith on ReceivedPackageSender {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReceivedPackageSender.copyWith(...)` or `instanceOfReceivedPackageSender.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReceivedPackageSenderCWProxy get copyWith =>
      _$ReceivedPackageSenderCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReceivedPackageSender _$ReceivedPackageSenderFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReceivedPackageSender', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['instanceId', 'keyId']);
  final val = ReceivedPackageSender(
    instanceId: $checkedConvert('instanceId', (v) => v as String),
    instanceName: $checkedConvert('instanceName', (v) => v as String?),
    keyId: $checkedConvert('keyId', (v) => v as String),
    peerId: $checkedConvert('peerId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ReceivedPackageSenderToJson(
  ReceivedPackageSender instance,
) => <String, dynamic>{
  'instanceId': instance.instanceId,
  'instanceName': ?instance.instanceName,
  'keyId': instance.keyId,
  'peerId': ?instance.peerId,
};
