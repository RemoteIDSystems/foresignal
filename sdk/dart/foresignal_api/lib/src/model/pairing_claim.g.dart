// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pairing_claim.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PairingClaimCWProxy {
  PairingClaim code(String code);

  PairingClaim device(DeviceInfo device);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PairingClaim(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PairingClaim(...).copyWith(id: 12, name: "My name")
  /// ```
  PairingClaim call({String code, DeviceInfo device});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPairingClaim.copyWith(...)` or call `instanceOfPairingClaim.copyWith.fieldName(value)` for a single field.
class _$PairingClaimCWProxyImpl implements _$PairingClaimCWProxy {
  const _$PairingClaimCWProxyImpl(this._value);

  final PairingClaim _value;

  @override
  PairingClaim code(String code) => call(code: code);

  @override
  PairingClaim device(DeviceInfo device) => call(device: device);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PairingClaim(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PairingClaim(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PairingClaim call({
    Object? code = const $CopyWithPlaceholder(),
    Object? device = const $CopyWithPlaceholder(),
  }) {
    return PairingClaim(
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as String,
      device: device == const $CopyWithPlaceholder() || device == null
          ? _value.device
          // ignore: cast_nullable_to_non_nullable
          : device as DeviceInfo,
    );
  }
}

extension $PairingClaimCopyWith on PairingClaim {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPairingClaim.copyWith(...)` or `instanceOfPairingClaim.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PairingClaimCWProxy get copyWith => _$PairingClaimCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PairingClaim _$PairingClaimFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PairingClaim', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['code', 'device']);
      final val = PairingClaim(
        code: $checkedConvert('code', (v) => v as String),
        device: $checkedConvert(
          'device',
          (v) => DeviceInfo.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PairingClaimToJson(PairingClaim instance) =>
    <String, dynamic>{
      'code': instance.code,
      'device': instance.device.toJson(),
    };
