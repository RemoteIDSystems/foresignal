//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'style_special.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StyleSpecial {
  /// Returns a new [StyleSpecial] instance.
  StyleSpecial({

    required  this.kind,

    required  this.color,

    required  this.label,
  });

  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final StyleSpecialKindEnum kind;



      /// `transparent` or a colour
  @JsonKey(
    
    name: r'color',
    required: true,
    includeIfNull: false,
  )


  final String color;



  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final String label;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StyleSpecial &&
      other.kind == kind &&
      other.color == color &&
      other.label == label;

    @override
    int get hashCode =>
        kind.hashCode +
        color.hashCode +
        label.hashCode;

  factory StyleSpecial.fromJson(Map<String, dynamic> json) => _$StyleSpecialFromJson(json);

  Map<String, dynamic> toJson() => _$StyleSpecialToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StyleSpecialKindEnum {
@JsonValue(r'noCoverage')
noCoverage(r'noCoverage'),
@JsonValue(r'noEcho')
noEcho(r'noEcho'),
@JsonValue(r'noData')
noData(r'noData');

const StyleSpecialKindEnum(this.value);

final String value;

@override
String toString() => value;
}


