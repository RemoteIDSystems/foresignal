//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_cap_override.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisCapOverride {
  /// Returns a new [HypothesisCapOverride] instance.
  HypothesisCapOverride({

    required  this.by,

    required  this.at,

    required  this.rationale,
  });

  @JsonKey(
    
    name: r'by',
    required: true,
    includeIfNull: false,
  )


  final String by;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'rationale',
    required: true,
    includeIfNull: false,
  )


  final String rationale;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisCapOverride &&
      other.by == by &&
      other.at == at &&
      other.rationale == rationale;

    @override
    int get hashCode =>
        by.hashCode +
        at.hashCode +
        rationale.hashCode;

  factory HypothesisCapOverride.fromJson(Map<String, dynamic> json) => _$HypothesisCapOverrideFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisCapOverrideToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

