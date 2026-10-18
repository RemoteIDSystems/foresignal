//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_layer_selector.dart';
import 'package:foresignal_api/src/model/chronicle_area.dart';
import 'package:foresignal_api/src/model/capture_mode.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_layer_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLayerInput {
  /// Returns a new [ChronicleLayerInput] instance.
  ChronicleLayerInput({

    required  this.name,

    required  this.selector,

     this.areaOverride,

    required  this.capture,

     this.enabled = true,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'selector',
    required: true,
    includeIfNull: false,
  )


  final ChronicleLayerSelector selector;



  @JsonKey(
    
    name: r'areaOverride',
    required: false,
    includeIfNull: false,
  )


  final ChronicleArea? areaOverride;



  @JsonKey(
    
    name: r'capture',
    required: true,
    includeIfNull: false,
  )


  final CaptureMode capture;



  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLayerInput &&
      other.name == name &&
      other.selector == selector &&
      other.areaOverride == areaOverride &&
      other.capture == capture &&
      other.enabled == enabled;

    @override
    int get hashCode =>
        name.hashCode +
        selector.hashCode +
        (areaOverride == null ? 0 : areaOverride.hashCode) +
        capture.hashCode +
        enabled.hashCode;

  factory ChronicleLayerInput.fromJson(Map<String, dynamic> json) => _$ChronicleLayerInputFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLayerInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

