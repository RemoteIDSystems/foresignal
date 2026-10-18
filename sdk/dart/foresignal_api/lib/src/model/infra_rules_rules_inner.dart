//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'infra_rules_rules_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InfraRulesRulesInner {
  /// Returns a new [InfraRulesRulesInner] instance.
  InfraRulesRulesInner({

    required  this.id,

    required  this.enabled,

     this.method,

     this.relation,

     this.label,

     this.params,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final InfraRulesRulesInnerIdEnum id;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'method',
    required: false,
    includeIfNull: false,
  )


  final InfraRulesRulesInnerMethodEnum? method;



  @JsonKey(
    
    name: r'relation',
    required: false,
    includeIfNull: false,
  )


  final InfraRulesRulesInnerRelationEnum? relation;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final InfraRulesRulesInnerLabelEnum? label;



      /// Overridable parameters (radii, distances)
  @JsonKey(
    
    name: r'params',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? params;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InfraRulesRulesInner &&
      other.id == id &&
      other.enabled == enabled &&
      other.method == method &&
      other.relation == relation &&
      other.label == label &&
      other.params == params;

    @override
    int get hashCode =>
        id.hashCode +
        enabled.hashCode +
        method.hashCode +
        relation.hashCode +
        label.hashCode +
        params.hashCode;

  factory InfraRulesRulesInner.fromJson(Map<String, dynamic> json) => _$InfraRulesRulesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$InfraRulesRulesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum InfraRulesRulesInnerIdEnum {
@JsonValue(r'cable-landing')
cableLanding(r'cable-landing'),
@JsonValue(r'ixp-facility')
ixpFacility(r'ixp-facility'),
@JsonValue(r'line-substation')
lineSubstation(r'line-substation'),
@JsonValue(r'plant-substation')
plantSubstation(r'plant-substation'),
@JsonValue(r'substation-critical-load')
substationCriticalLoad(r'substation-critical-load'),
@JsonValue(r'airport-port-serves')
airportPortServes(r'airport-port-serves');

const InfraRulesRulesInnerIdEnum(this.value);

final String value;

@override
String toString() => value;
}


enum InfraRulesRulesInnerMethodEnum {
@JsonValue(r'declared')
declared(r'declared'),
@JsonValue(r'topological')
topological(r'topological'),
@JsonValue(r'proximity')
proximity(r'proximity'),
@JsonValue(r'inferred')
inferred(r'inferred');

const InfraRulesRulesInnerMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


enum InfraRulesRulesInnerRelationEnum {
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

const InfraRulesRulesInnerRelationEnum(this.value);

final String value;

@override
String toString() => value;
}


enum InfraRulesRulesInnerLabelEnum {
@JsonValue(r'confirmed')
confirmed(r'confirmed'),
@JsonValue(r'likely')
likely(r'likely'),
@JsonValue(r'possible')
possible(r'possible'),
@JsonValue(r'speculative')
speculative(r'speculative');

const InfraRulesRulesInnerLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


