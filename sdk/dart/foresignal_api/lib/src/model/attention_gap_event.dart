//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'attention_gap_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AttentionGapEvent {
  /// Returns a new [AttentionGapEvent] instance.
  AttentionGapEvent({

    required  this.event,

    required  this.findingId,

    required  this.ref,

     this.regionCodes,

    required  this.expected,

    required  this.observed,

    required  this.platforms,

    required  this.at,
  });

  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final AttentionGapEventEventEnum event;



      /// ULID identifier
  @JsonKey(
    
    name: r'findingId',
    required: true,
    includeIfNull: false,
  )


  final String findingId;



      /// Canonical event or situation
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



      /// Expected posts in the 6 h after the event (normalised)
  @JsonKey(
    
    name: r'expected',
    required: true,
    includeIfNull: false,
  )


  final num expected;



  @JsonKey(
    
    name: r'observed',
    required: true,
    includeIfNull: false,
  )


  final num observed;



  @JsonKey(
    
    name: r'platforms',
    required: true,
    includeIfNull: false,
  )


  final List<String> platforms;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AttentionGapEvent &&
      other.event == event &&
      other.findingId == findingId &&
      other.ref == ref &&
      other.regionCodes == regionCodes &&
      other.expected == expected &&
      other.observed == observed &&
      other.platforms == platforms &&
      other.at == at;

    @override
    int get hashCode =>
        event.hashCode +
        findingId.hashCode +
        ref.hashCode +
        regionCodes.hashCode +
        expected.hashCode +
        observed.hashCode +
        platforms.hashCode +
        at.hashCode;

  factory AttentionGapEvent.fromJson(Map<String, dynamic> json) => _$AttentionGapEventFromJson(json);

  Map<String, dynamic> toJson() => _$AttentionGapEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AttentionGapEventEventEnum {
@JsonValue(r'attention.gap')
attentionPeriodGap(r'attention.gap');

const AttentionGapEventEventEnum(this.value);

final String value;

@override
String toString() => value;
}


