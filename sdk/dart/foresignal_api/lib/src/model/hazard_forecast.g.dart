// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hazard_forecast.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HazardForecastCWProxy {
  HazardForecast hazardType(HazardForecastHazardTypeEnum hazardType);

  HazardForecast sourceRef(String sourceRef);

  HazardForecast issuer(String? issuer);

  HazardForecast issuedAt(DateTime issuedAt);

  HazardForecast validFrom(DateTime validFrom);

  HazardForecast validTo(DateTime validTo);

  HazardForecast geometry(Geometry? geometry);

  HazardForecast probabilityField(
    List<HazardForecastProbabilityFieldInner>? probabilityField,
  );

  HazardForecast probabilityModel(
    HazardForecastProbabilityModelEnum probabilityModel,
  );

  HazardForecast category(String? category);

  HazardForecast probabilityRange(
    HazardForecastProbabilityRange? probabilityRange,
  );

  HazardForecast note(String? note);

  HazardForecast hazardKey(String? hazardKey);

  HazardForecast sourceRefs(List<String>? sourceRefs);

  HazardForecast pointRadiusKm(num? pointRadiusKm);

  HazardForecast details(Map<String, Object>? details);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HazardForecast(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HazardForecast(...).copyWith(id: 12, name: "My name")
  /// ```
  HazardForecast call({
    HazardForecastHazardTypeEnum hazardType,
    String sourceRef,
    String? issuer,
    DateTime issuedAt,
    DateTime validFrom,
    DateTime validTo,
    Geometry? geometry,
    List<HazardForecastProbabilityFieldInner>? probabilityField,
    HazardForecastProbabilityModelEnum probabilityModel,
    String? category,
    HazardForecastProbabilityRange? probabilityRange,
    String? note,
    String? hazardKey,
    List<String>? sourceRefs,
    num? pointRadiusKm,
    Map<String, Object>? details,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHazardForecast.copyWith(...)` or call `instanceOfHazardForecast.copyWith.fieldName(value)` for a single field.
class _$HazardForecastCWProxyImpl implements _$HazardForecastCWProxy {
  const _$HazardForecastCWProxyImpl(this._value);

  final HazardForecast _value;

  @override
  HazardForecast hazardType(HazardForecastHazardTypeEnum hazardType) =>
      call(hazardType: hazardType);

  @override
  HazardForecast sourceRef(String sourceRef) => call(sourceRef: sourceRef);

  @override
  HazardForecast issuer(String? issuer) => call(issuer: issuer);

  @override
  HazardForecast issuedAt(DateTime issuedAt) => call(issuedAt: issuedAt);

  @override
  HazardForecast validFrom(DateTime validFrom) => call(validFrom: validFrom);

  @override
  HazardForecast validTo(DateTime validTo) => call(validTo: validTo);

  @override
  HazardForecast geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  HazardForecast probabilityField(
    List<HazardForecastProbabilityFieldInner>? probabilityField,
  ) => call(probabilityField: probabilityField);

  @override
  HazardForecast probabilityModel(
    HazardForecastProbabilityModelEnum probabilityModel,
  ) => call(probabilityModel: probabilityModel);

  @override
  HazardForecast category(String? category) => call(category: category);

  @override
  HazardForecast probabilityRange(
    HazardForecastProbabilityRange? probabilityRange,
  ) => call(probabilityRange: probabilityRange);

  @override
  HazardForecast note(String? note) => call(note: note);

  @override
  HazardForecast hazardKey(String? hazardKey) => call(hazardKey: hazardKey);

  @override
  HazardForecast sourceRefs(List<String>? sourceRefs) =>
      call(sourceRefs: sourceRefs);

  @override
  HazardForecast pointRadiusKm(num? pointRadiusKm) =>
      call(pointRadiusKm: pointRadiusKm);

  @override
  HazardForecast details(Map<String, Object>? details) =>
      call(details: details);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HazardForecast(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HazardForecast(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HazardForecast call({
    Object? hazardType = const $CopyWithPlaceholder(),
    Object? sourceRef = const $CopyWithPlaceholder(),
    Object? issuer = const $CopyWithPlaceholder(),
    Object? issuedAt = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? validTo = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? probabilityField = const $CopyWithPlaceholder(),
    Object? probabilityModel = const $CopyWithPlaceholder(),
    Object? category = const $CopyWithPlaceholder(),
    Object? probabilityRange = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
    Object? hazardKey = const $CopyWithPlaceholder(),
    Object? sourceRefs = const $CopyWithPlaceholder(),
    Object? pointRadiusKm = const $CopyWithPlaceholder(),
    Object? details = const $CopyWithPlaceholder(),
  }) {
    return HazardForecast(
      hazardType:
          hazardType == const $CopyWithPlaceholder() || hazardType == null
          ? _value.hazardType
          // ignore: cast_nullable_to_non_nullable
          : hazardType as HazardForecastHazardTypeEnum,
      sourceRef: sourceRef == const $CopyWithPlaceholder() || sourceRef == null
          ? _value.sourceRef
          // ignore: cast_nullable_to_non_nullable
          : sourceRef as String,
      issuer: issuer == const $CopyWithPlaceholder()
          ? _value.issuer
          // ignore: cast_nullable_to_non_nullable
          : issuer as String?,
      issuedAt: issuedAt == const $CopyWithPlaceholder() || issuedAt == null
          ? _value.issuedAt
          // ignore: cast_nullable_to_non_nullable
          : issuedAt as DateTime,
      validFrom: validFrom == const $CopyWithPlaceholder() || validFrom == null
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime,
      validTo: validTo == const $CopyWithPlaceholder() || validTo == null
          ? _value.validTo
          // ignore: cast_nullable_to_non_nullable
          : validTo as DateTime,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      probabilityField: probabilityField == const $CopyWithPlaceholder()
          ? _value.probabilityField
          // ignore: cast_nullable_to_non_nullable
          : probabilityField as List<HazardForecastProbabilityFieldInner>?,
      probabilityModel:
          probabilityModel == const $CopyWithPlaceholder() ||
              probabilityModel == null
          ? _value.probabilityModel
          // ignore: cast_nullable_to_non_nullable
          : probabilityModel as HazardForecastProbabilityModelEnum,
      category: category == const $CopyWithPlaceholder()
          ? _value.category
          // ignore: cast_nullable_to_non_nullable
          : category as String?,
      probabilityRange: probabilityRange == const $CopyWithPlaceholder()
          ? _value.probabilityRange
          // ignore: cast_nullable_to_non_nullable
          : probabilityRange as HazardForecastProbabilityRange?,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
      hazardKey: hazardKey == const $CopyWithPlaceholder()
          ? _value.hazardKey
          // ignore: cast_nullable_to_non_nullable
          : hazardKey as String?,
      sourceRefs: sourceRefs == const $CopyWithPlaceholder()
          ? _value.sourceRefs
          // ignore: cast_nullable_to_non_nullable
          : sourceRefs as List<String>?,
      pointRadiusKm: pointRadiusKm == const $CopyWithPlaceholder()
          ? _value.pointRadiusKm
          // ignore: cast_nullable_to_non_nullable
          : pointRadiusKm as num?,
      details: details == const $CopyWithPlaceholder()
          ? _value.details
          // ignore: cast_nullable_to_non_nullable
          : details as Map<String, Object>?,
    );
  }
}

extension $HazardForecastCopyWith on HazardForecast {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHazardForecast.copyWith(...)` or `instanceOfHazardForecast.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HazardForecastCWProxy get copyWith => _$HazardForecastCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HazardForecast _$HazardForecastFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HazardForecast', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'hazardType',
      'sourceRef',
      'issuedAt',
      'validFrom',
      'validTo',
      'probabilityModel',
    ],
  );
  final val = HazardForecast(
    hazardType: $checkedConvert(
      'hazardType',
      (v) => $enumDecode(_$HazardForecastHazardTypeEnumEnumMap, v),
    ),
    sourceRef: $checkedConvert('sourceRef', (v) => v as String),
    issuer: $checkedConvert('issuer', (v) => v as String? ?? 'official'),
    issuedAt: $checkedConvert('issuedAt', (v) => DateTime.parse(v as String)),
    validFrom: $checkedConvert('validFrom', (v) => DateTime.parse(v as String)),
    validTo: $checkedConvert('validTo', (v) => DateTime.parse(v as String)),
    geometry: $checkedConvert(
      'geometry',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    probabilityField: $checkedConvert(
      'probabilityField',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => HazardForecastProbabilityFieldInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    probabilityModel: $checkedConvert(
      'probabilityModel',
      (v) => $enumDecode(_$HazardForecastProbabilityModelEnumEnumMap, v),
    ),
    category: $checkedConvert('category', (v) => v as String?),
    probabilityRange: $checkedConvert(
      'probabilityRange',
      (v) => v == null
          ? null
          : HazardForecastProbabilityRange.fromJson(v as Map<String, dynamic>),
    ),
    note: $checkedConvert('note', (v) => v as String?),
    hazardKey: $checkedConvert('hazardKey', (v) => v as String?),
    sourceRefs: $checkedConvert(
      'sourceRefs',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    pointRadiusKm: $checkedConvert('pointRadiusKm', (v) => v as num?),
    details: $checkedConvert(
      'details',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
});

Map<String, dynamic> _$HazardForecastToJson(
  HazardForecast instance,
) => <String, dynamic>{
  'hazardType': _$HazardForecastHazardTypeEnumEnumMap[instance.hazardType]!,
  'sourceRef': instance.sourceRef,
  'issuer': ?instance.issuer,
  'issuedAt': instance.issuedAt.toIso8601String(),
  'validFrom': instance.validFrom.toIso8601String(),
  'validTo': instance.validTo.toIso8601String(),
  'geometry': ?instance.geometry?.toJson(),
  'probabilityField': ?instance.probabilityField
      ?.map((e) => e.toJson())
      .toList(),
  'probabilityModel':
      _$HazardForecastProbabilityModelEnumEnumMap[instance.probabilityModel]!,
  'category': ?instance.category,
  'probabilityRange': ?instance.probabilityRange?.toJson(),
  'note': ?instance.note,
  'hazardKey': ?instance.hazardKey,
  'sourceRefs': ?instance.sourceRefs,
  'pointRadiusKm': ?instance.pointRadiusKm,
  'details': ?instance.details,
};

const _$HazardForecastHazardTypeEnumEnumMap = {
  HazardForecastHazardTypeEnum.tropicalWind34: 'tropical-wind-34',
  HazardForecastHazardTypeEnum.tropicalWind50: 'tropical-wind-50',
  HazardForecastHazardTypeEnum.tropicalWind64: 'tropical-wind-64',
  HazardForecastHazardTypeEnum.tropicalTrack: 'tropical-track',
  HazardForecastHazardTypeEnum.tornado: 'tornado',
  HazardForecastHazardTypeEnum.wind: 'wind',
  HazardForecastHazardTypeEnum.hail: 'hail',
  HazardForecastHazardTypeEnum.severeCategorical: 'severe-categorical',
  HazardForecastHazardTypeEnum.excessiveRain: 'excessive-rain',
  HazardForecastHazardTypeEnum.riverFlood: 'river-flood',
  HazardForecastHazardTypeEnum.stormCell: 'storm-cell',
};

const _$HazardForecastProbabilityModelEnumEnumMap = {
  HazardForecastProbabilityModelEnum.publishedPolygons: 'published-polygons',
  HazardForecastProbabilityModelEnum.publishedPoint: 'published-point',
  HazardForecastProbabilityModelEnum.publishedCategoryRange:
      'published-category-range',
  HazardForecastProbabilityModelEnum.gaugeCategory: 'gauge-category',
  HazardForecastProbabilityModelEnum.trackErrorEllipse: 'track-error-ellipse',
  HazardForecastProbabilityModelEnum.none: 'none',
};
