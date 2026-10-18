//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/condition.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_layer_selector_one_of.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLayerSelectorOneOf {
  /// Returns a new [ChronicleLayerSelectorOneOf] instance.
  ChronicleLayerSelectorOneOf({

    required  this.type,

     this.sources,

     this.kinds,

     this.conditions,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ChronicleLayerSelectorOneOfTypeEnum type;



  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



      /// 017 condition grammar
  @JsonKey(
    
    name: r'conditions',
    required: false,
    includeIfNull: false,
  )


  final List<Condition>? conditions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLayerSelectorOneOf &&
      other.type == type &&
      other.sources == sources &&
      other.kinds == kinds &&
      other.conditions == conditions;

    @override
    int get hashCode =>
        type.hashCode +
        sources.hashCode +
        kinds.hashCode +
        conditions.hashCode;

  factory ChronicleLayerSelectorOneOf.fromJson(Map<String, dynamic> json) => _$ChronicleLayerSelectorOneOfFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLayerSelectorOneOfToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleLayerSelectorOneOfTypeEnum {
@JsonValue(r'records')
records(r'records');

const ChronicleLayerSelectorOneOfTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


