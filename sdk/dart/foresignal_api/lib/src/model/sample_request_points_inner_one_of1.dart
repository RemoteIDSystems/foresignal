//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sample_request_points_inner_one_of1.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SampleRequestPointsInnerOneOf1 {
  /// Returns a new [SampleRequestPointsInnerOneOf1] instance.
  SampleRequestPointsInnerOneOf1({

    required  this.place,
  });

      /// Place id (requires a token)
  @JsonKey(
    
    name: r'place',
    required: true,
    includeIfNull: false,
  )


  final String place;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SampleRequestPointsInnerOneOf1 &&
      other.place == place;

    @override
    int get hashCode =>
        place.hashCode;

  factory SampleRequestPointsInnerOneOf1.fromJson(Map<String, dynamic> json) => _$SampleRequestPointsInnerOneOf1FromJson(json);

  Map<String, dynamic> toJson() => _$SampleRequestPointsInnerOneOf1ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

