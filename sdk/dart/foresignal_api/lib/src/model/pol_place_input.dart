//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'pol_place_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PolPlaceInput {
  /// Returns a new [PolPlaceInput] instance.
  PolPlaceInput({

    required  this.label,
  });

  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: true,
  )


  final String? label;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PolPlaceInput &&
      other.label == label;

    @override
    int get hashCode =>
        (label == null ? 0 : label.hashCode);

  factory PolPlaceInput.fromJson(Map<String, dynamic> json) => _$PolPlaceInputFromJson(json);

  Map<String, dynamic> toJson() => _$PolPlaceInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

