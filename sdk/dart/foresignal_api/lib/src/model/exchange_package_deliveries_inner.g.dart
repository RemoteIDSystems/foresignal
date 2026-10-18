// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exchange_package_deliveries_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExchangePackageDeliveriesInnerCWProxy {
  ExchangePackageDeliveriesInner instanceId(String instanceId);

  ExchangePackageDeliveriesInner peerId(String? peerId);

  ExchangePackageDeliveriesInner status(
    ExchangePackageDeliveriesInnerStatusEnum status,
  );

  ExchangePackageDeliveriesInner at(DateTime? at);

  ExchangePackageDeliveriesInner error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExchangePackageDeliveriesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExchangePackageDeliveriesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ExchangePackageDeliveriesInner call({
    String instanceId,
    String? peerId,
    ExchangePackageDeliveriesInnerStatusEnum status,
    DateTime? at,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExchangePackageDeliveriesInner.copyWith(...)` or call `instanceOfExchangePackageDeliveriesInner.copyWith.fieldName(value)` for a single field.
class _$ExchangePackageDeliveriesInnerCWProxyImpl
    implements _$ExchangePackageDeliveriesInnerCWProxy {
  const _$ExchangePackageDeliveriesInnerCWProxyImpl(this._value);

  final ExchangePackageDeliveriesInner _value;

  @override
  ExchangePackageDeliveriesInner instanceId(String instanceId) =>
      call(instanceId: instanceId);

  @override
  ExchangePackageDeliveriesInner peerId(String? peerId) => call(peerId: peerId);

  @override
  ExchangePackageDeliveriesInner status(
    ExchangePackageDeliveriesInnerStatusEnum status,
  ) => call(status: status);

  @override
  ExchangePackageDeliveriesInner at(DateTime? at) => call(at: at);

  @override
  ExchangePackageDeliveriesInner error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExchangePackageDeliveriesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExchangePackageDeliveriesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExchangePackageDeliveriesInner call({
    Object? instanceId = const $CopyWithPlaceholder(),
    Object? peerId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return ExchangePackageDeliveriesInner(
      instanceId:
          instanceId == const $CopyWithPlaceholder() || instanceId == null
          ? _value.instanceId
          // ignore: cast_nullable_to_non_nullable
          : instanceId as String,
      peerId: peerId == const $CopyWithPlaceholder()
          ? _value.peerId
          // ignore: cast_nullable_to_non_nullable
          : peerId as String?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ExchangePackageDeliveriesInnerStatusEnum,
      at: at == const $CopyWithPlaceholder()
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $ExchangePackageDeliveriesInnerCopyWith
    on ExchangePackageDeliveriesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExchangePackageDeliveriesInner.copyWith(...)` or `instanceOfExchangePackageDeliveriesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExchangePackageDeliveriesInnerCWProxy get copyWith =>
      _$ExchangePackageDeliveriesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExchangePackageDeliveriesInner _$ExchangePackageDeliveriesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExchangePackageDeliveriesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['instanceId', 'status']);
  final val = ExchangePackageDeliveriesInner(
    instanceId: $checkedConvert('instanceId', (v) => v as String),
    peerId: $checkedConvert('peerId', (v) => v as String?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ExchangePackageDeliveriesInnerStatusEnumEnumMap, v),
    ),
    at: $checkedConvert(
      'at',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    error: $checkedConvert('error', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ExchangePackageDeliveriesInnerToJson(
  ExchangePackageDeliveriesInner instance,
) => <String, dynamic>{
  'instanceId': instance.instanceId,
  'peerId': ?instance.peerId,
  'status': _$ExchangePackageDeliveriesInnerStatusEnumEnumMap[instance.status]!,
  'at': ?instance.at?.toIso8601String(),
  'error': ?instance.error,
};

const _$ExchangePackageDeliveriesInnerStatusEnumEnumMap = {
  ExchangePackageDeliveriesInnerStatusEnum.pending: 'pending',
  ExchangePackageDeliveriesInnerStatusEnum.delivered: 'delivered',
  ExchangePackageDeliveriesInnerStatusEnum.acknowledged: 'acknowledged',
  ExchangePackageDeliveriesInnerStatusEnum.failed: 'failed',
};
