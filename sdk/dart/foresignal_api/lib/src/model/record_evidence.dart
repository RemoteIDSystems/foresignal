//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/canonical_event.dart';
import 'package:foresignal_api/src/model/observation_link.dart';
import 'package:foresignal_api/src/model/originator.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'record_evidence.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecordEvidence {
  /// Returns a new [RecordEvidence] instance.
  RecordEvidence({

    required  this.sourceId,

    required  this.externalId,

    required  this.originator,

    required  this.links,

    required  this.canonicalEvents,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;



  @JsonKey(
    
    name: r'originator',
    required: true,
    includeIfNull: false,
  )


  final Originator originator;



  @JsonKey(
    
    name: r'links',
    required: true,
    includeIfNull: false,
  )


  final List<ObservationLink> links;



      /// Summaries
  @JsonKey(
    
    name: r'canonicalEvents',
    required: true,
    includeIfNull: false,
  )


  final List<CanonicalEvent> canonicalEvents;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecordEvidence &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.originator == originator &&
      other.links == links &&
      other.canonicalEvents == canonicalEvents;

    @override
    int get hashCode =>
        sourceId.hashCode +
        externalId.hashCode +
        originator.hashCode +
        links.hashCode +
        canonicalEvents.hashCode;

  factory RecordEvidence.fromJson(Map<String, dynamic> json) => _$RecordEvidenceFromJson(json);

  Map<String, dynamic> toJson() => _$RecordEvidenceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

