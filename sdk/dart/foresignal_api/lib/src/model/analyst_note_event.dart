//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/analyst_note.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_note_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystNoteEvent {
  /// Returns a new [AnalystNoteEvent] instance.
  AnalystNoteEvent({

    required  this.note,
  });

  @JsonKey(
    
    name: r'note',
    required: true,
    includeIfNull: false,
  )


  final AnalystNote note;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystNoteEvent &&
      other.note == note;

    @override
    int get hashCode =>
        note.hashCode;

  factory AnalystNoteEvent.fromJson(Map<String, dynamic> json) => _$AnalystNoteEventFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystNoteEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

