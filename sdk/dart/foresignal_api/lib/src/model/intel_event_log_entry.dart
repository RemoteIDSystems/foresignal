//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_event_log_entry.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelEventLogEntry {
  /// Returns a new [IntelEventLogEntry] instance.
  IntelEventLogEntry({

    required  this.at,

    required  this.actor,

    required  this.action,

     this.before,

     this.after,
  });

  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



      /// Token id or `recipe:<id>`
  @JsonKey(
    
    name: r'actor',
    required: true,
    includeIfNull: false,
  )


  final String actor;



  @JsonKey(
    
    name: r'action',
    required: true,
    includeIfNull: false,
  )


  final IntelEventLogEntryActionEnum action;



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
    bool operator ==(Object other) => identical(this, other) || other is IntelEventLogEntry &&
      other.at == at &&
      other.actor == actor &&
      other.action == action &&
      other.before == before &&
      other.after == after;

    @override
    int get hashCode =>
        at.hashCode +
        actor.hashCode +
        action.hashCode +
        (before == null ? 0 : before.hashCode) +
        (after == null ? 0 : after.hashCode);

  factory IntelEventLogEntry.fromJson(Map<String, dynamic> json) => _$IntelEventLogEntryFromJson(json);

  Map<String, dynamic> toJson() => _$IntelEventLogEntryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IntelEventLogEntryActionEnum {
@JsonValue(r'created')
created(r'created'),
@JsonValue(r'member_added')
memberAdded(r'member_added'),
@JsonValue(r'member_removed')
memberRemoved(r'member_removed'),
@JsonValue(r'status')
status(r'status'),
@JsonValue(r'merged')
merged(r'merged'),
@JsonValue(r'edited')
edited(r'edited'),
@JsonValue(r'reopened')
reopened(r'reopened');

const IntelEventLogEntryActionEnum(this.value);

final String value;

@override
String toString() => value;
}


