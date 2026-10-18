// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dossier_sections_forecast.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DossierSectionsForecastCWProxy {
  DossierSectionsForecast status(DossierSectionsForecastStatusEnum status);

  DossierSectionsForecast message(String? message);

  DossierSectionsForecast links(DossierSectionsOverviewLinks? links);

  DossierSectionsForecast facets(DossierSectionsOverviewFacets? facets);

  DossierSectionsForecast truncated(bool? truncated);

  DossierSectionsForecast official(
    List<DossierSectionsForecastOfficialInner>? official,
  );

  DossierSectionsForecast questions(List<LedgerQuestionBrief>? questions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsForecast(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsForecast(...).copyWith(id: 12, name: "My name")
  /// ```
  DossierSectionsForecast call({
    DossierSectionsForecastStatusEnum status,
    String? message,
    DossierSectionsOverviewLinks? links,
    DossierSectionsOverviewFacets? facets,
    bool? truncated,
    List<DossierSectionsForecastOfficialInner>? official,
    List<LedgerQuestionBrief>? questions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDossierSectionsForecast.copyWith(...)` or call `instanceOfDossierSectionsForecast.copyWith.fieldName(value)` for a single field.
class _$DossierSectionsForecastCWProxyImpl
    implements _$DossierSectionsForecastCWProxy {
  const _$DossierSectionsForecastCWProxyImpl(this._value);

  final DossierSectionsForecast _value;

  @override
  DossierSectionsForecast status(DossierSectionsForecastStatusEnum status) =>
      call(status: status);

  @override
  DossierSectionsForecast message(String? message) => call(message: message);

  @override
  DossierSectionsForecast links(DossierSectionsOverviewLinks? links) =>
      call(links: links);

  @override
  DossierSectionsForecast facets(DossierSectionsOverviewFacets? facets) =>
      call(facets: facets);

  @override
  DossierSectionsForecast truncated(bool? truncated) =>
      call(truncated: truncated);

  @override
  DossierSectionsForecast official(
    List<DossierSectionsForecastOfficialInner>? official,
  ) => call(official: official);

  @override
  DossierSectionsForecast questions(List<LedgerQuestionBrief>? questions) =>
      call(questions: questions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsForecast(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsForecast(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DossierSectionsForecast call({
    Object? status = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? facets = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? official = const $CopyWithPlaceholder(),
    Object? questions = const $CopyWithPlaceholder(),
  }) {
    return DossierSectionsForecast(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as DossierSectionsForecastStatusEnum,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as DossierSectionsOverviewLinks?,
      facets: facets == const $CopyWithPlaceholder()
          ? _value.facets
          // ignore: cast_nullable_to_non_nullable
          : facets as DossierSectionsOverviewFacets?,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
      official: official == const $CopyWithPlaceholder()
          ? _value.official
          // ignore: cast_nullable_to_non_nullable
          : official as List<DossierSectionsForecastOfficialInner>?,
      questions: questions == const $CopyWithPlaceholder()
          ? _value.questions
          // ignore: cast_nullable_to_non_nullable
          : questions as List<LedgerQuestionBrief>?,
    );
  }
}

extension $DossierSectionsForecastCopyWith on DossierSectionsForecast {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDossierSectionsForecast.copyWith(...)` or `instanceOfDossierSectionsForecast.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DossierSectionsForecastCWProxy get copyWith =>
      _$DossierSectionsForecastCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DossierSectionsForecast _$DossierSectionsForecastFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DossierSectionsForecast', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = DossierSectionsForecast(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$DossierSectionsForecastStatusEnumEnumMap, v),
    ),
    message: $checkedConvert('message', (v) => v as String?),
    links: $checkedConvert(
      'links',
      (v) => v == null
          ? null
          : DossierSectionsOverviewLinks.fromJson(v as Map<String, dynamic>),
    ),
    facets: $checkedConvert(
      'facets',
      (v) => v == null
          ? null
          : DossierSectionsOverviewFacets.fromJson(v as Map<String, dynamic>),
    ),
    truncated: $checkedConvert('truncated', (v) => v as bool?),
    official: $checkedConvert(
      'official',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => DossierSectionsForecastOfficialInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    questions: $checkedConvert(
      'questions',
      (v) => (v as List<dynamic>?)
          ?.map((e) => LedgerQuestionBrief.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$DossierSectionsForecastToJson(
  DossierSectionsForecast instance,
) => <String, dynamic>{
  'status': _$DossierSectionsForecastStatusEnumEnumMap[instance.status]!,
  'message': ?instance.message,
  'links': ?instance.links?.toJson(),
  'facets': ?instance.facets?.toJson(),
  'truncated': ?instance.truncated,
  'official': ?instance.official?.map((e) => e.toJson()).toList(),
  'questions': ?instance.questions?.map((e) => e.toJson()).toList(),
};

const _$DossierSectionsForecastStatusEnumEnumMap = {
  DossierSectionsForecastStatusEnum.ok: 'ok',
  DossierSectionsForecastStatusEnum.empty: 'empty',
  DossierSectionsForecastStatusEnum.unavailable: 'unavailable',
  DossierSectionsForecastStatusEnum.notInstalled: 'not-installed',
};
