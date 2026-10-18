// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_profile_scope.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystProfileScopeCWProxy {
  AnalystProfileScope sources(List<String>? sources);

  AnalystProfileScope kinds(List<String>? kinds);

  AnalystProfileScope places(List<String>? places);

  AnalystProfileScope bbox(List<num>? bbox);

  AnalystProfileScope eventTypes(
    List<AnalystProfileScopeEventTypesEnum>? eventTypes,
  );

  AnalystProfileScope ruleTags(List<String>? ruleTags);

  AnalystProfileScope minSeverity(Severity? minSeverity);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystProfileScope(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystProfileScope(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystProfileScope call({
    List<String>? sources,
    List<String>? kinds,
    List<String>? places,
    List<num>? bbox,
    List<AnalystProfileScopeEventTypesEnum>? eventTypes,
    List<String>? ruleTags,
    Severity? minSeverity,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystProfileScope.copyWith(...)` or call `instanceOfAnalystProfileScope.copyWith.fieldName(value)` for a single field.
class _$AnalystProfileScopeCWProxyImpl implements _$AnalystProfileScopeCWProxy {
  const _$AnalystProfileScopeCWProxyImpl(this._value);

  final AnalystProfileScope _value;

  @override
  AnalystProfileScope sources(List<String>? sources) => call(sources: sources);

  @override
  AnalystProfileScope kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  AnalystProfileScope places(List<String>? places) => call(places: places);

  @override
  AnalystProfileScope bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  AnalystProfileScope eventTypes(
    List<AnalystProfileScopeEventTypesEnum>? eventTypes,
  ) => call(eventTypes: eventTypes);

  @override
  AnalystProfileScope ruleTags(List<String>? ruleTags) =>
      call(ruleTags: ruleTags);

  @override
  AnalystProfileScope minSeverity(Severity? minSeverity) =>
      call(minSeverity: minSeverity);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystProfileScope(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystProfileScope(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystProfileScope call({
    Object? sources = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? places = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? eventTypes = const $CopyWithPlaceholder(),
    Object? ruleTags = const $CopyWithPlaceholder(),
    Object? minSeverity = const $CopyWithPlaceholder(),
  }) {
    return AnalystProfileScope(
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      places: places == const $CopyWithPlaceholder()
          ? _value.places
          // ignore: cast_nullable_to_non_nullable
          : places as List<String>?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      eventTypes: eventTypes == const $CopyWithPlaceholder()
          ? _value.eventTypes
          // ignore: cast_nullable_to_non_nullable
          : eventTypes as List<AnalystProfileScopeEventTypesEnum>?,
      ruleTags: ruleTags == const $CopyWithPlaceholder()
          ? _value.ruleTags
          // ignore: cast_nullable_to_non_nullable
          : ruleTags as List<String>?,
      minSeverity: minSeverity == const $CopyWithPlaceholder()
          ? _value.minSeverity
          // ignore: cast_nullable_to_non_nullable
          : minSeverity as Severity?,
    );
  }
}

extension $AnalystProfileScopeCopyWith on AnalystProfileScope {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystProfileScope.copyWith(...)` or `instanceOfAnalystProfileScope.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystProfileScopeCWProxy get copyWith =>
      _$AnalystProfileScopeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystProfileScope _$AnalystProfileScopeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalystProfileScope', json, ($checkedConvert) {
      final val = AnalystProfileScope(
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        kinds: $checkedConvert(
          'kinds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        places: $checkedConvert(
          'places',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        bbox: $checkedConvert(
          'bbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        eventTypes: $checkedConvert(
          'eventTypes',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) =>
                    $enumDecode(_$AnalystProfileScopeEventTypesEnumEnumMap, e),
              )
              .toList(),
        ),
        ruleTags: $checkedConvert(
          'ruleTags',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        minSeverity: $checkedConvert(
          'minSeverity',
          (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AnalystProfileScopeToJson(
  AnalystProfileScope instance,
) => <String, dynamic>{
  'sources': ?instance.sources,
  'kinds': ?instance.kinds,
  'places': ?instance.places,
  'bbox': ?instance.bbox,
  'eventTypes': ?instance.eventTypes
      ?.map((e) => _$AnalystProfileScopeEventTypesEnumEnumMap[e]!)
      .toList(),
  'ruleTags': ?instance.ruleTags,
  'minSeverity': ?_$SeverityEnumMap[instance.minSeverity],
};

const _$AnalystProfileScopeEventTypesEnumEnumMap = {
  AnalystProfileScopeEventTypesEnum.maritimeDisruption: 'maritime-disruption',
  AnalystProfileScopeEventTypesEnum.aviationAnomaly: 'aviation-anomaly',
  AnalystProfileScopeEventTypesEnum.infrastructureDisruption:
      'infrastructure-disruption',
  AnalystProfileScopeEventTypesEnum.weatherImpact: 'weather-impact',
  AnalystProfileScopeEventTypesEnum.seismic: 'seismic',
  AnalystProfileScopeEventTypesEnum.custom: 'custom',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};
