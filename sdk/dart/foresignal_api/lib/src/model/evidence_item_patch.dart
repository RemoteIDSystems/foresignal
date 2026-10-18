//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'evidence_item_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EvidenceItemPatch {
  /// Returns a new [EvidenceItemPatch] instance.
  EvidenceItemPatch({

     this.title,

     this.description,

     this.attachmentIds,

     this.url,

     this.sourceDescription,

     this.sourceReliability,

     this.informationCredibility,

     this.citedSourceId,

     this.citedRefs,

     this.geometry,

     this.eventTime,

     this.visibility,

     this.tlp,
  });

  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'attachmentIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? attachmentIds;



  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'sourceDescription',
    required: false,
    includeIfNull: false,
  )


  final String? sourceDescription;



      /// Admiralty source reliability (A completely reliable … F cannot be judged)
  @JsonKey(
    
    name: r'sourceReliability',
    required: false,
    includeIfNull: false,
  )


  final EvidenceItemPatchSourceReliabilityEnum? sourceReliability;



      /// Admiralty information credibility (1 confirmed … 6 cannot be judged)
          // minimum: 1
          // maximum: 6
  @JsonKey(
    
    name: r'informationCredibility',
    required: false,
    includeIfNull: false,
  )


  final int? informationCredibility;



  @JsonKey(
    
    name: r'citedSourceId',
    required: false,
    includeIfNull: false,
  )


  final String? citedSourceId;



  @JsonKey(
    
    name: r'citedRefs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? citedRefs;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



  @JsonKey(
    
    name: r'eventTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? eventTime;



  @JsonKey(
    
    name: r'visibility',
    required: false,
    includeIfNull: false,
  )


  final Visibility? visibility;



  @JsonKey(
    
    name: r'tlp',
    required: false,
    includeIfNull: false,
  )


  final Tlp? tlp;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EvidenceItemPatch &&
      other.title == title &&
      other.description == description &&
      other.attachmentIds == attachmentIds &&
      other.url == url &&
      other.sourceDescription == sourceDescription &&
      other.sourceReliability == sourceReliability &&
      other.informationCredibility == informationCredibility &&
      other.citedSourceId == citedSourceId &&
      other.citedRefs == citedRefs &&
      other.geometry == geometry &&
      other.eventTime == eventTime &&
      other.visibility == visibility &&
      other.tlp == tlp;

    @override
    int get hashCode =>
        title.hashCode +
        (description == null ? 0 : description.hashCode) +
        attachmentIds.hashCode +
        (url == null ? 0 : url.hashCode) +
        (sourceDescription == null ? 0 : sourceDescription.hashCode) +
        sourceReliability.hashCode +
        informationCredibility.hashCode +
        (citedSourceId == null ? 0 : citedSourceId.hashCode) +
        citedRefs.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        (eventTime == null ? 0 : eventTime.hashCode) +
        visibility.hashCode +
        tlp.hashCode;

  factory EvidenceItemPatch.fromJson(Map<String, dynamic> json) => _$EvidenceItemPatchFromJson(json);

  Map<String, dynamic> toJson() => _$EvidenceItemPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Admiralty source reliability (A completely reliable … F cannot be judged)
enum EvidenceItemPatchSourceReliabilityEnum {
@JsonValue(r'A')
A(r'A'),
@JsonValue(r'B')
B(r'B'),
@JsonValue(r'C')
C(r'C'),
@JsonValue(r'D')
D(r'D'),
@JsonValue(r'E')
E(r'E'),
@JsonValue(r'F')
F(r'F');

const EvidenceItemPatchSourceReliabilityEnum(this.value);

final String value;

@override
String toString() => value;
}


