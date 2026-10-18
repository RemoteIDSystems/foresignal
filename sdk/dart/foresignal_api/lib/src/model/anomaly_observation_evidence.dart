//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/anomaly_observation_evidence_fetch_log_ranges_inner.dart';
import 'package:foresignal_api/src/model/anomaly_observation_evidence_samples_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'anomaly_observation_evidence.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnomalyObservationEvidence {
  /// Returns a new [AnomalyObservationEvidence] instance.
  AnomalyObservationEvidence({

     this.samples,

     this.fetchLogRanges,
  });

  @JsonKey(
    
    name: r'samples',
    required: false,
    includeIfNull: false,
  )


  final List<AnomalyObservationEvidenceSamplesInner>? samples;



  @JsonKey(
    
    name: r'fetchLogRanges',
    required: false,
    includeIfNull: false,
  )


  final List<AnomalyObservationEvidenceFetchLogRangesInner>? fetchLogRanges;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnomalyObservationEvidence &&
      other.samples == samples &&
      other.fetchLogRanges == fetchLogRanges;

    @override
    int get hashCode =>
        samples.hashCode +
        fetchLogRanges.hashCode;

  factory AnomalyObservationEvidence.fromJson(Map<String, dynamic> json) => _$AnomalyObservationEvidenceFromJson(json);

  Map<String, dynamic> toJson() => _$AnomalyObservationEvidenceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

