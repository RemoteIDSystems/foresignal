//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_time_spec_one_of1.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingTimeSpecOneOf1 {
  /// Returns a new [MappingTimeSpecOneOf1] instance.
  MappingTimeSpecOneOf1({

    required  this.received,
  });

  @JsonKey(
    
    name: r'received',
    required: true,
    includeIfNull: false,
  )


  final bool received;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingTimeSpecOneOf1 &&
      other.received == received;

    @override
    int get hashCode =>
        received.hashCode;

  factory MappingTimeSpecOneOf1.fromJson(Map<String, dynamic> json) => _$MappingTimeSpecOneOf1FromJson(json);

  Map<String, dynamic> toJson() => _$MappingTimeSpecOneOf1ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

