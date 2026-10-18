//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/base_rate_definition.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'base_rate_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BaseRateInput {
  /// Returns a new [BaseRateInput] instance.
  BaseRateInput({

     this.name,

    required  this.definition,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'definition',
    required: true,
    includeIfNull: false,
  )


  final BaseRateDefinition definition;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BaseRateInput &&
      other.name == name &&
      other.definition == definition;

    @override
    int get hashCode =>
        (name == null ? 0 : name.hashCode) +
        definition.hashCode;

  factory BaseRateInput.fromJson(Map<String, dynamic> json) => _$BaseRateInputFromJson(json);

  Map<String, dynamic> toJson() => _$BaseRateInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

