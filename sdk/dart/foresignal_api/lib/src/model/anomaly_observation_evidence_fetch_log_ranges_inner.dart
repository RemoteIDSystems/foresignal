//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'anomaly_observation_evidence_fetch_log_ranges_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnomalyObservationEvidenceFetchLogRangesInner {
  /// Returns a new [AnomalyObservationEvidenceFetchLogRangesInner] instance.
  AnomalyObservationEvidenceFetchLogRangesInner({

    required  this.sourceId,

    required  this.fromId,

    required  this.toId,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'fromId',
    required: true,
    includeIfNull: false,
  )


  final int fromId;



  @JsonKey(
    
    name: r'toId',
    required: true,
    includeIfNull: false,
  )


  final int toId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnomalyObservationEvidenceFetchLogRangesInner &&
      other.sourceId == sourceId &&
      other.fromId == fromId &&
      other.toId == toId;

    @override
    int get hashCode =>
        sourceId.hashCode +
        fromId.hashCode +
        toId.hashCode;

  factory AnomalyObservationEvidenceFetchLogRangesInner.fromJson(Map<String, dynamic> json) => _$AnomalyObservationEvidenceFetchLogRangesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$AnomalyObservationEvidenceFetchLogRangesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

