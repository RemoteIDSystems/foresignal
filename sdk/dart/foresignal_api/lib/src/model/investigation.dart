//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/investigation_item.dart';
import 'package:foresignal_api/src/model/object_acl_entry.dart';
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'investigation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Investigation {
  /// Returns a new [Investigation] instance.
  Investigation({

    required  this.id,

    required  this.displayId,

    required  this.title,

    required  this.status,

     this.summary,

     this.tags,

     this.itemCount,

     this.items,

    required  this.createdBy,

    required  this.createdAt,

    required  this.updatedAt,

     this.owner,

     this.visibility,

     this.tlp,

     this.members,

     this.workspaceId,

     this.hypothesisSetIds,

     this.questionIds,

     this.chronicleIds,

     this.reportIds,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'displayId',
    required: true,
    includeIfNull: false,
  )


  final String displayId;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final InvestigationStatusEnum status;



      /// Operator-authored markdown
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
    
    name: r'itemCount',
    required: false,
    includeIfNull: false,
  )


  final int? itemCount;



      /// Present on getInvestigation
  @JsonKey(
    
    name: r'items',
    required: false,
    includeIfNull: false,
  )


  final List<InvestigationItem>? items;



  @JsonKey(
    
    name: r'createdBy',
    required: true,
    includeIfNull: false,
  )


  final String createdBy;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;



  @JsonKey(
    
    name: r'owner',
    required: false,
    includeIfNull: false,
  )


  final PrincipalSummary? owner;



  @JsonKey(
    
    name: r'visibility',
    required: false,
    includeIfNull: false,
  )


  final Visibility? visibility;



  @JsonKey(
    
    name: r'tlp',
    required: false,
    includeIfNull: false,
  )


  final Tlp? tlp;



      /// Access-list grants (viewer, commenter, editor, owner)
  @JsonKey(
    
    name: r'members',
    required: false,
    includeIfNull: false,
  )


  final List<ObjectAclEntry>? members;



      /// ULID identifier
  @JsonKey(
    
    name: r'workspaceId',
    required: false,
    includeIfNull: false,
  )


  final String? workspaceId;



  @JsonKey(
    
    name: r'hypothesisSetIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? hypothesisSetIds;



  @JsonKey(
    
    name: r'questionIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? questionIds;



  @JsonKey(
    
    name: r'chronicleIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? chronicleIds;



  @JsonKey(
    
    name: r'reportIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? reportIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Investigation &&
      other.id == id &&
      other.displayId == displayId &&
      other.title == title &&
      other.status == status &&
      other.summary == summary &&
      other.tags == tags &&
      other.itemCount == itemCount &&
      other.items == items &&
      other.createdBy == createdBy &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt &&
      other.owner == owner &&
      other.visibility == visibility &&
      other.tlp == tlp &&
      other.members == members &&
      other.workspaceId == workspaceId &&
      other.hypothesisSetIds == hypothesisSetIds &&
      other.questionIds == questionIds &&
      other.chronicleIds == chronicleIds &&
      other.reportIds == reportIds;

    @override
    int get hashCode =>
        id.hashCode +
        displayId.hashCode +
        title.hashCode +
        status.hashCode +
        (summary == null ? 0 : summary.hashCode) +
        tags.hashCode +
        itemCount.hashCode +
        items.hashCode +
        createdBy.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode +
        owner.hashCode +
        visibility.hashCode +
        tlp.hashCode +
        members.hashCode +
        (workspaceId == null ? 0 : workspaceId.hashCode) +
        hypothesisSetIds.hashCode +
        questionIds.hashCode +
        chronicleIds.hashCode +
        reportIds.hashCode;

  factory Investigation.fromJson(Map<String, dynamic> json) => _$InvestigationFromJson(json);

  Map<String, dynamic> toJson() => _$InvestigationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum InvestigationStatusEnum {
@JsonValue(r'open')
open(r'open'),
@JsonValue(r'closed')
closed(r'closed'),
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'archived')
archived(r'archived');

const InvestigationStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


