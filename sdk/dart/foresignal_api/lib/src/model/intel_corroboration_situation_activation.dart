//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_corroboration_situation_activation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelCorroborationSituationActivation {
  /// Returns a new [IntelCorroborationSituationActivation] instance.
  IntelCorroborationSituationActivation({

     this.minFamilies,

     this.minGroups,
  });

      /// Distinct signal families activation needs (default 2)
          // minimum: 1
  @JsonKey(
    
    name: r'minFamilies',
    required: false,
    includeIfNull: false,
  )


  final int? minFamilies;



      /// Independent evidence groups activation needs (default 2)
          // minimum: 1
  @JsonKey(
    
    name: r'minGroups',
    required: false,
    includeIfNull: false,
  )


  final int? minGroups;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelCorroborationSituationActivation &&
      other.minFamilies == minFamilies &&
      other.minGroups == minGroups;

    @override
    int get hashCode =>
        minFamilies.hashCode +
        minGroups.hashCode;

  factory IntelCorroborationSituationActivation.fromJson(Map<String, dynamic> json) => _$IntelCorroborationSituationActivationFromJson(json);

  Map<String, dynamic> toJson() => _$IntelCorroborationSituationActivationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

