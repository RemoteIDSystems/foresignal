// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'source_relevance_analysis.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SourceRelevanceAnalysisCWProxy {
  SourceRelevanceAnalysis advisory(bool advisory);

  SourceRelevanceAnalysis summary(String? summary);

  SourceRelevanceAnalysis sources(List<SourceRelevance> sources);

  SourceRelevanceAnalysis shared(SourceRelevanceAnalysisShared shared);

  SourceRelevanceAnalysis model(String? model);

  SourceRelevanceAnalysis template(SourceRelevanceAnalysisTemplate template);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceRelevanceAnalysis(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceRelevanceAnalysis(...).copyWith(id: 12, name: "My name")
  /// ```
  SourceRelevanceAnalysis call({
    bool advisory,
    String? summary,
    List<SourceRelevance> sources,
    SourceRelevanceAnalysisShared shared,
    String? model,
    SourceRelevanceAnalysisTemplate template,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSourceRelevanceAnalysis.copyWith(...)` or call `instanceOfSourceRelevanceAnalysis.copyWith.fieldName(value)` for a single field.
class _$SourceRelevanceAnalysisCWProxyImpl
    implements _$SourceRelevanceAnalysisCWProxy {
  const _$SourceRelevanceAnalysisCWProxyImpl(this._value);

  final SourceRelevanceAnalysis _value;

  @override
  SourceRelevanceAnalysis advisory(bool advisory) => call(advisory: advisory);

  @override
  SourceRelevanceAnalysis summary(String? summary) => call(summary: summary);

  @override
  SourceRelevanceAnalysis sources(List<SourceRelevance> sources) =>
      call(sources: sources);

  @override
  SourceRelevanceAnalysis shared(SourceRelevanceAnalysisShared shared) =>
      call(shared: shared);

  @override
  SourceRelevanceAnalysis model(String? model) => call(model: model);

  @override
  SourceRelevanceAnalysis template(SourceRelevanceAnalysisTemplate template) =>
      call(template: template);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceRelevanceAnalysis(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceRelevanceAnalysis(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SourceRelevanceAnalysis call({
    Object? advisory = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? shared = const $CopyWithPlaceholder(),
    Object? model = const $CopyWithPlaceholder(),
    Object? template = const $CopyWithPlaceholder(),
  }) {
    return SourceRelevanceAnalysis(
      advisory: advisory == const $CopyWithPlaceholder() || advisory == null
          ? _value.advisory
          // ignore: cast_nullable_to_non_nullable
          : advisory as bool,
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String?,
      sources: sources == const $CopyWithPlaceholder() || sources == null
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<SourceRelevance>,
      shared: shared == const $CopyWithPlaceholder() || shared == null
          ? _value.shared
          // ignore: cast_nullable_to_non_nullable
          : shared as SourceRelevanceAnalysisShared,
      model: model == const $CopyWithPlaceholder()
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as String?,
      template: template == const $CopyWithPlaceholder() || template == null
          ? _value.template
          // ignore: cast_nullable_to_non_nullable
          : template as SourceRelevanceAnalysisTemplate,
    );
  }
}

extension $SourceRelevanceAnalysisCopyWith on SourceRelevanceAnalysis {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSourceRelevanceAnalysis.copyWith(...)` or `instanceOfSourceRelevanceAnalysis.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SourceRelevanceAnalysisCWProxy get copyWith =>
      _$SourceRelevanceAnalysisCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SourceRelevanceAnalysis _$SourceRelevanceAnalysisFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SourceRelevanceAnalysis', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'advisory',
      'summary',
      'sources',
      'shared',
      'model',
      'template',
    ],
  );
  final val = SourceRelevanceAnalysis(
    advisory: $checkedConvert('advisory', (v) => v as bool),
    summary: $checkedConvert('summary', (v) => v as String?),
    sources: $checkedConvert(
      'sources',
      (v) => (v as List<dynamic>)
          .map((e) => SourceRelevance.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    shared: $checkedConvert(
      'shared',
      (v) => SourceRelevanceAnalysisShared.fromJson(v as Map<String, dynamic>),
    ),
    model: $checkedConvert('model', (v) => v as String?),
    template: $checkedConvert(
      'template',
      (v) =>
          SourceRelevanceAnalysisTemplate.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$SourceRelevanceAnalysisToJson(
  SourceRelevanceAnalysis instance,
) => <String, dynamic>{
  'advisory': instance.advisory,
  'summary': instance.summary,
  'sources': instance.sources.map((e) => e.toJson()).toList(),
  'shared': instance.shared.toJson(),
  'model': instance.model,
  'template': instance.template.toJson(),
};
