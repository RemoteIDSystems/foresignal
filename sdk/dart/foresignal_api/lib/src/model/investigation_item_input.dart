//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'investigation_item_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InvestigationItemInput {
  /// Returns a new [InvestigationItemInput] instance.
  InvestigationItemInput({

    required  this.itemType,

    required  this.ref,

     this.seq,

     this.note,
  });

  @JsonKey(
    
    name: r'itemType',
    required: true,
    includeIfNull: false,
  )


  final InvestigationItemInputItemTypeEnum itemType;



      /// Item reference; for evidence-item, attachment and object an object reference (or the bare id of an evidence item or attachment), which the caller must be able to see
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



      /// Pin a record version; default the current version
  @JsonKey(
    
    name: r'seq',
    required: false,
    includeIfNull: false,
  )


  final int? seq;



  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InvestigationItemInput &&
      other.itemType == itemType &&
      other.ref == ref &&
      other.seq == seq &&
      other.note == note;

    @override
    int get hashCode =>
        itemType.hashCode +
        ref.hashCode +
        seq.hashCode +
        note.hashCode;

  factory InvestigationItemInput.fromJson(Map<String, dynamic> json) => _$InvestigationItemInputFromJson(json);

  Map<String, dynamic> toJson() => _$InvestigationItemInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum InvestigationItemInputItemTypeEnum {
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

const InvestigationItemInputItemTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


