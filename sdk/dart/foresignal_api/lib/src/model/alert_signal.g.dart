// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'alert_signal.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AlertSignalCWProxy {
  AlertSignal alertId(int alertId);

  AlertSignal type(AlertSignalTypeEnum type);

  AlertSignal at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertSignal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertSignal(...).copyWith(id: 12, name: "My name")
  /// ```
  AlertSignal call({int alertId, AlertSignalTypeEnum type, DateTime at});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAlertSignal.copyWith(...)` or call `instanceOfAlertSignal.copyWith.fieldName(value)` for a single field.
class _$AlertSignalCWProxyImpl implements _$AlertSignalCWProxy {
  const _$AlertSignalCWProxyImpl(this._value);

  final AlertSignal _value;

  @override
  AlertSignal alertId(int alertId) => call(alertId: alertId);

  @override
  AlertSignal type(AlertSignalTypeEnum type) => call(type: type);

  @override
  AlertSignal at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertSignal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertSignal(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AlertSignal call({
    Object? alertId = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return AlertSignal(
      alertId: alertId == const $CopyWithPlaceholder() || alertId == null
          ? _value.alertId
          // ignore: cast_nullable_to_non_nullable
          : alertId as int,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as AlertSignalTypeEnum,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $AlertSignalCopyWith on AlertSignal {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAlertSignal.copyWith(...)` or `instanceOfAlertSignal.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AlertSignalCWProxy get copyWith => _$AlertSignalCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AlertSignal _$AlertSignalFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AlertSignal', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['alertId', 'type', 'at']);
      final val = AlertSignal(
        alertId: $checkedConvert('alertId', (v) => (v as num).toInt()),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$AlertSignalTypeEnumEnumMap, v),
        ),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$AlertSignalToJson(AlertSignal instance) =>
    <String, dynamic>{
      'alertId': instance.alertId,
      'type': _$AlertSignalTypeEnumEnumMap[instance.type]!,
      'at': instance.at.toIso8601String(),
    };

const _$AlertSignalTypeEnumEnumMap = {
  AlertSignalTypeEnum.ACK_FAST: 'ACK_FAST',
  AlertSignalTypeEnum.SNOOZED: 'SNOOZED',
  AlertSignalTypeEnum.FLAP: 'FLAP',
  AlertSignalTypeEnum.UNOPENED: 'UNOPENED',
};
