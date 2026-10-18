//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/relation.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'graph_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GraphResult {
  /// Returns a new [GraphResult] instance.
  GraphResult({

    required  this.nodes,

    required  this.edges,

     this.missing,

     this.truncated,

     this.asOf,
  });

  @JsonKey(
    
    name: r'nodes',
    required: true,
    includeIfNull: false,
  )


  final List<ObjectEnvelope> nodes;



  @JsonKey(
    
    name: r'edges',
    required: true,
    includeIfNull: false,
  )


  final List<Relation> edges;



  @JsonKey(
    
    name: r'missing',
    required: false,
    includeIfNull: false,
  )


  final List<String>? missing;



  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GraphResult &&
      other.nodes == nodes &&
      other.edges == edges &&
      other.missing == missing &&
      other.truncated == truncated &&
      other.asOf == asOf;

    @override
    int get hashCode =>
        nodes.hashCode +
        edges.hashCode +
        missing.hashCode +
        truncated.hashCode +
        (asOf == null ? 0 : asOf.hashCode);

  factory GraphResult.fromJson(Map<String, dynamic> json) => _$GraphResultFromJson(json);

  Map<String, dynamic> toJson() => _$GraphResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

