//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/source_relevance.dart';
import 'package:foresignal_api/src/model/source_relevance_analysis_shared.dart';
import 'package:foresignal_api/src/model/source_relevance_analysis_template.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'source_relevance_analysis.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SourceRelevanceAnalysis {
  /// Returns a new [SourceRelevanceAnalysis] instance.
  SourceRelevanceAnalysis({

    required  this.advisory,

    required  this.summary,

    required  this.sources,

    required  this.shared,

    required  this.model,

    required  this.template,
  });

      /// Always true: the verdicts are suggestions; the caller keeps or omits each source
  @JsonKey(
    
    name: r'advisory',
    required: true,
    includeIfNull: false,
  )


  final bool advisory;



      /// The model's one sentence on what the set as a whole watches; null when it gave none
  @JsonKey(
    
    name: r'summary',
    required: true,
    includeIfNull: true,
  )


  final String? summary;



      /// Every requested source exactly once, in request order
  @JsonKey(
    
    name: r'sources',
    required: true,
    includeIfNull: false,
  )


  final List<SourceRelevance> sources;



  @JsonKey(
    
    name: r'shared',
    required: true,
    includeIfNull: false,
  )


  final SourceRelevanceAnalysisShared shared;



      /// The model that answered; null when none was asked
  @JsonKey(
    
    name: r'model',
    required: true,
    includeIfNull: true,
  )


  final String? model;



  @JsonKey(
    
    name: r'template',
    required: true,
    includeIfNull: false,
  )


  final SourceRelevanceAnalysisTemplate template;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SourceRelevanceAnalysis &&
      other.advisory == advisory &&
      other.summary == summary &&
      other.sources == sources &&
      other.shared == shared &&
      other.model == model &&
      other.template == template;

    @override
    int get hashCode =>
        advisory.hashCode +
        (summary == null ? 0 : summary.hashCode) +
        sources.hashCode +
        shared.hashCode +
        (model == null ? 0 : model.hashCode) +
        template.hashCode;

  factory SourceRelevanceAnalysis.fromJson(Map<String, dynamic> json) => _$SourceRelevanceAnalysisFromJson(json);

  Map<String, dynamic> toJson() => _$SourceRelevanceAnalysisToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

