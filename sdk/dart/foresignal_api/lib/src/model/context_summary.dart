//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/context_summary_insights_inner.dart';
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/context_summary_layer_counts_inner.dart';
import 'package:foresignal_api/src/model/context_summary_activity_inner.dart';
import 'package:foresignal_api/src/model/context_summary_request.dart';
import 'package:foresignal_api/src/model/context_summary_narrative.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'context_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ContextSummary {
  /// Returns a new [ContextSummary] instance.
  ContextSummary({

     this.context,

    required  this.generatedAt,

     this.cacheAgeSeconds,

    required  this.layerCounts,

    required  this.activity,

    required  this.insights,

    required  this.related,

     this.narrative,

     this.truncated,
  });

  @JsonKey(
    
    name: r'context',
    required: false,
    includeIfNull: false,
  )


  final ContextSummaryRequest? context;



  @JsonKey(
    
    name: r'generatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime generatedAt;



  @JsonKey(
    
    name: r'cacheAgeSeconds',
    required: false,
    includeIfNull: false,
  )


  final int? cacheAgeSeconds;



  @JsonKey(
    
    name: r'layerCounts',
    required: true,
    includeIfNull: false,
  )


  final List<ContextSummaryLayerCountsInner> layerCounts;



  @JsonKey(
    
    name: r'activity',
    required: true,
    includeIfNull: false,
  )


  final List<ContextSummaryActivityInner> activity;



  @JsonKey(
    
    name: r'insights',
    required: true,
    includeIfNull: false,
  )


  final List<ContextSummaryInsightsInner> insights;



  @JsonKey(
    
    name: r'related',
    required: true,
    includeIfNull: false,
  )


  final List<ObjectEnvelope> related;



  @JsonKey(
    
    name: r'narrative',
    required: false,
    includeIfNull: false,
  )


  final ContextSummaryNarrative? narrative;



  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ContextSummary &&
      other.context == context &&
      other.generatedAt == generatedAt &&
      other.cacheAgeSeconds == cacheAgeSeconds &&
      other.layerCounts == layerCounts &&
      other.activity == activity &&
      other.insights == insights &&
      other.related == related &&
      other.narrative == narrative &&
      other.truncated == truncated;

    @override
    int get hashCode =>
        context.hashCode +
        generatedAt.hashCode +
        cacheAgeSeconds.hashCode +
        layerCounts.hashCode +
        activity.hashCode +
        insights.hashCode +
        related.hashCode +
        (narrative == null ? 0 : narrative.hashCode) +
        truncated.hashCode;

  factory ContextSummary.fromJson(Map<String, dynamic> json) => _$ContextSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$ContextSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

