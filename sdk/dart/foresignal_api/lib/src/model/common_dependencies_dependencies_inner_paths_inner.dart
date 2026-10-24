//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'common_dependencies_dependencies_inner_paths_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CommonDependenciesDependenciesInnerPathsInner {
  /// Returns a new [CommonDependenciesDependenciesInnerPathsInner] instance.
  CommonDependenciesDependenciesInnerPathsInner({

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


  final List<CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum>? relationTypes;



      /// Relation or dependency label (104 vocabulary)
  @JsonKey(
    
    name: r'weakestEdgeLabel',
    required: true,
    includeIfNull: false,
  )


  final CommonDependenciesDependenciesInnerPathsInnerWeakestEdgeLabelEnum weakestEdgeLabel;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CommonDependenciesDependenciesInnerPathsInner &&
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

  factory CommonDependenciesDependenciesInnerPathsInner.fromJson(Map<String, dynamic> json) => _$CommonDependenciesDependenciesInnerPathsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$CommonDependenciesDependenciesInnerPathsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Client-Contract §4 relation type
enum CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum {
@JsonValue(r'member-of')
memberOf(r'member-of'),
@JsonValue(r'observed-by')
observedBy(r'observed-by'),
@JsonValue(r'same-as')
sameAs(r'same-as'),
@JsonValue(r'located-in')
locatedIn(r'located-in'),
@JsonValue(r'near')
near(r'near'),
@JsonValue(r'depends-on')
dependsOn(r'depends-on'),
@JsonValue(r'affects')
affects(r'affects'),
@JsonValue(r'derived-from')
derivedFrom(r'derived-from'),
@JsonValue(r'evidence-for')
evidenceFor(r'evidence-for'),
@JsonValue(r'evidence-against')
evidenceAgainst(r'evidence-against'),
@JsonValue(r'about')
about(r'about'),
@JsonValue(r'mentions')
mentions(r'mentions'),
@JsonValue(r'precedes')
precedes(r'precedes'),
@JsonValue(r'causes')
causes(r'causes'),
@JsonValue(r'related-to')
relatedTo(r'related-to'),
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

const CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Relation or dependency label (104 vocabulary)
enum CommonDependenciesDependenciesInnerPathsInnerWeakestEdgeLabelEnum {
@JsonValue(r'confirmed')
confirmed(r'confirmed'),
@JsonValue(r'likely')
likely(r'likely'),
@JsonValue(r'possible')
possible(r'possible'),
@JsonValue(r'speculative')
speculative(r'speculative');

const CommonDependenciesDependenciesInnerPathsInnerWeakestEdgeLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


