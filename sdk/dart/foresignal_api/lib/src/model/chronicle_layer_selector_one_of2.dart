//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_layer_selector_one_of2.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLayerSelectorOneOf2 {
  /// Returns a new [ChronicleLayerSelectorOneOf2] instance.
  ChronicleLayerSelectorOneOf2({

    required  this.type,

    required  this.watchlistId,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ChronicleLayerSelectorOneOf2TypeEnum type;



  @JsonKey(
    
    name: r'watchlistId',
    required: true,
    includeIfNull: false,
  )


  final String watchlistId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLayerSelectorOneOf2 &&
      other.type == type &&
      other.watchlistId == watchlistId;

    @override
    int get hashCode =>
        type.hashCode +
        watchlistId.hashCode;

  factory ChronicleLayerSelectorOneOf2.fromJson(Map<String, dynamic> json) => _$ChronicleLayerSelectorOneOf2FromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLayerSelectorOneOf2ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleLayerSelectorOneOf2TypeEnum {
@JsonValue(r'watchlist')
watchlist(r'watchlist');

const ChronicleLayerSelectorOneOf2TypeEnum(this.value);

final String value;

@override
String toString() => value;
}


