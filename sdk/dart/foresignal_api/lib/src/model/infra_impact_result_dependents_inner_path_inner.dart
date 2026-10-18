//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'infra_impact_result_dependents_inner_path_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InfraImpactResultDependentsInnerPathInner {
  /// Returns a new [InfraImpactResultDependentsInnerPathInner] instance.
  InfraImpactResultDependentsInnerPathInner({

    required  this.edgeId,

    required  this.relation,

    required  this.method,

    required  this.label,
  });

  @JsonKey(
    
    name: r'edgeId',
    required: true,
    includeIfNull: false,
  )


  final int edgeId;



  @JsonKey(
    
    name: r'relation',
    required: true,
    includeIfNull: false,
  )


  final InfraImpactResultDependentsInnerPathInnerRelationEnum relation;



  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final InfraImpactResultDependentsInnerPathInnerMethodEnum method;



  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final InfraImpactResultDependentsInnerPathInnerLabelEnum label;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InfraImpactResultDependentsInnerPathInner &&
      other.edgeId == edgeId &&
      other.relation == relation &&
      other.method == method &&
      other.label == label;

    @override
    int get hashCode =>
        edgeId.hashCode +
        relation.hashCode +
        method.hashCode +
        label.hashCode;

  factory InfraImpactResultDependentsInnerPathInner.fromJson(Map<String, dynamic> json) => _$InfraImpactResultDependentsInnerPathInnerFromJson(json);

  Map<String, dynamic> toJson() => _$InfraImpactResultDependentsInnerPathInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum InfraImpactResultDependentsInnerPathInnerRelationEnum {
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

const InfraImpactResultDependentsInnerPathInnerRelationEnum(this.value);

final String value;

@override
String toString() => value;
}


enum InfraImpactResultDependentsInnerPathInnerMethodEnum {
@JsonValue(r'declared')
declared(r'declared'),
@JsonValue(r'topological')
topological(r'topological'),
@JsonValue(r'proximity')
proximity(r'proximity'),
@JsonValue(r'inferred')
inferred(r'inferred');

const InfraImpactResultDependentsInnerPathInnerMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


enum InfraImpactResultDependentsInnerPathInnerLabelEnum {
@JsonValue(r'confirmed')
confirmed(r'confirmed'),
@JsonValue(r'likely')
likely(r'likely'),
@JsonValue(r'possible')
possible(r'possible'),
@JsonValue(r'speculative')
speculative(r'speculative');

const InfraImpactResultDependentsInnerPathInnerLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


