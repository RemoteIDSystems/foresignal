//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'external_forecaster_registration.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExternalForecasterRegistration {
  /// Returns a new [ExternalForecasterRegistration] instance.
  ExternalForecasterRegistration({

    required  this.id,

     this.name,

     this.config,
  });

      /// Forecaster id, conventionally edp:{sourceId}/{streamId}.
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// Optional display name (defaults to the id).
  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



      /// Optional configuration; `sourceId` and `streamId` pin the provider for backtests, and `externalMinResolved` (integer) overrides the ensemble's global minimum resolved count.
  @JsonKey(
    
    name: r'config',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? config;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExternalForecasterRegistration &&
      other.id == id &&
      other.name == name &&
      other.config == config;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        config.hashCode;

  factory ExternalForecasterRegistration.fromJson(Map<String, dynamic> json) => _$ExternalForecasterRegistrationFromJson(json);

  Map<String, dynamic> toJson() => _$ExternalForecasterRegistrationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

