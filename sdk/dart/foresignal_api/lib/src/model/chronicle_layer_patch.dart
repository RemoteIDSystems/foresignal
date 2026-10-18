//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_layer_selector.dart';
import 'package:foresignal_api/src/model/chronicle_area.dart';
import 'package:foresignal_api/src/model/capture_mode.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_layer_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLayerPatch {
  /// Returns a new [ChronicleLayerPatch] instance.
  ChronicleLayerPatch({

     this.name,

     this.selector,

     this.areaOverride,

     this.capture,

     this.enabled,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'selector',
    required: false,
    includeIfNull: false,
  )


  final ChronicleLayerSelector? selector;



  @JsonKey(
    
    name: r'areaOverride',
    required: false,
    includeIfNull: false,
  )


  final ChronicleArea? areaOverride;



  @JsonKey(
    
    name: r'capture',
    required: false,
    includeIfNull: false,
  )


  final CaptureMode? capture;



  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLayerPatch &&
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

  factory ChronicleLayerPatch.fromJson(Map<String, dynamic> json) => _$ChronicleLayerPatchFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLayerPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

