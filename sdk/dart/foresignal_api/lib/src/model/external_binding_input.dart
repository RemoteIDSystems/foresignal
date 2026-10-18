//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'external_binding_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExternalBindingInput {
  /// Returns a new [ExternalBindingInput] instance.
  ExternalBindingInput({

    required  this.streamId,

    required  this.target,

     this.config,

     this.filter,

     this.enabled = true,

     this.backfill = true,
  });

  @JsonKey(
    
    name: r'streamId',
    required: true,
    includeIfNull: false,
  )


  final String streamId;



  @JsonKey(
    
    name: r'target',
    required: true,
    includeIfNull: false,
  )


  final ExternalBindingInputTargetEnum target;



      /// Target configuration (docs/External-Data-Protocol.md §5)
  @JsonKey(
    
    name: r'config',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? config;



      /// 017 conditions on item properties
  @JsonKey(
    
    name: r'filter',
    required: false,
    includeIfNull: false,
  )


  final List<Map<String, Object>>? filter;



  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



  @JsonKey(
    defaultValue: true,
    name: r'backfill',
    required: false,
    includeIfNull: false,
  )


  final bool? backfill;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExternalBindingInput &&
      other.streamId == streamId &&
      other.target == target &&
      other.config == config &&
      other.filter == filter &&
      other.enabled == enabled &&
      other.backfill == backfill;

    @override
    int get hashCode =>
        streamId.hashCode +
        target.hashCode +
        config.hashCode +
        (filter == null ? 0 : filter.hashCode) +
        enabled.hashCode +
        backfill.hashCode;

  factory ExternalBindingInput.fromJson(Map<String, dynamic> json) => _$ExternalBindingInputFromJson(json);

  Map<String, dynamic> toJson() => _$ExternalBindingInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExternalBindingInputTargetEnum {
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

const ExternalBindingInputTargetEnum(this.value);

final String value;

@override
String toString() => value;
}


