//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_settings_cues_triggers.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialSettingsCuesTriggers {
  /// Returns a new [SocialSettingsCuesTriggers] instance.
  SocialSettingsCuesTriggers({

     this.situationActivated = true,

     this.situationEscalated = true,

     this.findingTypes,

     this.anomalies = true,

     this.criticalAlerts = true,

     this.canonicalFamilies,

     this.minEvidenceGroups = 2,
  });

  @JsonKey(
    defaultValue: true,
    name: r'situationActivated',
    required: false,
    includeIfNull: false,
  )


  final bool? situationActivated;



  @JsonKey(
    defaultValue: true,
    name: r'situationEscalated',
    required: false,
    includeIfNull: false,
  )


  final bool? situationEscalated;



      /// `significant` finding types, e.g. change points in outage, vessel, flight and fire series
  @JsonKey(
    
    name: r'findingTypes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? findingTypes;



  @JsonKey(
    defaultValue: true,
    name: r'anomalies',
    required: false,
    includeIfNull: false,
  )


  final bool? anomalies;



  @JsonKey(
    defaultValue: true,
    name: r'criticalAlerts',
    required: false,
    includeIfNull: false,
  )


  final bool? criticalAlerts;



      /// Canonical-event families with ≥ minEvidenceGroups
  @JsonKey(
    
    name: r'canonicalFamilies',
    required: false,
    includeIfNull: false,
  )


  final List<String>? canonicalFamilies;



          // minimum: 1
  @JsonKey(
    defaultValue: 2,
    name: r'minEvidenceGroups',
    required: false,
    includeIfNull: false,
  )


  final int? minEvidenceGroups;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialSettingsCuesTriggers &&
      other.situationActivated == situationActivated &&
      other.situationEscalated == situationEscalated &&
      other.findingTypes == findingTypes &&
      other.anomalies == anomalies &&
      other.criticalAlerts == criticalAlerts &&
      other.canonicalFamilies == canonicalFamilies &&
      other.minEvidenceGroups == minEvidenceGroups;

    @override
    int get hashCode =>
        situationActivated.hashCode +
        situationEscalated.hashCode +
        findingTypes.hashCode +
        anomalies.hashCode +
        criticalAlerts.hashCode +
        canonicalFamilies.hashCode +
        minEvidenceGroups.hashCode;

  factory SocialSettingsCuesTriggers.fromJson(Map<String, dynamic> json) => _$SocialSettingsCuesTriggersFromJson(json);

  Map<String, dynamic> toJson() => _$SocialSettingsCuesTriggersToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

