//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'credential_field.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CredentialField {
  /// Returns a new [CredentialField] instance.
  CredentialField({

    required  this.name,

     this.description,

    required  this.required_,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'required',
    required: true,
    includeIfNull: false,
  )


  final bool required_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CredentialField &&
      other.name == name &&
      other.description == description &&
      other.required_ == required_;

    @override
    int get hashCode =>
        name.hashCode +
        description.hashCode +
        required_.hashCode;

  factory CredentialField.fromJson(Map<String, dynamic> json) => _$CredentialFieldFromJson(json);

  Map<String, dynamic> toJson() => _$CredentialFieldToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

