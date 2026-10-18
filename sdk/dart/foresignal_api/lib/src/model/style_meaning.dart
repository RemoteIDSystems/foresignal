//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'style_meaning.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StyleMeaning {
  /// Returns a new [StyleMeaning] instance.
  StyleMeaning({

     this.min,

     this.max,

    required  this.text,
  });

  @JsonKey(
    
    name: r'min',
    required: false,
    includeIfNull: false,
  )


  final num? min;



  @JsonKey(
    
    name: r'max',
    required: false,
    includeIfNull: false,
  )


  final num? max;



  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StyleMeaning &&
      other.min == min &&
      other.max == max &&
      other.text == text;

    @override
    int get hashCode =>
        (min == null ? 0 : min.hashCode) +
        (max == null ? 0 : max.hashCode) +
        text.hashCode;

  factory StyleMeaning.fromJson(Map<String, dynamic> json) => _$StyleMeaningFromJson(json);

  Map<String, dynamic> toJson() => _$StyleMeaningToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

