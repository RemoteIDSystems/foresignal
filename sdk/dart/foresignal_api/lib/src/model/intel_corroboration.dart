//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/intel_corroboration_event_quality.dart';
import 'package:foresignal_api/src/model/intel_corroboration_auto_corroborate.dart';
import 'package:foresignal_api/src/model/intel_corroboration_situation_activation.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_corroboration.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelCorroboration {
  /// Returns a new [IntelCorroboration] instance.
  IntelCorroboration({

     this.eventQuality,

     this.situationActivation,

     this.autoCorroborate,
  });

  @JsonKey(
    
    name: r'eventQuality',
    required: false,
    includeIfNull: false,
  )


  final IntelCorroborationEventQuality? eventQuality;



  @JsonKey(
    
    name: r'situationActivation',
    required: false,
    includeIfNull: false,
  )


  final IntelCorroborationSituationActivation? situationActivation;



  @JsonKey(
    
    name: r'autoCorroborate',
    required: false,
    includeIfNull: false,
  )


  final IntelCorroborationAutoCorroborate? autoCorroborate;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelCorroboration &&
      other.eventQuality == eventQuality &&
      other.situationActivation == situationActivation &&
      other.autoCorroborate == autoCorroborate;

    @override
    int get hashCode =>
        eventQuality.hashCode +
        situationActivation.hashCode +
        autoCorroborate.hashCode;

  factory IntelCorroboration.fromJson(Map<String, dynamic> json) => _$IntelCorroborationFromJson(json);

  Map<String, dynamic> toJson() => _$IntelCorroborationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

