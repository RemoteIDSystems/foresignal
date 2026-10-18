//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'comment_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CommentEvent {
  /// Returns a new [CommentEvent] instance.
  CommentEvent({

    required  this.reason,

    required  this.annotationId,

     this.parentId,

    required  this.targetRef,

    required  this.author,

     this.excerpt,

    required  this.at,
  });

  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final CommentEventReasonEnum reason;



      /// ULID identifier
  @JsonKey(
    
    name: r'annotationId',
    required: true,
    includeIfNull: false,
  )


  final String annotationId;



      /// ULID identifier
  @JsonKey(
    
    name: r'parentId',
    required: false,
    includeIfNull: false,
  )


  final String? parentId;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'targetRef',
    required: true,
    includeIfNull: false,
  )


  final String targetRef;



  @JsonKey(
    
    name: r'author',
    required: true,
    includeIfNull: false,
  )


  final PrincipalSummary author;



  @JsonKey(
    
    name: r'excerpt',
    required: false,
    includeIfNull: false,
  )


  final String? excerpt;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CommentEvent &&
      other.reason == reason &&
      other.annotationId == annotationId &&
      other.parentId == parentId &&
      other.targetRef == targetRef &&
      other.author == author &&
      other.excerpt == excerpt &&
      other.at == at;

    @override
    int get hashCode =>
        reason.hashCode +
        annotationId.hashCode +
        (parentId == null ? 0 : parentId.hashCode) +
        targetRef.hashCode +
        author.hashCode +
        excerpt.hashCode +
        at.hashCode;

  factory CommentEvent.fromJson(Map<String, dynamic> json) => _$CommentEventFromJson(json);

  Map<String, dynamic> toJson() => _$CommentEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CommentEventReasonEnum {
@JsonValue(r'mention')
mention(r'mention'),
@JsonValue(r'reply')
reply(r'reply');

const CommentEventReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


