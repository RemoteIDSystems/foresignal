//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'investigation_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InvestigationPatch {
  /// Returns a new [InvestigationPatch] instance.
  InvestigationPatch({

     this.title,

     this.status,

     this.summary,

     this.tags,

     this.workspaceId,

     this.hypothesisSetIds,

     this.questionIds,

     this.chronicleIds,

     this.reportIds,
  });

  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final InvestigationPatchStatusEnum? status;



  @JsonKey(
    
    name: r'summary',
    required: false,
    includeIfNull: false,
  )


  final String? summary;



  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



      /// Linked 129 workspace (130)
  @JsonKey(
    
    name: r'workspaceId',
    required: false,
    includeIfNull: false,
  )


  final String? workspaceId;



      /// Linked 125 hypothesis sets (130)
  @JsonKey(
    
    name: r'hypothesisSetIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? hypothesisSetIds;



      /// Linked 122 forecast questions (130)
  @JsonKey(
    
    name: r'questionIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? questionIds;



      /// Linked chronicles (131)
  @JsonKey(
    
    name: r'chronicleIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? chronicleIds;



      /// Linked reports (132)
  @JsonKey(
    
    name: r'reportIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? reportIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InvestigationPatch &&
      other.title == title &&
      other.status == status &&
      other.summary == summary &&
      other.tags == tags &&
      other.workspaceId == workspaceId &&
      other.hypothesisSetIds == hypothesisSetIds &&
      other.questionIds == questionIds &&
      other.chronicleIds == chronicleIds &&
      other.reportIds == reportIds;

    @override
    int get hashCode =>
        title.hashCode +
        status.hashCode +
        (summary == null ? 0 : summary.hashCode) +
        tags.hashCode +
        (workspaceId == null ? 0 : workspaceId.hashCode) +
        hypothesisSetIds.hashCode +
        questionIds.hashCode +
        chronicleIds.hashCode +
        reportIds.hashCode;

  factory InvestigationPatch.fromJson(Map<String, dynamic> json) => _$InvestigationPatchFromJson(json);

  Map<String, dynamic> toJson() => _$InvestigationPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum InvestigationPatchStatusEnum {
@JsonValue(r'open')
open(r'open'),
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'closed')
closed(r'closed'),
@JsonValue(r'archived')
archived(r'archived');

const InvestigationPatchStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


