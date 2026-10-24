//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecaster_update.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecasterUpdate {
  /// Returns a new [ForecasterUpdate] instance.
  ForecasterUpdate({

    required  this.active,

     this.config,

     this.name,
  });

  @JsonKey(
    
    name: r'active',
    required: true,
    includeIfNull: false,
  )


  final bool active;



      /// Replaces the forecaster's configuration. For an external forecaster, `externalMinResolved` (integer) overrides the ensemble's global minimum resolved count before the forecaster is weighted (155).
  @JsonKey(
    
    name: r'config',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? config;



      /// Optional display name; set for a first-class external forecaster (155).
  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecasterUpdate &&
      other.active == active &&
      other.config == config &&
      other.name == name;

    @override
    int get hashCode =>
        active.hashCode +
        config.hashCode +
        name.hashCode;

  factory ForecasterUpdate.fromJson(Map<String, dynamic> json) => _$ForecasterUpdateFromJson(json);

  Map<String, dynamic> toJson() => _$ForecasterUpdateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

