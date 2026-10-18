//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'style_stop.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StyleStop {
  /// Returns a new [StyleStop] instance.
  StyleStop({

    required  this.value,

    required  this.color,

     this.label,
  });

  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: false,
  )


  final num value;



  @JsonKey(
    
    name: r'color',
    required: true,
    includeIfNull: false,
  )


  final String color;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StyleStop &&
      other.value == value &&
      other.color == color &&
      other.label == label;

    @override
    int get hashCode =>
        value.hashCode +
        color.hashCode +
        (label == null ? 0 : label.hashCode);

  factory StyleStop.fromJson(Map<String, dynamic> json) => _$StyleStopFromJson(json);

  Map<String, dynamic> toJson() => _$StyleStopToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

