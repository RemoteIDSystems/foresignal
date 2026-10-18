//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_layer_selector_one_of5.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLayerSelectorOneOf5 {
  /// Returns a new [ChronicleLayerSelectorOneOf5] instance.
  ChronicleLayerSelectorOneOf5({

    required  this.type,

     this.findingTypes,

     this.families,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ChronicleLayerSelectorOneOf5TypeEnum type;



  @JsonKey(
    
    name: r'findingTypes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? findingTypes;



  @JsonKey(
    
    name: r'families',
    required: false,
    includeIfNull: false,
  )


  final List<String>? families;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLayerSelectorOneOf5 &&
      other.type == type &&
      other.findingTypes == findingTypes &&
      other.families == families;

    @override
    int get hashCode =>
        type.hashCode +
        findingTypes.hashCode +
        families.hashCode;

  factory ChronicleLayerSelectorOneOf5.fromJson(Map<String, dynamic> json) => _$ChronicleLayerSelectorOneOf5FromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLayerSelectorOneOf5ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleLayerSelectorOneOf5TypeEnum {
@JsonValue(r'findings')
findings(r'findings');

const ChronicleLayerSelectorOneOf5TypeEnum(this.value);

final String value;

@override
String toString() => value;
}


