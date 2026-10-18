//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'provenance_hop.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProvenanceHop {
  /// Returns a new [ProvenanceHop] instance.
  ProvenanceHop({

    required  this.instanceId,

    required  this.sourceId,

    required  this.externalId,

    required  this.seq,

    required  this.ingestedAt,
  });

      /// Install id of the instance (003)
  @JsonKey(
    
    name: r'instanceId',
    required: true,
    includeIfNull: false,
  )


  final String instanceId;



  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



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



  @JsonKey(
    
    name: r'ingestedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime ingestedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProvenanceHop &&
      other.instanceId == instanceId &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.seq == seq &&
      other.ingestedAt == ingestedAt;

    @override
    int get hashCode =>
        instanceId.hashCode +
        sourceId.hashCode +
        externalId.hashCode +
        seq.hashCode +
        ingestedAt.hashCode;

  factory ProvenanceHop.fromJson(Map<String, dynamic> json) => _$ProvenanceHopFromJson(json);

  Map<String, dynamic> toJson() => _$ProvenanceHopToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

