// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_records_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestRecordsRequestCWProxy {
  IngestRecordsRequest type(IngestRecordsRequestTypeEnum type);

  IngestRecordsRequest features(List<Feature> features);

  IngestRecordsRequest nextCursor(String? nextCursor);

  IngestRecordsRequest attributions(List<Attribution>? attributions);

  IngestRecordsRequest watermarkSeq(int? watermarkSeq);

  IngestRecordsRequest asOfSeq(int? asOfSeq);

  IngestRecordsRequest asOf(DateTime? asOf);

  IngestRecordsRequest approximate(bool? approximate);

  IngestRecordsRequest horizon(DateTime? horizon);

  IngestRecordsRequest licenceFiltered(int? licenceFiltered);

  IngestRecordsRequest records(List<NormalizedRecordPreview> records);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestRecordsRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestRecordsRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestRecordsRequest call({
    IngestRecordsRequestTypeEnum type,
    List<Feature> features,
    String? nextCursor,
    List<Attribution>? attributions,
    int? watermarkSeq,
    int? asOfSeq,
    DateTime? asOf,
    bool? approximate,
    DateTime? horizon,
    int? licenceFiltered,
    List<NormalizedRecordPreview> records,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestRecordsRequest.copyWith(...)` or call `instanceOfIngestRecordsRequest.copyWith.fieldName(value)` for a single field.
class _$IngestRecordsRequestCWProxyImpl
    implements _$IngestRecordsRequestCWProxy {
  const _$IngestRecordsRequestCWProxyImpl(this._value);

  final IngestRecordsRequest _value;

  @override
  IngestRecordsRequest type(IngestRecordsRequestTypeEnum type) =>
      call(type: type);

  @override
  IngestRecordsRequest features(List<Feature> features) =>
      call(features: features);

  @override
  IngestRecordsRequest nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  @override
  IngestRecordsRequest attributions(List<Attribution>? attributions) =>
      call(attributions: attributions);

  @override
  IngestRecordsRequest watermarkSeq(int? watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  @override
  IngestRecordsRequest asOfSeq(int? asOfSeq) => call(asOfSeq: asOfSeq);

  @override
  IngestRecordsRequest asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  IngestRecordsRequest approximate(bool? approximate) =>
      call(approximate: approximate);

  @override
  IngestRecordsRequest horizon(DateTime? horizon) => call(horizon: horizon);

  @override
  IngestRecordsRequest licenceFiltered(int? licenceFiltered) =>
      call(licenceFiltered: licenceFiltered);

  @override
  IngestRecordsRequest records(List<NormalizedRecordPreview> records) =>
      call(records: records);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestRecordsRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestRecordsRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestRecordsRequest call({
    Object? type = const $CopyWithPlaceholder(),
    Object? features = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
    Object? attributions = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
    Object? asOfSeq = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? approximate = const $CopyWithPlaceholder(),
    Object? horizon = const $CopyWithPlaceholder(),
    Object? licenceFiltered = const $CopyWithPlaceholder(),
    Object? records = const $CopyWithPlaceholder(),
  }) {
    return IngestRecordsRequest(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as IngestRecordsRequestTypeEnum,
      features: features == const $CopyWithPlaceholder() || features == null
          ? _value.features
          // ignore: cast_nullable_to_non_nullable
          : features as List<Feature>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
      attributions: attributions == const $CopyWithPlaceholder()
          ? _value.attributions
          // ignore: cast_nullable_to_non_nullable
          : attributions as List<Attribution>?,
      watermarkSeq: watermarkSeq == const $CopyWithPlaceholder()
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int?,
      asOfSeq: asOfSeq == const $CopyWithPlaceholder()
          ? _value.asOfSeq
          // ignore: cast_nullable_to_non_nullable
          : asOfSeq as int?,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      approximate: approximate == const $CopyWithPlaceholder()
          ? _value.approximate
          // ignore: cast_nullable_to_non_nullable
          : approximate as bool?,
      horizon: horizon == const $CopyWithPlaceholder()
          ? _value.horizon
          // ignore: cast_nullable_to_non_nullable
          : horizon as DateTime?,
      licenceFiltered: licenceFiltered == const $CopyWithPlaceholder()
          ? _value.licenceFiltered
          // ignore: cast_nullable_to_non_nullable
          : licenceFiltered as int?,
      records: records == const $CopyWithPlaceholder() || records == null
          ? _value.records
          // ignore: cast_nullable_to_non_nullable
          : records as List<NormalizedRecordPreview>,
    );
  }
}

extension $IngestRecordsRequestCopyWith on IngestRecordsRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestRecordsRequest.copyWith(...)` or `instanceOfIngestRecordsRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestRecordsRequestCWProxy get copyWith =>
      _$IngestRecordsRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestRecordsRequest _$IngestRecordsRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IngestRecordsRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type', 'features', 'records']);
  final val = IngestRecordsRequest(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$IngestRecordsRequestTypeEnumEnumMap, v),
    ),
    features: $checkedConvert(
      'features',
      (v) => (v as List<dynamic>)
          .map((e) => Feature.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
    attributions: $checkedConvert(
      'attributions',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Attribution.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num?)?.toInt()),
    asOfSeq: $checkedConvert('asOfSeq', (v) => (v as num?)?.toInt()),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    approximate: $checkedConvert('approximate', (v) => v as bool?),
    horizon: $checkedConvert(
      'horizon',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    licenceFiltered: $checkedConvert(
      'licenceFiltered',
      (v) => (v as num?)?.toInt(),
    ),
    records: $checkedConvert(
      'records',
      (v) => (v as List<dynamic>)
          .map(
            (e) => NormalizedRecordPreview.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$IngestRecordsRequestToJson(
  IngestRecordsRequest instance,
) => <String, dynamic>{
  'type': _$IngestRecordsRequestTypeEnumEnumMap[instance.type]!,
  'features': instance.features.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
  'attributions': ?instance.attributions?.map((e) => e.toJson()).toList(),
  'watermarkSeq': ?instance.watermarkSeq,
  'asOfSeq': ?instance.asOfSeq,
  'asOf': ?instance.asOf?.toIso8601String(),
  'approximate': ?instance.approximate,
  'horizon': ?instance.horizon?.toIso8601String(),
  'licenceFiltered': ?instance.licenceFiltered,
  'records': instance.records.map((e) => e.toJson()).toList(),
};

const _$IngestRecordsRequestTypeEnumEnumMap = {
  IngestRecordsRequestTypeEnum.featureCollection: 'FeatureCollection',
};
