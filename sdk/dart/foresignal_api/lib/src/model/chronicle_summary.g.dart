// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSummaryCWProxy {
  ChronicleSummary chronicleId(String chronicleId);

  ChronicleSummary span(TimeRange span);

  ChronicleSummary layers(List<ChronicleSummaryLayersInner> layers);

  ChronicleSummary topEntities(
    List<ChronicleSummaryTopEntitiesInner>? topEntities,
  );

  ChronicleSummary situations(List<ObjectEnvelope>? situations);

  ChronicleSummary hypothesisSets(List<ObjectEnvelope>? hypothesisSets);

  ChronicleSummary forecasts(List<ObjectEnvelope>? forecasts);

  ChronicleSummary storage(ChronicleSummaryStorage storage);

  ChronicleSummary gaps(List<ChronicleSummaryGapsInner> gaps);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSummary call({
    String chronicleId,
    TimeRange span,
    List<ChronicleSummaryLayersInner> layers,
    List<ChronicleSummaryTopEntitiesInner>? topEntities,
    List<ObjectEnvelope>? situations,
    List<ObjectEnvelope>? hypothesisSets,
    List<ObjectEnvelope>? forecasts,
    ChronicleSummaryStorage storage,
    List<ChronicleSummaryGapsInner> gaps,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSummary.copyWith(...)` or call `instanceOfChronicleSummary.copyWith.fieldName(value)` for a single field.
class _$ChronicleSummaryCWProxyImpl implements _$ChronicleSummaryCWProxy {
  const _$ChronicleSummaryCWProxyImpl(this._value);

  final ChronicleSummary _value;

  @override
  ChronicleSummary chronicleId(String chronicleId) =>
      call(chronicleId: chronicleId);

  @override
  ChronicleSummary span(TimeRange span) => call(span: span);

  @override
  ChronicleSummary layers(List<ChronicleSummaryLayersInner> layers) =>
      call(layers: layers);

  @override
  ChronicleSummary topEntities(
    List<ChronicleSummaryTopEntitiesInner>? topEntities,
  ) => call(topEntities: topEntities);

  @override
  ChronicleSummary situations(List<ObjectEnvelope>? situations) =>
      call(situations: situations);

  @override
  ChronicleSummary hypothesisSets(List<ObjectEnvelope>? hypothesisSets) =>
      call(hypothesisSets: hypothesisSets);

  @override
  ChronicleSummary forecasts(List<ObjectEnvelope>? forecasts) =>
      call(forecasts: forecasts);

  @override
  ChronicleSummary storage(ChronicleSummaryStorage storage) =>
      call(storage: storage);

  @override
  ChronicleSummary gaps(List<ChronicleSummaryGapsInner> gaps) =>
      call(gaps: gaps);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSummary call({
    Object? chronicleId = const $CopyWithPlaceholder(),
    Object? span = const $CopyWithPlaceholder(),
    Object? layers = const $CopyWithPlaceholder(),
    Object? topEntities = const $CopyWithPlaceholder(),
    Object? situations = const $CopyWithPlaceholder(),
    Object? hypothesisSets = const $CopyWithPlaceholder(),
    Object? forecasts = const $CopyWithPlaceholder(),
    Object? storage = const $CopyWithPlaceholder(),
    Object? gaps = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSummary(
      chronicleId:
          chronicleId == const $CopyWithPlaceholder() || chronicleId == null
          ? _value.chronicleId
          // ignore: cast_nullable_to_non_nullable
          : chronicleId as String,
      span: span == const $CopyWithPlaceholder() || span == null
          ? _value.span
          // ignore: cast_nullable_to_non_nullable
          : span as TimeRange,
      layers: layers == const $CopyWithPlaceholder() || layers == null
          ? _value.layers
          // ignore: cast_nullable_to_non_nullable
          : layers as List<ChronicleSummaryLayersInner>,
      topEntities: topEntities == const $CopyWithPlaceholder()
          ? _value.topEntities
          // ignore: cast_nullable_to_non_nullable
          : topEntities as List<ChronicleSummaryTopEntitiesInner>?,
      situations: situations == const $CopyWithPlaceholder()
          ? _value.situations
          // ignore: cast_nullable_to_non_nullable
          : situations as List<ObjectEnvelope>?,
      hypothesisSets: hypothesisSets == const $CopyWithPlaceholder()
          ? _value.hypothesisSets
          // ignore: cast_nullable_to_non_nullable
          : hypothesisSets as List<ObjectEnvelope>?,
      forecasts: forecasts == const $CopyWithPlaceholder()
          ? _value.forecasts
          // ignore: cast_nullable_to_non_nullable
          : forecasts as List<ObjectEnvelope>?,
      storage: storage == const $CopyWithPlaceholder() || storage == null
          ? _value.storage
          // ignore: cast_nullable_to_non_nullable
          : storage as ChronicleSummaryStorage,
      gaps: gaps == const $CopyWithPlaceholder() || gaps == null
          ? _value.gaps
          // ignore: cast_nullable_to_non_nullable
          : gaps as List<ChronicleSummaryGapsInner>,
    );
  }
}

extension $ChronicleSummaryCopyWith on ChronicleSummary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSummary.copyWith(...)` or `instanceOfChronicleSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSummaryCWProxy get copyWith => _$ChronicleSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSummary _$ChronicleSummaryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleSummary', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['chronicleId', 'span', 'layers', 'storage', 'gaps'],
  );
  final val = ChronicleSummary(
    chronicleId: $checkedConvert('chronicleId', (v) => v as String),
    span: $checkedConvert(
      'span',
      (v) => TimeRange.fromJson(v as Map<String, dynamic>),
    ),
    layers: $checkedConvert(
      'layers',
      (v) => (v as List<dynamic>)
          .map(
            (e) =>
                ChronicleSummaryLayersInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    topEntities: $checkedConvert(
      'topEntities',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => ChronicleSummaryTopEntitiesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    situations: $checkedConvert(
      'situations',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ObjectEnvelope.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    hypothesisSets: $checkedConvert(
      'hypothesisSets',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ObjectEnvelope.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    forecasts: $checkedConvert(
      'forecasts',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ObjectEnvelope.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    storage: $checkedConvert(
      'storage',
      (v) => ChronicleSummaryStorage.fromJson(v as Map<String, dynamic>),
    ),
    gaps: $checkedConvert(
      'gaps',
      (v) => (v as List<dynamic>)
          .map(
            (e) =>
                ChronicleSummaryGapsInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleSummaryToJson(
  ChronicleSummary instance,
) => <String, dynamic>{
  'chronicleId': instance.chronicleId,
  'span': instance.span.toJson(),
  'layers': instance.layers.map((e) => e.toJson()).toList(),
  'topEntities': ?instance.topEntities?.map((e) => e.toJson()).toList(),
  'situations': ?instance.situations?.map((e) => e.toJson()).toList(),
  'hypothesisSets': ?instance.hypothesisSets?.map((e) => e.toJson()).toList(),
  'forecasts': ?instance.forecasts?.map((e) => e.toJson()).toList(),
  'storage': instance.storage.toJson(),
  'gaps': instance.gaps.map((e) => e.toJson()).toList(),
};
