//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'registration_token_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RegistrationTokenInput {
  /// Returns a new [RegistrationTokenInput] instance.
  RegistrationTokenInput({

    required  this.name,

     this.expiresAt,

     this.maxUses = 1,

     this.sourceIdPrefix,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;



          // minimum: 1
  @JsonKey(
    defaultValue: 1,
    name: r'maxUses',
    required: false,
    includeIfNull: false,
  )


  final int? maxUses;



      /// Only manifests whose source.id starts with this prefix
  @JsonKey(
    
    name: r'sourceIdPrefix',
    required: false,
    includeIfNull: false,
  )


  final String? sourceIdPrefix;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RegistrationTokenInput &&
      other.name == name &&
      other.expiresAt == expiresAt &&
      other.maxUses == maxUses &&
      other.sourceIdPrefix == sourceIdPrefix;

    @override
    int get hashCode =>
        name.hashCode +
        (expiresAt == null ? 0 : expiresAt.hashCode) +
        maxUses.hashCode +
        (sourceIdPrefix == null ? 0 : sourceIdPrefix.hashCode);

  factory RegistrationTokenInput.fromJson(Map<String, dynamic> json) => _$RegistrationTokenInputFromJson(json);

  Map<String, dynamic> toJson() => _$RegistrationTokenInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

