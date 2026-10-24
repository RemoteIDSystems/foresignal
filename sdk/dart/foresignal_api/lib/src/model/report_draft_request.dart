//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:foresignal_api/src/model/report_source.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_draft_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportDraftRequest {
  /// Returns a new [ReportDraftRequest] instance.
  ReportDraftRequest({

    required  this.source_,

     this.template,

     this.title,

     this.timeWindow,

     this.area,

     this.narrative = false,

     this.assessmentId,
  });

  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final ReportSource source_;



  @JsonKey(
    
    name: r'template',
    required: false,
    includeIfNull: false,
  )


  final ReportDraftRequestTemplateEnum? template;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'timeWindow',
    required: false,
    includeIfNull: false,
  )


  final TimeRange? timeWindow;



  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final Geometry? area;



      /// Add a labeled LLM narrative block (agent scope, budgeted)
  @JsonKey(
    defaultValue: false,
    name: r'narrative',
    required: false,
    includeIfNull: false,
  )


  final bool? narrative;



      /// ULID identifier
  @JsonKey(
    
    name: r'assessmentId',
    required: false,
    includeIfNull: false,
  )


  final String? assessmentId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportDraftRequest &&
      other.source_ == source_ &&
      other.template == template &&
      other.title == title &&
      other.timeWindow == timeWindow &&
      other.area == area &&
      other.narrative == narrative &&
      other.assessmentId == assessmentId;

    @override
    int get hashCode =>
        source_.hashCode +
        template.hashCode +
        (title == null ? 0 : title.hashCode) +
        (timeWindow == null ? 0 : timeWindow.hashCode) +
        (area == null ? 0 : area.hashCode) +
        narrative.hashCode +
        (assessmentId == null ? 0 : assessmentId.hashCode);

  factory ReportDraftRequest.fromJson(Map<String, dynamic> json) => _$ReportDraftRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ReportDraftRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReportDraftRequestTemplateEnum {
@JsonValue(r'situation-brief')
situationBrief(r'situation-brief'),
@JsonValue(r'investigation-summary')
investigationSummary(r'investigation-summary'),
@JsonValue(r'chronicle-recap')
chronicleRecap(r'chronicle-recap'),
@JsonValue(r'weekly-assessment')
weeklyAssessment(r'weekly-assessment'),
@JsonValue(r'custom')
custom(r'custom');

const ReportDraftRequestTemplateEnum(this.value);

final String value;

@override
String toString() => value;
}


