//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'investigation_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InvestigationInput {
  /// Returns a new [InvestigationInput] instance.
  InvestigationInput({

    required  this.title,

     this.summary,

     this.tags,

     this.status,

     this.visibility,

     this.tlp,

     this.workspaceId,

     this.hypothesisSetIds,

     this.questionIds,

     this.chronicleIds,

     this.reportIds,
  });

  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



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



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final InvestigationInputStatusEnum? status;



      /// Default instance (investigations were instance-wide before 130)
  @JsonKey(
    
    name: r'visibility',
    required: false,
    includeIfNull: false,
  )


  final Visibility? visibility;



      /// Default collab.defaultTlp (AMBER)
  @JsonKey(
    
    name: r'tlp',
    required: false,
    includeIfNull: false,
  )


  final Tlp? tlp;



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
    bool operator ==(Object other) => identical(this, other) || other is InvestigationInput &&
      other.title == title &&
      other.summary == summary &&
      other.tags == tags &&
      other.status == status &&
      other.visibility == visibility &&
      other.tlp == tlp &&
      other.workspaceId == workspaceId &&
      other.hypothesisSetIds == hypothesisSetIds &&
      other.questionIds == questionIds &&
      other.chronicleIds == chronicleIds &&
      other.reportIds == reportIds;

    @override
    int get hashCode =>
        title.hashCode +
        summary.hashCode +
        tags.hashCode +
        status.hashCode +
        visibility.hashCode +
        tlp.hashCode +
        (workspaceId == null ? 0 : workspaceId.hashCode) +
        hypothesisSetIds.hashCode +
        questionIds.hashCode +
        chronicleIds.hashCode +
        reportIds.hashCode;

  factory InvestigationInput.fromJson(Map<String, dynamic> json) => _$InvestigationInputFromJson(json);

  Map<String, dynamic> toJson() => _$InvestigationInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum InvestigationInputStatusEnum {
@JsonValue(r'open')
open(r'open'),
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'closed')
closed(r'closed'),
@JsonValue(r'archived')
archived(r'archived');

const InvestigationInputStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


