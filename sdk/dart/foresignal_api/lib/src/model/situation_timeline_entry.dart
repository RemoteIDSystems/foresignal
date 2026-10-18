//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/situation_member.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_timeline_entry.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationTimelineEntry {
  /// Returns a new [SituationTimelineEntry] instance.
  SituationTimelineEntry({

    required  this.at,

    required  this.entryType,

     this.member,

     this.action,

     this.actor,

     this.before,

     this.after,
  });

  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'entryType',
    required: true,
    includeIfNull: false,
  )


  final SituationTimelineEntryEntryTypeEnum entryType;



  @JsonKey(
    
    name: r'member',
    required: false,
    includeIfNull: false,
  )


  final SituationMember? member;



      /// Log action (log entries)
  @JsonKey(
    
    name: r'action',
    required: false,
    includeIfNull: false,
  )


  final SituationTimelineEntryActionEnum? action;



      /// Token id, `linker:<version>` or `analyst:<profile>`
  @JsonKey(
    
    name: r'actor',
    required: false,
    includeIfNull: false,
  )


  final String? actor;



  @JsonKey(
    
    name: r'before',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? before;



  @JsonKey(
    
    name: r'after',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? after;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationTimelineEntry &&
      other.at == at &&
      other.entryType == entryType &&
      other.member == member &&
      other.action == action &&
      other.actor == actor &&
      other.before == before &&
      other.after == after;

    @override
    int get hashCode =>
        at.hashCode +
        entryType.hashCode +
        (member == null ? 0 : member.hashCode) +
        (action == null ? 0 : action.hashCode) +
        (actor == null ? 0 : actor.hashCode) +
        (before == null ? 0 : before.hashCode) +
        (after == null ? 0 : after.hashCode);

  factory SituationTimelineEntry.fromJson(Map<String, dynamic> json) => _$SituationTimelineEntryFromJson(json);

  Map<String, dynamic> toJson() => _$SituationTimelineEntryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SituationTimelineEntryEntryTypeEnum {
@JsonValue(r'member')
member(r'member'),
@JsonValue(r'log')
log(r'log');

const SituationTimelineEntryEntryTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Log action (log entries)
enum SituationTimelineEntryActionEnum {
@JsonValue(r'created')
created(r'created'),
@JsonValue(r'member_added')
memberAdded(r'member_added'),
@JsonValue(r'member_removed')
memberRemoved(r'member_removed'),
@JsonValue(r'status')
status(r'status'),
@JsonValue(r'severity')
severity(r'severity'),
@JsonValue(r'merged')
merged(r'merged'),
@JsonValue(r'split')
split(r'split'),
@JsonValue(r'renamed')
renamed(r'renamed'),
@JsonValue(r'theme')
theme(r'theme'),
@JsonValue(r'brief')
brief(r'brief');

const SituationTimelineEntryActionEnum(this.value);

final String value;

@override
String toString() => value;
}


