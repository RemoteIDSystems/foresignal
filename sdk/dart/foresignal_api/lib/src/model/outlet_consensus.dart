//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/outlet_consensus_providers_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'outlet_consensus.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OutletConsensus {
  /// Returns a new [OutletConsensus] instance.
  OutletConsensus({

    required  this.outletId,

    required  this.dimension,

     this.median,

    required  this.nProviders,

     this.disagreement,

     this.bucket,

    required  this.providers,

     this.computedAt,
  });

  @JsonKey(
    
    name: r'outletId',
    required: true,
    includeIfNull: false,
  )


  final String outletId;



      /// Rating dimension; provider axes that are not left–right are recorded as `{provider}:{axis}`, never forced into lean
  @JsonKey(
    
    name: r'dimension',
    required: true,
    includeIfNull: true,
  )


  final Object? dimension;



  @JsonKey(
    
    name: r'median',
    required: false,
    includeIfNull: false,
  )


  final num? median;



  @JsonKey(
    
    name: r'nProviders',
    required: true,
    includeIfNull: false,
  )


  final int nProviders;



      /// max − min of valueNorm
  @JsonKey(
    
    name: r'disagreement',
    required: false,
    includeIfNull: false,
  )


  final num? disagreement;



      /// Lean only: |median| < 0.75 is center
  @JsonKey(
    
    name: r'bucket',
    required: false,
    includeIfNull: false,
  )


  final OutletConsensusBucketEnum? bucket;



  @JsonKey(
    
    name: r'providers',
    required: true,
    includeIfNull: false,
  )


  final List<OutletConsensusProvidersInner> providers;



  @JsonKey(
    
    name: r'computedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? computedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OutletConsensus &&
      other.outletId == outletId &&
      other.dimension == dimension &&
      other.median == median &&
      other.nProviders == nProviders &&
      other.disagreement == disagreement &&
      other.bucket == bucket &&
      other.providers == providers &&
      other.computedAt == computedAt;

    @override
    int get hashCode =>
        outletId.hashCode +
        (dimension == null ? 0 : dimension.hashCode) +
        (median == null ? 0 : median.hashCode) +
        nProviders.hashCode +
        (disagreement == null ? 0 : disagreement.hashCode) +
        (bucket == null ? 0 : bucket.hashCode) +
        providers.hashCode +
        computedAt.hashCode;

  factory OutletConsensus.fromJson(Map<String, dynamic> json) => _$OutletConsensusFromJson(json);

  Map<String, dynamic> toJson() => _$OutletConsensusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Lean only: |median| < 0.75 is center
enum OutletConsensusBucketEnum {
@JsonValue(r'left')
left(r'left'),
@JsonValue(r'center')
center(r'center'),
@JsonValue(r'right')
right(r'right');

const OutletConsensusBucketEnum(this.value);

final String value;

@override
String toString() => value;
}


