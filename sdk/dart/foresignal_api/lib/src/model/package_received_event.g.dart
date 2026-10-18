// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'package_received_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PackageReceivedEventCWProxy {
  PackageReceivedEvent receivedId(String receivedId);

  PackageReceivedEvent packageId(String packageId);

  PackageReceivedEvent type(PackageReceivedEventTypeEnum type);

  PackageReceivedEvent senderInstanceId(String senderInstanceId);

  PackageReceivedEvent title(String? title);

  PackageReceivedEvent tlp(Tlp tlp);

  PackageReceivedEvent signatureStatus(
    PackageReceivedEventSignatureStatusEnum? signatureStatus,
  );

  PackageReceivedEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PackageReceivedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PackageReceivedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  PackageReceivedEvent call({
    String receivedId,
    String packageId,
    PackageReceivedEventTypeEnum type,
    String senderInstanceId,
    String? title,
    Tlp tlp,
    PackageReceivedEventSignatureStatusEnum? signatureStatus,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPackageReceivedEvent.copyWith(...)` or call `instanceOfPackageReceivedEvent.copyWith.fieldName(value)` for a single field.
class _$PackageReceivedEventCWProxyImpl
    implements _$PackageReceivedEventCWProxy {
  const _$PackageReceivedEventCWProxyImpl(this._value);

  final PackageReceivedEvent _value;

  @override
  PackageReceivedEvent receivedId(String receivedId) =>
      call(receivedId: receivedId);

  @override
  PackageReceivedEvent packageId(String packageId) =>
      call(packageId: packageId);

  @override
  PackageReceivedEvent type(PackageReceivedEventTypeEnum type) =>
      call(type: type);

  @override
  PackageReceivedEvent senderInstanceId(String senderInstanceId) =>
      call(senderInstanceId: senderInstanceId);

  @override
  PackageReceivedEvent title(String? title) => call(title: title);

  @override
  PackageReceivedEvent tlp(Tlp tlp) => call(tlp: tlp);

  @override
  PackageReceivedEvent signatureStatus(
    PackageReceivedEventSignatureStatusEnum? signatureStatus,
  ) => call(signatureStatus: signatureStatus);

  @override
  PackageReceivedEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PackageReceivedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PackageReceivedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PackageReceivedEvent call({
    Object? receivedId = const $CopyWithPlaceholder(),
    Object? packageId = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? senderInstanceId = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? signatureStatus = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return PackageReceivedEvent(
      receivedId:
          receivedId == const $CopyWithPlaceholder() || receivedId == null
          ? _value.receivedId
          // ignore: cast_nullable_to_non_nullable
          : receivedId as String,
      packageId: packageId == const $CopyWithPlaceholder() || packageId == null
          ? _value.packageId
          // ignore: cast_nullable_to_non_nullable
          : packageId as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as PackageReceivedEventTypeEnum,
      senderInstanceId:
          senderInstanceId == const $CopyWithPlaceholder() ||
              senderInstanceId == null
          ? _value.senderInstanceId
          // ignore: cast_nullable_to_non_nullable
          : senderInstanceId as String,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      tlp: tlp == const $CopyWithPlaceholder() || tlp == null
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp,
      signatureStatus: signatureStatus == const $CopyWithPlaceholder()
          ? _value.signatureStatus
          // ignore: cast_nullable_to_non_nullable
          : signatureStatus as PackageReceivedEventSignatureStatusEnum?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $PackageReceivedEventCopyWith on PackageReceivedEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPackageReceivedEvent.copyWith(...)` or `instanceOfPackageReceivedEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PackageReceivedEventCWProxy get copyWith =>
      _$PackageReceivedEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PackageReceivedEvent _$PackageReceivedEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PackageReceivedEvent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'receivedId',
      'packageId',
      'type',
      'senderInstanceId',
      'tlp',
      'at',
    ],
  );
  final val = PackageReceivedEvent(
    receivedId: $checkedConvert('receivedId', (v) => v as String),
    packageId: $checkedConvert('packageId', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$PackageReceivedEventTypeEnumEnumMap, v),
    ),
    senderInstanceId: $checkedConvert('senderInstanceId', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String?),
    tlp: $checkedConvert('tlp', (v) => $enumDecode(_$TlpEnumMap, v)),
    signatureStatus: $checkedConvert(
      'signatureStatus',
      (v) => $enumDecodeNullable(
        _$PackageReceivedEventSignatureStatusEnumEnumMap,
        v,
      ),
    ),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$PackageReceivedEventToJson(
  PackageReceivedEvent instance,
) => <String, dynamic>{
  'receivedId': instance.receivedId,
  'packageId': instance.packageId,
  'type': _$PackageReceivedEventTypeEnumEnumMap[instance.type]!,
  'senderInstanceId': instance.senderInstanceId,
  'title': ?instance.title,
  'tlp': _$TlpEnumMap[instance.tlp]!,
  'signatureStatus':
      ?_$PackageReceivedEventSignatureStatusEnumEnumMap[instance
          .signatureStatus],
  'at': instance.at.toIso8601String(),
};

const _$PackageReceivedEventTypeEnumEnumMap = {
  PackageReceivedEventTypeEnum.report: 'report',
  PackageReceivedEventTypeEnum.investigation: 'investigation',
  PackageReceivedEventTypeEnum.chronicleExtract: 'chronicle-extract',
  PackageReceivedEventTypeEnum.evidence: 'evidence',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};

const _$PackageReceivedEventSignatureStatusEnumEnumMap = {
  PackageReceivedEventSignatureStatusEnum.valid: 'valid',
  PackageReceivedEventSignatureStatusEnum.invalid: 'invalid',
  PackageReceivedEventSignatureStatusEnum.unknownKey: 'unknown-key',
};
