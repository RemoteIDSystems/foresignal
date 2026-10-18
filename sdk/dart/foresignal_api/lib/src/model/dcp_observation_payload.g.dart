// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dcp_observation_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DcpObservationPayloadCWProxy {
  DcpObservationPayload address(String address);

  DcpObservationPayload name(String? name);

  DcpObservationPayload agency(String? agency);

  DcpObservationPayload channel(String? channel);

  DcpObservationPayload signalDbm(num? signalDbm);

  DcpObservationPayload frequencyOffsetHz(num? frequencyOffsetHz);

  DcpObservationPayload failureCode(String? failureCode);

  DcpObservationPayload dataAscii(String? dataAscii);

  DcpObservationPayload dataHex(String? dataHex);

  DcpObservationPayload decoded(Object? decoded);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DcpObservationPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DcpObservationPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  DcpObservationPayload call({
    String address,
    String? name,
    String? agency,
    String? channel,
    num? signalDbm,
    num? frequencyOffsetHz,
    String? failureCode,
    String? dataAscii,
    String? dataHex,
    Object? decoded,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDcpObservationPayload.copyWith(...)` or call `instanceOfDcpObservationPayload.copyWith.fieldName(value)` for a single field.
class _$DcpObservationPayloadCWProxyImpl
    implements _$DcpObservationPayloadCWProxy {
  const _$DcpObservationPayloadCWProxyImpl(this._value);

  final DcpObservationPayload _value;

  @override
  DcpObservationPayload address(String address) => call(address: address);

  @override
  DcpObservationPayload name(String? name) => call(name: name);

  @override
  DcpObservationPayload agency(String? agency) => call(agency: agency);

  @override
  DcpObservationPayload channel(String? channel) => call(channel: channel);

  @override
  DcpObservationPayload signalDbm(num? signalDbm) => call(signalDbm: signalDbm);

  @override
  DcpObservationPayload frequencyOffsetHz(num? frequencyOffsetHz) =>
      call(frequencyOffsetHz: frequencyOffsetHz);

  @override
  DcpObservationPayload failureCode(String? failureCode) =>
      call(failureCode: failureCode);

  @override
  DcpObservationPayload dataAscii(String? dataAscii) =>
      call(dataAscii: dataAscii);

  @override
  DcpObservationPayload dataHex(String? dataHex) => call(dataHex: dataHex);

  @override
  DcpObservationPayload decoded(Object? decoded) => call(decoded: decoded);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DcpObservationPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DcpObservationPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DcpObservationPayload call({
    Object? address = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? agency = const $CopyWithPlaceholder(),
    Object? channel = const $CopyWithPlaceholder(),
    Object? signalDbm = const $CopyWithPlaceholder(),
    Object? frequencyOffsetHz = const $CopyWithPlaceholder(),
    Object? failureCode = const $CopyWithPlaceholder(),
    Object? dataAscii = const $CopyWithPlaceholder(),
    Object? dataHex = const $CopyWithPlaceholder(),
    Object? decoded = const $CopyWithPlaceholder(),
  }) {
    return DcpObservationPayload(
      address: address == const $CopyWithPlaceholder() || address == null
          ? _value.address
          // ignore: cast_nullable_to_non_nullable
          : address as String,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      agency: agency == const $CopyWithPlaceholder()
          ? _value.agency
          // ignore: cast_nullable_to_non_nullable
          : agency as String?,
      channel: channel == const $CopyWithPlaceholder()
          ? _value.channel
          // ignore: cast_nullable_to_non_nullable
          : channel as String?,
      signalDbm: signalDbm == const $CopyWithPlaceholder()
          ? _value.signalDbm
          // ignore: cast_nullable_to_non_nullable
          : signalDbm as num?,
      frequencyOffsetHz: frequencyOffsetHz == const $CopyWithPlaceholder()
          ? _value.frequencyOffsetHz
          // ignore: cast_nullable_to_non_nullable
          : frequencyOffsetHz as num?,
      failureCode: failureCode == const $CopyWithPlaceholder()
          ? _value.failureCode
          // ignore: cast_nullable_to_non_nullable
          : failureCode as String?,
      dataAscii: dataAscii == const $CopyWithPlaceholder()
          ? _value.dataAscii
          // ignore: cast_nullable_to_non_nullable
          : dataAscii as String?,
      dataHex: dataHex == const $CopyWithPlaceholder()
          ? _value.dataHex
          // ignore: cast_nullable_to_non_nullable
          : dataHex as String?,
      decoded: decoded == const $CopyWithPlaceholder()
          ? _value.decoded
          // ignore: cast_nullable_to_non_nullable
          : decoded as Object?,
    );
  }
}

extension $DcpObservationPayloadCopyWith on DcpObservationPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDcpObservationPayload.copyWith(...)` or `instanceOfDcpObservationPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DcpObservationPayloadCWProxy get copyWith =>
      _$DcpObservationPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DcpObservationPayload _$DcpObservationPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DcpObservationPayload', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['address']);
  final val = DcpObservationPayload(
    address: $checkedConvert('address', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String?),
    agency: $checkedConvert('agency', (v) => v as String?),
    channel: $checkedConvert('channel', (v) => v as String?),
    signalDbm: $checkedConvert('signalDbm', (v) => v as num?),
    frequencyOffsetHz: $checkedConvert('frequencyOffsetHz', (v) => v as num?),
    failureCode: $checkedConvert('failureCode', (v) => v as String?),
    dataAscii: $checkedConvert('dataAscii', (v) => v as String?),
    dataHex: $checkedConvert('dataHex', (v) => v as String?),
    decoded: $checkedConvert('decoded', (v) => v),
  );
  return val;
});

Map<String, dynamic> _$DcpObservationPayloadToJson(
  DcpObservationPayload instance,
) => <String, dynamic>{
  'address': instance.address,
  'name': ?instance.name,
  'agency': ?instance.agency,
  'channel': ?instance.channel,
  'signalDbm': ?instance.signalDbm,
  'frequencyOffsetHz': ?instance.frequencyOffsetHz,
  'failureCode': ?instance.failureCode,
  'dataAscii': ?instance.dataAscii,
  'dataHex': ?instance.dataHex,
  'decoded': ?instance.decoded,
};
