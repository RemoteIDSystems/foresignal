//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/infra_edge_evidence.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'infra_edge.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InfraEdge {
  /// Returns a new [InfraEdge] instance.
  InfraEdge({

    required  this.id,

    required  this.fromNode,

    required  this.toNode,

    required  this.relation,

    required  this.method,

    required  this.label,

    required  this.score,

    required  this.ruleId,

    required  this.ruleVersion,

     this.params,

     this.evidence,

    required  this.licence,

     this.builtAt,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'fromNode',
    required: true,
    includeIfNull: false,
  )


  final int fromNode;



  @JsonKey(
    
    name: r'toNode',
    required: true,
    includeIfNull: false,
  )


  final int toNode;



  @JsonKey(
    
    name: r'relation',
    required: true,
    includeIfNull: false,
  )


  final InfraEdgeRelationEnum relation;



  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final InfraEdgeMethodEnum method;



  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final InfraEdgeLabelEnum label;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'score',
    required: true,
    includeIfNull: false,
  )


  final num score;



  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



  @JsonKey(
    
    name: r'ruleVersion',
    required: true,
    includeIfNull: false,
  )


  final String ruleVersion;



      /// Rule parameters used
  @JsonKey(
    
    name: r'params',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? params;



  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final InfraEdgeEvidence? evidence;



      /// Most restrictive input licence (ODbL-1.0 for OSM-derived edges)
  @JsonKey(
    
    name: r'licence',
    required: true,
    includeIfNull: false,
  )


  final String licence;



  @JsonKey(
    
    name: r'builtAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? builtAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InfraEdge &&
      other.id == id &&
      other.fromNode == fromNode &&
      other.toNode == toNode &&
      other.relation == relation &&
      other.method == method &&
      other.label == label &&
      other.score == score &&
      other.ruleId == ruleId &&
      other.ruleVersion == ruleVersion &&
      other.params == params &&
      other.evidence == evidence &&
      other.licence == licence &&
      other.builtAt == builtAt;

    @override
    int get hashCode =>
        id.hashCode +
        fromNode.hashCode +
        toNode.hashCode +
        relation.hashCode +
        method.hashCode +
        label.hashCode +
        score.hashCode +
        ruleId.hashCode +
        ruleVersion.hashCode +
        params.hashCode +
        evidence.hashCode +
        licence.hashCode +
        builtAt.hashCode;

  factory InfraEdge.fromJson(Map<String, dynamic> json) => _$InfraEdgeFromJson(json);

  Map<String, dynamic> toJson() => _$InfraEdgeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum InfraEdgeRelationEnum {
@JsonValue(r'supplies_power')
suppliesPower(r'supplies_power'),
@JsonValue(r'transmits_power')
transmitsPower(r'transmits_power'),
@JsonValue(r'lands_at')
landsAt(r'lands_at'),
@JsonValue(r'interconnects')
interconnects(r'interconnects'),
@JsonValue(r'hosts')
hosts(r'hosts'),
@JsonValue(r'serves')
serves(r'serves');

const InfraEdgeRelationEnum(this.value);

final String value;

@override
String toString() => value;
}


enum InfraEdgeMethodEnum {
@JsonValue(r'declared')
declared(r'declared'),
@JsonValue(r'topological')
topological(r'topological'),
@JsonValue(r'proximity')
proximity(r'proximity'),
@JsonValue(r'inferred')
inferred(r'inferred');

const InfraEdgeMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


enum InfraEdgeLabelEnum {
@JsonValue(r'confirmed')
confirmed(r'confirmed'),
@JsonValue(r'likely')
likely(r'likely'),
@JsonValue(r'possible')
possible(r'possible'),
@JsonValue(r'speculative')
speculative(r'speculative');

const InfraEdgeLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


