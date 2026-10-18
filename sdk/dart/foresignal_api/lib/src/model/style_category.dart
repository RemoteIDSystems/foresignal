//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'style_category.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StyleCategory {
  /// Returns a new [StyleCategory] instance.
  StyleCategory({

    required  this.code,

    required  this.color,

    required  this.label,
  });

  @JsonKey(
    
    name: r'code',
    required: true,
    includeIfNull: false,
  )


  final int code;



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
    bool operator ==(Object other) => identical(this, other) || other is StyleCategory &&
      other.code == code &&
      other.color == color &&
      other.label == label;

    @override
    int get hashCode =>
        code.hashCode +
        color.hashCode +
        label.hashCode;

  factory StyleCategory.fromJson(Map<String, dynamic> json) => _$StyleCategoryFromJson(json);

  Map<String, dynamic> toJson() => _$StyleCategoryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

