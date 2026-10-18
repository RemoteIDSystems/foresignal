//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'opaque_chronicle_link.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OpaqueChronicleLink {
  /// Returns a new [OpaqueChronicleLink] instance.
  OpaqueChronicleLink({

    required  this.linkId,

    required  this.disclosure,

     this.chronicleId,

    required  this.strength,

    required  this.dimensions,

    required  this.timeOverlap,

     this.remote,

     this.ownerHandle,

     this.title,

     this.accessRequestStatus,
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


  final OpaqueChronicleLinkDisclosureEnum disclosure;



      /// The caller's own chronicle in this link
  @JsonKey(
    
    name: r'chronicleId',
    required: false,
    includeIfNull: false,
  )


  final String? chronicleId;



      /// From estimated Jaccard: low < 0.1 ≤ medium < 0.3 ≤ high
  @JsonKey(
    
    name: r'strength',
    required: true,
    includeIfNull: false,
  )


  final OpaqueChronicleLinkStrengthEnum strength;



  @JsonKey(
    
    name: r'dimensions',
    required: true,
    includeIfNull: false,
  )


  final List<OpaqueChronicleLinkDimensionsEnum> dimensions;



  @JsonKey(
    
    name: r'timeOverlap',
    required: true,
    includeIfNull: false,
  )


  final OpaqueChronicleLinkTimeOverlapEnum timeOverlap;



  @JsonKey(
    
    name: r'remote',
    required: false,
    includeIfNull: false,
  )


  final bool? remote;



  @JsonKey(
    
    name: r'ownerHandle',
    required: false,
    includeIfNull: false,
  )


  final String? ownerHandle;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'accessRequestStatus',
    required: false,
    includeIfNull: false,
  )


  final OpaqueChronicleLinkAccessRequestStatusEnum? accessRequestStatus;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OpaqueChronicleLink &&
      other.linkId == linkId &&
      other.disclosure == disclosure &&
      other.chronicleId == chronicleId &&
      other.strength == strength &&
      other.dimensions == dimensions &&
      other.timeOverlap == timeOverlap &&
      other.remote == remote &&
      other.ownerHandle == ownerHandle &&
      other.title == title &&
      other.accessRequestStatus == accessRequestStatus;

    @override
    int get hashCode =>
        linkId.hashCode +
        disclosure.hashCode +
        chronicleId.hashCode +
        strength.hashCode +
        dimensions.hashCode +
        timeOverlap.hashCode +
        remote.hashCode +
        (ownerHandle == null ? 0 : ownerHandle.hashCode) +
        (title == null ? 0 : title.hashCode) +
        (accessRequestStatus == null ? 0 : accessRequestStatus.hashCode);

  factory OpaqueChronicleLink.fromJson(Map<String, dynamic> json) => _$OpaqueChronicleLinkFromJson(json);

  Map<String, dynamic> toJson() => _$OpaqueChronicleLinkToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OpaqueChronicleLinkDisclosureEnum {
@JsonValue(r'opaque')
opaque(r'opaque');

const OpaqueChronicleLinkDisclosureEnum(this.value);

final String value;

@override
String toString() => value;
}


/// From estimated Jaccard: low < 0.1 ≤ medium < 0.3 ≤ high
enum OpaqueChronicleLinkStrengthEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'medium')
medium(r'medium'),
@JsonValue(r'high')
high(r'high');

const OpaqueChronicleLinkStrengthEnum(this.value);

final String value;

@override
String toString() => value;
}


enum OpaqueChronicleLinkDimensionsEnum {
@JsonValue(r'evidence')
evidence(r'evidence'),
@JsonValue(r'entities')
entities(r'entities'),
@JsonValue(r'area-time')
areaTime(r'area-time'),
@JsonValue(r'topics')
topics(r'topics');

const OpaqueChronicleLinkDimensionsEnum(this.value);

final String value;

@override
String toString() => value;
}


enum OpaqueChronicleLinkTimeOverlapEnum {
@JsonValue(r'none')
none(r'none'),
@JsonValue(r'partial')
partial(r'partial'),
@JsonValue(r'full')
full(r'full');

const OpaqueChronicleLinkTimeOverlapEnum(this.value);

final String value;

@override
String toString() => value;
}


enum OpaqueChronicleLinkAccessRequestStatusEnum {
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'granted')
granted(r'granted'),
@JsonValue(r'declined')
declined(r'declined');

const OpaqueChronicleLinkAccessRequestStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


