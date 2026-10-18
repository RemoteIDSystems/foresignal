// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exchange_receipt.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExchangeReceiptCWProxy {
  ExchangeReceipt packageId(String packageId);

  ExchangeReceipt type(ExchangeReceiptTypeEnum type);

  ExchangeReceipt receiverInstanceId(String receiverInstanceId);

  ExchangeReceipt principalHandle(String? principalHandle);

  ExchangeReceipt at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExchangeReceipt(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExchangeReceipt(...).copyWith(id: 12, name: "My name")
  /// ```
  ExchangeReceipt call({
    String packageId,
    ExchangeReceiptTypeEnum type,
    String receiverInstanceId,
    String? principalHandle,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExchangeReceipt.copyWith(...)` or call `instanceOfExchangeReceipt.copyWith.fieldName(value)` for a single field.
class _$ExchangeReceiptCWProxyImpl implements _$ExchangeReceiptCWProxy {
  const _$ExchangeReceiptCWProxyImpl(this._value);

  final ExchangeReceipt _value;

  @override
  ExchangeReceipt packageId(String packageId) => call(packageId: packageId);

  @override
  ExchangeReceipt type(ExchangeReceiptTypeEnum type) => call(type: type);

  @override
  ExchangeReceipt receiverInstanceId(String receiverInstanceId) =>
      call(receiverInstanceId: receiverInstanceId);

  @override
  ExchangeReceipt principalHandle(String? principalHandle) =>
      call(principalHandle: principalHandle);

  @override
  ExchangeReceipt at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExchangeReceipt(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExchangeReceipt(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExchangeReceipt call({
    Object? packageId = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? receiverInstanceId = const $CopyWithPlaceholder(),
    Object? principalHandle = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return ExchangeReceipt(
      packageId: packageId == const $CopyWithPlaceholder() || packageId == null
          ? _value.packageId
          // ignore: cast_nullable_to_non_nullable
          : packageId as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ExchangeReceiptTypeEnum,
      receiverInstanceId:
          receiverInstanceId == const $CopyWithPlaceholder() ||
              receiverInstanceId == null
          ? _value.receiverInstanceId
          // ignore: cast_nullable_to_non_nullable
          : receiverInstanceId as String,
      principalHandle: principalHandle == const $CopyWithPlaceholder()
          ? _value.principalHandle
          // ignore: cast_nullable_to_non_nullable
          : principalHandle as String?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $ExchangeReceiptCopyWith on ExchangeReceipt {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExchangeReceipt.copyWith(...)` or `instanceOfExchangeReceipt.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExchangeReceiptCWProxy get copyWith => _$ExchangeReceiptCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExchangeReceipt _$ExchangeReceiptFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExchangeReceipt', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['packageId', 'type', 'receiverInstanceId', 'at'],
      );
      final val = ExchangeReceipt(
        packageId: $checkedConvert('packageId', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ExchangeReceiptTypeEnumEnumMap, v),
        ),
        receiverInstanceId: $checkedConvert(
          'receiverInstanceId',
          (v) => v as String,
        ),
        principalHandle: $checkedConvert(
          'principalHandle',
          (v) => v as String?,
        ),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$ExchangeReceiptToJson(ExchangeReceipt instance) =>
    <String, dynamic>{
      'packageId': instance.packageId,
      'type': _$ExchangeReceiptTypeEnumEnumMap[instance.type]!,
      'receiverInstanceId': instance.receiverInstanceId,
      'principalHandle': ?instance.principalHandle,
      'at': instance.at.toIso8601String(),
    };

const _$ExchangeReceiptTypeEnumEnumMap = {
  ExchangeReceiptTypeEnum.delivered: 'delivered',
  ExchangeReceiptTypeEnum.acknowledged: 'acknowledged',
};
