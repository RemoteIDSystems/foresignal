//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/analyst_claim.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_note.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystNote {
  /// Returns a new [AnalystNote] instance.
  AnalystNote({

    required  this.id,

    required  this.profileId,

    required  this.runId,

    required  this.title,

    required  this.body,

    required  this.priority,

    required  this.claims,

     this.citations,

     this.private,

    required  this.status,

     this.model,

     this.footer,

    required  this.createdAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// ULID identifier
  @JsonKey(
    
    name: r'profileId',
    required: true,
    includeIfNull: false,
  )


  final String profileId;



      /// ULID identifier
  @JsonKey(
    
    name: r'runId',
    required: true,
    includeIfNull: false,
  )


  final String runId;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



      /// Markdown, ≤ 2,000 words
  @JsonKey(
    
    name: r'body',
    required: true,
    includeIfNull: false,
  )


  final String body;



  @JsonKey(
    
    name: r'priority',
    required: true,
    includeIfNull: false,
  )


  final AnalystNotePriorityEnum priority;



  @JsonKey(
    
    name: r'claims',
    required: true,
    includeIfNull: false,
  )


  final List<AnalystClaim> claims;



  @JsonKey(
    
    name: r'citations',
    required: false,
    includeIfNull: false,
  )


  final List<String>? citations;



      /// True when it cites anything private
  @JsonKey(
    
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final AnalystNoteStatusEnum status;



  @JsonKey(
    
    name: r'model',
    required: false,
    includeIfNull: false,
  )


  final String? model;



  @JsonKey(
    
    name: r'footer',
    required: false,
    includeIfNull: false,
  )


  final String? footer;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystNote &&
      other.id == id &&
      other.profileId == profileId &&
      other.runId == runId &&
      other.title == title &&
      other.body == body &&
      other.priority == priority &&
      other.claims == claims &&
      other.citations == citations &&
      other.private == private &&
      other.status == status &&
      other.model == model &&
      other.footer == footer &&
      other.createdAt == createdAt;

    @override
    int get hashCode =>
        id.hashCode +
        profileId.hashCode +
        runId.hashCode +
        title.hashCode +
        body.hashCode +
        priority.hashCode +
        claims.hashCode +
        citations.hashCode +
        private.hashCode +
        status.hashCode +
        (model == null ? 0 : model.hashCode) +
        footer.hashCode +
        createdAt.hashCode;

  factory AnalystNote.fromJson(Map<String, dynamic> json) => _$AnalystNoteFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystNoteToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnalystNotePriorityEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'normal')
normal(r'normal'),
@JsonValue(r'high')
high(r'high');

const AnalystNotePriorityEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnalystNoteStatusEnum {
@JsonValue(r'new')
new_(r'new'),
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'dismissed')
dismissed(r'dismissed');

const AnalystNoteStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


