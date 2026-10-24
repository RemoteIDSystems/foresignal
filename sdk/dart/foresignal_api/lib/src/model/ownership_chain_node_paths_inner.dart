//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ownership_chain_node_paths_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OwnershipChainNodePathsInner {
  /// Returns a new [OwnershipChainNodePathsInner] instance.
  OwnershipChainNodePathsInner({

    required  this.from,

    required  this.hops,

     this.relationTypes,

    required  this.weakestEdgeLabel,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final String from;



  @JsonKey(
    
    name: r'hops',
    required: true,
    includeIfNull: false,
  )


  final List<String> hops;



  @JsonKey(
    
    name: r'relationTypes',
    required: false,
    includeIfNull: false,
  )


  final List<OwnershipChainNodePathsInnerRelationTypesEnum>? relationTypes;



  @JsonKey(
    
    name: r'weakestEdgeLabel',
    required: true,
    includeIfNull: false,
  )


  final OwnershipChainNodePathsInnerWeakestEdgeLabelEnum weakestEdgeLabel;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OwnershipChainNodePathsInner &&
      other.from == from &&
      other.hops == hops &&
      other.relationTypes == relationTypes &&
      other.weakestEdgeLabel == weakestEdgeLabel;

    @override
    int get hashCode =>
        from.hashCode +
        hops.hashCode +
        relationTypes.hashCode +
        weakestEdgeLabel.hashCode;

  factory OwnershipChainNodePathsInner.fromJson(Map<String, dynamic> json) => _$OwnershipChainNodePathsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$OwnershipChainNodePathsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OwnershipChainNodePathsInnerRelationTypesEnum {
@JsonValue(r'owns')
owns(r'owns'),
@JsonValue(r'controls')
controls(r'controls'),
@JsonValue(r'officer-of')
officerOf(r'officer-of'),
@JsonValue(r'beneficial-owner')
beneficialOwner(r'beneficial-owner'),
@JsonValue(r'affiliated-with')
affiliatedWith(r'affiliated-with');

const OwnershipChainNodePathsInnerRelationTypesEnum(this.value);

final String value;

@override
String toString() => value;
}


enum OwnershipChainNodePathsInnerWeakestEdgeLabelEnum {
@JsonValue(r'confirmed')
confirmed(r'confirmed'),
@JsonValue(r'likely')
likely(r'likely'),
@JsonValue(r'possible')
possible(r'possible'),
@JsonValue(r'speculative')
speculative(r'speculative');

const OwnershipChainNodePathsInnerWeakestEdgeLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


