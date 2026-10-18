// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'network_salt_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NetworkSaltInputCWProxy {
  NetworkSaltInput networkId(String networkId);

  NetworkSaltInput saltRef(String saltRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NetworkSaltInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NetworkSaltInput(...).copyWith(id: 12, name: "My name")
  /// ```
  NetworkSaltInput call({String networkId, String saltRef});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNetworkSaltInput.copyWith(...)` or call `instanceOfNetworkSaltInput.copyWith.fieldName(value)` for a single field.
class _$NetworkSaltInputCWProxyImpl implements _$NetworkSaltInputCWProxy {
  const _$NetworkSaltInputCWProxyImpl(this._value);

  final NetworkSaltInput _value;

  @override
  NetworkSaltInput networkId(String networkId) => call(networkId: networkId);

  @override
  NetworkSaltInput saltRef(String saltRef) => call(saltRef: saltRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NetworkSaltInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NetworkSaltInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NetworkSaltInput call({
    Object? networkId = const $CopyWithPlaceholder(),
    Object? saltRef = const $CopyWithPlaceholder(),
  }) {
    return NetworkSaltInput(
      networkId: networkId == const $CopyWithPlaceholder() || networkId == null
          ? _value.networkId
          // ignore: cast_nullable_to_non_nullable
          : networkId as String,
      saltRef: saltRef == const $CopyWithPlaceholder() || saltRef == null
          ? _value.saltRef
          // ignore: cast_nullable_to_non_nullable
          : saltRef as String,
    );
  }
}

extension $NetworkSaltInputCopyWith on NetworkSaltInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNetworkSaltInput.copyWith(...)` or `instanceOfNetworkSaltInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NetworkSaltInputCWProxy get copyWith => _$NetworkSaltInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NetworkSaltInput _$NetworkSaltInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NetworkSaltInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['networkId', 'saltRef']);
      final val = NetworkSaltInput(
        networkId: $checkedConvert('networkId', (v) => v as String),
        saltRef: $checkedConvert('saltRef', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$NetworkSaltInputToJson(NetworkSaltInput instance) =>
    <String, dynamic>{
      'networkId': instance.networkId,
      'saltRef': instance.saltRef,
    };
