//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'media_reuse_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MediaReuseEvent {
  /// Returns a new [MediaReuseEvent] instance.
  MediaReuseEvent({

    required  this.event,

    required  this.findingId,

    required  this.fingerprintId,

    required  this.narrativeId,

     this.earlierNarrativeId,

    required  this.firstSeenAt,

    required  this.reason,

    required  this.at,
  });

  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final MediaReuseEventEventEnum event;



      /// ULID identifier
  @JsonKey(
    
    name: r'findingId',
    required: true,
    includeIfNull: false,
  )


  final String findingId;



      /// ULID identifier
  @JsonKey(
    
    name: r'fingerprintId',
    required: true,
    includeIfNull: false,
  )


  final String fingerprintId;



      /// ULID identifier
  @JsonKey(
    
    name: r'narrativeId',
    required: true,
    includeIfNull: false,
  )


  final String narrativeId;



      /// ULID identifier
  @JsonKey(
    
    name: r'earlierNarrativeId',
    required: false,
    includeIfNull: false,
  )


  final String? earlierNarrativeId;



  @JsonKey(
    
    name: r'firstSeenAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime firstSeenAt;



      /// First seen ≥ 7 days earlier, or attached to an earlier narrative in a different place
  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final MediaReuseEventReasonEnum reason;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MediaReuseEvent &&
      other.event == event &&
      other.findingId == findingId &&
      other.fingerprintId == fingerprintId &&
      other.narrativeId == narrativeId &&
      other.earlierNarrativeId == earlierNarrativeId &&
      other.firstSeenAt == firstSeenAt &&
      other.reason == reason &&
      other.at == at;

    @override
    int get hashCode =>
        event.hashCode +
        findingId.hashCode +
        fingerprintId.hashCode +
        narrativeId.hashCode +
        (earlierNarrativeId == null ? 0 : earlierNarrativeId.hashCode) +
        firstSeenAt.hashCode +
        reason.hashCode +
        at.hashCode;

  factory MediaReuseEvent.fromJson(Map<String, dynamic> json) => _$MediaReuseEventFromJson(json);

  Map<String, dynamic> toJson() => _$MediaReuseEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MediaReuseEventEventEnum {
@JsonValue(r'media.reuse')
mediaPeriodReuse(r'media.reuse');

const MediaReuseEventEventEnum(this.value);

final String value;

@override
String toString() => value;
}


/// First seen ≥ 7 days earlier, or attached to an earlier narrative in a different place
enum MediaReuseEventReasonEnum {
@JsonValue(r'earlier')
earlier(r'earlier'),
@JsonValue(r'elsewhere')
elsewhere(r'elsewhere');

const MediaReuseEventReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


