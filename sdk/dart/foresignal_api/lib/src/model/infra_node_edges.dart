//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/infra_edge.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'infra_node_edges.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InfraNodeEdges {
  /// Returns a new [InfraNodeEdges] instance.
  InfraNodeEdges({

     this.in_,

     this.out_,
  });

  @JsonKey(
    
    name: r'in',
    required: false,
    includeIfNull: false,
  )


  final List<InfraEdge>? in_;



  @JsonKey(
    
    name: r'out',
    required: false,
    includeIfNull: false,
  )


  final List<InfraEdge>? out_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InfraNodeEdges &&
      other.in_ == in_ &&
      other.out_ == out_;

    @override
    int get hashCode =>
        in_.hashCode +
        out_.hashCode;

  factory InfraNodeEdges.fromJson(Map<String, dynamic> json) => _$InfraNodeEdgesFromJson(json);

  Map<String, dynamic> toJson() => _$InfraNodeEdgesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

