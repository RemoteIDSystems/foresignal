//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_layer_selector_one_of3.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLayerSelectorOneOf3 {
  /// Returns a new [ChronicleLayerSelectorOneOf3] instance.
  ChronicleLayerSelectorOneOf3({

    required  this.type,

    required  this.entityIds,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ChronicleLayerSelectorOneOf3TypeEnum type;



  @JsonKey(
    
    name: r'entityIds',
    required: true,
    includeIfNull: false,
  )


  final List<String> entityIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLayerSelectorOneOf3 &&
      other.type == type &&
      other.entityIds == entityIds;

    @override
    int get hashCode =>
        type.hashCode +
        entityIds.hashCode;

  factory ChronicleLayerSelectorOneOf3.fromJson(Map<String, dynamic> json) => _$ChronicleLayerSelectorOneOf3FromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLayerSelectorOneOf3ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleLayerSelectorOneOf3TypeEnum {
@JsonValue(r'entities')
entities(r'entities');

const ChronicleLayerSelectorOneOf3TypeEnum(this.value);

final String value;

@override
String toString() => value;
}


