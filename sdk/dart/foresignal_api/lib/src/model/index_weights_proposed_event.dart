//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/regional_index_def_skill.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'index_weights_proposed_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IndexWeightsProposedEvent {
  /// Returns a new [IndexWeightsProposedEvent] instance.
  IndexWeightsProposedEvent({

    required  this.indexId,

    required  this.fitId,

    required  this.weightsVersion,

    required  this.proposedWeights,

     this.skill,

     this.autoApplied,
  });

  @JsonKey(
    
    name: r'indexId',
    required: true,
    includeIfNull: false,
  )


  final String indexId;



      /// ULID identifier
  @JsonKey(
    
    name: r'fitId',
    required: true,
    includeIfNull: false,
  )


  final String fitId;



  @JsonKey(
    
    name: r'weightsVersion',
    required: true,
    includeIfNull: false,
  )


  final int weightsVersion;



  @JsonKey(
    
    name: r'proposedWeights',
    required: true,
    includeIfNull: false,
  )


  final Map<String, num> proposedWeights;



  @JsonKey(
    
    name: r'skill',
    required: false,
    includeIfNull: false,
  )


  final RegionalIndexDefSkill? skill;



  @JsonKey(
    
    name: r'autoApplied',
    required: false,
    includeIfNull: false,
  )


  final bool? autoApplied;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IndexWeightsProposedEvent &&
      other.indexId == indexId &&
      other.fitId == fitId &&
      other.weightsVersion == weightsVersion &&
      other.proposedWeights == proposedWeights &&
      other.skill == skill &&
      other.autoApplied == autoApplied;

    @override
    int get hashCode =>
        indexId.hashCode +
        fitId.hashCode +
        weightsVersion.hashCode +
        proposedWeights.hashCode +
        (skill == null ? 0 : skill.hashCode) +
        autoApplied.hashCode;

  factory IndexWeightsProposedEvent.fromJson(Map<String, dynamic> json) => _$IndexWeightsProposedEventFromJson(json);

  Map<String, dynamic> toJson() => _$IndexWeightsProposedEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

