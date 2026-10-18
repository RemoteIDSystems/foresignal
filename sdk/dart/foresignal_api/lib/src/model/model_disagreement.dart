//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'model_disagreement.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ModelDisagreement {
  /// Returns a new [ModelDisagreement] instance.
  ModelDisagreement({

    required  this.fields,

    required  this.spread,

    required  this.models,
  });

  @JsonKey(
    
    name: r'fields',
    required: true,
    includeIfNull: false,
  )


  final List<ModelDisagreementFieldsEnum> fields;



  @JsonKey(
    
    name: r'spread',
    required: true,
    includeIfNull: false,
  )


  final Map<String, num> spread;



  @JsonKey(
    
    name: r'models',
    required: true,
    includeIfNull: false,
  )


  final List<String> models;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ModelDisagreement &&
      other.fields == fields &&
      other.spread == spread &&
      other.models == models;

    @override
    int get hashCode =>
        fields.hashCode +
        spread.hashCode +
        models.hashCode;

  factory ModelDisagreement.fromJson(Map<String, dynamic> json) => _$ModelDisagreementFromJson(json);

  Map<String, dynamic> toJson() => _$ModelDisagreementToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ModelDisagreementFieldsEnum {
@JsonValue(r'gust')
gust(r'gust'),
@JsonValue(r'precip24h')
precip24h(r'precip24h'),
@JsonValue(r't2mMax')
t2mMax(r't2mMax');

const ModelDisagreementFieldsEnum(this.value);

final String value;

@override
String toString() => value;
}


