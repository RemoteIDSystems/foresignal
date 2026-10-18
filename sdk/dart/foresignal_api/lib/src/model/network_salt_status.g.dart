// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'network_salt_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NetworkSaltStatusCWProxy {
  NetworkSaltStatus networkId(String networkId);

  NetworkSaltStatus saltId(String saltId);

  NetworkSaltStatus createdAt(DateTime createdAt);

  NetworkSaltStatus rotatesAt(DateTime rotatesAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NetworkSaltStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NetworkSaltStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  NetworkSaltStatus call({
    String networkId,
    String saltId,
    DateTime createdAt,
    DateTime rotatesAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNetworkSaltStatus.copyWith(...)` or call `instanceOfNetworkSaltStatus.copyWith.fieldName(value)` for a single field.
class _$NetworkSaltStatusCWProxyImpl implements _$NetworkSaltStatusCWProxy {
  const _$NetworkSaltStatusCWProxyImpl(this._value);

  final NetworkSaltStatus _value;

  @override
  NetworkSaltStatus networkId(String networkId) => call(networkId: networkId);

  @override
  NetworkSaltStatus saltId(String saltId) => call(saltId: saltId);

  @override
  NetworkSaltStatus createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  NetworkSaltStatus rotatesAt(DateTime rotatesAt) => call(rotatesAt: rotatesAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NetworkSaltStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NetworkSaltStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NetworkSaltStatus call({
    Object? networkId = const $CopyWithPlaceholder(),
    Object? saltId = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? rotatesAt = const $CopyWithPlaceholder(),
  }) {
    return NetworkSaltStatus(
      networkId: networkId == const $CopyWithPlaceholder() || networkId == null
          ? _value.networkId
          // ignore: cast_nullable_to_non_nullable
          : networkId as String,
      saltId: saltId == const $CopyWithPlaceholder() || saltId == null
          ? _value.saltId
          // ignore: cast_nullable_to_non_nullable
          : saltId as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      rotatesAt: rotatesAt == const $CopyWithPlaceholder() || rotatesAt == null
          ? _value.rotatesAt
          // ignore: cast_nullable_to_non_nullable
          : rotatesAt as DateTime,
    );
  }
}

extension $NetworkSaltStatusCopyWith on NetworkSaltStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNetworkSaltStatus.copyWith(...)` or `instanceOfNetworkSaltStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NetworkSaltStatusCWProxy get copyWith =>
      _$NetworkSaltStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NetworkSaltStatus _$NetworkSaltStatusFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NetworkSaltStatus', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['networkId', 'saltId', 'createdAt', 'rotatesAt'],
  );
  final val = NetworkSaltStatus(
    networkId: $checkedConvert('networkId', (v) => v as String),
    saltId: $checkedConvert('saltId', (v) => v as String),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    rotatesAt: $checkedConvert('rotatesAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$NetworkSaltStatusToJson(NetworkSaltStatus instance) =>
    <String, dynamic>{
      'networkId': instance.networkId,
      'saltId': instance.saltId,
      'createdAt': instance.createdAt.toIso8601String(),
      'rotatesAt': instance.rotatesAt.toIso8601String(),
    };
