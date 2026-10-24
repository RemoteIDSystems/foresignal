//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_corroboration_event_quality.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelCorroborationEventQuality {
  /// Returns a new [IntelCorroborationEventQuality] instance.
  IntelCorroborationEventQuality({

     this.strongFamilies,

     this.strongGroups,

     this.moderateFamilies,

     this.requireModalities,
  });

      /// Distinct signal families a strong event needs (default 3)
          // minimum: 1
  @JsonKey(
    
    name: r'strongFamilies',
    required: false,
    includeIfNull: false,
  )


  final int? strongFamilies;



      /// Independent evidence groups a strong event needs (default 2)
          // minimum: 1
  @JsonKey(
    
    name: r'strongGroups',
    required: false,
    includeIfNull: false,
  )


  final int? strongGroups;



      /// Distinct signal families a moderate event needs (default 2)
          // minimum: 1
  @JsonKey(
    
    name: r'moderateFamilies',
    required: false,
    includeIfNull: false,
  )


  final int? moderateFamilies;



      /// Distinct sensing modalities a strong event also needs, or 0 to not require any (default 0). A modality is sensor, imagery, official, social, media, crowd, operator or derived, derived from the originator class.
          // minimum: 0
  @JsonKey(
    
    name: r'requireModalities',
    required: false,
    includeIfNull: false,
  )


  final int? requireModalities;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelCorroborationEventQuality &&
      other.strongFamilies == strongFamilies &&
      other.strongGroups == strongGroups &&
      other.moderateFamilies == moderateFamilies &&
      other.requireModalities == requireModalities;

    @override
    int get hashCode =>
        strongFamilies.hashCode +
        strongGroups.hashCode +
        moderateFamilies.hashCode +
        requireModalities.hashCode;

  factory IntelCorroborationEventQuality.fromJson(Map<String, dynamic> json) => _$IntelCorroborationEventQualityFromJson(json);

  Map<String, dynamic> toJson() => _$IntelCorroborationEventQualityToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

