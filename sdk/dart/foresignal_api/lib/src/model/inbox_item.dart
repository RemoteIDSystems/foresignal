//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/severity.dart';
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'inbox_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InboxItem {
  /// Returns a new [InboxItem] instance.
  InboxItem({

    required  this.id,

    required  this.event,

    required  this.category,

     this.severity,

    required  this.title,

     this.body,

     this.ref,

     this.envelope,

     this.actor,

     this.tlp,

    required  this.createdAt,

    required  this.read,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



      /// Event type, e.g. `mention`, `share.received`, `report.received`, `package.received`, `chronicle.proposed`, `chronicle.cap_warning`, `chronicle.gap`, `chronicle.link.found`, `chronicle.access.requested`, `chronicle.access.granted`, `chronicle.access.declined`, `chronicle.discoverability_lowered`, `saved-search.matches`, `assessment.published`, `comment.reply`
  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final String event;



  @JsonKey(
    
    name: r'category',
    required: true,
    includeIfNull: false,
  )


  final InboxItemCategoryEnum category;



  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final Severity? severity;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'body',
    required: false,
    includeIfNull: false,
  )


  final String? body;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



  @JsonKey(
    
    name: r'envelope',
    required: false,
    includeIfNull: false,
  )


  final ObjectEnvelope? envelope;



  @JsonKey(
    
    name: r'actor',
    required: false,
    includeIfNull: false,
  )


  final PrincipalSummary? actor;



  @JsonKey(
    
    name: r'tlp',
    required: false,
    includeIfNull: false,
  )


  final Tlp? tlp;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'read',
    required: true,
    includeIfNull: false,
  )


  final bool read;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InboxItem &&
      other.id == id &&
      other.event == event &&
      other.category == category &&
      other.severity == severity &&
      other.title == title &&
      other.body == body &&
      other.ref == ref &&
      other.envelope == envelope &&
      other.actor == actor &&
      other.tlp == tlp &&
      other.createdAt == createdAt &&
      other.read == read;

    @override
    int get hashCode =>
        id.hashCode +
        event.hashCode +
        category.hashCode +
        severity.hashCode +
        title.hashCode +
        (body == null ? 0 : body.hashCode) +
        (ref == null ? 0 : ref.hashCode) +
        (envelope == null ? 0 : envelope.hashCode) +
        (actor == null ? 0 : actor.hashCode) +
        (tlp == null ? 0 : tlp.hashCode) +
        createdAt.hashCode +
        read.hashCode;

  factory InboxItem.fromJson(Map<String, dynamic> json) => _$InboxItemFromJson(json);

  Map<String, dynamic> toJson() => _$InboxItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum InboxItemCategoryEnum {
@JsonValue(r'mention')
mention(r'mention'),
@JsonValue(r'share')
share(r'share'),
@JsonValue(r'report')
report(r'report'),
@JsonValue(r'chronicle')
chronicle(r'chronicle'),
@JsonValue(r'assessment')
assessment(r'assessment'),
@JsonValue(r'comment-reply')
commentReply(r'comment-reply'),
@JsonValue(r'access-request')
accessRequest(r'access-request'),
@JsonValue(r'saved-search')
savedSearch(r'saved-search'),
@JsonValue(r'system')
system(r'system');

const InboxItemCategoryEnum(this.value);

final String value;

@override
String toString() => value;
}


