//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_input_prior_basis.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisInputPriorBasis {
  /// Returns a new [HypothesisInputPriorBasis] instance.
  HypothesisInputPriorBasis({

    required  this.type,

     this.ref,

     this.rationale,

     this.value,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final HypothesisInputPriorBasisTypeEnum type;



      /// 121 base-rate id, analog set reference, or null for an operator judgment
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



  @JsonKey(
    
    name: r'rationale',
    required: false,
    includeIfNull: false,
  )


  final String? rationale;



      /// For a base-rate basis: the rate's pAtLeastOne when the prior was set (filled by the server); a later change of more than 20% relative records a base-rate-refresh revision
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'value',
    required: false,
    includeIfNull: false,
  )


  final num? value;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisInputPriorBasis &&
      other.type == type &&
      other.ref == ref &&
      other.rationale == rationale &&
      other.value == value;

    @override
    int get hashCode =>
        type.hashCode +
        (ref == null ? 0 : ref.hashCode) +
        (rationale == null ? 0 : rationale.hashCode) +
        (value == null ? 0 : value.hashCode);

  factory HypothesisInputPriorBasis.fromJson(Map<String, dynamic> json) => _$HypothesisInputPriorBasisFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisInputPriorBasisToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum HypothesisInputPriorBasisTypeEnum {
@JsonValue(r'base-rate')
baseRate(r'base-rate'),
@JsonValue(r'analogs')
analogs(r'analogs'),
@JsonValue(r'operator')
operator_(r'operator');

const HypothesisInputPriorBasisTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


