//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_versus_observed_narratives_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialVersusObservedNarrativesInner {
  /// Returns a new [SocialVersusObservedNarrativesInner] instance.
  SocialVersusObservedNarrativesInner({

    required  this.narrativeId,

    required  this.title,

    required  this.status,

     this.situationId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'narrativeId',
    required: true,
    includeIfNull: false,
  )


  final String narrativeId;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final SocialVersusObservedNarrativesInnerStatusEnum status;



      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: false,
    includeIfNull: false,
  )


  final String? situationId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialVersusObservedNarrativesInner &&
      other.narrativeId == narrativeId &&
      other.title == title &&
      other.status == status &&
      other.situationId == situationId;

    @override
    int get hashCode =>
        narrativeId.hashCode +
        title.hashCode +
        status.hashCode +
        (situationId == null ? 0 : situationId.hashCode);

  factory SocialVersusObservedNarrativesInner.fromJson(Map<String, dynamic> json) => _$SocialVersusObservedNarrativesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SocialVersusObservedNarrativesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SocialVersusObservedNarrativesInnerStatusEnum {
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

const SocialVersusObservedNarrativesInnerStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


