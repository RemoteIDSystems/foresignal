//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_preset_setup.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingPresetSetup {
  /// Returns a new [MappingPresetSetup] instance.
  MappingPresetSetup({

    required  this.instructions,

    required  this.urlTemplate,

     this.url,
  });

      /// Markdown setup steps for the device
  @JsonKey(
    
    name: r'instructions',
    required: true,
    includeIfNull: false,
  )


  final String instructions;



  @JsonKey(
    
    name: r'urlTemplate',
    required: true,
    includeIfNull: false,
  )


  final String urlTemplate;



      /// Concrete URL (creation response only; shown once)
  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingPresetSetup &&
      other.instructions == instructions &&
      other.urlTemplate == urlTemplate &&
      other.url == url;

    @override
    int get hashCode =>
        instructions.hashCode +
        urlTemplate.hashCode +
        (url == null ? 0 : url.hashCode);

  factory MappingPresetSetup.fromJson(Map<String, dynamic> json) => _$MappingPresetSetupFromJson(json);

  Map<String, dynamic> toJson() => _$MappingPresetSetupToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

