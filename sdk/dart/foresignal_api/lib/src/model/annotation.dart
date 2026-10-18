//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'annotation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Annotation {
  /// Returns a new [Annotation] instance.
  Annotation({

    required  this.id,

     this.ref,

    required  this.targetRef,

    required  this.kind,

     this.verdict,

     this.duplicateOfRef,

     this.body,

     this.tags,

    required  this.author,

    required  this.private,

     this.statusApplied,

    required  this.createdAt,

    required  this.updatedAt,

     this.deletedAt,

     this.parentId,

     this.mentions,

     this.droppedMentions,

     this.resolved,

     this.editedAt,

     this.tlp,

     this.visibility,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'targetRef',
    required: true,
    includeIfNull: false,
  )


  final String targetRef;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final AnnotationKindEnum kind;



  @JsonKey(
    
    name: r'verdict',
    required: false,
    includeIfNull: false,
  )


  final AnnotationVerdictEnum? verdict;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'duplicateOfRef',
    required: false,
    includeIfNull: false,
  )


  final String? duplicateOfRef;



  @JsonKey(
    
    name: r'body',
    required: false,
    includeIfNull: false,
  )


  final String? body;



  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



      /// Principal id
  @JsonKey(
    
    name: r'author',
    required: true,
    includeIfNull: false,
  )


  final String author;



  @JsonKey(
    
    name: r'private',
    required: true,
    includeIfNull: false,
  )


  final bool private;



      /// A rejected verdict with `applyStatus` dismissed the target
  @JsonKey(
    
    name: r'statusApplied',
    required: false,
    includeIfNull: false,
  )


  final bool? statusApplied;



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
    
    name: r'deletedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? deletedAt;



      /// Root annotation for replies (threads)
  @JsonKey(
    
    name: r'parentId',
    required: false,
    includeIfNull: false,
  )


  final String? parentId;



      /// Principal handles parsed from `@handle`
  @JsonKey(
    
    name: r'mentions',
    required: false,
    includeIfNull: false,
  )


  final List<String>? mentions;



      /// Mentions dropped because the principal cannot see the target (returned to the author only)
  @JsonKey(
    
    name: r'droppedMentions',
    required: false,
    includeIfNull: false,
  )


  final List<String>? droppedMentions;



      /// Thread resolved (root annotations)
  @JsonKey(
    
    name: r'resolved',
    required: false,
    includeIfNull: false,
  )


  final bool? resolved;



  @JsonKey(
    
    name: r'editedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? editedAt;



  @JsonKey(
    
    name: r'tlp',
    required: false,
    includeIfNull: false,
  )


  final Tlp? tlp;



  @JsonKey(
    
    name: r'visibility',
    required: false,
    includeIfNull: false,
  )


  final Visibility? visibility;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Annotation &&
      other.id == id &&
      other.ref == ref &&
      other.targetRef == targetRef &&
      other.kind == kind &&
      other.verdict == verdict &&
      other.duplicateOfRef == duplicateOfRef &&
      other.body == body &&
      other.tags == tags &&
      other.author == author &&
      other.private == private &&
      other.statusApplied == statusApplied &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt &&
      other.deletedAt == deletedAt &&
      other.parentId == parentId &&
      other.mentions == mentions &&
      other.droppedMentions == droppedMentions &&
      other.resolved == resolved &&
      other.editedAt == editedAt &&
      other.tlp == tlp &&
      other.visibility == visibility;

    @override
    int get hashCode =>
        id.hashCode +
        ref.hashCode +
        targetRef.hashCode +
        kind.hashCode +
        (verdict == null ? 0 : verdict.hashCode) +
        (duplicateOfRef == null ? 0 : duplicateOfRef.hashCode) +
        (body == null ? 0 : body.hashCode) +
        tags.hashCode +
        author.hashCode +
        private.hashCode +
        statusApplied.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode +
        (deletedAt == null ? 0 : deletedAt.hashCode) +
        (parentId == null ? 0 : parentId.hashCode) +
        mentions.hashCode +
        droppedMentions.hashCode +
        resolved.hashCode +
        (editedAt == null ? 0 : editedAt.hashCode) +
        tlp.hashCode +
        visibility.hashCode;

  factory Annotation.fromJson(Map<String, dynamic> json) => _$AnnotationFromJson(json);

  Map<String, dynamic> toJson() => _$AnnotationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnnotationKindEnum {
@JsonValue(r'note')
note(r'note'),
@JsonValue(r'verdict')
verdict(r'verdict');

const AnnotationKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnnotationVerdictEnum {
@JsonValue(r'confirmed')
confirmed(r'confirmed'),
@JsonValue(r'rejected')
rejected(r'rejected'),
@JsonValue(r'needs-review')
needsReview(r'needs-review'),
@JsonValue(r'duplicate-of')
duplicateOf(r'duplicate-of');

const AnnotationVerdictEnum(this.value);

final String value;

@override
String toString() => value;
}


