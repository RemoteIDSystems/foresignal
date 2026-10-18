//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/precursor_event_precursor.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorEvent {
  /// Returns a new [PrecursorEvent] instance.
  PrecursorEvent({

    required  this.event,

    required  this.precursor,

    required  this.at,
  });

  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final PrecursorEventEventEnum event;



  @JsonKey(
    
    name: r'precursor',
    required: true,
    includeIfNull: false,
  )


  final PrecursorEventPrecursor precursor;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorEvent &&
      other.event == event &&
      other.precursor == precursor &&
      other.at == at;

    @override
    int get hashCode =>
        event.hashCode +
        precursor.hashCode +
        at.hashCode;

  factory PrecursorEvent.fromJson(Map<String, dynamic> json) => _$PrecursorEventFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecursorEventEventEnum {
@JsonValue(r'precursor.validated')
precursorPeriodValidated(r'precursor.validated'),
@JsonValue(r'precursor.stale')
precursorPeriodStale(r'precursor.stale'),
@JsonValue(r'precursor.retired')
precursorPeriodRetired(r'precursor.retired');

const PrecursorEventEventEnum(this.value);

final String value;

@override
String toString() => value;
}


