//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/infra_impact_result_dependents_inner_path_inner.dart';
import 'package:foresignal_api/src/model/infra_node.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'infra_impact_result_dependents_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InfraImpactResultDependentsInner {
  /// Returns a new [InfraImpactResultDependentsInner] instance.
  InfraImpactResultDependentsInner({

    required  this.node,

    required  this.depth,

    required  this.pathLabel,

    required  this.pathScore,

    required  this.path,
  });

  @JsonKey(
    
    name: r'node',
    required: true,
    includeIfNull: false,
  )


  final InfraNode node;



  @JsonKey(
    
    name: r'depth',
    required: true,
    includeIfNull: false,
  )


  final int depth;



      /// Weakest edge label
  @JsonKey(
    
    name: r'pathLabel',
    required: true,
    includeIfNull: false,
  )


  final InfraImpactResultDependentsInnerPathLabelEnum pathLabel;



      /// Product of edge scores
  @JsonKey(
    
    name: r'pathScore',
    required: true,
    includeIfNull: false,
  )


  final num pathScore;



  @JsonKey(
    
    name: r'path',
    required: true,
    includeIfNull: false,
  )


  final List<InfraImpactResultDependentsInnerPathInner> path;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InfraImpactResultDependentsInner &&
      other.node == node &&
      other.depth == depth &&
      other.pathLabel == pathLabel &&
      other.pathScore == pathScore &&
      other.path == path;

    @override
    int get hashCode =>
        node.hashCode +
        depth.hashCode +
        pathLabel.hashCode +
        pathScore.hashCode +
        path.hashCode;

  factory InfraImpactResultDependentsInner.fromJson(Map<String, dynamic> json) => _$InfraImpactResultDependentsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$InfraImpactResultDependentsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Weakest edge label
enum InfraImpactResultDependentsInnerPathLabelEnum {
@JsonValue(r'confirmed')
confirmed(r'confirmed'),
@JsonValue(r'likely')
likely(r'likely'),
@JsonValue(r'possible')
possible(r'possible'),
@JsonValue(r'speculative')
speculative(r'speculative');

const InfraImpactResultDependentsInnerPathLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


