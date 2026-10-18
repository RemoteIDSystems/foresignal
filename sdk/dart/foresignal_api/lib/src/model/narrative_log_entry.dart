//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'narrative_log_entry.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NarrativeLogEntry {
  /// Returns a new [NarrativeLogEntry] instance.
  NarrativeLogEntry({

    required  this.at,

    required  this.action,

    required  this.by,

     this.relatedNarrativeIds,

     this.from,

     this.to,

     this.reason,
  });

  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'action',
    required: true,
    includeIfNull: false,
  )


  final NarrativeLogEntryActionEnum action;



      /// `system` or principal id
  @JsonKey(
    
    name: r'by',
    required: true,
    includeIfNull: false,
  )


  final String by;



  @JsonKey(
    
    name: r'relatedNarrativeIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? relatedNarrativeIds;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final String? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final String? to;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NarrativeLogEntry &&
      other.at == at &&
      other.action == action &&
      other.by == by &&
      other.relatedNarrativeIds == relatedNarrativeIds &&
      other.from == from &&
      other.to == to &&
      other.reason == reason;

    @override
    int get hashCode =>
        at.hashCode +
        action.hashCode +
        by.hashCode +
        relatedNarrativeIds.hashCode +
        (from == null ? 0 : from.hashCode) +
        (to == null ? 0 : to.hashCode) +
        (reason == null ? 0 : reason.hashCode);

  factory NarrativeLogEntry.fromJson(Map<String, dynamic> json) => _$NarrativeLogEntryFromJson(json);

  Map<String, dynamic> toJson() => _$NarrativeLogEntryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NarrativeLogEntryActionEnum {
@JsonValue(r'created')
created(r'created'),
@JsonValue(r'merged')
merged(r'merged'),
@JsonValue(r'merged-into')
mergedInto(r'merged-into'),
@JsonValue(r'split')
split(r'split'),
@JsonValue(r'split-from')
splitFrom(r'split-from'),
@JsonValue(r'state-changed')
stateChanged(r'state-changed'),
@JsonValue(r'status-changed')
statusChanged(r'status-changed'),
@JsonValue(r'title-changed')
titleChanged(r'title-changed');

const NarrativeLogEntryActionEnum(this.value);

final String value;

@override
String toString() => value;
}


