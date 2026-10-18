// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'entity_link.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EntityLinkCWProxy {
  EntityLink sourceId(String sourceId);

  EntityLink externalId(String externalId);

  EntityLink keyType(EntityLinkKeyTypeEnum keyType);

  EntityLink keyValue(String keyValue);

  EntityLink confidence(num confidence);

  EntityLink validFrom(DateTime validFrom);

  EntityLink validTo(DateTime? validTo);

  EntityLink firstSeq(int? firstSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EntityLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EntityLink(...).copyWith(id: 12, name: "My name")
  /// ```
  EntityLink call({
    String sourceId,
    String externalId,
    EntityLinkKeyTypeEnum keyType,
    String keyValue,
    num confidence,
    DateTime validFrom,
    DateTime? validTo,
    int? firstSeq,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEntityLink.copyWith(...)` or call `instanceOfEntityLink.copyWith.fieldName(value)` for a single field.
class _$EntityLinkCWProxyImpl implements _$EntityLinkCWProxy {
  const _$EntityLinkCWProxyImpl(this._value);

  final EntityLink _value;

  @override
  EntityLink sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  EntityLink externalId(String externalId) => call(externalId: externalId);

  @override
  EntityLink keyType(EntityLinkKeyTypeEnum keyType) => call(keyType: keyType);

  @override
  EntityLink keyValue(String keyValue) => call(keyValue: keyValue);

  @override
  EntityLink confidence(num confidence) => call(confidence: confidence);

  @override
  EntityLink validFrom(DateTime validFrom) => call(validFrom: validFrom);

  @override
  EntityLink validTo(DateTime? validTo) => call(validTo: validTo);

  @override
  EntityLink firstSeq(int? firstSeq) => call(firstSeq: firstSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EntityLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EntityLink(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EntityLink call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? keyType = const $CopyWithPlaceholder(),
    Object? keyValue = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? validTo = const $CopyWithPlaceholder(),
    Object? firstSeq = const $CopyWithPlaceholder(),
  }) {
    return EntityLink(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      keyType: keyType == const $CopyWithPlaceholder() || keyType == null
          ? _value.keyType
          // ignore: cast_nullable_to_non_nullable
          : keyType as EntityLinkKeyTypeEnum,
      keyValue: keyValue == const $CopyWithPlaceholder() || keyValue == null
          ? _value.keyValue
          // ignore: cast_nullable_to_non_nullable
          : keyValue as String,
      confidence:
          confidence == const $CopyWithPlaceholder() || confidence == null
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num,
      validFrom: validFrom == const $CopyWithPlaceholder() || validFrom == null
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime,
      validTo: validTo == const $CopyWithPlaceholder()
          ? _value.validTo
          // ignore: cast_nullable_to_non_nullable
          : validTo as DateTime?,
      firstSeq: firstSeq == const $CopyWithPlaceholder()
          ? _value.firstSeq
          // ignore: cast_nullable_to_non_nullable
          : firstSeq as int?,
    );
  }
}

extension $EntityLinkCopyWith on EntityLink {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEntityLink.copyWith(...)` or `instanceOfEntityLink.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EntityLinkCWProxy get copyWith => _$EntityLinkCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EntityLink _$EntityLinkFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EntityLink', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'sourceId',
          'externalId',
          'keyType',
          'keyValue',
          'confidence',
          'validFrom',
        ],
      );
      final val = EntityLink(
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        externalId: $checkedConvert('externalId', (v) => v as String),
        keyType: $checkedConvert(
          'keyType',
          (v) => $enumDecode(_$EntityLinkKeyTypeEnumEnumMap, v),
        ),
        keyValue: $checkedConvert('keyValue', (v) => v as String),
        confidence: $checkedConvert('confidence', (v) => v as num),
        validFrom: $checkedConvert(
          'validFrom',
          (v) => DateTime.parse(v as String),
        ),
        validTo: $checkedConvert(
          'validTo',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        firstSeq: $checkedConvert('firstSeq', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$EntityLinkToJson(EntityLink instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'externalId': instance.externalId,
      'keyType': _$EntityLinkKeyTypeEnumEnumMap[instance.keyType]!,
      'keyValue': instance.keyValue,
      'confidence': instance.confidence,
      'validFrom': instance.validFrom.toIso8601String(),
      'validTo': ?instance.validTo?.toIso8601String(),
      'firstSeq': ?instance.firstSeq,
    };

const _$EntityLinkKeyTypeEnumEnumMap = {
  EntityLinkKeyTypeEnum.airframe: 'airframe',
  EntityLinkKeyTypeEnum.icao24: 'icao24',
  EntityLinkKeyTypeEnum.registration: 'registration',
  EntityLinkKeyTypeEnum.imo: 'imo',
  EntityLinkKeyTypeEnum.mmsi: 'mmsi',
  EntityLinkKeyTypeEnum.norad: 'norad',
  EntityLinkKeyTypeEnum.cospar: 'cospar',
  EntityLinkKeyTypeEnum.callsign: 'callsign',
};
