//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_presentation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpPresentation {
  /// Returns a new [EdpPresentation] instance.
  EdpPresentation({

     this.color,

     this.icon,

     this.labelField,

     this.minZoom,

     this.maxZoom,

     this.tileProperties,
  });

  @JsonKey(
    
    name: r'color',
    required: false,
    includeIfNull: false,
  )


  final String? color;



  @JsonKey(
    
    name: r'icon',
    required: false,
    includeIfNull: false,
  )


  final String? icon;



  @JsonKey(
    
    name: r'labelField',
    required: false,
    includeIfNull: false,
  )


  final String? labelField;



  @JsonKey(
    
    name: r'minZoom',
    required: false,
    includeIfNull: false,
  )


  final int? minZoom;



  @JsonKey(
    
    name: r'maxZoom',
    required: false,
    includeIfNull: false,
  )


  final int? maxZoom;



  @JsonKey(
    
    name: r'tileProperties',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tileProperties;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpPresentation &&
      other.color == color &&
      other.icon == icon &&
      other.labelField == labelField &&
      other.minZoom == minZoom &&
      other.maxZoom == maxZoom &&
      other.tileProperties == tileProperties;

    @override
    int get hashCode =>
        (color == null ? 0 : color.hashCode) +
        (icon == null ? 0 : icon.hashCode) +
        (labelField == null ? 0 : labelField.hashCode) +
        (minZoom == null ? 0 : minZoom.hashCode) +
        (maxZoom == null ? 0 : maxZoom.hashCode) +
        tileProperties.hashCode;

  factory EdpPresentation.fromJson(Map<String, dynamic> json) => _$EdpPresentationFromJson(json);

  Map<String, dynamic> toJson() => _$EdpPresentationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

