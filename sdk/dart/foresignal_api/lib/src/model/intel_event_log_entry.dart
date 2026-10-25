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



      /// What happened. The list grows as the server adds actions; a client shows an action it does not know as its text.
  @JsonKey(
    
    name: r'action',
    required: true,
    includeIfNull: false,
  )


  final String action;



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

