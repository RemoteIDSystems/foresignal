//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/tag.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tag_assignment.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TagAssignment {
  /// Returns a new [TagAssignment] instance.
  TagAssignment({

    required  this.tagId,

     this.tag,

    required  this.objectRef,

     this.assignedBy,

    required  this.assignedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'tagId',
    required: true,
    includeIfNull: false,
  )


  final String tagId;



  @JsonKey(
    
    name: r'tag',
    required: false,
    includeIfNull: false,
  )


  final Tag? tag;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'objectRef',
    required: true,
    includeIfNull: false,
  )


  final String objectRef;



  @JsonKey(
    
    name: r'assignedBy',
    required: false,
    includeIfNull: false,
  )


  final PrincipalSummary? assignedBy;



  @JsonKey(
    
    name: r'assignedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime assignedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TagAssignment &&
      other.tagId == tagId &&
      other.tag == tag &&
      other.objectRef == objectRef &&
      other.assignedBy == assignedBy &&
      other.assignedAt == assignedAt;

    @override
    int get hashCode =>
        tagId.hashCode +
        tag.hashCode +
        objectRef.hashCode +
        assignedBy.hashCode +
        assignedAt.hashCode;

  factory TagAssignment.fromJson(Map<String, dynamic> json) => _$TagAssignmentFromJson(json);

  Map<String, dynamic> toJson() => _$TagAssignmentToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

