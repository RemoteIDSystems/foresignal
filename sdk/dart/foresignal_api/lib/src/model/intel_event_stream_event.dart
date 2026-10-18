//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/intel_event_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_event_stream_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelEventStreamEvent {
  /// Returns a new [IntelEventStreamEvent] instance.
  IntelEventStreamEvent({

    required  this.eventId,

    required  this.change,

    required  this.version,

    required  this.summary,

     this.mergedInto,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'eventId',
    required: true,
    includeIfNull: false,
  )


  final String eventId;



  @JsonKey(
    
    name: r'change',
    required: true,
    includeIfNull: false,
  )


  final IntelEventStreamEventChangeEnum change;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'summary',
    required: true,
    includeIfNull: false,
  )


  final IntelEventSummary summary;



      /// ULID identifier
  @JsonKey(
    
    name: r'mergedInto',
    required: false,
    includeIfNull: false,
  )


  final String? mergedInto;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelEventStreamEvent &&
      other.eventId == eventId &&
      other.change == change &&
      other.version == version &&
      other.summary == summary &&
      other.mergedInto == mergedInto;

    @override
    int get hashCode =>
        eventId.hashCode +
        change.hashCode +
        version.hashCode +
        summary.hashCode +
        (mergedInto == null ? 0 : mergedInto.hashCode);

  factory IntelEventStreamEvent.fromJson(Map<String, dynamic> json) => _$IntelEventStreamEventFromJson(json);

  Map<String, dynamic> toJson() => _$IntelEventStreamEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IntelEventStreamEventChangeEnum {
@JsonValue(r'created')
created(r'created'),
@JsonValue(r'updated')
updated(r'updated'),
@JsonValue(r'resolved')
resolved(r'resolved'),
@JsonValue(r'merged')
merged(r'merged');

const IntelEventStreamEventChangeEnum(this.value);

final String value;

@override
String toString() => value;
}


