//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'contract.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Contract {
  /// Returns a new [Contract] instance.
  Contract({

    required  this.contractVersion,

    required  this.objectTypes,

    required  this.relationTypes,

    required  this.contextSchemaVersion,

    required  this.deepLinkVersion,
  });

  @JsonKey(
    
    name: r'contractVersion',
    required: true,
    includeIfNull: false,
  )


  final String contractVersion;



  @JsonKey(
    
    name: r'objectTypes',
    required: true,
    includeIfNull: false,
  )


  final List<String> objectTypes;



  @JsonKey(
    
    name: r'relationTypes',
    required: true,
    includeIfNull: false,
  )


  final List<String> relationTypes;



  @JsonKey(
    
    name: r'contextSchemaVersion',
    required: true,
    includeIfNull: false,
  )


  final int contextSchemaVersion;



  @JsonKey(
    
    name: r'deepLinkVersion',
    required: true,
    includeIfNull: false,
  )


  final int deepLinkVersion;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Contract &&
      other.contractVersion == contractVersion &&
      other.objectTypes == objectTypes &&
      other.relationTypes == relationTypes &&
      other.contextSchemaVersion == contextSchemaVersion &&
      other.deepLinkVersion == deepLinkVersion;

    @override
    int get hashCode =>
        contractVersion.hashCode +
        objectTypes.hashCode +
        relationTypes.hashCode +
        contextSchemaVersion.hashCode +
        deepLinkVersion.hashCode;

  factory Contract.fromJson(Map<String, dynamic> json) => _$ContractFromJson(json);

  Map<String, dynamic> toJson() => _$ContractToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

