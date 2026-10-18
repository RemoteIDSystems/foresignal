//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_layer_selector_one_of8.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLayerSelectorOneOf8 {
  /// Returns a new [ChronicleLayerSelectorOneOf8] instance.
  ChronicleLayerSelectorOneOf8({

    required  this.type,

     this.ruleIds,

     this.tags,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ChronicleLayerSelectorOneOf8TypeEnum type;



  @JsonKey(
    
    name: r'ruleIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? ruleIds;



  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLayerSelectorOneOf8 &&
      other.type == type &&
      other.ruleIds == ruleIds &&
      other.tags == tags;

    @override
    int get hashCode =>
        type.hashCode +
        ruleIds.hashCode +
        tags.hashCode;

  factory ChronicleLayerSelectorOneOf8.fromJson(Map<String, dynamic> json) => _$ChronicleLayerSelectorOneOf8FromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLayerSelectorOneOf8ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleLayerSelectorOneOf8TypeEnum {
@JsonValue(r'alerts')
alerts(r'alerts');

const ChronicleLayerSelectorOneOf8TypeEnum(this.value);

final String value;

@override
String toString() => value;
}


