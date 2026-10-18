//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/chronicle_link_scores.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_link.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLink {
  /// Returns a new [ChronicleLink] instance.
  ChronicleLink({

    required  this.linkId,

    required  this.disclosure,

    required  this.a,

    required  this.b,

     this.other,

    required  this.strength,

    required  this.dimensions,

     this.scores,

    required  this.temporal,

     this.timeOverlap,

     this.sharedSituations,

     this.sharedCanonicalEvents,

     this.sharedEntities,

     this.sharedKeyCount,

    required  this.status,

    required  this.firstFound,

     this.lastUpdated,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'linkId',
    required: true,
    includeIfNull: false,
  )


  final String linkId;



  @JsonKey(
    
    name: r'disclosure',
    required: true,
    includeIfNull: false,
  )


  final ChronicleLinkDisclosureEnum disclosure;



      /// Local chronicle id
  @JsonKey(
    
    name: r'a',
    required: true,
    includeIfNull: false,
  )


  final String a;



      /// Local chronicle id or `remote:{instanceId}/chronicle:{sketchId}`
  @JsonKey(
    
    name: r'b',
    required: true,
    includeIfNull: false,
  )


  final String b;



  @JsonKey(
    
    name: r'other',
    required: false,
    includeIfNull: false,
  )


  final ObjectEnvelope? other;



      /// From estimated Jaccard: low < 0.1 ≤ medium < 0.3 ≤ high
  @JsonKey(
    
    name: r'strength',
    required: true,
    includeIfNull: false,
  )


  final ChronicleLinkStrengthEnum strength;



  @JsonKey(
    
    name: r'dimensions',
    required: true,
    includeIfNull: false,
  )


  final List<ChronicleLinkDimensionsEnum> dimensions;



  @JsonKey(
    
    name: r'scores',
    required: false,
    includeIfNull: false,
  )


  final ChronicleLinkScores? scores;



  @JsonKey(
    
    name: r'temporal',
    required: true,
    includeIfNull: false,
  )


  final ChronicleLinkTemporalEnum temporal;



  @JsonKey(
    
    name: r'timeOverlap',
    required: false,
    includeIfNull: false,
  )


  final ChronicleLinkTimeOverlapEnum? timeOverlap;



  @JsonKey(
    
    name: r'sharedSituations',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sharedSituations;



      /// Computed exactly from both key sets (capped)
  @JsonKey(
    
    name: r'sharedCanonicalEvents',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sharedCanonicalEvents;



  @JsonKey(
    
    name: r'sharedEntities',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sharedEntities;



  @JsonKey(
    
    name: r'sharedKeyCount',
    required: false,
    includeIfNull: false,
  )


  final int? sharedKeyCount;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ChronicleLinkStatusEnum status;



  @JsonKey(
    
    name: r'firstFound',
    required: true,
    includeIfNull: false,
  )


  final DateTime firstFound;



  @JsonKey(
    
    name: r'lastUpdated',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastUpdated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLink &&
      other.linkId == linkId &&
      other.disclosure == disclosure &&
      other.a == a &&
      other.b == b &&
      other.other == other &&
      other.strength == strength &&
      other.dimensions == dimensions &&
      other.scores == scores &&
      other.temporal == temporal &&
      other.timeOverlap == timeOverlap &&
      other.sharedSituations == sharedSituations &&
      other.sharedCanonicalEvents == sharedCanonicalEvents &&
      other.sharedEntities == sharedEntities &&
      other.sharedKeyCount == sharedKeyCount &&
      other.status == status &&
      other.firstFound == firstFound &&
      other.lastUpdated == lastUpdated;

    @override
    int get hashCode =>
        linkId.hashCode +
        disclosure.hashCode +
        a.hashCode +
        b.hashCode +
        (other == null ? 0 : other.hashCode) +
        strength.hashCode +
        dimensions.hashCode +
        scores.hashCode +
        temporal.hashCode +
        timeOverlap.hashCode +
        sharedSituations.hashCode +
        sharedCanonicalEvents.hashCode +
        sharedEntities.hashCode +
        sharedKeyCount.hashCode +
        status.hashCode +
        firstFound.hashCode +
        lastUpdated.hashCode;

  factory ChronicleLink.fromJson(Map<String, dynamic> json) => _$ChronicleLinkFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLinkToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleLinkDisclosureEnum {
@JsonValue(r'full')
full(r'full');

const ChronicleLinkDisclosureEnum(this.value);

final String value;

@override
String toString() => value;
}


/// From estimated Jaccard: low < 0.1 ≤ medium < 0.3 ≤ high
enum ChronicleLinkStrengthEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'medium')
medium(r'medium'),
@JsonValue(r'high')
high(r'high');

const ChronicleLinkStrengthEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChronicleLinkDimensionsEnum {
@JsonValue(r'evidence')
evidence(r'evidence'),
@JsonValue(r'entities')
entities(r'entities'),
@JsonValue(r'area-time')
areaTime(r'area-time'),
@JsonValue(r'topics')
topics(r'topics');

const ChronicleLinkDimensionsEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChronicleLinkTemporalEnum {
@JsonValue(r'a-precedes-b')
aPrecedesB(r'a-precedes-b'),
@JsonValue(r'b-precedes-a')
bPrecedesA(r'b-precedes-a'),
@JsonValue(r'concurrent')
concurrent(r'concurrent');

const ChronicleLinkTemporalEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChronicleLinkTimeOverlapEnum {
@JsonValue(r'none')
none(r'none'),
@JsonValue(r'partial')
partial(r'partial'),
@JsonValue(r'full')
full(r'full');

const ChronicleLinkTimeOverlapEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChronicleLinkStatusEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'dismissed')
dismissed(r'dismissed');

const ChronicleLinkStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


