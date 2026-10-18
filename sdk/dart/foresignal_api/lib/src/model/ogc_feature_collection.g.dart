// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_feature_collection.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcFeatureCollectionCWProxy {
  OgcFeatureCollection type(OgcFeatureCollectionTypeEnum type);

  OgcFeatureCollection features(List<OgcFeature> features);

  OgcFeatureCollection links(List<OgcLink> links);

  OgcFeatureCollection numberReturned(int numberReturned);

  OgcFeatureCollection numberMatched(int? numberMatched);

  OgcFeatureCollection timeStamp(DateTime timeStamp);

  OgcFeatureCollection attributions(List<Attribution>? attributions);

  OgcFeatureCollection licenceFiltered(int? licenceFiltered);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcFeatureCollection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcFeatureCollection(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcFeatureCollection call({
    OgcFeatureCollectionTypeEnum type,
    List<OgcFeature> features,
    List<OgcLink> links,
    int numberReturned,
    int? numberMatched,
    DateTime timeStamp,
    List<Attribution>? attributions,
    int? licenceFiltered,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcFeatureCollection.copyWith(...)` or call `instanceOfOgcFeatureCollection.copyWith.fieldName(value)` for a single field.
class _$OgcFeatureCollectionCWProxyImpl
    implements _$OgcFeatureCollectionCWProxy {
  const _$OgcFeatureCollectionCWProxyImpl(this._value);

  final OgcFeatureCollection _value;

  @override
  OgcFeatureCollection type(OgcFeatureCollectionTypeEnum type) =>
      call(type: type);

  @override
  OgcFeatureCollection features(List<OgcFeature> features) =>
      call(features: features);

  @override
  OgcFeatureCollection links(List<OgcLink> links) => call(links: links);

  @override
  OgcFeatureCollection numberReturned(int numberReturned) =>
      call(numberReturned: numberReturned);

  @override
  OgcFeatureCollection numberMatched(int? numberMatched) =>
      call(numberMatched: numberMatched);

  @override
  OgcFeatureCollection timeStamp(DateTime timeStamp) =>
      call(timeStamp: timeStamp);

  @override
  OgcFeatureCollection attributions(List<Attribution>? attributions) =>
      call(attributions: attributions);

  @override
  OgcFeatureCollection licenceFiltered(int? licenceFiltered) =>
      call(licenceFiltered: licenceFiltered);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcFeatureCollection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcFeatureCollection(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcFeatureCollection call({
    Object? type = const $CopyWithPlaceholder(),
    Object? features = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? numberReturned = const $CopyWithPlaceholder(),
    Object? numberMatched = const $CopyWithPlaceholder(),
    Object? timeStamp = const $CopyWithPlaceholder(),
    Object? attributions = const $CopyWithPlaceholder(),
    Object? licenceFiltered = const $CopyWithPlaceholder(),
  }) {
    return OgcFeatureCollection(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as OgcFeatureCollectionTypeEnum,
      features: features == const $CopyWithPlaceholder() || features == null
          ? _value.features
          // ignore: cast_nullable_to_non_nullable
          : features as List<OgcFeature>,
      links: links == const $CopyWithPlaceholder() || links == null
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<OgcLink>,
      numberReturned:
          numberReturned == const $CopyWithPlaceholder() ||
              numberReturned == null
          ? _value.numberReturned
          // ignore: cast_nullable_to_non_nullable
          : numberReturned as int,
      numberMatched: numberMatched == const $CopyWithPlaceholder()
          ? _value.numberMatched
          // ignore: cast_nullable_to_non_nullable
          : numberMatched as int?,
      timeStamp: timeStamp == const $CopyWithPlaceholder() || timeStamp == null
          ? _value.timeStamp
          // ignore: cast_nullable_to_non_nullable
          : timeStamp as DateTime,
      attributions: attributions == const $CopyWithPlaceholder()
          ? _value.attributions
          // ignore: cast_nullable_to_non_nullable
          : attributions as List<Attribution>?,
      licenceFiltered: licenceFiltered == const $CopyWithPlaceholder()
          ? _value.licenceFiltered
          // ignore: cast_nullable_to_non_nullable
          : licenceFiltered as int?,
    );
  }
}

extension $OgcFeatureCollectionCopyWith on OgcFeatureCollection {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcFeatureCollection.copyWith(...)` or `instanceOfOgcFeatureCollection.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcFeatureCollectionCWProxy get copyWith =>
      _$OgcFeatureCollectionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcFeatureCollection _$OgcFeatureCollectionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OgcFeatureCollection', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'type',
      'features',
      'links',
      'numberReturned',
      'timeStamp',
    ],
  );
  final val = OgcFeatureCollection(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$OgcFeatureCollectionTypeEnumEnumMap, v),
    ),
    features: $checkedConvert(
      'features',
      (v) => (v as List<dynamic>)
          .map((e) => OgcFeature.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    links: $checkedConvert(
      'links',
      (v) => (v as List<dynamic>)
          .map((e) => OgcLink.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    numberReturned: $checkedConvert(
      'numberReturned',
      (v) => (v as num).toInt(),
    ),
    numberMatched: $checkedConvert(
      'numberMatched',
      (v) => (v as num?)?.toInt(),
    ),
    timeStamp: $checkedConvert('timeStamp', (v) => DateTime.parse(v as String)),
    attributions: $checkedConvert(
      'attributions',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Attribution.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    licenceFiltered: $checkedConvert(
      'licenceFiltered',
      (v) => (v as num?)?.toInt(),
    ),
  );
  return val;
});

Map<String, dynamic> _$OgcFeatureCollectionToJson(
  OgcFeatureCollection instance,
) => <String, dynamic>{
  'type': _$OgcFeatureCollectionTypeEnumEnumMap[instance.type]!,
  'features': instance.features.map((e) => e.toJson()).toList(),
  'links': instance.links.map((e) => e.toJson()).toList(),
  'numberReturned': instance.numberReturned,
  'numberMatched': ?instance.numberMatched,
  'timeStamp': instance.timeStamp.toIso8601String(),
  'attributions': ?instance.attributions?.map((e) => e.toJson()).toList(),
  'licenceFiltered': ?instance.licenceFiltered,
};

const _$OgcFeatureCollectionTypeEnumEnumMap = {
  OgcFeatureCollectionTypeEnum.featureCollection: 'FeatureCollection',
};
