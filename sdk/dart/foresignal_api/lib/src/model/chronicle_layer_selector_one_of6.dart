//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_layer_selector_one_of6.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLayerSelectorOneOf6 {
  /// Returns a new [ChronicleLayerSelectorOneOf6] instance.
  ChronicleLayerSelectorOneOf6({

    required  this.type,

    required  this.refs,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ChronicleLayerSelectorOneOf6TypeEnum type;



  @JsonKey(
    
    name: r'refs',
    required: true,
    includeIfNull: false,
  )


  final List<String> refs;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLayerSelectorOneOf6 &&
      other.type == type &&
      other.refs == refs;

    @override
    int get hashCode =>
        type.hashCode +
        refs.hashCode;

  factory ChronicleLayerSelectorOneOf6.fromJson(Map<String, dynamic> json) => _$ChronicleLayerSelectorOneOf6FromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLayerSelectorOneOf6ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleLayerSelectorOneOf6TypeEnum {
@JsonValue(r'series')
series(r'series');

const ChronicleLayerSelectorOneOf6TypeEnum(this.value);

final String value;

@override
String toString() => value;
}


