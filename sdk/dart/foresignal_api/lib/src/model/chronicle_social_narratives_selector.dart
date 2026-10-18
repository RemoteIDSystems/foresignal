//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_social_narratives_selector.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSocialNarrativesSelector {
  /// Returns a new [ChronicleSocialNarrativesSelector] instance.
  ChronicleSocialNarrativesSelector({

    required  this.type,

     this.topicIds,

     this.placeIds,

     this.regionCodes,

     this.situationIds,

     this.statuses,

     this.keepPostText = false,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ChronicleSocialNarrativesSelectorTypeEnum type;



  @JsonKey(
    
    name: r'topicIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? topicIds;



  @JsonKey(
    
    name: r'placeIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? placeIds;



  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



  @JsonKey(
    
    name: r'situationIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? situationIds;



  @JsonKey(
    
    name: r'statuses',
    required: false,
    includeIfNull: false,
  )


  final List<ChronicleSocialNarrativesSelectorStatusesEnum>? statuses;



      /// Keep member post text in chronicle storage (else claim summaries and counts only)
  @JsonKey(
    defaultValue: false,
    name: r'keepPostText',
    required: false,
    includeIfNull: false,
  )


  final bool? keepPostText;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSocialNarrativesSelector &&
      other.type == type &&
      other.topicIds == topicIds &&
      other.placeIds == placeIds &&
      other.regionCodes == regionCodes &&
      other.situationIds == situationIds &&
      other.statuses == statuses &&
      other.keepPostText == keepPostText;

    @override
    int get hashCode =>
        type.hashCode +
        topicIds.hashCode +
        placeIds.hashCode +
        regionCodes.hashCode +
        situationIds.hashCode +
        statuses.hashCode +
        keepPostText.hashCode;

  factory ChronicleSocialNarrativesSelector.fromJson(Map<String, dynamic> json) => _$ChronicleSocialNarrativesSelectorFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSocialNarrativesSelectorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleSocialNarrativesSelectorTypeEnum {
@JsonValue(r'social-narratives')
socialNarratives(r'social-narratives');

const ChronicleSocialNarrativesSelectorTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChronicleSocialNarrativesSelectorStatusesEnum {
@JsonValue(r'unassessed')
unassessed(r'unassessed'),
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'corroborated')
corroborated(r'corroborated'),
@JsonValue(r'unobserved')
unobserved(r'unobserved'),
@JsonValue(r'contradicted')
contradicted(r'contradicted'),
@JsonValue(r'mixed')
mixed(r'mixed'),
@JsonValue(r'insufficient-coverage')
insufficientCoverage(r'insufficient-coverage');

const ChronicleSocialNarrativesSelectorStatusesEnum(this.value);

final String value;

@override
String toString() => value;
}


