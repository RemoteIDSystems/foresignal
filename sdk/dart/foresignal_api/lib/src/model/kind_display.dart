//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'kind_display.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class KindDisplay {
  /// Returns a new [KindDisplay] instance.
  KindDisplay({

     this.titleTemplate,

     this.icon,

     this.colour,

     this.sidc,

     this.rotateField,

     this.sizeField,

     this.colorField,
  });

      /// Client title template with `{field}` placeholders
  @JsonKey(
    
    name: r'titleTemplate',
    required: false,
    includeIfNull: false,
  )


  final String? titleTemplate;



      /// Icon name
  @JsonKey(
    
    name: r'icon',
    required: false,
    includeIfNull: false,
  )


  final String? icon;



      /// Colour `#RRGGBB`
  @JsonKey(
    
    name: r'colour',
    required: false,
    includeIfNull: false,
  )


  final String? colour;



      /// Optional MIL-STD-2525 symbol identification code (15 or 20 characters)
  @JsonKey(
    
    name: r'sidc',
    required: false,
    includeIfNull: false,
  )


  final String? sidc;



      /// Payload field rotating the icon (e.g. `track`); must be in tileProperties
  @JsonKey(
    
    name: r'rotateField',
    required: false,
    includeIfNull: false,
  )


  final String? rotateField;



      /// Payload field scaling circle radius (e.g. `frp`, `mag`); must be in tileProperties
  @JsonKey(
    
    name: r'sizeField',
    required: false,
    includeIfNull: false,
  )


  final String? sizeField;



      /// Payload field driving a data-driven colour match (default `severity`); must be in tileProperties
  @JsonKey(
    
    name: r'colorField',
    required: false,
    includeIfNull: false,
  )


  final String? colorField;





    @override
    bool operator ==(Object other) => identical(this, other) || other is KindDisplay &&
      other.titleTemplate == titleTemplate &&
      other.icon == icon &&
      other.colour == colour &&
      other.sidc == sidc &&
      other.rotateField == rotateField &&
      other.sizeField == sizeField &&
      other.colorField == colorField;

    @override
    int get hashCode =>
        (titleTemplate == null ? 0 : titleTemplate.hashCode) +
        (icon == null ? 0 : icon.hashCode) +
        (colour == null ? 0 : colour.hashCode) +
        (sidc == null ? 0 : sidc.hashCode) +
        (rotateField == null ? 0 : rotateField.hashCode) +
        (sizeField == null ? 0 : sizeField.hashCode) +
        (colorField == null ? 0 : colorField.hashCode);

  factory KindDisplay.fromJson(Map<String, dynamic> json) => _$KindDisplayFromJson(json);

  Map<String, dynamic> toJson() => _$KindDisplayToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

