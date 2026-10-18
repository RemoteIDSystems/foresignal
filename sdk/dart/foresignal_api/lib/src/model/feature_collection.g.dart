// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'feature_collection.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FeatureCollectionCWProxy {
  FeatureCollection type(FeatureCollectionTypeEnum type);

  FeatureCollection features(List<Feature> features);

  FeatureCollection nextCursor(String? nextCursor);

  FeatureCollection attributions(List<Attribution>? attributions);

  FeatureCollection watermarkSeq(int? watermarkSeq);

  FeatureCollection asOfSeq(int? asOfSeq);

  FeatureCollection asOf(DateTime? asOf);

  FeatureCollection approximate(bool? approximate);

  FeatureCollection horizon(DateTime? horizon);

  FeatureCollection licenceFiltered(int? licenceFiltered);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FeatureCollection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FeatureCollection(...).copyWith(id: 12, name: "My name")
  /// ```
  FeatureCollection call({
    FeatureCollectionTypeEnum type,
    List<Feature> features,
    String? nextCursor,
    List<Attribution>? attributions,
    int? watermarkSeq,
    int? asOfSeq,
    DateTime? asOf,
    bool? approximate,
    DateTime? horizon,
    int? licenceFiltered,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFeatureCollection.copyWith(...)` or call `instanceOfFeatureCollection.copyWith.fieldName(value)` for a single field.
class _$FeatureCollectionCWProxyImpl implements _$FeatureCollectionCWProxy {
  const _$FeatureCollectionCWProxyImpl(this._value);

  final FeatureCollection _value;

  @override
  FeatureCollection type(FeatureCollectionTypeEnum type) => call(type: type);

  @override
  FeatureCollection features(List<Feature> features) =>
      call(features: features);

  @override
  FeatureCollection nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  @override
  FeatureCollection attributions(List<Attribution>? attributions) =>
      call(attributions: attributions);

  @override
  FeatureCollection watermarkSeq(int? watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  @override
  FeatureCollection asOfSeq(int? asOfSeq) => call(asOfSeq: asOfSeq);

  @override
  FeatureCollection asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  FeatureCollection approximate(bool? approximate) =>
      call(approximate: approximate);

  @override
  FeatureCollection horizon(DateTime? horizon) => call(horizon: horizon);

  @override
  FeatureCollection licenceFiltered(int? licenceFiltered) =>
      call(licenceFiltered: licenceFiltered);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FeatureCollection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FeatureCollection(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FeatureCollection call({
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
  }) {
    return FeatureCollection(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as FeatureCollectionTypeEnum,
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
    );
  }
}

extension $FeatureCollectionCopyWith on FeatureCollection {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFeatureCollection.copyWith(...)` or `instanceOfFeatureCollection.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FeatureCollectionCWProxy get copyWith =>
      _$FeatureCollectionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FeatureCollection _$FeatureCollectionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FeatureCollection', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'features']);
      final val = FeatureCollection(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$FeatureCollectionTypeEnumEnumMap, v),
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
        watermarkSeq: $checkedConvert(
          'watermarkSeq',
          (v) => (v as num?)?.toInt(),
        ),
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
      );
      return val;
    });

Map<String, dynamic> _$FeatureCollectionToJson(FeatureCollection instance) =>
    <String, dynamic>{
      'type': _$FeatureCollectionTypeEnumEnumMap[instance.type]!,
      'features': instance.features.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
      'attributions': ?instance.attributions?.map((e) => e.toJson()).toList(),
      'watermarkSeq': ?instance.watermarkSeq,
      'asOfSeq': ?instance.asOfSeq,
      'asOf': ?instance.asOf?.toIso8601String(),
      'approximate': ?instance.approximate,
      'horizon': ?instance.horizon?.toIso8601String(),
      'licenceFiltered': ?instance.licenceFiltered,
    };

const _$FeatureCollectionTypeEnumEnumMap = {
  FeatureCollectionTypeEnum.featureCollection: 'FeatureCollection',
};
