// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dossier_sections.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DossierSectionsCWProxy {
  DossierSections overview(DossierSectionsOverview? overview);

  DossierSections imagery(DossierSectionsImagery? imagery);

  DossierSections forecast(DossierSectionsForecast? forecast);

  DossierSections impacts(DossierSectionsImpacts? impacts);

  DossierSections related(DossierSectionsRelated? related);

  DossierSections timeline(DossierSectionsTimeline? timeline);

  DossierSections analysis(DossierSectionsAnalysis? analysis);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSections(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSections(...).copyWith(id: 12, name: "My name")
  /// ```
  DossierSections call({
    DossierSectionsOverview? overview,
    DossierSectionsImagery? imagery,
    DossierSectionsForecast? forecast,
    DossierSectionsImpacts? impacts,
    DossierSectionsRelated? related,
    DossierSectionsTimeline? timeline,
    DossierSectionsAnalysis? analysis,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDossierSections.copyWith(...)` or call `instanceOfDossierSections.copyWith.fieldName(value)` for a single field.
class _$DossierSectionsCWProxyImpl implements _$DossierSectionsCWProxy {
  const _$DossierSectionsCWProxyImpl(this._value);

  final DossierSections _value;

  @override
  DossierSections overview(DossierSectionsOverview? overview) =>
      call(overview: overview);

  @override
  DossierSections imagery(DossierSectionsImagery? imagery) =>
      call(imagery: imagery);

  @override
  DossierSections forecast(DossierSectionsForecast? forecast) =>
      call(forecast: forecast);

  @override
  DossierSections impacts(DossierSectionsImpacts? impacts) =>
      call(impacts: impacts);

  @override
  DossierSections related(DossierSectionsRelated? related) =>
      call(related: related);

  @override
  DossierSections timeline(DossierSectionsTimeline? timeline) =>
      call(timeline: timeline);

  @override
  DossierSections analysis(DossierSectionsAnalysis? analysis) =>
      call(analysis: analysis);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSections(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSections(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DossierSections call({
    Object? overview = const $CopyWithPlaceholder(),
    Object? imagery = const $CopyWithPlaceholder(),
    Object? forecast = const $CopyWithPlaceholder(),
    Object? impacts = const $CopyWithPlaceholder(),
    Object? related = const $CopyWithPlaceholder(),
    Object? timeline = const $CopyWithPlaceholder(),
    Object? analysis = const $CopyWithPlaceholder(),
  }) {
    return DossierSections(
      overview: overview == const $CopyWithPlaceholder()
          ? _value.overview
          // ignore: cast_nullable_to_non_nullable
          : overview as DossierSectionsOverview?,
      imagery: imagery == const $CopyWithPlaceholder()
          ? _value.imagery
          // ignore: cast_nullable_to_non_nullable
          : imagery as DossierSectionsImagery?,
      forecast: forecast == const $CopyWithPlaceholder()
          ? _value.forecast
          // ignore: cast_nullable_to_non_nullable
          : forecast as DossierSectionsForecast?,
      impacts: impacts == const $CopyWithPlaceholder()
          ? _value.impacts
          // ignore: cast_nullable_to_non_nullable
          : impacts as DossierSectionsImpacts?,
      related: related == const $CopyWithPlaceholder()
          ? _value.related
          // ignore: cast_nullable_to_non_nullable
          : related as DossierSectionsRelated?,
      timeline: timeline == const $CopyWithPlaceholder()
          ? _value.timeline
          // ignore: cast_nullable_to_non_nullable
          : timeline as DossierSectionsTimeline?,
      analysis: analysis == const $CopyWithPlaceholder()
          ? _value.analysis
          // ignore: cast_nullable_to_non_nullable
          : analysis as DossierSectionsAnalysis?,
    );
  }
}

extension $DossierSectionsCopyWith on DossierSections {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDossierSections.copyWith(...)` or `instanceOfDossierSections.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DossierSectionsCWProxy get copyWith => _$DossierSectionsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DossierSections _$DossierSectionsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DossierSections', json, ($checkedConvert) {
      final val = DossierSections(
        overview: $checkedConvert(
          'overview',
          (v) => v == null
              ? null
              : DossierSectionsOverview.fromJson(v as Map<String, dynamic>),
        ),
        imagery: $checkedConvert(
          'imagery',
          (v) => v == null
              ? null
              : DossierSectionsImagery.fromJson(v as Map<String, dynamic>),
        ),
        forecast: $checkedConvert(
          'forecast',
          (v) => v == null
              ? null
              : DossierSectionsForecast.fromJson(v as Map<String, dynamic>),
        ),
        impacts: $checkedConvert(
          'impacts',
          (v) => v == null
              ? null
              : DossierSectionsImpacts.fromJson(v as Map<String, dynamic>),
        ),
        related: $checkedConvert(
          'related',
          (v) => v == null
              ? null
              : DossierSectionsRelated.fromJson(v as Map<String, dynamic>),
        ),
        timeline: $checkedConvert(
          'timeline',
          (v) => v == null
              ? null
              : DossierSectionsTimeline.fromJson(v as Map<String, dynamic>),
        ),
        analysis: $checkedConvert(
          'analysis',
          (v) => v == null
              ? null
              : DossierSectionsAnalysis.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$DossierSectionsToJson(DossierSections instance) =>
    <String, dynamic>{
      'overview': ?instance.overview?.toJson(),
      'imagery': ?instance.imagery?.toJson(),
      'forecast': ?instance.forecast?.toJson(),
      'impacts': ?instance.impacts?.toJson(),
      'related': ?instance.related?.toJson(),
      'timeline': ?instance.timeline?.toJson(),
      'analysis': ?instance.analysis?.toJson(),
    };
