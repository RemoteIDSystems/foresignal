// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dossier_sections_analysis.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DossierSectionsAnalysisCWProxy {
  DossierSectionsAnalysis status(DossierSectionsAnalysisStatusEnum status);

  DossierSectionsAnalysis message(String? message);

  DossierSectionsAnalysis links(DossierSectionsOverviewLinks? links);

  DossierSectionsAnalysis facets(DossierSectionsOverviewFacets? facets);

  DossierSectionsAnalysis truncated(bool? truncated);

  DossierSectionsAnalysis findings(List<ObjectEnvelope>? findings);

  DossierSectionsAnalysis hypothesisSets(List<ObjectEnvelope>? hypothesisSets);

  DossierSectionsAnalysis assessments(List<ObjectEnvelope>? assessments);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsAnalysis(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsAnalysis(...).copyWith(id: 12, name: "My name")
  /// ```
  DossierSectionsAnalysis call({
    DossierSectionsAnalysisStatusEnum status,
    String? message,
    DossierSectionsOverviewLinks? links,
    DossierSectionsOverviewFacets? facets,
    bool? truncated,
    List<ObjectEnvelope>? findings,
    List<ObjectEnvelope>? hypothesisSets,
    List<ObjectEnvelope>? assessments,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDossierSectionsAnalysis.copyWith(...)` or call `instanceOfDossierSectionsAnalysis.copyWith.fieldName(value)` for a single field.
class _$DossierSectionsAnalysisCWProxyImpl
    implements _$DossierSectionsAnalysisCWProxy {
  const _$DossierSectionsAnalysisCWProxyImpl(this._value);

  final DossierSectionsAnalysis _value;

  @override
  DossierSectionsAnalysis status(DossierSectionsAnalysisStatusEnum status) =>
      call(status: status);

  @override
  DossierSectionsAnalysis message(String? message) => call(message: message);

  @override
  DossierSectionsAnalysis links(DossierSectionsOverviewLinks? links) =>
      call(links: links);

  @override
  DossierSectionsAnalysis facets(DossierSectionsOverviewFacets? facets) =>
      call(facets: facets);

  @override
  DossierSectionsAnalysis truncated(bool? truncated) =>
      call(truncated: truncated);

  @override
  DossierSectionsAnalysis findings(List<ObjectEnvelope>? findings) =>
      call(findings: findings);

  @override
  DossierSectionsAnalysis hypothesisSets(
    List<ObjectEnvelope>? hypothesisSets,
  ) => call(hypothesisSets: hypothesisSets);

  @override
  DossierSectionsAnalysis assessments(List<ObjectEnvelope>? assessments) =>
      call(assessments: assessments);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsAnalysis(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsAnalysis(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DossierSectionsAnalysis call({
    Object? status = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? facets = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? findings = const $CopyWithPlaceholder(),
    Object? hypothesisSets = const $CopyWithPlaceholder(),
    Object? assessments = const $CopyWithPlaceholder(),
  }) {
    return DossierSectionsAnalysis(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as DossierSectionsAnalysisStatusEnum,
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
      findings: findings == const $CopyWithPlaceholder()
          ? _value.findings
          // ignore: cast_nullable_to_non_nullable
          : findings as List<ObjectEnvelope>?,
      hypothesisSets: hypothesisSets == const $CopyWithPlaceholder()
          ? _value.hypothesisSets
          // ignore: cast_nullable_to_non_nullable
          : hypothesisSets as List<ObjectEnvelope>?,
      assessments: assessments == const $CopyWithPlaceholder()
          ? _value.assessments
          // ignore: cast_nullable_to_non_nullable
          : assessments as List<ObjectEnvelope>?,
    );
  }
}

extension $DossierSectionsAnalysisCopyWith on DossierSectionsAnalysis {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDossierSectionsAnalysis.copyWith(...)` or `instanceOfDossierSectionsAnalysis.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DossierSectionsAnalysisCWProxy get copyWith =>
      _$DossierSectionsAnalysisCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DossierSectionsAnalysis _$DossierSectionsAnalysisFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DossierSectionsAnalysis', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = DossierSectionsAnalysis(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$DossierSectionsAnalysisStatusEnumEnumMap, v),
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
    findings: $checkedConvert(
      'findings',
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
    assessments: $checkedConvert(
      'assessments',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ObjectEnvelope.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$DossierSectionsAnalysisToJson(
  DossierSectionsAnalysis instance,
) => <String, dynamic>{
  'status': _$DossierSectionsAnalysisStatusEnumEnumMap[instance.status]!,
  'message': ?instance.message,
  'links': ?instance.links?.toJson(),
  'facets': ?instance.facets?.toJson(),
  'truncated': ?instance.truncated,
  'findings': ?instance.findings?.map((e) => e.toJson()).toList(),
  'hypothesisSets': ?instance.hypothesisSets?.map((e) => e.toJson()).toList(),
  'assessments': ?instance.assessments?.map((e) => e.toJson()).toList(),
};

const _$DossierSectionsAnalysisStatusEnumEnumMap = {
  DossierSectionsAnalysisStatusEnum.ok: 'ok',
  DossierSectionsAnalysisStatusEnum.empty: 'empty',
  DossierSectionsAnalysisStatusEnum.unavailable: 'unavailable',
  DossierSectionsAnalysisStatusEnum.notInstalled: 'not-installed',
};
