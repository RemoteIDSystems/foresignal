//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ask_citation_corroboration.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AskCitationCorroboration {
  /// Returns a new [AskCitationCorroboration] instance.
  AskCitationCorroboration({

    required  this.evidenceQuality,

    required  this.independentSources,

     this.credibility,

     this.reliability,
  });

  @JsonKey(
    
    name: r'evidenceQuality',
    required: true,
    includeIfNull: false,
  )


  final AskCitationCorroborationEvidenceQualityEnum evidenceQuality;



          // minimum: 0
  @JsonKey(
    
    name: r'independentSources',
    required: true,
    includeIfNull: false,
  )


  final int independentSources;



      /// Admiralty information credibility; 6 means unknown
          // minimum: 1
          // maximum: 6
  @JsonKey(
    
    name: r'credibility',
    required: false,
    includeIfNull: false,
  )


  final int? credibility;



      /// Source reliability (109); F means unknown
  @JsonKey(
    
    name: r'reliability',
    required: false,
    includeIfNull: false,
  )


  final AskCitationCorroborationReliabilityEnum? reliability;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AskCitationCorroboration &&
      other.evidenceQuality == evidenceQuality &&
      other.independentSources == independentSources &&
      other.credibility == credibility &&
      other.reliability == reliability;

    @override
    int get hashCode =>
        evidenceQuality.hashCode +
        independentSources.hashCode +
        (credibility == null ? 0 : credibility.hashCode) +
        (reliability == null ? 0 : reliability.hashCode);

  factory AskCitationCorroboration.fromJson(Map<String, dynamic> json) => _$AskCitationCorroborationFromJson(json);

  Map<String, dynamic> toJson() => _$AskCitationCorroborationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AskCitationCorroborationEvidenceQualityEnum {
@JsonValue(r'strong')
strong(r'strong'),
@JsonValue(r'moderate')
moderate(r'moderate'),
@JsonValue(r'weak')
weak(r'weak'),
@JsonValue(r'insufficient')
insufficient(r'insufficient');

const AskCitationCorroborationEvidenceQualityEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Source reliability (109); F means unknown
enum AskCitationCorroborationReliabilityEnum {
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

const AskCitationCorroborationReliabilityEnum(this.value);

final String value;

@override
String toString() => value;
}


