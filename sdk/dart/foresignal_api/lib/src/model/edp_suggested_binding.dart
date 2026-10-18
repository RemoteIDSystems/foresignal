//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_suggested_binding.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpSuggestedBinding {
  /// Returns a new [EdpSuggestedBinding] instance.
  EdpSuggestedBinding({

    required  this.target,

     this.config,
  });

  @JsonKey(
    
    name: r'target',
    required: true,
    includeIfNull: false,
  )


  final EdpSuggestedBindingTargetEnum target;



  @JsonKey(
    
    name: r'config',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? config;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpSuggestedBinding &&
      other.target == target &&
      other.config == config;

    @override
    int get hashCode =>
        target.hashCode +
        config.hashCode;

  factory EdpSuggestedBinding.fromJson(Map<String, dynamic> json) => _$EdpSuggestedBindingFromJson(json);

  Map<String, dynamic> toJson() => _$EdpSuggestedBindingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpSuggestedBindingTargetEnum {
@JsonValue(r'series')
series(r'series'),
@JsonValue(r'canonical-family')
canonicalFamily(r'canonical-family'),
@JsonValue(r'social')
social(r'social'),
@JsonValue(r'news')
news(r'news'),
@JsonValue(r'evidence')
evidence(r'evidence'),
@JsonValue(r'indicator')
indicator(r'indicator'),
@JsonValue(r'forecast')
forecast(r'forecast'),
@JsonValue(r'place')
place(r'place'),
@JsonValue(r'situation')
situation(r'situation'),
@JsonValue(r'chronicle')
chronicle(r'chronicle');

const EdpSuggestedBindingTargetEnum(this.value);

final String value;

@override
String toString() => value;
}


