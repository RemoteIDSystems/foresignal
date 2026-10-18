//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_layer_selector_one_of1.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLayerSelectorOneOf1 {
  /// Returns a new [ChronicleLayerSelectorOneOf1] instance.
  ChronicleLayerSelectorOneOf1({

    required  this.type,

    required  this.viewId,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ChronicleLayerSelectorOneOf1TypeEnum type;



  @JsonKey(
    
    name: r'viewId',
    required: true,
    includeIfNull: false,
  )


  final String viewId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLayerSelectorOneOf1 &&
      other.type == type &&
      other.viewId == viewId;

    @override
    int get hashCode =>
        type.hashCode +
        viewId.hashCode;

  factory ChronicleLayerSelectorOneOf1.fromJson(Map<String, dynamic> json) => _$ChronicleLayerSelectorOneOf1FromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLayerSelectorOneOf1ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleLayerSelectorOneOf1TypeEnum {
@JsonValue(r'saved-view')
savedView(r'saved-view');

const ChronicleLayerSelectorOneOf1TypeEnum(this.value);

final String value;

@override
String toString() => value;
}


