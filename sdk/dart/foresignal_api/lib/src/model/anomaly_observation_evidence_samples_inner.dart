//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'anomaly_observation_evidence_samples_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnomalyObservationEvidenceSamplesInner {
  /// Returns a new [AnomalyObservationEvidenceSamplesInner] instance.
  AnomalyObservationEvidenceSamplesInner({

    required  this.source_,

    required  this.externalId,

    required  this.seq,
  });

  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final String source_;



  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;



  @JsonKey(
    
    name: r'seq',
    required: true,
    includeIfNull: false,
  )


  final int seq;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnomalyObservationEvidenceSamplesInner &&
      other.source_ == source_ &&
      other.externalId == externalId &&
      other.seq == seq;

    @override
    int get hashCode =>
        source_.hashCode +
        externalId.hashCode +
        seq.hashCode;

  factory AnomalyObservationEvidenceSamplesInner.fromJson(Map<String, dynamic> json) => _$AnomalyObservationEvidenceSamplesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$AnomalyObservationEvidenceSamplesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

