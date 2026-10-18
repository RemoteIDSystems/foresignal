//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'infra_edge_evidence.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InfraEdgeEvidence {
  /// Returns a new [InfraEdgeEvidence] instance.
  InfraEdgeEvidence({

     this.dataset,

     this.distanceKm,

     this.matched,

     this.notes,
  });

  @JsonKey(
    
    name: r'dataset',
    required: false,
    includeIfNull: false,
  )


  final String? dataset;



  @JsonKey(
    
    name: r'distanceKm',
    required: false,
    includeIfNull: false,
  )


  final num? distanceKm;



  @JsonKey(
    
    name: r'matched',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? matched;



  @JsonKey(
    
    name: r'notes',
    required: false,
    includeIfNull: false,
  )


  final String? notes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InfraEdgeEvidence &&
      other.dataset == dataset &&
      other.distanceKm == distanceKm &&
      other.matched == matched &&
      other.notes == notes;

    @override
    int get hashCode =>
        dataset.hashCode +
        (distanceKm == null ? 0 : distanceKm.hashCode) +
        matched.hashCode +
        (notes == null ? 0 : notes.hashCode);

  factory InfraEdgeEvidence.fromJson(Map<String, dynamic> json) => _$InfraEdgeEvidenceFromJson(json);

  Map<String, dynamic> toJson() => _$InfraEdgeEvidenceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

