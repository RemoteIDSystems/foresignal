//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'investigation_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InvestigationItem {
  /// Returns a new [InvestigationItem] instance.
  InvestigationItem({

    required  this.id,

    required  this.itemType,

    required  this.ref,

     this.seq,

     this.snapshot,

     this.note,

    required  this.addedAt,

    required  this.addedBy,

     this.objectRef,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'itemType',
    required: true,
    includeIfNull: false,
  )


  final InvestigationItemItemTypeEnum itemType;



      /// Item reference: event id, alert id, `{sourceId}/{externalId}`, anomaly id, place id, briefing run id; 129 ObjectRef grammar accepted from 129 on
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



      /// Pinned version seq (records)
  @JsonKey(
    
    name: r'seq',
    required: false,
    includeIfNull: false,
  )


  final int? seq;



      /// ≤ 64 KB, taken when added
  @JsonKey(
    
    name: r'snapshot',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? snapshot;



  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;



  @JsonKey(
    
    name: r'addedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime addedAt;



  @JsonKey(
    
    name: r'addedBy',
    required: true,
    includeIfNull: false,
  )


  final String addedBy;



      /// Any 129 object reference, `evidence-item:{id}`, `attachment:{id}` or `remote:…` (an explicit copy of received material keeps provenance)
  @JsonKey(
    
    name: r'objectRef',
    required: false,
    includeIfNull: false,
  )


  final String? objectRef;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InvestigationItem &&
      other.id == id &&
      other.itemType == itemType &&
      other.ref == ref &&
      other.seq == seq &&
      other.snapshot == snapshot &&
      other.note == note &&
      other.addedAt == addedAt &&
      other.addedBy == addedBy &&
      other.objectRef == objectRef;

    @override
    int get hashCode =>
        id.hashCode +
        itemType.hashCode +
        ref.hashCode +
        (seq == null ? 0 : seq.hashCode) +
        (snapshot == null ? 0 : snapshot.hashCode) +
        (note == null ? 0 : note.hashCode) +
        addedAt.hashCode +
        addedBy.hashCode +
        objectRef.hashCode;

  factory InvestigationItem.fromJson(Map<String, dynamic> json) => _$InvestigationItemFromJson(json);

  Map<String, dynamic> toJson() => _$InvestigationItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum InvestigationItemItemTypeEnum {
@JsonValue(r'event')
event(r'event'),
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'record')
record(r'record'),
@JsonValue(r'anomaly')
anomaly(r'anomaly'),
@JsonValue(r'place')
place(r'place'),
@JsonValue(r'briefing_run')
briefingRun(r'briefing_run'),
@JsonValue(r'note')
note(r'note'),
@JsonValue(r'evidence-item')
evidenceItem(r'evidence-item'),
@JsonValue(r'attachment')
attachment(r'attachment'),
@JsonValue(r'object')
object(r'object');

const InvestigationItemItemTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


