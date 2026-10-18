//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_layer_selector_one_of4.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLayerSelectorOneOf4 {
  /// Returns a new [ChronicleLayerSelectorOneOf4] instance.
  ChronicleLayerSelectorOneOf4({

    required  this.type,

    required  this.situationIds,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ChronicleLayerSelectorOneOf4TypeEnum type;



  @JsonKey(
    
    name: r'situationIds',
    required: true,
    includeIfNull: false,
  )


  final List<String> situationIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLayerSelectorOneOf4 &&
      other.type == type &&
      other.situationIds == situationIds;

    @override
    int get hashCode =>
        type.hashCode +
        situationIds.hashCode;

  factory ChronicleLayerSelectorOneOf4.fromJson(Map<String, dynamic> json) => _$ChronicleLayerSelectorOneOf4FromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLayerSelectorOneOf4ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleLayerSelectorOneOf4TypeEnum {
@JsonValue(r'situations')
situations(r'situations');

const ChronicleLayerSelectorOneOf4TypeEnum(this.value);

final String value;

@override
String toString() => value;
}


