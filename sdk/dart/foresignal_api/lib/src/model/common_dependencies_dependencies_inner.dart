//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/common_dependencies_dependencies_inner_paths_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'common_dependencies_dependencies_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CommonDependenciesDependenciesInner {
  /// Returns a new [CommonDependenciesDependenciesInner] instance.
  CommonDependenciesDependenciesInner({

    required  this.node,

    required  this.sharedBy,

    required  this.weakestEdgeLabel,

    required  this.paths,
  });

  @JsonKey(
    
    name: r'node',
    required: true,
    includeIfNull: false,
  )


  final ObjectEnvelope node;



  @JsonKey(
    
    name: r'sharedBy',
    required: true,
    includeIfNull: false,
  )


  final List<String> sharedBy;



      /// Relation or dependency label (104 vocabulary)
  @JsonKey(
    
    name: r'weakestEdgeLabel',
    required: true,
    includeIfNull: false,
  )


  final CommonDependenciesDependenciesInnerWeakestEdgeLabelEnum weakestEdgeLabel;



  @JsonKey(
    
    name: r'paths',
    required: true,
    includeIfNull: false,
  )


  final List<CommonDependenciesDependenciesInnerPathsInner> paths;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CommonDependenciesDependenciesInner &&
      other.node == node &&
      other.sharedBy == sharedBy &&
      other.weakestEdgeLabel == weakestEdgeLabel &&
      other.paths == paths;

    @override
    int get hashCode =>
        node.hashCode +
        sharedBy.hashCode +
        weakestEdgeLabel.hashCode +
        paths.hashCode;

  factory CommonDependenciesDependenciesInner.fromJson(Map<String, dynamic> json) => _$CommonDependenciesDependenciesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$CommonDependenciesDependenciesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Relation or dependency label (104 vocabulary)
enum CommonDependenciesDependenciesInnerWeakestEdgeLabelEnum {
@JsonValue(r'confirmed')
confirmed(r'confirmed'),
@JsonValue(r'likely')
likely(r'likely'),
@JsonValue(r'possible')
possible(r'possible'),
@JsonValue(r'speculative')
speculative(r'speculative');

const CommonDependenciesDependenciesInnerWeakestEdgeLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


