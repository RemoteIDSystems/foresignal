//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_input_attributes.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceInputAttributes {
  /// Returns a new [PlaceInputAttributes] instance.
  PlaceInputAttributes({

     this.zip,
  });

      /// US ZIP for EPA UV (106)
  @JsonKey(
    
    name: r'zip',
    required: false,
    includeIfNull: false,
  )


  final String? zip;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceInputAttributes &&
      other.zip == zip;

    @override
    int get hashCode =>
        zip.hashCode;

  factory PlaceInputAttributes.fromJson(Map<String, dynamic> json) => _$PlaceInputAttributesFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceInputAttributesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

