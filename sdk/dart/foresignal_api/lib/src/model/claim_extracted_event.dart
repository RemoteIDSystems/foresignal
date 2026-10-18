//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/claim.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'claim_extracted_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ClaimExtractedEvent {
  /// Returns a new [ClaimExtractedEvent] instance.
  ClaimExtractedEvent({

    required  this.event,

    required  this.claim,

    required  this.at,
  });

  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final ClaimExtractedEventEventEnum event;



  @JsonKey(
    
    name: r'claim',
    required: true,
    includeIfNull: false,
  )


  final Claim claim;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ClaimExtractedEvent &&
      other.event == event &&
      other.claim == claim &&
      other.at == at;

    @override
    int get hashCode =>
        event.hashCode +
        claim.hashCode +
        at.hashCode;

  factory ClaimExtractedEvent.fromJson(Map<String, dynamic> json) => _$ClaimExtractedEventFromJson(json);

  Map<String, dynamic> toJson() => _$ClaimExtractedEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ClaimExtractedEventEventEnum {
@JsonValue(r'claim.extracted')
claimPeriodExtracted(r'claim.extracted');

const ClaimExtractedEventEventEnum(this.value);

final String value;

@override
String toString() => value;
}


