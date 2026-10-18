//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'projected_impact_dependency_paths_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProjectedImpactDependencyPathsInner {
  /// Returns a new [ProjectedImpactDependencyPathsInner] instance.
  ProjectedImpactDependencyPathsInner({

    required  this.from,

    required  this.to,

     this.hops,

    required  this.weakestEdgeLabel,

     this.depth,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final String from;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final String to;



  @JsonKey(
    
    name: r'hops',
    required: false,
    includeIfNull: false,
  )


  final List<String>? hops;



      /// Relation or dependency label (104 vocabulary)
  @JsonKey(
    
    name: r'weakestEdgeLabel',
    required: true,
    includeIfNull: false,
  )


  final ProjectedImpactDependencyPathsInnerWeakestEdgeLabelEnum weakestEdgeLabel;



      /// Edges from the exposed facility
  @JsonKey(
    
    name: r'depth',
    required: false,
    includeIfNull: false,
  )


  final int? depth;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProjectedImpactDependencyPathsInner &&
      other.from == from &&
      other.to == to &&
      other.hops == hops &&
      other.weakestEdgeLabel == weakestEdgeLabel &&
      other.depth == depth;

    @override
    int get hashCode =>
        from.hashCode +
        to.hashCode +
        hops.hashCode +
        weakestEdgeLabel.hashCode +
        depth.hashCode;

  factory ProjectedImpactDependencyPathsInner.fromJson(Map<String, dynamic> json) => _$ProjectedImpactDependencyPathsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ProjectedImpactDependencyPathsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Relation or dependency label (104 vocabulary)
enum ProjectedImpactDependencyPathsInnerWeakestEdgeLabelEnum {
@JsonValue(r'confirmed')
confirmed(r'confirmed'),
@JsonValue(r'likely')
likely(r'likely'),
@JsonValue(r'possible')
possible(r'possible'),
@JsonValue(r'speculative')
speculative(r'speculative');

const ProjectedImpactDependencyPathsInnerWeakestEdgeLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


